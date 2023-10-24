package com.game.main

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.routing.websockets
import org.http4k.routing.ws.bind
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.websocket.WsClient
import org.http4k.websocket.WsHandler
import org.http4k.websocket.WsMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import kotlin.properties.Delegates

class GameWebSocketTest {
    private val websocket = GameWebSocket()
    private val testApp: WsHandler = websockets("/{gameId}" bind websocket.handler())
    private lateinit var server: Http4kServer
    private var port by Delegates.notNull<Int>()

    private lateinit var game: Game
    private lateinit var client: WsClient

    @BeforeEach
    fun before() {
        server = testApp.asServer(Jetty(0))
        server.start()
        port = server.port()
        val gameId = "testGameId"
        game = Game(gameId)
        game.addUser("alice")
        game.addUser("zuno")
        game.addUser("grace")
        GameRepository.createGame(gameId, game)
        client = wsClient(gameId)
    }

    @AfterEach
    fun after() {
        server.stop()
    }

    @Test
    @Timeout(value = 4)
    fun `replies to user-joined message with the current list of players`() {
        assertReceivedUserJoinedMessage()
    }

    @Test
    @Timeout(value = 4)
    fun `replies with an error when the game has already started`() {
        assertReceivedUserJoinedMessage()

        game.start()
        client = wsClient(game.gameId)
        assertFirstReplyEquals(mapOf("type" to WsMessageType.ERROR.name, "data" to "Game already started"))
    }

    @Test
    fun `replies with an error when the game is not found`() {
        client = wsClient("non-existing-game")
        assertFirstReplyEquals(mapOf("type" to WsMessageType.ERROR.name, "data" to "Game not found"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies with an error to messages with the wrong format`() {
        client.send(WsMessage("Nonsense"))
        assertNthReplyEquals(2, mapOf("type" to WsMessageType.ERROR.name, "data" to "Invalid message"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies with game not found if game disappears`() {
        // Wait for the websocket connection to be open
        assertReceivedUserJoinedMessage()
        // Then make the game disappear
        GameRepository.reset()

        send(GameWsMessage(WsMessageType.CATEGORY_SELECTED.name, mapOf("category" to "Science")))

        assertFirstReplyEquals(mapOf("type" to WsMessageType.ERROR.name, "data" to "Game not found"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies to next-player message with the next player in the game`() {
        assertReceivedUserJoinedMessage()
        game.startForTest()

        send(GameWsMessage(WsMessageType.NEXT_PLAYER.name, emptyMap()))

        assertFirstReplyEquals(mapOf("type" to WsMessageType.NEXT_PLAYER.name, "data" to "zuno"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies to category selected message with a corresponding response`() {
        assertReceivedUserJoinedMessage()
        game.start()

        send(GameWsMessage(WsMessageType.CATEGORY_SELECTED.name, mapOf("category" to "Science")))

        assertFirstReplyEquals(mapOf("type" to WsMessageType.CATEGORY_SELECTED.name, "data" to "Science"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies to player-hit-or-miss message with a updated score response`() {
        assertReceivedUserJoinedMessage()
        game.startForTest()
        game.updateDiceResult(DiceResult.HIT)

        send(GameWsMessage(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS.name, mapOf("hitOrMiss" to "HIT", "username" to "grace")))

        assertFirstReplyEquals(mapOf("type" to WsMessageType.PLAYER_CHOSE_HIT_OR_MISS.name, "data" to mapOf("alice" to 1, "zuno" to 0, "grace" to 1)))
    }

    private fun wsClient(gameId: String): WsClient = WebsocketClient.blocking(Uri.of("ws://localhost:$port/$gameId"))

    private fun send(body: GameWsMessage) {
        client.send(WsMessage(jacksonObjectMapper().writeValueAsString(body)))
    }

    private fun assertFirstReplyEquals(expectedMessage: Map<String, Any>) {
        assertNthReplyEquals(1, expectedMessage)
    }

    private fun assertNthReplyEquals(n: Int, expectedMessage: Map<String, Any>) {
        val reply = client.received().take(n).last()
        val expected = WsMessage(jacksonObjectMapper().writeValueAsString(expectedMessage))
        assertEquals(expected, reply)
    }

    private fun assertReceivedUserJoinedMessage() {
        assertFirstReplyEquals(mapOf("type" to WsMessageType.USER_JOINED.name, "data" to listOf("alice", "zuno", "grace")))
    }
}
