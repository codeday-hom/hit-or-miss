package com.game.main

data class JoinGameResponse(
    val gameId: String,
    val hostId: String,
    val usernames: List<String>,
    val isStarted: Boolean
)