package com.game.model

import java.lang.RuntimeException
import java.util.*

/**
 * The set of players in a particular game
 */
class Players(
    private val users: MutableMap<String, String> = Collections.synchronizedMap(mutableMapOf()),
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

    fun addPlayer(id: String, username: String) {
        users[id] = username
    }

    fun shufflePlayerOrders(random: Random = Random()) {
        val shuffledPlayerOrders = users.keys.shuffled(random)
        playerOrders.clear()
        playerOrders.addAll(shuffledPlayerOrders)
    }

    fun count() = users.size

    // Should only be used in Game for serializing the set of users in a wire message.
    fun userMapForSerialization() = users

    // For use in tests
    fun playersInOrder() = playerOrders.map { users[it] }

    // For use in tests
    fun useUnshuffledOrder() {
        playerOrders.clear()
        playerOrders.addAll(users.keys)
    }
}