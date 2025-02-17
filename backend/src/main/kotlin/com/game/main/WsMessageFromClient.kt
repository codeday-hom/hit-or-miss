package com.game.main

data class WsMessageFromClient(val type: WsMessageType, val data: Map<String, String>)