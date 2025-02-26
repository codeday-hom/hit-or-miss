package com.game.main.ws

import com.game.main.hitormiss.Game
import com.game.main.ws.WsMessageType.ERROR
import com.game.main.ws.WsMessageType.USER_DISCONNECTED
import com.game.main.ws.WsMessageType.USER_JOINED
import com.game.main.ws.WsMessageType.USER_RECONNECTED
import java.util.Collections
import org.http4k.websocket.Websocket
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger(WebSocketConnections::class.java.simpleName)

class WebSocketConnections(private val messenger: WsMessenger) {

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
            LOGGER.info("Adding anonymous connection for game $gameId")
            anonymousConnections.getOrPut(gameId) { mutableListOf() }.add(ws)
        } else {
            val gameConnections = identifiedConnections.getOrPut(gameId) { mutableListOf() }
            val playerConnections = gameConnections.find { it.first == playerId }
            if (playerConnections == null) {
                LOGGER.info("Adding first connection in game $gameId for player $playerId")
                gameConnections.add(Pair(playerId, mutableListOf(ws)))
            } else {
                synchronized(playerConnections) {
                    if (playerConnections.second.isEmpty()) {
                        LOGGER.info("Player $playerId has reconnected to game $gameId")
                        messenger.broadcast(gameConnections.map { it.second }.flatten(), USER_RECONNECTED, playerId)
                    } else {
                        LOGGER.info("Adding new connection in game $gameId for player $playerId")
                    }
                    playerConnections.second.add(ws)
                }
            }
        }

        if (!game.isStarted()) {
            messenger.send(ws, USER_JOINED, game.playerListForSerialization())
        } else if (!game.hasPlayer(playerId)) {
            messenger.send(ws, ERROR, "Game already started")
        }
    }

    /**
     * Cleaning up closed connections allows us to detect when a player has disconnected.
     */
    fun onClose(ws: Websocket, gameId: String) {
        if (anonymousConnections[gameId]?.remove(ws) == true) {
            LOGGER.info("Anonymous connection for game $gameId has closed")
        }

        val gameConnections = identifiedConnections[gameId]
        val playerConnections = gameConnections?.find { it.second.contains(ws) }
        if (playerConnections != null) {
            synchronized(playerConnections) {
                val player = playerConnections.first
                if (playerConnections.second.remove(ws)) {
                    LOGGER.info("Connection for game $gameId of player $player has closed")
                }
                if (playerConnections.second.isEmpty()) {
                    LOGGER.info("Player $player has disconnected from game $gameId")
                    messenger.broadcast(gameConnections.map { it.second }.flatten(), USER_DISCONNECTED, player)
                }
            }
        }
    }

    /**
     * Connections to all players in a lobby.
     *
     * Such players have not yet sent any websocket messages to the server and so are not
     * identifiable by their player id.
     */
    fun forLobby(game: Game): List<Websocket> {
        val connections = anonymousConnections[game.gameId] ?: throw RuntimeException("Cannot broadcast for non-existing lobby ${game.gameId}")
        checkConnectionCount(game, connections.size)
        return connections
    }

    /**
     * Connections to all connections for all players in a running game.
     */
    fun forGame(game: Game): List<Websocket> {
        val connections = identifiedConnections[game.gameId] ?: throw RuntimeException("Cannot broadcast for non-existing game ${game.gameId}")
        checkConnectionCount(game, connections.size)
        return connections.map { it.second }.flatten()
    }

    private fun checkConnectionCount(game: Game, numConnections: Int) {
        if (numConnections != game.countPlayers()) {
            // If connections > players, then it means that each player has >1 websocket connection to the server.
            // For now, that seems to be fine. We can refactor the frontend to share a single websocket connection between components in the view
            //   at a later time if it becomes necessary.
            LOGGER.warn("There are ${numConnections} websocket connections, but this gameId has ${game.countPlayers()} associated players.")
        }
    }
}