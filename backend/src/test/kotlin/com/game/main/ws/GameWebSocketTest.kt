package com.game.main.ws

import com.game.main.api.GameRepository
import com.game.main.hitormiss.DiceResult
import com.game.main.hitormiss.Game
import org.http4k.routing.websockets
import org.http4k.routing.ws.bind
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.websocket.WsMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

class GameWebSocketTest {
    private val websocket = GameWebSocket()

    private lateinit var server: Http4kServer
    private lateinit var game: Game

    private val alice = TestPlayer("alice")
    private val zuno = TestPlayer("zuno")
    private val grace = TestPlayer("grace")

    @BeforeEach
    fun before() {
        server = websockets("/{gameId}/{playerId}" bind websocket.handler()).asServer(Jetty(0))
        server.start()
        val gameId = "testGameId"
        game = Game(gameId)
        GameRepository.createGame(gameId, game)
        alice.connect(server, game)
        zuno.connect(server, game)
        grace.connect(server, game)
    }

    @AfterEach
    fun after() {
        server.stop()
    }

    @Test
    @Timeout(value = 4)
    fun `multiple players can connect to the game`() {
        // The assertions are in the setup.
    }

    @Test
    @Timeout(value = 4)
    fun `replies with an error when the game has already started`() {
        game.start()
        val rob = TestPlayer("rob")

        rob.connect(server, game, skipConnectionAssertion = true)

        rob.assertFirstReplyEquals(mapOf("type" to WsMessageType.ERROR.name, "data" to "Game already started"))
    }

    @Test
    fun `replies with an error when the game is not found`() {
        val rob = TestPlayer("rob")

        rob.connect(server, Game("non-existent-game"), skipConnectionAssertion = true)

        rob.assertFirstReplyEquals(mapOf("type" to WsMessageType.ERROR.name, "data" to "Game not found"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies with an error to messages with the wrong format`() {
        alice.send(WsMessage("Nonsense"))

        alice.assertFirstReplyEquals(mapOf("type" to WsMessageType.ERROR.name, "data" to "Invalid message"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies with game not found if game disappears`() {
        GameRepository.reset()

        alice.send(WsMessageType.CATEGORY_SELECTED, mapOf("category" to "Science"))

        alice.assertFirstReplyEquals(mapOf("type" to WsMessageType.ERROR.name, "data" to "Game not found"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies to category selected message with a corresponding response`() {
        game.start()

        alice.send(WsMessageType.CATEGORY_SELECTED, mapOf("category" to "Science"))

        alice.assertFirstReplyEquals(mapOf("type" to WsMessageType.CATEGORY_SELECTED, "data" to "Science"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies to player-hit-or-miss message with updated scores`() {
        game.startForTest()
        game.startRound()
        game.startTurn("alice", DiceResult.HIT)

        grace.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))

        listOf(alice, zuno, grace).forEach { player ->
            player.assertFirstReplyEquals(
                mapOf(
                    "type" to WsMessageType.SCORES.name,
                    "data" to listOf(
                        mapOf("playerId" to "alice", "score" to 1),
                        mapOf("playerId" to "zuno", "score" to 0),
                        mapOf("playerId" to "grace", "score" to 1)
                    )
                )
            )
        }
    }

    @Test
    @Timeout(value = 4)
    fun `when all players have chosen hit-or-miss a next turn message is broadcast`() {
        game.startForTest()
        game.startRound()
        game.startTurn("alice", DiceResult.HIT)

        grace.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))
        zuno.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))

        listOf(alice, zuno, grace).forEach { player ->
            player.assertNthReplyEquals(3, mapOf("type" to WsMessageType.NEXT_TURN.name, "data" to "zuno"))
        }

        assertEquals("zuno", game.currentPlayer().id)
    }

    @Test
    @Timeout(value = 4)
    fun `when all players have chosen rolled the dice a next round message is broadcast`() {
        game.startForTest()

        // Alice's turn
        game.startRound()
        game.startTurn("alice", DiceResult.HIT)
        grace.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))
        zuno.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))
        listOf(alice, zuno, grace).forEach { player ->
            player.assertNthReplyEquals(3, mapOf("type" to WsMessageType.NEXT_TURN.name, "data" to "zuno"))
        }

        // Zuno's turn
        game.startTurn("zuno", DiceResult.MISS)
        alice.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))
        grace.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))
        listOf(alice, zuno, grace).forEach { player ->
            player.assertNthReplyEquals(3, mapOf("type" to WsMessageType.NEXT_TURN.name, "data" to "grace"))
        }

        // Grace's turn
        game.startTurn("grace", DiceResult.HIT)
        alice.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))
        zuno.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))
        listOf(alice, zuno, grace).forEach { player ->
            player.assertNthReplyEquals(3, mapOf("type" to WsMessageType.NEXT_ROUND.name, "data" to "zuno"))
        }

        assertEquals("zuno", game.currentPlayer().id)
    }
}
