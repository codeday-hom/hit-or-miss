package com.game.main

import java.util.*

data class Game(val gameId: String) {

    lateinit var hostId: String

    private val players: Players = Players()

    private var started: Boolean = false
    private var currentRound: Round? = null
    private var currentTurn: Turn? = null

    fun currentPlayer() = players.currentPlayer()

    fun addPlayer(username: String): Player {
        if (isStarted()) {
            throw IllegalStateException("Game $gameId already started.")
        }
        if (countPlayers() == 0) {
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

    fun countPlayers() = players.count()

    fun playerListForSerialization() = players.playerListForSerialization()

    fun rollDice(): Int {
        return Random().nextInt(6) + 1
    }

    fun startRound() {
        currentRound = Round()
    }

    fun startTurn(username: String, diceResult: DiceResult) {
        if (username != currentPlayer().getUsername()) {
            throw IllegalArgumentException("The current player is '${currentPlayer().getUsername()}' so can't start turn for player '$username'.")
        } else if (players.getPlayer(username) == null) {
            throw IllegalArgumentException("No such player '$username'.")
        }

        currentTurn = Turn(currentPlayer(), diceResult)
        Optional.ofNullable(currentRound)
            .orElseThrow { IllegalStateException("Game not started: $gameId") }
            .playerRolledTheDice(currentPlayer())
    }

    fun nextTurn() {
        players.nextPlayer()
    }

    fun nextRound() {
        players.skipPlayer()
    }

    fun turnResult(username: String, turnResult: TurnResult) {
        val player = players.getPlayer(username) ?: throw IllegalArgumentException("Player doesn't exist: $username")
        Optional.ofNullable(currentTurn)
            .orElseThrow { IllegalStateException("Game not started: $gameId") }
            .result(player, turnResult)
    }

    fun scores(): Map<String, Int> = players.scores()

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

    inner class Round {
        private var playersWhoRolledTheDice = mutableSetOf<String>()

        fun allPlayersRolledTheDice(): Boolean {
            return playersWhoRolledTheDice.size == countPlayers()
        }

        fun playerRolledTheDice(player: Player) {
            playersWhoRolledTheDice.add(player.getUsername())
        }
    }

    inner class Turn(private val selector: Player, private val diceResult: DiceResult) {
        private val playersWhoChoseHitOrMiss = mutableSetOf<String>()

        fun allPlayersChoseHitOrMiss(): Boolean {
            return playersWhoChoseHitOrMiss.size == countPlayers() - 1
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