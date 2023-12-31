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
        game.addPlayer("alice")
        game.addPlayer("zuno")
        game.addPlayer("grace")
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
    fun `replies to category selected message with a corresponding response`() {
        assertReceivedUserJoinedMessage()
        game.start()

        send(GameWsMessage(WsMessageType.CATEGORY_SELECTED.name, mapOf("category" to "Science")))

        assertFirstReplyEquals(mapOf("type" to WsMessageType.CATEGORY_SELECTED.name, "data" to "Science"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies to player-hit-or-miss message with updated scores`() {
        assertReceivedUserJoinedMessage()
        game.startForTest()
        game.startRound()
        game.startTurn("alice", DiceResult.HIT)

        send(GameWsMessage(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS.name, mapOf("hitOrMiss" to "HIT", "username" to "grace")))

        assertFirstReplyEquals(
            mapOf(
                "type" to WsMessageType.SCORES.name, "data" to listOf(
                    mapOf("username" to "alice", "score" to 1),
                    mapOf("username" to "zuno", "score" to 0),
                    mapOf("username" to "grace", "score" to 1)
                )
            )
        )
    }

    @Test
    @Timeout(value = 4)
    fun `when all players have chosen hit-or-miss a next turn message is broadcast`() {
        assertReceivedUserJoinedMessage()
        game.startForTest()
        game.startRound()
        game.startTurn("alice", DiceResult.HIT)

        send(GameWsMessage(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS.name, mapOf("hitOrMiss" to "HIT", "username" to "grace")))
        send(GameWsMessage(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS.name, mapOf("hitOrMiss" to "HIT", "username" to "zuno")))

        assertNthReplyEquals(3, mapOf("type" to WsMessageType.NEXT_TURN.name, "data" to "zuno"))

        assertEquals("zuno", game.currentPlayer().name)
    }

    @Test
    @Timeout(value = 4)
    fun `when all players have chosen rolled the dice a next round message is broadcast`() {
        assertReceivedUserJoinedMessage()
        game.startForTest()

        // Alice's turn
        game.startRound()
        game.startTurn("alice", DiceResult.HIT)
        send(GameWsMessage(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS.name, mapOf("hitOrMiss" to "HIT", "username" to "grace")))
        send(GameWsMessage(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS.name, mapOf("hitOrMiss" to "HIT", "username" to "zuno")))
        assertNthReplyEquals(3, mapOf("type" to WsMessageType.NEXT_TURN.name, "data" to "zuno"))

        // Zuno's turn
        game.startTurn("zuno", DiceResult.MISS)
        send(GameWsMessage(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS.name, mapOf("hitOrMiss" to "HIT", "username" to "alice")))
        send(GameWsMessage(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS.name, mapOf("hitOrMiss" to "HIT", "username" to "grace")))
        assertNthReplyEquals(3, mapOf("type" to WsMessageType.NEXT_TURN.name, "data" to "grace"))

        // Grace's turn
        game.startTurn("grace", DiceResult.HIT)
        send(GameWsMessage(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS.name, mapOf("hitOrMiss" to "HIT", "username" to "alice")))
        send(GameWsMessage(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS.name, mapOf("hitOrMiss" to "HIT", "username" to "zuno")))
        assertNthReplyEquals(3, mapOf("type" to WsMessageType.NEXT_ROUND.name, "data" to "zuno"))

        assertEquals("zuno", game.currentPlayer().name)
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
