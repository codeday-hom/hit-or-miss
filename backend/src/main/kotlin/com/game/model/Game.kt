package com.game.model

import java.lang.RuntimeException
import java.util.*
data class Game(
    val gameId: String,
    var hostId: String,
    val users: MutableMap<String, String> = Collections.synchronizedMap(mutableMapOf()),
    private var started: Boolean = false,
    private val playerOrders: MutableList<String> = mutableListOf(),
    private var currentPlayerIndex: Int = 0
) {
    fun currentPlayer(): String {
        return users[playerOrders[currentPlayerIndex]] ?: throw RuntimeException("Current player was unexpectedly null")
    }

    fun nextPlayer(): String {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerOrders.size
        return currentPlayer()
    }

    fun addUser(username: String) {
        val userId = UUID.randomUUID().toString()
        if (hostId.isEmpty()) {
            hostId = userId
        }
        users[userId] = username
    }

    fun start() {
        started = true
        val shuffledPlayerOrders = users.keys.shuffled()
        playerOrders.clear()
        playerOrders.addAll(shuffledPlayerOrders)
    }

    fun isStarted(): Boolean {
        return started
    }
}