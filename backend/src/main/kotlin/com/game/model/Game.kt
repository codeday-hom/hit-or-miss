package com.game.model

data class Game(val gameId: String, var hostId: String, val userIds: MutableList<String>, var currentPlayerIndex: Int = 0)
