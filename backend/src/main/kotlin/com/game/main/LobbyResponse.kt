package com.game.main

data class LobbyResponse(val gameId: String, val hostId: String, val userIds: MutableMap<String, String>)