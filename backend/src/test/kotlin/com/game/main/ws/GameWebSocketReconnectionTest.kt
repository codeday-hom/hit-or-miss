package com.game.main.ws

import com.game.main.hitormiss.DiceResult
import com.game.main.hitormiss.Game
import java.time.Duration
import java.time.Instant
import java.util.Locale
import java.util.Random
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

class GameWebSocketReconnectionTest {

    private val testClock = FixedSettableClock(Instant.now())
    private val testServer = TestGameServer(testClock)
    private val game = Game("testGameId", Random(1234567L))
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
    fun `disconnected player receives list of disconnected player ids`() {
        game.startForTest()

        grace.disconnect()
        listOf(alice, zuno).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.USER_DISCONNECTED, "grace")
        }

        grace.reconnectAnonymously()

        grace.assertFirstReplyEquals(WsMessageType.DISCONNECTED_PLAYER_IDS, mapOf("disconnectedPlayerIds" to listOf("grace")))
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
                mapOf("category" to category, "countdownTimerStart" to testClock.instant().toEpochMilli())
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
                "phaseData" to mapOf("category" to category, "countdownTimerStart" to testClock.instant().toEpochMilli())
            )
        )
    }

    @Test
    @Timeout(value = 4)
    fun `player can reconnect during dice roll`() {
        game.startForTest()

        // Alice selects a category, triggering the start of the countdown
        val category = "Type of pasta"
        alice.send(WsMessageType.CATEGORY_SELECTED, mapOf("category" to category))
        listOf(alice, zuno, grace).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.CATEGORY_SELECTED,
                mapOf("category" to category, "countdownTimerStart" to testClock.instant().toEpochMilli())
            )
        }

        // Countdown finishes
        testClock.windForward(Duration.ofSeconds(31))

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
                "phase" to GamePhase.ROLL_DICE,
                "phaseData" to mapOf("category" to category)
            )
        )
    }

    @Test
    @Timeout(value = 4)
    fun `player can reconnect during word selection`() {
        game.startForTest()

        // Alice selects a category, triggering the start of the countdown
        val category = "Type of pasta"
        alice.send(WsMessageType.CATEGORY_SELECTED, mapOf("category" to category))
        listOf(alice, zuno, grace).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.CATEGORY_SELECTED,
                mapOf("category" to category, "countdownTimerStart" to testClock.instant().toEpochMilli())
            )
        }

        // Countdown finishes
        testClock.windForward(Duration.ofSeconds(31))

        // Alice rolls the dice
        alice.send(WsMessageType.ROLL_DICE, emptyMap())
        val seededRandomDiceResultInt = 1
        val seededRandomDiceResult = DiceResult.HIT.name.lowercase().replaceFirstChar { it.uppercase(Locale.getDefault()) }
        listOf(alice, zuno, grace).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.ROLL_DICE_RESULT, seededRandomDiceResultInt)
        }
        alice.send(WsMessageType.ROLL_DICE_HIT_OR_MISS, mapOf("diceResult" to seededRandomDiceResult))
        listOf(alice, zuno, grace).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.ROLL_DICE_HIT_OR_MISS, seededRandomDiceResult)
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
                "phase" to GamePhase.SELECT_WORD,
                "phaseData" to mapOf("category" to category, "diceResult" to seededRandomDiceResult.uppercase())
            )
        )
    }

    @Test
    @Timeout(value = 4)
    fun `player can reconnect during hit or miss selection`() {
        game.startForTest()

        // Alice selects a category, triggering the start of the countdown
        val category = "Type of pasta"
        alice.send(WsMessageType.CATEGORY_SELECTED, mapOf("category" to category))
        listOf(alice, zuno, grace).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.CATEGORY_SELECTED,
                mapOf("category" to category, "countdownTimerStart" to testClock.instant().toEpochMilli())
            )
        }

        // Countdown finishes
        testClock.windForward(Duration.ofSeconds(31))

        // Alice rolls the dice
        alice.send(WsMessageType.ROLL_DICE, emptyMap())
        val seededRandomDiceResultInt = 1
        val seededRandomDiceResult = DiceResult.HIT.name.lowercase().replaceFirstChar { it.uppercase(Locale.getDefault()) }
        listOf(alice, zuno, grace).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.ROLL_DICE_RESULT, seededRandomDiceResultInt)
        }
        alice.send(WsMessageType.ROLL_DICE_HIT_OR_MISS, mapOf("diceResult" to seededRandomDiceResult))
        listOf(alice, zuno, grace).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.ROLL_DICE_HIT_OR_MISS, seededRandomDiceResult)
        }

        // Alice selects a word
        val selectedWord = "Spaghetti"
        alice.send(WsMessageType.SELECTED_WORD, mapOf("selectedWord" to selectedWord))
        listOf(alice, zuno, grace).forEach { player ->
            player.assertFirstReplyEquals(WsMessageType.SELECTED_WORD, selectedWord)
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
                "phase" to GamePhase.SELECT_HIT_OR_MISS,
                "phaseData" to mapOf(
                    "category" to category,
                    "diceResult" to seededRandomDiceResult.uppercase(),
                    "selectedWord" to selectedWord
                )
            )
        )
    }
}
