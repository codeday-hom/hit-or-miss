package com.game.main.hitormiss

import org.slf4j.LoggerFactory
import java.util.*

private val LOGGER = LoggerFactory.getLogger(Game::class.java.simpleName)

data class Game(val gameId: String) {

    lateinit var hostPlayerId: String

    private val players: Players = Players()
    private val playersWhoHavePickedACategory: MutableSet<String> = mutableSetOf()

    private var started: Boolean = false
    private var gameOver: Boolean = false
    private var currentRound: Round? = null
    private var currentTurn: Turn? = null

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

    fun startRound() {
        currentRound = Round()
    }

    fun startTurn(diceResult: DiceResult) {
        currentTurn = Turn(currentPlayer(), diceResult)
        Optional.ofNullable(currentRound)
            .orElseThrow { IllegalStateException("Game not started: $gameId") }
            .playerRolledTheDice(currentPlayer())
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

    fun turnResult(playerId: String, turnResult: TurnResult) {
        val player = players.getPlayer(playerId) ?: throw IllegalArgumentException("No such player with id '$playerId'")
        Optional.ofNullable(currentTurn)
            .orElseThrow { IllegalStateException("Game not started: $gameId") }
            .result(player, turnResult)
    }

    fun scoresForSerialization(): List<Map<String, Any>> = players.scores().map { mapOf("playerId" to it.key, "score" to it.value) }

    fun allPlayersChoseHitOrMiss(): Boolean {
        return Optional.ofNullable(currentTurn)
            .orElseThrow { IllegalStateException("Game not started: $gameId") }
            .allPlayersChoseHitOrMiss()
    }

    fun allPlayersRolledTheDice(): Boolean {
        return Optional.ofNullable(currentRound)
            .orElseThrow { IllegalStateException("Game not started: $gameId") }
            .allPlayersRolledTheDice()
    }

    fun hasPlayer(playerId: String?): Boolean {
        if (playerId == null) {
            return false
        }

        return players.getPlayer(playerId) != null
    }

    inner class Round {
        private var playersWhoRolledTheDice = mutableSetOf<String>()

        fun allPlayersRolledTheDice(): Boolean {
            return playersWhoRolledTheDice.size == countPlayers()
        }

        fun playerRolledTheDice(player: Player) {
            playersWhoRolledTheDice.add(player.id)
        }
    }

    inner class Turn(private val selector: Player, private val diceResult: DiceResult) {
        private val playersWhoChoseHitOrMiss = mutableSetOf<String>()

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