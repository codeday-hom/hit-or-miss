package org.http4k.main

data class Player(val name: String, var points: Int)

class Players {
    val players: MutableList<Player> = mutableListOf()

    fun addPlayer(name: String) {
        val player = Player(name, 0)
        players.add(player)
    }

    fun getPlayerPoints(name: String): Int? {
        val player = players.find { it.name == name }
        return player?.points
    }

    fun addPoints(name: String) {
        val player = players.find { it.name == name }
        player?.let { it.points++ }
    }
}
