package com.game.main

import java.util.*

/**
 * The set of players in a particular game
 */
class Players {
    private val players: MutableMap<String, Player> = Collections.synchronizedMap(mutableMapOf())
    private val playerOrders: MutableList<String> = mutableListOf()
    private var currentPlayerIndex: Int = 0

    fun currentPlayer(): Player {
        return players[playerOrders[currentPlayerIndex]] ?: throw RuntimeException("Current player was unexpectedly null")
    }

    fun nextPlayer(): Player {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerOrders.size
        return currentPlayer()
    }

    fun skipPlayer(): Player {
        currentPlayerIndex = (currentPlayerIndex + 2) % playerOrders.size
        return currentPlayer()
    }

    fun addPlayer(username: String): Player {
        val newPlayer = Player(username)
        players[username] = newPlayer
        return newPlayer
    }

    fun shufflePlayerOrders(random: Random = Random()) {
        val shuffledPlayerOrders = players.keys.shuffled(random)
        playerOrders.clear()
        playerOrders.addAll(shuffledPlayerOrders)
    }

    fun count() = players.size

    // Should only be used in Game for serializing the set of users in a wire message.
    fun playerListForSerialization() = players.keys.toList()

    fun userMapForSerialization() = players


    // For use in tests
    fun playersInOrder() = playerOrders.map { players[it]?.getUsername() }

    // For use in tests
    fun useUnshuffledOrder() {
        playerOrders.clear()
        playerOrders.addAll(players.keys)
    }

    fun getPlayer(username: String): Player? {
        return players[username]
    }

    fun scores() = players.values.associateBy(
        { p -> p.getUsername() },
        { p -> p.getPlayerPoints() }
    )
}