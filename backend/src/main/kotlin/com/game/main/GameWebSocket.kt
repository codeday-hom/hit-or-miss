package com.game.main

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.game.main.WsMessageType.*
import com.game.model.DiceResult
import com.game.model.Game
import com.game.model.TurnResult
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
                WsResponse { ws: Websocket -> sendWsMessage(ws, ERROR, "Game not found") }
            } else {
                WsResponse { ws: Websocket ->
                    onOpen(ws, game)
                    ws.onMessage {
                        try {
                            onMessage(ws, it, game.gameId)
                        } catch (e: Exception) {
                            LOGGER.info("Uncaught exception while processing message: ${e.message}")
                        }
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

        // You can't join (i.e. open a websocket connection to) a game that has already started.
        if (game.isStarted()) {
            sendWsMessage(ws, ERROR, "Game already started")
            return
        }

        sendWsMessage(ws, USER_JOINED, game.playerListForSerialization())
    }

    private fun onMessage(ws: Websocket, wsMessage: WsMessage, gameId: String) {
        val messageBody = wsMessage.bodyString()
        LOGGER.info("Received a message: $messageBody")

        val incomingData = parseMessage(messageBody, ws) ?: throw IllegalArgumentException("the message couldn't be parsed")
        val type = incomingData.type
        val data = incomingData.data

        val game = getGame(gameId)
        if (game == null) {
            sendWsMessage(ws, ERROR, "Game not found")
            return
        }

        when (type) {
            NEXT_PLAYER.name -> {
                game.nextTurn()
                broadcastNextPlayerMessage(game)
            }

            CATEGORY_SELECTED.name -> {
                val dataField = "category"
                val category = data[dataField] ?: throw IllegalArgumentException("Message of type $CATEGORY_SELECTED requires data field '$dataField'")
                broadcastCategoryChosen(game, category)
            }

            HEARTBEAT.name -> {
                isAlive[gameId] = true
                broadcastHeartbeatAckMessage(game)
            }

            ROLL_DICE.name -> {
                broadcastRollDiceResultMessage(game)
            }

            HIT_OR_MISS.name -> {
                val dataField = "diceResult"
                val diceResultString = data[dataField] ?: throw IllegalArgumentException("Message of type $HIT_OR_MISS requires data field '$dataField'")
                val diceResult = DiceResult.valueOf(diceResultString.uppercase())
                game.updateDiceResult(diceResult)
                broadcastHitOrMissMessage(game, diceResult)
            }

            SELECTED_WORD.name -> {
                val dataField = "selectedWord"
                val selectedWord = data[dataField] ?: throw IllegalArgumentException("Message of type $SELECTED_WORD requires data field '$dataField'")
                broadcastSelectedWordMessage(game, selectedWord)
            }

            PLAYER_CHOSE_HIT_OR_MISS.name -> {
                val username = data["username"] ?: throw IllegalArgumentException("Message of type $PLAYER_CHOSE_HIT_OR_MISS requires data field 'username'")
                val hitOrMiss = data["hitOrMiss"] ?: throw IllegalArgumentException("Message of type $PLAYER_CHOSE_HIT_OR_MISS requires data field 'hitOrMiss'")
                val player = game.getPlayer(username) ?: throw IllegalArgumentException("Player doesn't exist: $username")
                game.turnResult(player, TurnResult.valueOf(hitOrMiss.uppercase()))
                broadcast(game, PLAYER_CHOSE_HIT_OR_MISS, game.playerPoints())

                game.addPlayerWhoChoseHitOrMiss(username)

                if (game.allPlayersChoseHitOrMiss()) {
                    broadcastScoreboardMessage(game)
                    game.resetPlayersWhoChoseHitOrMiss()
                    println("scoreboard!")
                }
            }
        }
    }

    private fun parseMessage(messageBody: String, ws: Websocket): GameWsMessage? {
        return try {
            val incomingData = Jackson.asA(messageBody, GameWsMessage::class)
            LOGGER.info("Parsed data: $incomingData")
            incomingData
        } catch (e: JsonProcessingException) {
            LOGGER.info("Rejected message '${messageBody}': ${e.message}")
            sendWsMessage(ws, ERROR, "Invalid message")
            null
        }
    }

    private fun sendWsMessage(ws: Websocket, type: WsMessageType, data: Any?) {
        val message = mapOf("type" to type.name, "data" to data)
        LOGGER.info("Sending a message: $message")
        ws.send(WsMessage(mapper.writeValueAsString(message)))
    }

    private fun broadcastCategoryChosen(game: Game, category: String) {
        broadcast(game, CATEGORY_CHOSEN, category)
    }

    private fun broadcastNextPlayerMessage(game: Game) {
        broadcast(game, NEXT_PLAYER, game.currentPlayer().name)
    }

    private fun broadcastHeartbeatAckMessage(game: Game) {
        broadcast(game, HEARTBEAT_ACK, "")
    }

    private fun broadcastRollDiceResultMessage(game: Game) {
        broadcast(game, ROLL_DICE_RESULT, game.rollDice())
    }

    private fun broadcastHitOrMissMessage(game: Game, hitOrMiss: DiceResult) {
        broadcast(game, HIT_OR_MISS, hitOrMiss.name.lowercase().replaceFirstChar { it.titlecaseChar() })
    }


    private fun broadcastSelectedWordMessage(game: Game, word: String) {
        broadcast(game, SELECTED_WORD, word)
    }

    private fun broadcastScoreboardMessage(game: Game) {
        val scoresMap = game.playerPoints()
        val scores = scoresMap.map { mapEntry ->
            mapOf("username" to mapEntry.key, "score" to mapEntry.value)
        }.toList()
        broadcast(game, SHOW_SCOREBOARD, scores)
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
}