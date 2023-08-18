package com.game.model

import java.util.*

data class Game(
    val gameId: String,
    var hostId: String,
    val users: MutableMap<String, String> = mutableMapOf(),
    // sortedMap will maintain the sorting order provided by the given comparator.
    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/to-sorted-map.html
    private var started: Boolean = false,
    private val playerOrders: MutableList<String> = mutableListOf(),
    private var currentPlayerIndex: Int = 0
) {
    fun currentPlayer(): String? {
        return users[playerOrders[currentPlayerIndex]]
    }

    fun nextPlayer(): String? {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerOrders.size
        return currentPlayer()
    }

    fun addUser(username: String): Game {
        val userId = UUID.randomUUID().toString()
        if (hostId.isEmpty()) {
            hostId = userId
        }
        users[userId] = username
        return this
    }

    fun start(): Boolean {
        started = true
        val shuffledPlayerOrders = users.keys.shuffled()
        playerOrders.clear()
        playerOrders.addAll(shuffledPlayerOrders)
        return true
    }

    fun isStarted(): Boolean {
        return started
    }
}