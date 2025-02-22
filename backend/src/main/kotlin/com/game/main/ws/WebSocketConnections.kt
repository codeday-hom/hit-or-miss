package com.game.main.ws

import com.game.main.hitormiss.Game
import org.http4k.websocket.Websocket
import org.slf4j.LoggerFactory
import java.util.*

private val LOGGER = LoggerFactory.getLogger(WebSocketConnections::class.java.simpleName)

class WebSocketConnections {

    /**
     * Maps a gameId to a list of websocket connections associated with a game lobby.
     */
    private val anonymousConnections = Collections.synchronizedMap(HashMap<String, MutableList<Websocket>>())

    /**
     * Maps a gameId to a list of websocket connections and the corresponding player id.
     */
    private val gameConnections = Collections.synchronizedMap(HashMap<String, MutableList<Pair<String, MutableList<Websocket>>>>())

    fun onOpen(ws: Websocket, gameId: String, playerId: String?) {
        if (playerId == null) {
            LOGGER.info("Adding anonymous connection for game $gameId")
            anonymousConnections.getOrPut(gameId) { mutableListOf() }.add(ws)
        } else {
            val gameConnections = gameConnections.getOrPut(gameId) { mutableListOf() }
            val playerConnections = gameConnections.find { it.first == playerId }
            if (playerConnections == null) {
                LOGGER.info("Adding first connection in game $gameId for player ${playerId}")
                gameConnections.add(Pair(playerId, mutableListOf(ws)))
            } else {
                LOGGER.info("Adding new connection in game $gameId for player ${playerId}")
                playerConnections.second.add(ws)
            }
        }
    }

    /**
     * Cleaning up closed connections allows us to detect when a player has disconnected.
     */
    fun onClose(ws: Websocket, gameId: String) {
        if (anonymousConnections[gameId]?.remove(ws) == true) {
            LOGGER.info("Anonymous connection for game $gameId has closed")
        }

        val matchingGameConnection = gameConnections[gameId]?.find { it.second.contains(ws) }
        if (matchingGameConnection != null) {
            if (matchingGameConnection.second.remove(ws)) {
                LOGGER.info("Connection for game $gameId of player ${matchingGameConnection.first} has closed")
            }
            if (matchingGameConnection.second.isEmpty()) {
                LOGGER.info("Player ${matchingGameConnection.first} has disconnected from game $gameId")
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
        val connections = gameConnections[game.gameId] ?: throw RuntimeException("Cannot broadcast for non-existing game ${game.gameId}")
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