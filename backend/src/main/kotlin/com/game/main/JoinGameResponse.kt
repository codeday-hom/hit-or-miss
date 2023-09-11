package com.game.main

import com.game.model.Player

data class JoinGameResponse(
    val gameId: String,
    val hostId: String,
    val userIds: MutableMap<String, Player>,
    val isStarted: Boolean
)