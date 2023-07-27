package com.game.main

data class lobbyResponse(val gameId: String, val hostId: String, val userIds: MutableList<String>)