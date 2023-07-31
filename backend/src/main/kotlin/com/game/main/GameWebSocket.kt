package com.game.main

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.repository.GameRepository
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
                wsConnections.getOrPut(gameId) { mutableListOf() }.add(ws)

                ws.onMessage {
                    println("Received a message: ${it.bodyString()}")
                    val userIds = GameRepository.getGame(gameId)!!.userIds
                    println("Sending user IDs: $userIds")
                    sendWsMessage(ws, "userJoined", userIds)
                }

                ws.onClose {
                    println("$gameId is closing")
                    wsConnections[gameId]?.remove(ws)
                }
            }
        }
    }

    fun sendWsMessage(ws: Websocket, type: String, data: Any) {
        val message = mapOf("type" to type, "data" to data)
        val mapper = ObjectMapper()
        val messageJson = mapper.writeValueAsString(message)
        println("Sending a message: $message")
        ws.send(WsMessage(messageJson))
    }

    fun sendUserJoinedMessages(gameId: String, userIds: List<String>) {
        wsConnections[gameId]?.forEach { ws ->
            sendWsMessage(ws, "userJoined", userIds)
        }
    }
}