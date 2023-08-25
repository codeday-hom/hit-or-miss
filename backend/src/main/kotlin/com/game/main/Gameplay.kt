package com.game.main

enum class DiceResult {
    HIT, MISS
}

enum class TurnResult {
    HIT, MISS
}

class Gameplay(host: Player) {
    private val players: MutableList<Player> = mutableListOf()

    init {
        players.add(host)
    }

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
                            player.addPlayerPoints(1)
                            selector.addPlayerPoints(1)
                        }

                        DiceResult.MISS -> {
                            TODO()
                        }
                    }
                }

                TurnResult.MISS -> {
                    when (diceResult) {
                        DiceResult.HIT -> {}
                        DiceResult.MISS -> {
                            TODO()
                        }
                    }
                }
            }
        }
    }

    fun addPlayer(name: String): Player {
        val player = Player(name)
        players.add(player)
        return player
    }

}