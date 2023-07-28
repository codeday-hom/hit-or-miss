package com.game.main

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class GameTest {

    @Test
    fun `play a turn with a hit`() {
        // Set up a game with Ian as the host, and two other players, Rob and Tom.
        val ian = Player("ian")
        val game = Gameplay(ian) // Ian is the host of the game
        val rob = game.addPlayer("rob")  // Rob and Tom are other players of the game
        val tom = game.addPlayer("tom")

        // Play the first turn of the round
        // Ian's turn is first and randomly selects a category
        // Ian rolled the dice and shared a word
        val ianTurn = game.startTurn(ian, "Ocean Animals", DiceResult.HIT, "Shark")
        ianTurn.result(rob, TurnResult.HIT) // Rob had Shark
        ianTurn.result(tom, TurnResult.MISS) // Tom didn't have Shark

        assertEquals(1, ian.getPlayerPoints()) // Ian gets a point because Rob had his word
        assertEquals(1, rob.getPlayerPoints()) // Rob gets a point for having Ian's word
        assertEquals(0, tom.getPlayerPoints()) // Tom get no points because he didn't have Ian's word
    }

    @Test
    fun `play a turn with a miss`() {
        val ian = Player("ian")
        val game = Gameplay(ian) // Ian is the host of the game
        val rob = game.addPlayer("rob")  // Rob and Tom are other players of the game
        val tom = game.addPlayer("tom")
        val timmy = game.addPlayer("timmy")

        // Play the first turn of the round
        // Ian's turn is first and randomly selects a category
        // Ian rolled the dice and shared a word
        val ianTurn = game.startTurn(ian, "Ocean Animals", DiceResult.MISS, "Shark")
        ianTurn.result(rob, TurnResult.MISS) // Rob did not have Shark
        ianTurn.result(tom, TurnResult.MISS) // Tom did not have Shark
        ianTurn.result(timmy, TurnResult.HIT)//Timmy did have Shark


        assertEquals(2, ian.getPlayerPoints()) // Ian gets a point because Rob had his word
        assertEquals(3, rob.getPlayerPoints()) // Rob gets a point for having Ian's word
        assertEquals(3, tom.getPlayerPoints()) // Tom get no points because he didn't have Ian's word
        assertEquals(0, timmy.getPlayerPoints())
    }
}
