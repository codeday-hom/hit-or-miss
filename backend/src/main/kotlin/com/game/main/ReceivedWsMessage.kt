package com.game.main

data class ReceivedWsMessage(val gameId: String, val player: String, val type: WsMessageType, val data: Map<String, String>)