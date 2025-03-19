package com.game.main.api

import com.game.main.hitormiss.Game
import java.util.concurrent.ConcurrentHashMap

object GameRepository {

    private val games = ConcurrentHashMap<String, Game>()

    fun registerGame(game: Game) {
        games[game.gameId] = game
    }

    fun getGame(gameId: String): Game? {
        return games[gameId]
    }

    fun reset() {
        games.clear()
    }
}
