package com.game.main.ws

import com.game.main.api.GameRepository
import com.game.main.hitormiss.DiceResult
import com.game.main.hitormiss.Game
import org.http4k.websocket.WsMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

class GameWebSocketTest {

    private val testServer = TestGameServer()
    private val game = Game("testGameId")
    private val alice = TestPlayer("alice")
    private val zuno = TestPlayer("zuno")
    private val grace = TestPlayer("grace")

    @BeforeEach
    fun before() {
        testServer.start()
        testServer.createLobby(game, alice, zuno, grace)
    }

    @AfterEach
    fun after() {
        testServer.stop()
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

        testServer.connectWithError(rob, game)

        rob.assertFirstReplyEquals(WsMessageType.ERROR, "Game already started")
    }

    @Test
    fun `replies with an error when the game is not found`() {
        val rob = TestPlayer("rob")

        testServer.connectWithError(rob, Game("non-existent-game"))

        rob.assertFirstReplyEquals(WsMessageType.ERROR, "Game not found")
    }

    @Test
    @Timeout(value = 4)
    fun `replies with an error to messages with the wrong format`() {
        alice.send(WsMessage("Nonsense"))

        alice.assertFirstReplyEquals(WsMessageType.ERROR, "Invalid message")
    }

    @Test
    @Timeout(value = 4)
    fun `replies with game not found if game disappears`() {
        GameRepository.reset()

        alice.send(WsMessageType.CATEGORY_SELECTED, mapOf("category" to "Science"))

        alice.assertFirstReplyEquals(WsMessageType.ERROR, "Game not found")
    }

    @Test
    @Timeout(value = 4)
    fun `replies to category selected message with a corresponding response`() {
        game.start()

        alice.send(WsMessageType.CATEGORY_SELECTED, mapOf("category" to "Science"))

        alice.assertFirstReplyEquals(WsMessageType.CATEGORY_SELECTED, "Science")
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
                WsMessageType.SCORES,
                listOf(
                    mapOf("playerId" to "alice", "score" to 1),
                    mapOf("playerId" to "zuno", "score" to 0),
                    mapOf("playerId" to "grace", "score" to 1)
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
            player.assertNthReplyEquals(3, WsMessageType.NEXT_TURN, "zuno")
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
            player.assertNthReplyEquals(3, WsMessageType.NEXT_TURN, "zuno")
        }

        // Zuno's turn
        game.startTurn("zuno", DiceResult.MISS)
        alice.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))
        grace.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))
        listOf(alice, zuno, grace).forEach { player ->
            player.assertNthReplyEquals(3, WsMessageType.NEXT_TURN, "grace")
        }

        // Grace's turn
        game.startTurn("grace", DiceResult.HIT)
        alice.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))
        zuno.send(WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, mapOf("hitOrMiss" to "HIT"))
        listOf(alice, zuno, grace).forEach { player ->
            player.assertNthReplyEquals(3, WsMessageType.NEXT_ROUND, "zuno")
        }

        assertEquals("zuno", game.currentPlayer().id)
    }
}
