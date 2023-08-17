package com.game.repository

import com.game.model.Game
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object GameRepository {
    private val games = ConcurrentHashMap<String, Game>()

    fun createGame(gameId: String, game: Game) {
        games[gameId] = game
    }

    fun getGame(gameId: String): Game? {
        return games[gameId]
    }

    fun getCurrentPlayer(gameId: String): String? {
        val game = games[gameId] ?: return null
        return game.users[game.playerOrders[game.currentPlayerIndex]]
    }

    fun addUserToGame(gameId: String, username: String): Game? {
        val game = getGame(gameId) ?: return null
        val userId = UUID.randomUUID().toString()
        if (game.hostId.isEmpty()) {
            game.hostId = userId
        }
        game.users[userId] = username
        createGame(gameId, game)
        return game
    }

    fun reset() {
        games.clear()
    }

    fun startGame(gameId: String): Boolean {
        val game = games[gameId] ?: return false
        game.started = true
        val shuffledPlayerOrders = game.users.keys.shuffled()
        game.playerOrders.clear()
        game.playerOrders.addAll(shuffledPlayerOrders)
        return true
    }

    fun nextPlayer(gameId: String): String? {
        val game = games[gameId] ?: return null
        game.currentPlayerIndex = (game.currentPlayerIndex + 1) % game.playerOrders.size
        return game.users[game.playerOrders[game.currentPlayerIndex]]
    }
}
