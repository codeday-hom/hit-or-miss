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
    private var diceResult: DiceResult = DiceResult.HIT
) {
    fun currentCategory() = this.chosenCategory

    fun updateCurrentCategory(category: String) {
        this.chosenCategory = category
    }

    fun currentDiceResult() = this.diceResult

    fun updateDiceResult(diceResult: String) {
        println(diceResult)
        if (diceResult == "Hit") {
            this.diceResult = DiceResult.HIT
        } else {
            this.diceResult = DiceResult.MISS
        }
        println("Dice result " + this.diceResult)

    }

    fun currentPlayer() = players.currentPlayer()

    fun nextPlayer() = players.nextPlayer()

    fun addUser(username: String): Player {
        val userId = username
        if (hostId.isEmpty()) {
            hostId = userId
        }
        val newPlayer = players.addPlayer(userId, username)
        return newPlayer
    }

    fun start() {
        started = true
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

    fun startTurn(selector: Player, category: String, diceResult: DiceResult, selectedWord: String): Turn {
        println("${selector.name} chose $category, rolled $diceResult and selected the word '$selectedWord'")
        return Turn(selector, diceResult)
    }

    class Turn(private val selector: Player, private val diceResult: DiceResult) {

        fun result(player: Player, result: TurnResult) {
            println("${player.name} ${if (result == TurnResult.HIT) "had the word" else "didn't have the word"}")
            when (result) {
                TurnResult.HIT -> {
                    when (diceResult) {
                        DiceResult.HIT -> {
                            println(player.getUserName() + " has " + player.getPlayerPoints())
                            player.addPlayerPoints(1)
                            selector.addPlayerPoints(1)
                            println(player.getUserName() + " has " + player.getPlayerPoints())
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