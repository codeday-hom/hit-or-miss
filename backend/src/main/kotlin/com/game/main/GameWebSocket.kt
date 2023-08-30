package com.game.main

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.game.repository.GameRepository.getGame
import org.http4k.core.Request
import org.http4k.format.Jackson
import org.http4k.lens.Path
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import java.util.*

class GameWebSocket {

    private val wsConnections = Collections.synchronizedMap(HashMap<String, MutableList<Websocket>>())
    private val mapper = ObjectMapper()

    fun gameWsHandler(): (Request) -> WsResponse {
        return { req: Request ->
            WsResponse { ws: Websocket ->
                println("Websocket request path: ${req.uri.path}")
                val gameId = Path.of("gameId")(req)

                onOpen(ws, gameId)

                ws.onMessage {
                    onMessage(ws, it, gameId)
                }

                ws.onClose {
                    println("$gameId is closing")
                    wsConnections[gameId]?.remove(ws)
                }
            }
        }
    }

    private fun onOpen(ws: Websocket, gameId: String) {
        val connection = wsConnections[gameId]
        if (connection == null || !connection.contains(ws)) {
            wsConnections.getOrPut(gameId) { mutableListOf() }.add(ws)
        }

        val game = getGame(gameId)
        if (game == null) {
            sendWsMessage(ws, WsMessageType.ERROR, "Game not found")
            return
        }

        if (game.isStarted()) {
            sendWsMessage(ws, WsMessageType.ERROR, "Game already started")
            return
        }

        val users = game.users
        sendWsMessage(ws, WsMessageType.USER_JOINED, users)
    }

    private fun onMessage(ws: Websocket, wsMessage: WsMessage, gameId: String) {
        val messageBody = wsMessage.bodyString()
        println("Received a message: $messageBody")

        val game = getGame(gameId)
        if (game == null) {
            sendWsMessage(ws, WsMessageType.ERROR, "Game not found")
            return
        }

        try {
            val incomingData = Jackson.asA(messageBody, GameWsMessage::class)
            println("Parsed data: $incomingData")
            val type = incomingData.type
            val data = incomingData.data

            if (type == WsMessageType.NEXT_PLAYER.name) {
                broadcastNextPlayerMessage(gameId, game.nextPlayer())
            } else if (type == WsMessageType.CATEGORY_SELECTED.name) {
                broadcastCategoryChosen(gameId, data)
            }
        } catch (e: JsonProcessingException) {
            sendWsMessage(ws, WsMessageType.ERROR, "Invalid message")
            return
        }
    }

    private fun sendWsMessage(ws: Websocket, type: WsMessageType, data: Any?) {
        val message = mapOf("type" to type.name, "data" to data)
        println("Sending a message: $message")
        ws.send(WsMessage(mapper.writeValueAsString(message)))
    }

    private fun broadcastCategoryChosen(gameId: String, category: String) {
        broadcast(gameId, WsMessageType.CATEGORY_CHOSEN, category)
    }

    private fun broadcastNextPlayerMessage(gameId: String, nextPlayer: String) {
        broadcast(gameId, WsMessageType.NEXT_PLAYER, nextPlayer)
    }

    fun broadcast(game: Game, type: WsMessageType, body: Any?) {
        val connections = wsConnections[game.gameId] ?: throw RuntimeException("Cannot broadcast for non-existing game ${game.gameId}")
        if (connections.size != game.users.size) {
            println("Warning! There are ${connections.size} websocket connections, but the game has ${game.users.size} players.")
        }
        connections.forEach { ws ->
            sendWsMessage(ws, type, body)
        }
    }
}