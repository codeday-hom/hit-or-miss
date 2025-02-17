package com.game.main

data class WsMessageFromClient(val gameId: String, val player: String, val type: WsMessageType, val data: Map<String, String>)