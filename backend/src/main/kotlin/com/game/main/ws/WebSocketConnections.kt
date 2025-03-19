package com.game.main.ws

import com.game.main.hitormiss.Game
import java.time.InstantSource
import java.util.Collections
import java.util.function.Consumer
import org.http4k.websocket.Websocket
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger(WebSocketConnections::class.java.simpleName)

class WebSocketConnections(private val messenger: WsMessenger, private val clock: InstantSource) {

    /**
     * Maps a gameId to a list of websocket connections associated with a game lobby.
     */
    private val anonymousConnections = Collections.synchronizedMap(HashMap<String, MutableList<Websocket>>())

    /**
     * Maps a gameId to a list of websocket connections and the corresponding player id.
     */
    private val identifiedConnections = Collections.synchronizedMap(HashMap<String, MutableList<Pair<String, MutableList<Websocket>>>>())

    fun onOpen(ws: Websocket, game: Game, playerId: String?) {
        val gameId = game.gameId
        if (playerId == null) {
            if (!game.isStarted()) {
                LOGGER.info("Adding anonymous connection for game $gameId")
                val anonymousConnections = anonymousConnections.getOrPut(gameId) { Collections.synchronizedList(mutableListOf()) }
                synchronized(anonymousConnections) {
                    anonymousConnections.add(ws)
                }
            } else {
                // If the game is already started, then reaching this point suggests that a disconnected player
                // is trying to reconnect to an existing game.
                // We send them a list of the disconnected player ids. If there's only one, they will automatically adopt that.
                // Otherwise, they can choose their id from the list.
                val gameConnections = identifiedConnections[gameId]
                if (gameConnections != null) {
                    val disconnectedPlayerIds = synchronized(gameConnections) {
                        gameConnections.filter { it.second.isEmpty() }.map { it.first }
                    }.distinct()
                    if (disconnectedPlayerIds.isNotEmpty()) {
                        LOGGER.info("Sending list of disconnected player ids $disconnectedPlayerIds to anonymous connection for game $gameId")
                        messenger.send(ws, WsMessageType.DISCONNECTED_PLAYER_IDS, mapOf("disconnectedPlayerIds" to disconnectedPlayerIds))
                    }
                }
            }
        } else {
            val gameConnections = identifiedConnections.getOrPut(gameId) { Collections.synchronizedList(mutableListOf()) }
            val playerConnections = synchronized(gameConnections) {
                gameConnections.find { it.first == playerId }
            }
            if (playerConnections == null) {
                LOGGER.info("Adding first connection in game $gameId for player $playerId")
                synchronized(gameConnections) {
                    gameConnections.add(Pair(playerId, Collections.synchronizedList(mutableListOf(ws))))
                }
            } else {
                synchronized(playerConnections) {
                    if (playerConnections.second.isNotEmpty()) {
                        LOGGER.info("Adding new connection in game $gameId for player $playerId")
                    } else {
                        LOGGER.info("Player $playerId has reconnected to game $gameId")
                        val broadcastTargets = synchronized(gameConnections) {
                            gameConnections.map { it.second }.flatten()
                        }
                        messenger.broadcast(broadcastTargets, WsMessageType.USER_RECONNECTED, playerId)
                        messenger.send(ws, WsMessageType.GAME_JOINABLE, game.gameStateForSerialization(clock))
                    }
                    playerConnections.second.add(ws)
                }
            }
        }

        if (!game.isStarted()) {
            messenger.send(ws, WsMessageType.USER_JOINED, game.playerListForSerialization())
        } else if (playerId != null && !game.hasPlayer(playerId)) {
            messenger.send(ws, WsMessageType.ERROR, "Game already started, new players (such as you, $playerId) cannot join")
        }
    }

    /**
     * Cleaning up closed connections allows us to detect when a player has disconnected.
     */
    fun onClose(ws: Websocket, gameId: String) {
        val anonymousConnectionsForGame = anonymousConnections[gameId]
        if (anonymousConnectionsForGame != null) {
            synchronized(anonymousConnectionsForGame) {
                if (anonymousConnectionsForGame.remove(ws)) {
                    LOGGER.info("Anonymous connection for game $gameId has closed")
                }
            }
        }

        val gameConnections = identifiedConnections[gameId] ?: return
        val playerConnections = synchronized(gameConnections) {
            gameConnections.find { it.second.contains(ws) }
        }
        if (playerConnections != null) {
            synchronized(playerConnections) {
                val player = playerConnections.first
                if (playerConnections.second.remove(ws)) {
                    LOGGER.info("Connection for game $gameId of player $player has closed")
                }
                if (playerConnections.second.isEmpty()) {
                    LOGGER.info("Player $player has disconnected from game $gameId")
                    val broadcastTargets = synchronized(gameConnections) {
                        gameConnections.map { it.second }.flatten()
                    }
                    messenger.broadcast(broadcastTargets, WsMessageType.USER_DISCONNECTED, player)
                }
            }
        }
    }

    /**
     * Performs a thread-safe operation on all connections to players in the game lobby.
     *
     * These clients have not yet sent any websocket messages to the server and so are not
     * identifiable by their player id.
     */
    fun withConnectionsForLobby(game: Game, action: Consumer<List<Websocket>>) {
        val connections = anonymousConnections[game.gameId] ?: throw RuntimeException("Cannot broadcast for non-existing lobby ${game.gameId}")
        synchronized(connections) {
            checkConnectionCount(game, connections.size)
            action.accept(connections)
        }
    }

    /**
     * Performs a thread-safe operation on all connections to identified players in a running game.
     */
    fun withConnectionsForGame(game: Game, action: Consumer<List<Websocket>>) {
        val connections = identifiedConnections[game.gameId] ?: throw RuntimeException("Cannot broadcast for non-existing game ${game.gameId}")
        synchronized(connections) {
            checkConnectionCount(game, connections.size)
            val flattenedConnections = connections.map { it.second }.flatten()
            action.accept(flattenedConnections)
        }
    }

    private fun checkConnectionCount(game: Game, numConnections: Int) {
        if (numConnections != game.countPlayers()) {
            // If connections > players, then it means that each player has >1 websocket connection to the server.
            // For now, that seems to be fine. We can refactor the frontend to share a single websocket connection between components in the view
            //   at a later time if it becomes necessary.
            LOGGER.warn("There are $numConnections websocket connections, but this gameId has ${game.countPlayers()} associated players.")
        }
    }
}