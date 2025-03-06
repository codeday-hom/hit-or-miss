package com.game.main.ws

import com.fasterxml.jackson.core.JsonProcessingException
import com.game.main.api.GameRepository
import com.game.main.hitormiss.DiceResult
import com.game.main.hitormiss.Game
import com.game.main.hitormiss.TurnResult
import com.game.main.ws.WsMessageType.CATEGORY_SELECTED
import com.game.main.ws.WsMessageType.ERROR
import com.game.main.ws.WsMessageType.GAME_OVER
import com.game.main.ws.WsMessageType.HEARTBEAT
import com.game.main.ws.WsMessageType.HEARTBEAT_ACK
import com.game.main.ws.WsMessageType.NEXT_ROUND
import com.game.main.ws.WsMessageType.NEXT_TURN
import com.game.main.ws.WsMessageType.PLAYER_CHOSE_HIT_OR_MISS
import com.game.main.ws.WsMessageType.ROLL_DICE
import com.game.main.ws.WsMessageType.ROLL_DICE_HIT_OR_MISS
import com.game.main.ws.WsMessageType.ROLL_DICE_RESULT
import com.game.main.ws.WsMessageType.SCORES
import com.game.main.ws.WsMessageType.SELECTED_WORD
import java.time.Clock
import org.http4k.core.Request
import org.http4k.format.Jackson
import org.http4k.routing.path
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.slf4j.LoggerFactory


private val LOGGER = LoggerFactory.getLogger(GameWebSocket::class.java.simpleName)

class GameWebSocket(private val clock: Clock) {
    private val messenger = WsMessenger()
    private val connections = WebSocketConnections(messenger)

    fun handler(): (Request) -> WsResponse = this::handle

    fun handle(req: Request): WsResponse {
        LOGGER.info("Websocket request path: ${req.uri.path}")
        val gameId = req.path("gameId")
            ?: return WsResponse { ws: Websocket ->
                messenger.send(ws, ERROR, "Missing path variable gameId")
            }
        val game = GameRepository.getGame(gameId)
            ?: return WsResponse { ws: Websocket ->
                messenger.send(ws, ERROR, "Game not found")
            }

        val playerId = req.path("playerId")
        if (playerId != null) {
            LOGGER.info("Handling ws connection for player $playerId")
        }

        return WsResponse { ws: Websocket ->
            connections.onOpen(ws, game, playerId)
            ws.onMessage {
                try {
                    onMessage(ws, it, game.gameId)
                } catch (e: Exception) {
                    LOGGER.info("Uncaught exception while processing message: ${e.message}")
                }
            }
            ws.onClose {
                connections.onClose(ws, gameId)
            }
        }
    }

    private fun onMessage(ws: Websocket, wsMessage: WsMessage, gameId: String) {
        val messageBody = wsMessage.bodyString()
        val parsedMessage = parseMessage(messageBody, ws) ?: throw IllegalArgumentException("the message couldn't be parsed")

        if (parsedMessage.type != HEARTBEAT) {
            LOGGER.info("Received ${parsedMessage.type} (${parsedMessage.data}) from ${parsedMessage.player} for game $gameId")
        }

        val game = GameRepository.getGame(gameId)
        if (game == null) {
            messenger.send(ws, ERROR, "Game not found")
            return
        }

        when (parsedMessage.type) {
            HEARTBEAT -> {
                broadcastHeartbeatAckMessage(game)
            }

            CATEGORY_SELECTED -> {
                val category = parsedMessage.getRequiredData("category")
                game.startRound()
                broadcastCategorySelected(game, category)
            }

            ROLL_DICE -> {
                broadcastRollDiceResultMessage(game)
            }

            ROLL_DICE_HIT_OR_MISS -> {
                val diceResult = DiceResult.valueOf(parsedMessage.getRequiredData("diceResult").uppercase())
                game.startTurn(diceResult)
                broadcastHitOrMissMessage(game, diceResult)
            }

            SELECTED_WORD -> {
                val selectedWord = parsedMessage.getRequiredData("selectedWord")
                broadcastSelectedWordMessage(game, selectedWord)
            }

            PLAYER_CHOSE_HIT_OR_MISS -> {
                val hitOrMiss = parsedMessage.getRequiredData("hitOrMiss")

                // We need to synchronize on the game here to prevent a race condition which
                // could happen when two players' turn results are processed simultaneously,
                // leading both executions to enter the below conditional block and cause
                // a player to be skipped, derailing the course of the game.
                synchronized(game) {
                    game.turnResult(parsedMessage.player, TurnResult.valueOf(hitOrMiss.uppercase()))
                    broadcastScores(game)

                    if (game.allPlayersChoseHitOrMiss()) {
                        if (game.allPlayersRolledTheDice()) {
                            game.nextRound()

                            if (game.isOver()) {
                                broadcastGameOverMessage(game)
                            } else {
                                broadcastNextRoundMessage(game)
                            }
                        } else {
                            game.nextTurn()
                            broadcastNextTurnMessage(game)
                        }
                    }
                }
            }

            else -> {
                throw IllegalArgumentException("Received unexpected message type '${parsedMessage.type}'")
            }
        }
    }

    private fun parseMessage(messageBody: String, ws: Websocket): ReceivedWsMessage? {
        return try {
            return Jackson.asA(messageBody, ReceivedWsMessage::class)
        } catch (e: JsonProcessingException) {
            LOGGER.info("Rejected message '${messageBody}': ${e.message}")
            messenger.send(ws, ERROR, "Invalid message")
            null
        }
    }

    private fun broadcastCategorySelected(game: Game, category: String) {
        broadcast(game, CATEGORY_SELECTED, mapOf(
            "category" to category,
            "countdownTimerStart" to clock.instant().toEpochMilli()
        ))
    }

    private fun broadcastNextTurnMessage(game: Game) {
        broadcast(game, NEXT_TURN, game.currentPlayer().id)
    }

    private fun broadcastNextRoundMessage(game: Game) {
        broadcast(game, NEXT_ROUND, game.currentPlayer().id)
    }

    private fun broadcastHeartbeatAckMessage(game: Game) {
        broadcast(game, HEARTBEAT_ACK, "")
    }

    private fun broadcastRollDiceResultMessage(game: Game) {
        broadcast(game, ROLL_DICE_RESULT, game.rollDice())
    }

    private fun broadcastHitOrMissMessage(game: Game, hitOrMiss: DiceResult) {
        broadcast(game, ROLL_DICE_HIT_OR_MISS, hitOrMiss.name.lowercase().replaceFirstChar { it.titlecaseChar() })
    }

    private fun broadcastSelectedWordMessage(game: Game, word: String) {
        broadcast(game, SELECTED_WORD, word)
    }

    private fun broadcastGameOverMessage(game: Game) {
        broadcast(game, GAME_OVER, game.scoresForSerialization())
    }

    private fun broadcastScores(game: Game) {
        broadcast(game, SCORES, game.scoresForSerialization())
    }

    private fun broadcast(game: Game, type: WsMessageType, data: Any?) {
        messenger.broadcast(connections.forGame(game), type, data)
    }

    fun lobbyBroadcast(game: Game, type: WsMessageType, data: Any?) {
        messenger.broadcast(connections.forLobby(game), type, data)
    }
}