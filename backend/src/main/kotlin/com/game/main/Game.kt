package com.game.main

import java.util.*

enum class DiceResult {
    HIT, MISS
}

enum class TurnResult {
    HIT, MISS
}

data class Game(val gameId: String) {

    lateinit var hostId: String

    private val players: Players = Players()

    private var started: Boolean = false
    private var currentTurn: Turn? = null

    fun updateDiceResult(diceResult: DiceResult) {
        currentTurn = Turn(currentPlayer(), diceResult, countPlayers())
    }

    fun currentPlayer() = players.currentPlayer()

    fun addUser(username: String): Player {
        if (players.count() == 0) {
            hostId = username
        }
        return players.addPlayer(username)
    }

    fun start(shufflePlayerOrders: (Players) -> Unit = { _ -> players.shufflePlayerOrders() }) {
        started = true
        shufflePlayerOrders(players)
    }

    fun startForTest() {
        start { it.useUnshuffledOrder() }
    }

    fun isStarted(): Boolean {
        return started
    }

    fun rollDice(): Int {
        return Random().nextInt(6) + 1
    }

    fun countPlayers() = players.count()

    fun playerListForSerialization() = players.playerListForSerialization()

    fun nextTurn() {
        players.nextPlayer()
    }

    fun getPlayer(userName: String) = players.getPlayer(userName)

    fun turnResult(player: Player, turnResult: TurnResult) {
        Optional.ofNullable(currentTurn)
            .orElseThrow { IllegalStateException("Game not started: $gameId") }
            .result(player, turnResult)
    }

    fun playerPoints(): Map<String, Int> = players.playerPoints()
    fun allPlayersChoseHitOrMiss(): Boolean {
        return Optional.ofNullable(currentTurn)
                .orElseThrow { IllegalStateException("Game not started: $gameId") }
                .allPlayersChoseHitOrMiss()
    }

    class Turn(private val selector: Player, private val diceResult: DiceResult, private val numberOfPlayers: Int) {

        private val playersWhoChoseHitOrMiss = mutableSetOf<String>()

        fun allPlayersChoseHitOrMiss(): Boolean {
            return playersWhoChoseHitOrMiss.size == numberOfPlayers - 1
        }

        fun result(player: Player, result: TurnResult) {
            playersWhoChoseHitOrMiss.add(player.getUsername())
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