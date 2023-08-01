package com.game.model

data class Game(val gameId: String, var hostId: String, val users: MutableMap<String, String> = mutableMapOf())