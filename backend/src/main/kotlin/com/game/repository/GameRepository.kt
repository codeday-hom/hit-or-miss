package com.game.repository

import com.game.model.Game
import java.util.concurrent.ConcurrentHashMap

object GameRepository {

    private val games = ConcurrentHashMap<String, Game>()

    fun createGame(gameId: String, game: Game) {
        games[gameId] = game
    }

    fun getGame(gameId: String): Game? {
        return games[gameId]
    }

    fun reset() {
        games.clear()
    }
}
