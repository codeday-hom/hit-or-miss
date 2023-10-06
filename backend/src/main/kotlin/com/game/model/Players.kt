package com.game.model

import java.lang.RuntimeException
import java.util.*

/**
 * The set of players in a particular game
 */
class Players(
    private val users: MutableMap<String, Player> = Collections.synchronizedMap(mutableMapOf()),
    private val userNameMap: MutableMap<String, String> = Collections.synchronizedMap(mutableMapOf()),
    private val playerOrders: MutableList<String> = mutableListOf(),
    private var currentPlayerIndex: Int = 0
) {

    fun currentPlayer(): Player {
        return users[playerOrders[currentPlayerIndex]] ?: throw RuntimeException("Current player was unexpectedly null")
    }

    fun nextPlayer(): Player {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerOrders.size
        return currentPlayer()
    }

    fun addPlayer(id: String, username: String): Player {
        val newPlayer = Player(username)
        users[id] = newPlayer
        userNameMap[id] = username
        return newPlayer
    }

    fun shufflePlayerOrders(random: Random = Random()) {
        val shuffledPlayerOrders = users.keys.shuffled(random)
        playerOrders.clear()
        playerOrders.addAll(shuffledPlayerOrders)
    }

    fun count() = users.size

    // Should only be used in Game for serializing the set of users in a wire message.
    fun userNameMapForSerialization() = userNameMap

    fun userMapForSerialization() = users


    // For use in tests
    fun playersInOrder() = playerOrders.map { users[it]?.getUserName() }

    // For use in tests
    fun useUnshuffledOrder() {
        playerOrders.clear()
        playerOrders.addAll(users.keys)
    }

    fun getPlayer(userName: String): Player? {
        val userId = userNameMap.entries.find { it.value == userName }?.key ?: return null
        return users[userId]
    }
}