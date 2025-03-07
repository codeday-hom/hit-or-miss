package com.game.main.hitormiss

import com.game.main.ws.GamePhase
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GameStateSerializationTest {

    private val category = "Breakfast foods"
    private val clock = Clock.fixed(Instant.now(), ZoneOffset.UTC)
    private val selectedWord = "Oats"

    @Test
    fun `serializes the initial state of the game`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.SELECT_CATEGORY.name, state["phase"])
        assertEquals(emptyMap<String, Any>(), state["phaseData"])
    }

    @Test
    fun `serializes the state of the game after a category is selected`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        val countdownTimerStart = clock.instant().minusSeconds(5)
        game.startRound(category, countdownTimerStart)

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.WAIT_FOR_COUNTDOWN.name, state["phase"])
        assertEquals(mapOf("category" to category, "countdownTimerStart" to countdownTimerStart.toEpochMilli()), state["phaseData"])
    }

    @Test
    fun `serializes the state of the game after the countdown is over`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        game.startRound(category, clock.instant().minusSeconds(31))

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.ROLL_DICE.name, state["phase"])
        assertEquals(mapOf("category" to category), state["phaseData"])
    }

    @Test
    fun `serializes the state of the game after the dice is rolled`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        game.startRound(category, clock.instant().minusSeconds(31))
        game.startTurn(DiceResult.HIT)

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.SELECT_WORD.name, state["phase"])
        assertEquals(mapOf<String, Any>("category" to category, "diceResult" to DiceResult.HIT.name), state["phaseData"])
    }

    @Test
    fun `serializes the state of the game after a word is selected`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        game.startRound(category, clock.instant().minusSeconds(31))
        game.startTurn(DiceResult.HIT)
        game.selectedWord(selectedWord)

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.SELECT_HIT_OR_MISS.name, state["phase"])
        assertEquals(
            mapOf<String, Any>("category" to category, "diceResult" to DiceResult.HIT.name, "selectedWord" to selectedWord),
            state["phaseData"]
        )
    }

    @Test
    fun `serializes the state of the game during dice rolling in the second turn`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        game.startRound(category, clock.instant().minusSeconds(31))
        game.startTurn(DiceResult.HIT)
        game.selectedWord(selectedWord)
        game.turnResult("veera", TurnResult.HIT)
        game.nextTurn()

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.ROLL_DICE.name, state["phase"])
        assertEquals(mapOf("category" to category), state["phaseData"])
    }

    @Test
    fun `serializes the state of the game during word selection in the second turn`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        game.startRound(category, clock.instant().minusSeconds(31))
        game.startTurn(DiceResult.HIT)
        game.selectedWord(selectedWord)
        game.turnResult("veera", TurnResult.HIT)
        game.nextTurn()
        game.startTurn(DiceResult.MISS)

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.SELECT_WORD.name, state["phase"])
        assertEquals(mapOf<String, Any>("category" to category, "diceResult" to DiceResult.MISS.name), state["phaseData"])
    }

    @Test
    fun `serializes the state of the game during hit or miss selection in the second turn`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        game.startRound(category, clock.instant().minusSeconds(31))
        game.startTurn(DiceResult.HIT)
        game.selectedWord(selectedWord)
        game.turnResult("veera", TurnResult.HIT)
        game.nextTurn()
        game.startTurn(DiceResult.MISS)
        game.selectedWord(selectedWord)

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.SELECT_HIT_OR_MISS.name, state["phase"])
        assertEquals(
            mapOf<String, Any>("category" to category, "diceResult" to DiceResult.MISS.name, "selectedWord" to selectedWord),
            state["phaseData"]
        )
    }

    @Test
    fun `serializes the state of the game during category selection in the second round`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        game.startRound(category, clock.instant().minusSeconds(31))
        game.startTurn(DiceResult.HIT)
        game.selectedWord(selectedWord)
        game.turnResult("veera", TurnResult.HIT)
        game.nextTurn()
        game.startTurn(DiceResult.MISS)
        game.selectedWord(selectedWord)
        game.turnResult("rob", TurnResult.HIT)
        game.nextRound()

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.SELECT_CATEGORY.name, state["phase"])
        assertEquals(emptyMap<String, Any>(), state["phaseData"])
    }

    @Test
    fun `serializes the state of the game during the countdown in the second round`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        game.startRound(category, clock.instant().minusSeconds(80))
        game.startTurn(DiceResult.HIT)
        game.selectedWord(selectedWord)
        game.turnResult("veera", TurnResult.HIT)
        game.nextTurn()
        game.startTurn(DiceResult.MISS)
        game.selectedWord(selectedWord)
        game.turnResult("rob", TurnResult.HIT)
        game.nextRound()
        val countdownTimerStart = clock.instant().minusSeconds(3)
        game.startRound(category, countdownTimerStart)

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.WAIT_FOR_COUNTDOWN.name, state["phase"])
        assertEquals(mapOf("category" to category, "countdownTimerStart" to countdownTimerStart.toEpochMilli()), state["phaseData"])
    }

    @Test
    fun `serializes the state of the game after the countdown in the second round`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        game.startRound(category, clock.instant().minusSeconds(80))
        game.startTurn(DiceResult.HIT)
        game.selectedWord(selectedWord)
        game.turnResult("veera", TurnResult.HIT)
        game.nextTurn()
        game.startTurn(DiceResult.MISS)
        game.selectedWord(selectedWord)
        game.turnResult("rob", TurnResult.HIT)
        game.nextRound()
        game.startRound(category, clock.instant().minusSeconds(31))

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.ROLL_DICE.name, state["phase"])
        assertEquals(mapOf("category" to category), state["phaseData"])
    }

    @Test
    fun `serializes the state of the game during word selection in the second round`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        game.startRound(category, clock.instant().minusSeconds(80))
        game.startTurn(DiceResult.HIT)
        game.selectedWord(selectedWord)
        game.turnResult("veera", TurnResult.HIT)
        game.nextTurn()
        game.startTurn(DiceResult.MISS)
        game.selectedWord(selectedWord)
        game.turnResult("rob", TurnResult.HIT)
        game.nextRound()
        game.startRound(category, clock.instant().minusSeconds(31))
        game.startTurn(DiceResult.MISS)

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.SELECT_WORD.name, state["phase"])
        assertEquals(mapOf<String, Any>("category" to category, "diceResult" to DiceResult.MISS.name), state["phaseData"])
    }

    @Test
    fun `serializes the state of the game during hit or miss selection in the second round`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        game.startRound(category, clock.instant().minusSeconds(80))
        game.startTurn(DiceResult.HIT)
        game.selectedWord(selectedWord)
        game.turnResult("veera", TurnResult.HIT)
        game.nextTurn()
        game.startTurn(DiceResult.MISS)
        game.selectedWord(selectedWord)
        game.turnResult("rob", TurnResult.HIT)
        game.nextRound()
        game.startRound(category, clock.instant().minusSeconds(31))
        game.startTurn(DiceResult.MISS)
        game.selectedWord(selectedWord)

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.SELECT_HIT_OR_MISS.name, state["phase"])
        assertEquals(
            mapOf<String, Any>("category" to category, "diceResult" to DiceResult.MISS.name, "selectedWord" to selectedWord),
            state["phaseData"]
        )
    }

    @Test
    fun `serializes the state of the game after the game is over`() {
        val game = Game("testGameId")
        game.addPlayer("rob")
        game.addPlayer("veera")
        game.startForTest()
        game.startRound(category, clock.instant().minusSeconds(80))
        game.startTurn(DiceResult.HIT)
        game.selectedWord(selectedWord)
        game.turnResult("veera", TurnResult.HIT)
        game.nextTurn()
        game.startTurn(DiceResult.MISS)
        game.selectedWord(selectedWord)
        game.turnResult("rob", TurnResult.HIT)
        game.nextRound()
        game.startRound(category, clock.instant().minusSeconds(31))
        game.startTurn(DiceResult.MISS)
        game.selectedWord(selectedWord)
        game.turnResult("rob", TurnResult.HIT)
        game.nextTurn()
        game.startTurn(DiceResult.MISS)
        game.selectedWord(selectedWord)
        game.turnResult("veera", TurnResult.HIT)
        game.nextRound()

        val state = game.gameStateForSerialization(clock)
        assertEquals(GamePhase.GAME_OVER.name, state["phase"])
        assertEquals(emptyMap<String, Any>(), state["phaseData"])
    }
}
