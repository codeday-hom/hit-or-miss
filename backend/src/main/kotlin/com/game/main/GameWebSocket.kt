package com.game.main

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.game.model.Game
import com.game.repository.GameRepository.getGame
import org.http4k.core.Request
import org.http4k.format.Jackson
import org.http4k.lens.Path
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.slf4j.LoggerFactory
import java.util.*

private val LOGGER = LoggerFactory.getLogger(GameWebSocket::class.java.simpleName)

class GameWebSocket {
    private val wsConnections = Collections.synchronizedMap(HashMap<String, MutableList<Websocket>>())
    private val isAlive = Collections.synchronizedMap(HashMap<String, Boolean>())
    private val mapper = ObjectMapper()

    fun handler(): (Request) -> WsResponse {
        return { req: Request ->
            LOGGER.info("Websocket request path: ${req.uri.path}")
            val gameId = Path.of("gameId")(req)
            val game = getGame(gameId)
            if (game == null) {
                WsResponse { ws: Websocket -> sendWsMessage(ws, WsMessageType.ERROR, "Game not found") }
            } else {
                WsResponse { ws: Websocket ->
                    onOpen(ws, game)
                    ws.onMessage {
                        onMessage(ws, it, game.gameId)
                    }
                    ws.onClose {
                        LOGGER.info("${game.gameId} is closing")
                        wsConnections[game.gameId]?.remove(ws)
                    }
                }
            }
        }
    }

    private fun onOpen(ws: Websocket, game: Game) {
        val connection = wsConnections[game.gameId]
        if (connection == null || !connection.contains(ws)) {
            wsConnections.getOrPut(game.gameId) { mutableListOf() }.add(ws)
        }

        if (game.isStarted()) {
            sendWsMessage(ws, WsMessageType.ERROR, "Game already started")
            return
        }

        sendWsMessage(ws, WsMessageType.USER_JOINED, game.users)
    }

    private fun onMessage(ws: Websocket, wsMessage: WsMessage, gameId: String) {
        val messageBody = wsMessage.bodyString()
        LOGGER.info("Received a message: $messageBody")

        val game = getGame(gameId)
        if (game == null) {
            sendWsMessage(ws, WsMessageType.ERROR, "Game not found")
            return
        }

        try {
            val incomingData = Jackson.asA(messageBody, GameWsMessage::class)
            LOGGER.info("Parsed data: $incomingData")
            val type = incomingData.type
            val data = incomingData.data

            if (type == WsMessageType.NEXT_PLAYER.name) {
                broadcastNextPlayerMessage(game)
            } else if (type == WsMessageType.CATEGORY_SELECTED.name) {
                broadcastCategoryChosen(game, data)
            } else if (type == WsMessageType.HEARTBEAT.name) {
                isAlive[gameId] = true
                broadcastHeartbeatAckMessage(game)
            }
        } catch (e: JsonProcessingException) {
            sendWsMessage(ws, WsMessageType.ERROR, "Invalid message")
            return
        }
    }

    private fun sendWsMessage(ws: Websocket, type: WsMessageType, data: Any?) {
        val message = mapOf("type" to type.name, "data" to data)
        LOGGER.info("Sending a message: $message")
        ws.send(WsMessage(mapper.writeValueAsString(message)))
    }

    private fun broadcastCategoryChosen(game: Game, category: String) {
        broadcast(game, WsMessageType.CATEGORY_CHOSEN, category)
    }

    private fun broadcastNextPlayerMessage(game: Game) {
        broadcast(game, WsMessageType.NEXT_PLAYER, game.nextPlayer())
    }

    private fun broadcastHeartbeatAckMessage(game: Game) {
        broadcast(game, WsMessageType.HEARTBEAT_ACK, null)
    }

    fun broadcast(game: Game, type: WsMessageType, body: Any?) {
        val connections = wsConnections[game.gameId] ?: throw RuntimeException("Cannot broadcast for non-existing game ${game.gameId}")
        if (connections.size != game.users.size) {
            // If connections > players, then it means that each player has >1 websocket connection to the server.
            // For now, that seems to be fine. We can refactor the frontend to share a single websocket connection between components in the view
            //   at a later time if it becomes necessary.
            LOGGER.warn("There are ${connections.size} websocket connections, but the game has ${game.users.size} players.")
        }
        connections.forEach { ws ->
            sendWsMessage(ws, type, body)
        }
    }
}