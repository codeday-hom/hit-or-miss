package com.game.main.api

data class JoinGameResponse(
    val gameId: String,
    val hostPlayerId: String,
    val players: List<String>,
    val isStarted: Boolean
)