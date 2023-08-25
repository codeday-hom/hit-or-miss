package com.game.main

import java.io.ByteArrayInputStream
import org.junit.jupiter.api.Test

class GameTest {

    @Test
    fun `play game`() {
//        val game = Gameplay()
        // Set up a game with Ian as the host, and two other players, Rob and Tom.
        val ian = Players("ian", 0)
        val game = Gameplay(ian) // Ian is the host of the game
        val rob = game.addPlayer("rob")  // Rob and Tom are other players of the game
        val tom = game.addPlayer("tom")

// Play the first turn of the round
        val ianTurn = game.startTurn(ian, "Ocean Animals") // Ian's turn is first and randomly selects a category
        //ianTurn.timer() // Do not actually wait for 30 seconds here. This method should do nothing for now.
        ianTurn.selectWord(ian, DiceResult.HIT, "Shark") // Ian rolled the dice and shared a word
        ianTurn.result(rob, TurnResult.HIT) // Rob had Shark
        ianTurn.result(tom, TurnResult.MISS) // Tom didn't have Shark

        ian.addPlayerPoints(1)// Ian gets a point because Rob had his word
        rob.addPlayerPoints(1) // Rob gets a point for having Ian's word
        tom.addPlayerPoints(0) // Tom get no points because he didn't have Ian's word

// Play the second turn of the round
        //val robTurn = game.startTurn(rob)
// Etc.
/*        val mockInput = "my choice"
        System.setIn(ByteArrayInputStream(mockInput.toByteArray()))
        println(game.hitOrMiss())
        println(game.categories())
        game.timer()*/
    }
}
