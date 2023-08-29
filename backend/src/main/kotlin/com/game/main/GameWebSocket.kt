package com.game.main

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.game.repository.GameRepository.getGame
import org.http4k.core.Request
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

        try {
            val incomingData = mapper.readValue(messageBody, Map::class.java)
            println("Parsed data: $incomingData")
            if (messageBody == WsMessageType.NEXT_PLAYER.name) {
                broadcastNextPlayerMessage(gameId, getGame(gameId)?.nextPlayer())
            } else if (messageBody == WsMessageType.CATEGORY_SELECTED.name) {
                val selectedCategory = incomingData["data"] as? String
                if (selectedCategory != null) {
                    announceCategoryChosen(gameId, selectedCategory)
                }
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

    private fun announceCategoryChosen(gameId: String, category: String) {
        broadcast(gameId, WsMessageType.CATEGORY_CHOSEN, category)
    }

    private fun broadcastNextPlayerMessage(gameId: String, nextPlayer: String?) {
        broadcast(gameId, WsMessageType.NEXT_PLAYER, nextPlayer)
    }

    fun broadcast(gameId: String, type: WsMessageType, body: Any?) {
        wsConnections[gameId]?.forEach { ws ->
            sendWsMessage(ws, type, body)
        }
    }
}