package com.game.main

class Gameplay {
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
}