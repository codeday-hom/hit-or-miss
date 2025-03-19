package com.game.main.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.main.ws.WsMessageType.HEARTBEAT_ACK
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger(WsMessenger::class.java.simpleName)

class WsMessenger {

    private val mapper = ObjectMapper()

    fun send(ws: Websocket, type: WsMessageType, data: Any?) {
        if (type != HEARTBEAT_ACK) {
            LOGGER.info("Sending a $type message with data $data")
        }
        sendMessageInternal(ws, type, data)
    }

    fun broadcast(connections: List<Websocket>, type: WsMessageType, data: Any?) {
        if (type != HEARTBEAT_ACK) {
            LOGGER.info("Broadcasting a $type message with data $data")
        }
        connections.forEach { ws ->
            sendMessageInternal(ws, type, data)
        }
    }

    private fun sendMessageInternal(ws: Websocket, type: WsMessageType, data: Any?) {
        ws.send(WsMessage(mapper.writeValueAsString(mapOf("type" to type.name, "data" to data))))
    }
}
