package com.game.main

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.game.model.Game
import com.game.model.Player
import com.game.model.TurnResult
import com.game.repository.GameRepository.getGame
import org.http4k.core.Request
import org.http4k.format.Jackson
import org.http4k.lens.Path
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
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

        sendWsMessage(ws, WsMessageType.USER_JOINED, game.userNameMapForSerialization())
    }

    private fun onMessage(ws: Websocket, wsMessage: WsMessage, gameId: String) {
        val messageBody = wsMessage.bodyString()
        LOGGER.info("Received a message: $messageBody")

        val incomingData =
            parseMessage(messageBody, ws) ?: throw IllegalArgumentException("the message couldn't be parsed")
        val type = incomingData.type
        val body = incomingData.data
        val data = body["data"] ?: throw IllegalArgumentException("Message body doesn't include data")

        val game = getGame(gameId)
        if (game == null) {
            sendWsMessage(ws, WsMessageType.ERROR, "Game not found")
            return
        }
        var currentTurn: Game.Turn? = startNewTurn(game)

        when (type) {
            WsMessageType.NEXT_PLAYER.name -> {
                currentTurn = startNewTurn(game)
                broadcastNextPlayerMessage(game)
            }
            WsMessageType.CATEGORY_SELECTED.name -> {
                game.updateCurrentCategory(data)
                broadcastCategoryChosen(game, data)
            }
            WsMessageType.HEARTBEAT.name -> {
                isAlive[gameId] = true
                broadcastHeartbeatAckMessage(game)
            }
            WsMessageType.ROLL_DICE.name -> {
                broadcastRollDiceResultMessage(game)
            }
            WsMessageType.HIT_OR_MISS.name -> {
                game.updateDiceResult(data)
                broadcastHitOrMissMessage(game, data)
            }
            WsMessageType.SELECTED_WORD.name -> broadcastSelectedWordMessage(game, data)
            WsMessageType.PLAYER_CHOSE_HIT_OR_MISS.name -> {
                val players = game.userMapForSerialization().values
                val userName =
                    body["username"] ?: throw IllegalArgumentException("The message body doesn't include userName")
                println(userName)

                val player = game.getPlayer(userName) ?: throw IllegalArgumentException("Player doesn't exist: $userName")
                if (currentTurn != null) {
                    updatePlayerScore(data, player, currentTurn)
                }

                val playerScoreMap: MutableMap<String, Int> = Collections.synchronizedMap(mutableMapOf())
                for (p in players) {
                    playerScoreMap[player.name] = player.getPlayerPoints()
                    broadcast(game, WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, playerScoreMap)
                }
            }
        }
//        try {
//            val incomingData = Jackson.asA(messageBody, GameWsMessage::class)
//            LOGGER.info("Parsed data: $incomingData")
//            val type = incomingData.type
//            val data = incomingData.data
//
//            when (type) {
//                WsMessageType.NEXT_PLAYER.name -> broadcastNextPlayerMessage(game)
//                WsMessageType.CATEGORY_SELECTED.name -> broadcastCategoryChosen(game, data)
//                WsMessageType.HEARTBEAT.name -> {
//                    isAlive[gameId] = true
//                    broadcastHeartbeatAckMessage(game)
//                }
//                WsMessageType.ROLL_DICE.name -> broadcastRollDiceResultMessage(game)
//                WsMessageType.HIT_OR_MISS.name -> broadcastHitOrMissMessage(game, data)
//                WsMessageType.SELECTED_WORD.name -> broadcastSelectedWordMessage(game, data)
//            }
//
//        } catch (e: JsonProcessingException) {
//            sendWsMessage(ws, WsMessageType.ERROR, "Invalid message")
//            return
//        }
    }

    private fun startNewTurn(game: Game): Game.Turn? {
        return game.startTurn(
            game.currentPlayer(),
            game.currentCategory(),
            game.currentDiceResult()
        )
    }

    private fun parseMessage(messageBody: String, ws: Websocket): GameWsMessage? {
        return try {
            val incomingData = Jackson.asA(messageBody, GameWsMessage::class)
            LOGGER.info("Parsed data: $incomingData")
            incomingData
        } catch (e: JsonProcessingException) {
            sendWsMessage(ws, WsMessageType.ERROR, "Invalid message")
            null
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
        broadcast(game, WsMessageType.HEARTBEAT_ACK, "")
    }

    private fun broadcastRollDiceResultMessage(game: Game) {
        val result = game.rollDice()
        broadcast(game, WsMessageType.ROLL_DICE_RESULT, result)
    }

    private fun broadcastHitOrMissMessage(game: Game, hitOrMiss: String) {
        broadcast(game, WsMessageType.HIT_OR_MISS, hitOrMiss)
    }


    private fun broadcastSelectedWordMessage(game: Game, word: String) {
        broadcast(game, WsMessageType.SELECTED_WORD, word)
    }

    fun broadcast(game: Game, type: WsMessageType, body: Any?) {
        val connections = wsConnections[game.gameId] ?: throw RuntimeException("Cannot broadcast for non-existing game ${game.gameId}")
        if (connections.size != game.countPlayers()) {
            // If connections > players, then it means that each player has >1 websocket connection to the server.
            // For now, that seems to be fine. We can refactor the frontend to share a single websocket connection between components in the view
            //   at a later time if it becomes necessary.
            LOGGER.warn("There are ${connections.size} websocket connections, but the game has ${game.countPlayers()} players.")
        }
        connections.forEach { ws ->
            sendWsMessage(ws, type, body)
        }
    }

    private fun updatePlayerScore(
        turnResult: String,
        player: Player,
        currentTurn: Game.Turn
    ) {
        if (turnResult == "HIT") {
            currentTurn.result(player, TurnResult.HIT)
        } else {
            currentTurn.result(player, TurnResult.MISS)
        }
    }

}