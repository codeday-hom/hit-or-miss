package com.game.main

data class JoinGameResponse(
    val gameId: String,
    val hostId: String,
    val userIds: List<String>,
    val isStarted: Boolean
)