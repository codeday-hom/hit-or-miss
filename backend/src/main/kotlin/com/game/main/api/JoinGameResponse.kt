package com.game.main.api

data class JoinGameResponse(
    val gameId: String,
    val hostId: String,
    val usernames: List<String>,
    val isStarted: Boolean
)