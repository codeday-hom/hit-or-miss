package com.game.main
import java.util.Random


enum class DiceResult {
    HIT, MISS
}

enum class TurnResult {
    HIT, MISS
}

class Gameplay(host: Player) {
    private val players: MutableList<Player> = mutableListOf()
    private val random = Random()
    private var currentPlayerIndex = 0

    fun getPlayer(index: Int): Player{
        return players[index]
    }

    init {
        players.add(host)
    }

    fun mixOrder(){
        players.shuffle(random)
    }

    fun nextTurn(): Player{
        val firstPlayer = players[currentPlayerIndex]
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        return firstPlayer

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

    fun addPlayer(name: String): Player {
        val player = Player(name)
        players.add(player)
        return player
    }

}