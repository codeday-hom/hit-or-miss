package org.http4k.repository

import org.http4k.model.Game

object GameRepository {
    private val games = HashMap<String, Game>()

    fun createGame(gameId: String, hostId: String) {
        games[gameId] = Game(gameId, hostId)
    }

    fun getHostId(gameId: String): String? {
        return games[gameId]?.hostId
    }
}
