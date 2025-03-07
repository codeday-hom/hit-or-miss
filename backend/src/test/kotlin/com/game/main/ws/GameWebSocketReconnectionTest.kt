package com.game.main.ws

import com.game.main.hitormiss.Game
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

class GameWebSocketReconnectionTest {

    private val fixedClock = Clock.fixed(Instant.now(), ZoneOffset.UTC)
    private val testServer = TestGameServer(fixedClock)
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
    fun `players are informed when a player disconnects`() {
        game.startForTest()

        alice.disconnect()

        listOf(zuno, grace).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.USER_DISCONNECTED, "alice")
        }
    }

    @Test
    @Timeout(value = 4)
    fun `players are informed when a disconnected player reconnects`() {
        game.startForTest()

        grace.disconnect()
        listOf(alice, zuno).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.USER_DISCONNECTED, "grace")
        }

        grace.reconnect()

        listOf(alice, zuno).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.USER_RECONNECTED, "grace")
        }
    }

    @Test
    @Timeout(value = 4)
    fun `player can reconnect during category selection`() {
        game.startForTest()

        zuno.disconnect()
        listOf(alice, grace).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.USER_DISCONNECTED, "zuno")
        }

        zuno.reconnect()

        zuno.assertFirstReplyEquals(
            WsMessageType.GAME_JOINABLE,
            mapOf(
                "currentPlayer" to "alice",
                "players" to listOf("alice", "zuno", "grace"),
                "scores" to listOf(
                    mapOf("playerId" to "alice", "score" to 0),
                    mapOf("playerId" to "zuno", "score" to 0),
                    mapOf("playerId" to "grace", "score" to 0)
                ),
                "phase" to GamePhase.SELECT_CATEGORY,
                "phaseData" to mapOf<String, Any>()
            )
        )
    }

    @Test
    @Timeout(value = 4)
    fun `player can reconnect during countdown`() {
        game.startForTest()

        // Alice selects a category, triggering the start of the countdown
        val category = "Type of pasta"
        alice.send(WsMessageType.CATEGORY_SELECTED, mapOf("category" to category))
        listOf(alice, zuno, grace).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.CATEGORY_SELECTED,
                mapOf("category" to category, "countdownTimerStart" to fixedClock.instant().toEpochMilli())
            )
        }

        zuno.disconnect()
        listOf(alice, grace).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.USER_DISCONNECTED, "zuno")
        }

        zuno.reconnect()

        zuno.assertFirstReplyEquals(
            WsMessageType.GAME_JOINABLE,
            mapOf(
                "currentPlayer" to "alice",
                "players" to listOf("alice", "zuno", "grace"),
                "scores" to listOf(
                    mapOf("playerId" to "alice", "score" to 0),
                    mapOf("playerId" to "zuno", "score" to 0),
                    mapOf("playerId" to "grace", "score" to 0)
                ),
                "phase" to GamePhase.WAIT_FOR_COUNTDOWN,
                "phaseData" to mapOf("category" to category, "countdownTimerStart" to fixedClock.instant().toEpochMilli())
            )
        )
    }

    // TODO: Fill out these functional tests.

    @Test
    @Timeout(value = 4)
    fun `player can reconnect during dice roll`() {
    }

    @Test
    @Timeout(value = 4)
    fun `player can reconnect during word selection`() {
    }

    @Test
    @Timeout(value = 4)
    fun `player can reconnect during hit or miss selection`() {
    }

    @Test
    @Timeout(value = 4)
    fun `player can reconnect after the game is over`() {
    }
}
