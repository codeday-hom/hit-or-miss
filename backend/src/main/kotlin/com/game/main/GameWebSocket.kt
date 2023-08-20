package com.game.main

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.repository.GameRepository
import com.game.repository.GameRepository.getGame
import org.http4k.core.Request
import org.http4k.lens.Path
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import java.util.concurrent.ConcurrentHashMap

class GameWebSocket {
    private val wsConnections = ConcurrentHashMap<String, MutableList<Websocket>>()

    fun gameWsHandler(): (Request) -> WsResponse {
        return { req: Request ->
            WsResponse { ws: Websocket ->
                val gameId = Path.of("gameId")(req)
                val connection = wsConnections[gameId]
                if (connection == null || !connection.contains(ws)) {
                    wsConnections.getOrPut(gameId) { mutableListOf() }.add(ws)
                }
                val game = getGame(gameId)
                if (!game!!.isStarted()) {
                    val users = game.users
                    sendWsMessage(ws, WsMessageType.USER_JOINED, users)
                }
                ws.onMessage {
                    println("Received a message: ${it.bodyString()}")
                    if (it.bodyString() == "NEXT_PLAYER") {
                        sendNextPlayerMessage(gameId)
                    }
                }
                ws.onClose {
                    println("$gameId is closing")
                    wsConnections[gameId]?.remove(ws)
                }
            }
        }
    }

    private fun sendWsMessage(ws: Websocket, type: WsMessageType, data: Any?) {
        val message = mapOf("type" to type.name, "data" to data)
        val mapper = ObjectMapper()
        val messageJson = mapper.writeValueAsString(message)
        println("Sending a message: $message")
        ws.send(WsMessage(messageJson))
    }


    fun sendUserJoinedMessages(gameId: String, users: MutableMap<String, String>) {
        wsConnections[gameId]?.forEach { ws ->
            sendWsMessage(ws, WsMessageType.USER_JOINED, users)
        }
    }
    fun sendGameStartMessages(gameId: String, currentPlayer: String) {
        wsConnections[gameId]?.forEach { ws ->
            sendWsMessage(ws, WsMessageType.GAME_START, currentPlayer)
        }
    }
    fun sendNextPlayerMessage(gameId: String) {
        val nextPlayer = getGame(gameId)?.nextPlayer()
        wsConnections[gameId]?.forEach { ws ->
            sendWsMessage(ws, WsMessageType.NEXT_PLAYER, nextPlayer)
        }
    }
}