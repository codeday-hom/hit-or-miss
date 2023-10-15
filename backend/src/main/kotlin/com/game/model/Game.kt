package com.game.model

import java.util.*

enum class DiceResult {
    HIT, MISS
}

enum class TurnResult {
    HIT, MISS
}

data class Game(
    val gameId: String,
    var hostId: String,
    private var started: Boolean = false,
    private val players: Players = Players(),
    private var chosenCategory: String = "",
    private var diceResult: DiceResult = DiceResult.HIT,
    private var currentTurn: Turn? = null
) {
    fun currentCategory() = this.chosenCategory

    fun updateCurrentCategory(category: String) {
        this.chosenCategory = category
    }

    fun currentDiceResult() = this.diceResult

    fun updateDiceResult(diceResult: String) {
        if (diceResult == "Hit") {
            this.diceResult = DiceResult.HIT
        } else {
            this.diceResult = DiceResult.MISS
        }

    }

    fun currentPlayer() = players.currentPlayer()

    fun nextPlayer() = players.nextPlayer()

    fun addUser(username: String): Player {
        if (hostId.isEmpty()) {
            hostId = username
        }
        return players.addPlayer(username, username)
    }

    fun start() {
        started = true
        this.currentTurn = startTurn(
            currentPlayer(),
            currentDiceResult()
        )
        players.shufflePlayerOrders()
    }

    fun isStarted(): Boolean {
        return started
    }

    fun rollDice(): Int {
        return Random().nextInt(6) + 1
    }

    fun countPlayers() = players.count()


    fun userMapForSerialization() = players.userMapForSerialization()

    fun userNameMapForSerialization() = players.userNameMapForSerialization()

    fun startTurn(selector: Player, diceResult: DiceResult): Turn {
        return Turn(selector, diceResult)
    }

    fun getCurrentTurn(): Turn? {return this.currentTurn; }

    fun updateCurrentTurn(currentTurn: Turn) {this.currentTurn = currentTurn; }


    class Turn(private val selector: Player, private val diceResult: DiceResult) {

        fun result(player: Player, result: TurnResult) {
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

    fun startForTest() {
        started = true
        players.useUnshuffledOrder()
    }

    fun getPlayer(userName: String) = players.getPlayer(userName)
}