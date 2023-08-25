package com.game.main

enum class DiceResult {
    HIT, MISS
}
enum class TurnResult {
    HIT, MISS
}
class Gameplay(val host: Players) {
    val players: MutableList<Players> = mutableListOf()

    init {
        players.add(host)
    }

    fun startTurn(selector: Players, category: String ): Turn {
        println("${selector.name} chose $category")
        return Turn(selector)
    }
    class Turn(val selector: Players) {

        fun selectWord(selector: Players, dice: DiceResult, wordChosen: String) {
            println("${selector.name} rolled the dice and got $dice. He chose the word $wordChosen")
        }

        fun result(player: Players, result: TurnResult) {
            println("${player.name} had ${if (result == TurnResult.HIT) "the word" else "no word"} for the category")
  /*          if (result == TurnResult.HIT) {
                player.addPoints()
            }*/
        }
    }

    fun diceRoll(): Int {
        return (1..2).random()
    }

    fun hitOrMiss(): String? {
        val result = when (diceRoll()) {
            1 -> "hit"
            2 -> "miss"
            /*else -> {
                println("Do you want hit or miss?")
                val choice = readLine() ?: ""
                println("You chose: $choice")
                choice
            }*/
            else -> null
        }
        return result
    }

    fun categories(): String {
        val topics = listOf<String>(
            "Candy brands",
            "Ocean animals",
            "Musical instruments",
            "Things in outer space",
            "Items used by a magician",
            "Famous superheroes"
        )
        return topics.random()
    }

    fun timer() {
        val seconds = 30
        val millis = seconds * 1000L
        println("Timer started for $seconds seconds.")
        for (i in seconds downTo 1) {
            println("Time remaining: $i seconds")
            Thread.sleep(1000) // Wait for 1 second
        }
        println("$seconds seconds ended.")
    }
    fun addPlayer(name: String): Players {
        val player = Players(name, 0)
        players.add(player)
        return player
    }
}