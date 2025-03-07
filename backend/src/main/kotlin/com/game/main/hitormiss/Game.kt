package com.game.main.hitormiss

import com.game.main.ws.GamePhase
import java.time.Clock
import java.time.Instant
import java.util.Random
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger(Game::class.java.simpleName)

data class Game(val gameId: String) {

    lateinit var hostPlayerId: String

    private val players: Players = Players()
    private val playersWhoHavePickedACategory: MutableSet<String> = mutableSetOf()

    private var started: Boolean = false
    private var gameOver: Boolean = false

    private lateinit var currentRound: Round
    private lateinit var currentTurn: Turn

    fun currentPlayer() = players.currentPlayer()

    fun addPlayer(playerId: String): Player {
        if (isStarted()) {
            throw IllegalStateException("Game $gameId already started.")
        }
        if (countPlayers() == 0) {
            hostPlayerId = playerId
        }
        return players.addPlayer(playerId)
    }

    fun start(shufflePlayerOrders: (Players) -> Unit = { _ -> players.shufflePlayerOrders() }) {
        started = true
        shufflePlayerOrders(players)
        LOGGER.info("Starting game $gameId with player order: ${players.playersInOrder()}")
    }

    fun startForTest() {
        start { it.useUnshuffledOrder() }
    }

    fun isStarted() = started

    fun isOver() = gameOver

    fun countPlayers() = players.count()

    fun playerListForSerialization() = players.playerListForSerialization()

    fun rollDice(): Int {
        return Random().nextInt(6) + 1
    }

    fun startRound(category: String, countdownTimerStart: Instant) {
        currentRound = Round(category, countdownTimerStart)
    }

    fun startTurn(diceResult: DiceResult) {
        currentTurn = Turn(currentPlayer(), diceResult)
        currentRound.playerRolledTheDice(currentPlayer())
    }

    /**
     * To be used when it is the next player's turn to roll the dice for the currently chosen category.
     */
    fun nextTurn() {
        players.nextPlayer()
    }

    /**
     * To be used when it is the next player's turn to pick a new category.
     */
    fun nextRound() {
        playersWhoHavePickedACategory.add(currentPlayer().id)
        if (playersWhoHavePickedACategory.size < countPlayers()) {
            players.skipPlayer()
        } else {
            gameOver = true
        }
    }

    fun selectedWord(selectedWord: String) {
        currentTurn.selectWord(selectedWord)
    }

    fun turnResult(playerId: String, turnResult: TurnResult) {
        val player = players.getPlayer(playerId) ?: throw IllegalArgumentException("No such player with id '$playerId'")
        currentTurn.result(player, turnResult)
    }

    fun scoresForSerialization(): List<Map<String, Any>> = players.scores().map { mapOf("playerId" to it.key, "score" to it.value) }

    fun allPlayersChoseHitOrMiss(): Boolean {
        return currentTurn.allPlayersChoseHitOrMiss()
    }

    fun allPlayersRolledTheDice(): Boolean {
        return currentRound.allPlayersRolledTheDice()
    }

    fun hasPlayer(playerId: String?): Boolean {
        if (playerId == null) {
            return false
        }

        return players.getPlayer(playerId) != null
    }

    private fun phaseWithData(clock: Clock): Pair<GamePhase, Map<String, Any>> {
        if (!this::currentRound.isInitialized) {
            return Pair(GamePhase.SELECT_CATEGORY, emptyMap())
        }

        // If the currentTurn refers to the last player of the round, and everyone has selected hit or miss,
        // then we are either back in category selection, or the game is over.
        if (this::currentTurn.isInitialized && allPlayersChoseHitOrMiss() && allPlayersRolledTheDice()) {
            return if (isOver()) {
                Pair(GamePhase.GAME_OVER, emptyMap())
            } else {
                Pair(GamePhase.SELECT_CATEGORY, emptyMap())
            }
        }

        if (clock.instant().isBefore(currentRound.countdownTimerStart.plusSeconds(30))) {
            return Pair(GamePhase.WAIT_FOR_COUNTDOWN, mapOf(
                "category" to currentRound.category,
                "countdownTimerStart" to currentRound.countdownTimerStart.toEpochMilli()
            ))
        }

        // If either this is the first turn of the game, or if the currentTurn refers to the previous turn
        if (!this::currentTurn.isInitialized || allPlayersChoseHitOrMiss()) {
            return Pair(GamePhase.ROLL_DICE, mapOf("category" to currentRound.category))
        }

        if (currentTurn.selectedWord == null) {
            return Pair(GamePhase.SELECT_WORD, mapOf(
                "category" to currentRound.category,
                "diceResult" to currentTurn.diceResult.name
            ))
        }

        return Pair(GamePhase.SELECT_HIT_OR_MISS, mapOf<String, Any>(
            "category" to currentRound.category,
            "diceResult" to currentTurn.diceResult.name,
            "selectedWord" to currentTurn.selectedWord!!
        ))
    }

    fun gameStateForSerialization(clock: Clock): Map<String, Any> {
        val (phase, phaseData) = phaseWithData(clock)
        return mapOf(
            "currentPlayer" to currentPlayer().id,
            "players" to playerListForSerialization(),
            "scores" to scoresForSerialization(),
            "phase" to phase.name,
            "phaseData" to phaseData
        )
    }

    inner class Round(val category: String, val countdownTimerStart: Instant) {
        private var playersWhoRolledTheDice = mutableSetOf<String>()

        fun allPlayersRolledTheDice(): Boolean {
            return playersWhoRolledTheDice.size == countPlayers()
        }

        fun playerRolledTheDice(player: Player) {
            playersWhoRolledTheDice.add(player.id)
        }
    }

    inner class Turn(val selector: Player, val diceResult: DiceResult) {
        private val playersWhoChoseHitOrMiss = mutableSetOf<String>()

        var selectedWord: String? = null

        fun selectWord(selectedWord: String) {
            this.selectedWord = selectedWord
        }

        fun allPlayersChoseHitOrMiss(): Boolean {
            return playersWhoChoseHitOrMiss.size == countPlayers() - 1
        }

        fun result(player: Player, result: TurnResult) {
            playersWhoChoseHitOrMiss.add(player.id)
            when (result) {
                TurnResult.HIT -> {
                    when (diceResult) {
                        DiceResult.HIT -> {
                            player.addPlayerPoints(1)
                            selector.addPlayerPoints(1)
                        }

                        DiceResult.MISS -> {
                            player.addPlayerPoints(3)
                            selector.addPlayerPoints(0)
                        }
                    }
                }

                TurnResult.MISS -> {
                    when (diceResult) {
                        DiceResult.HIT -> {
                            player.addPlayerPoints(0)
                            selector.addPlayerPoints(0)
                        }

                        DiceResult.MISS -> {
                            player.addPlayerPoints(0)
                            selector.addPlayerPoints(1)
                        }
                    }
                }
            }
        }
    }
}