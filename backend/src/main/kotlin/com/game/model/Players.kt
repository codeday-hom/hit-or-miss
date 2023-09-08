package com.game.model

import java.lang.RuntimeException
import java.util.*

/**
 * The set of players in a particular game
 */
class Players(
    val users: MutableMap<String, String> = Collections.synchronizedMap(mutableMapOf()),
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

    fun updateUsername(forPlayerWithId: String, username: String) {
        users[forPlayerWithId] = username
    }

    fun shufflePlayerOrders() {
        val shuffledPlayerOrders = users.keys.shuffled()
        playerOrders.clear()
        playerOrders.addAll(shuffledPlayerOrders)
    }

    fun count() = users.size
}