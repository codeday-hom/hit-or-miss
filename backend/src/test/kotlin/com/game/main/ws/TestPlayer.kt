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
            assertFirstReplyEquals(mapOf("type" to WsMessageType.USER_JOINED.name, "data" to game.playerListForSerialization()))
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

    fun assertNthReplyEquals(n: Int, expectedMessage: Map<String, Any>) {
        val reply = client.received().take(n).last()
        val expected = WsMessage(jacksonObjectMapper().writeValueAsString(expectedMessage))
        Assertions.assertEquals(expected, reply)
    }

    fun assertFirstReplyEquals(expectedMessage: Map<String, Any>) {
        assertNthReplyEquals(1, expectedMessage)
    }
}