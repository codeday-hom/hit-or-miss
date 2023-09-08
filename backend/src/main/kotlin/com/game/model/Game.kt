package com.game.model

import java.util.*

data class Game(
    val gameId: String,
    var hostId: String,
    private var started: Boolean = false,
    private val players: Players = Players()
) {
    fun currentPlayer() = players.currentPlayer()

    fun nextPlayer() = players.nextPlayer()

    fun addUser(username: String) {
        val userId = UUID.randomUUID().toString()
        if (hostId.isEmpty()) {
            hostId = userId
        }
        players.addPlayer(userId, username)
    }

    fun start() {
        started = true
        players.shufflePlayerOrders()
    }

    fun isStarted(): Boolean {
        return started
    }

    fun rollDice(): Int {
        return Random().nextInt(6) + 1
    }

    fun countPlayers() = players.count()

    fun userMapForSerialization() = players.userMapForSerialization()
}