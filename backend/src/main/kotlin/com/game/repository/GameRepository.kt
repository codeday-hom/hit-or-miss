package com.game.repository

import com.game.model.Game
import org.http4k.core.cookie.Cookie

object GameRepository {
    private val games = HashMap<String, Game>()

    fun addGame(gameId: String, hostId: String) {
        games[gameId] = Game(gameId, hostId)
    }

    fun findGame(gameId: String): Game? {
        return games[gameId]
    }
}
