package com.game.main

data class ReceivedWsMessage(val gameId: String, val player: String, val type: WsMessageType, val data: Map<String, String>) {

    fun getRequiredData(fieldName: String): String = data[fieldName]
        ?: throw IllegalArgumentException("Message of type $type requires data field '$fieldName'")
}
