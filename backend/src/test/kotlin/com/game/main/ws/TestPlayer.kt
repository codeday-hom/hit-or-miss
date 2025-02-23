package com.game.main.ws

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.game.main.hitormiss.Game
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.server.Http4kServer
import org.http4k.websocket.WsClient
import org.http4k.websocket.WsMessage
import org.junit.jupiter.api.Assertions

class TestPlayer(private val name: String) {

    private lateinit var client: WsClient
    private lateinit var game: Game

    fun connect(server: Http4kServer, game: Game, skipConnectionAssertion: Boolean = false) {
        client = WebsocketClient.blocking(Uri.of("ws://localhost:${server.port()}/${game.gameId}/$name"))
        if (!skipConnectionAssertion) {
            assertFirstReplyEquals(WsMessageType.USER_JOINED, game.playerListForSerialization())
            game.addPlayer(name)
        }
        this.game = game
    }

    fun send(message: WsMessage) {
        client.send(message)
    }

    fun send(body: ReceivedWsMessage) {
        send(WsMessage(jacksonObjectMapper().writeValueAsString(body)))
    }

    fun send(type: WsMessageType, data: Map<String, String>) {
        send(ReceivedWsMessage(game.gameId, name, type, data))
    }

    fun assertNthReplyEquals(n: Int, type: WsMessageType, data: Any) {
        val reply = client.received().take(n).last()
        val expected = WsMessage(jacksonObjectMapper().writeValueAsString(mapOf("type" to type, "data" to data)))
        Assertions.assertEquals(expected, reply)
    }

    fun assertFirstReplyEquals(type: WsMessageType, data: Any) {
        assertNthReplyEquals(1, type, data)
    }
}