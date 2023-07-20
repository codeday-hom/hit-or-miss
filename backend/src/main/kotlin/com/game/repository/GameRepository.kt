package com.game.repository

import com.game.model.Game

object GameRepository {
    private val games = HashMap<String, Game>()

    fun createGame(gameId: String, hostId: String, userIds: List<String> = emptyList()) {
        games[gameId] = Game(gameId, hostId, userIds)
    }

    fun getHostId(gameId: String): String? {
        return games[gameId]?.hostId
    }

    fun updateUserIds(gameId: String, userList: List<String>) {
        val hostId = userList.first()
        val userIds = userList.toList()
        createGame(gameId, hostId, userIds)
    }

    fun getUserIds(gameId: String): List<String>? {
        return games[gameId]?.userIds
    }
}
