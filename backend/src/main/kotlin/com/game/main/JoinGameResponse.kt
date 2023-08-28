package com.game.main

data class JoinGameResponse(
    val gameId: String,
    val hostId: String,
    val userIds: MutableMap<String, String>,
    val isStarted: Boolean
)