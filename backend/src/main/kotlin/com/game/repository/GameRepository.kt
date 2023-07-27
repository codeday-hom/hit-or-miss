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

//    fun createGame(gameId: String, hostId: String, userIds: List<String> = emptyList()) {
//        games[gameId] = Game(gameId, hostId, userIds)
//    }
//
//    fun getHostId(gameId: String): String? {
//        return games[gameId]?.hostId
//    }
//
//    fun updateUserIds(gameId: String, userList: List<String>) {
//        val hostId = userList.first()
//        val userIds = userList.toList()
//        createGame(gameId, hostId, userIds)
//    }
//
//    fun getUserIds(gameId: String): List<String>? {
//        return games[gameId]?.userIds
//    }
}
