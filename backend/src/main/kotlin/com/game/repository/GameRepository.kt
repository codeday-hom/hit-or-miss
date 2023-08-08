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

    fun addUserToGame(gameId: String): Game {
        val game = getGame(gameId)
        if (game == null) {
            println("Could not find game: $gameId") // TODO: Should result in a HTTP 404.
            throw IllegalArgumentException("Game not found: $gameId")
        }
        val userId = UUID.randomUUID().toString()
        if (game.hostId.isEmpty()) {
            game.hostId = userId
            game.userIds.add(userId)
        } else {
            game.userIds.add(userId)
        }
        createGame(gameId, game)
        return game
    }
}
