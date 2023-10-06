package com.game.main

import com.game.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GameTest {

    @Test
    fun `play a turn with a hit`() {
        // Set up a game with Ian as the host, and two other players, Rob and Tom.
        val game = Game("testGameId", "ian", false, Players(mutableMapOf("aaaa" to Player("ian"))))
        val ian = game.userMapForSerialization().values.first()
        val rob = game.addUser("rob")  // Rob and Tom are other players of the game
        val tom = game.addUser("tom")
        println(game.userMapForSerialization().count())
        game.startForTest()


//        Mix who the first selector
//        game.mixOrder()

        // Play the first turn of the round
        // Ian's turn is first and randomly selects a category
        // Ian rolled the dice and shared a word
        val firstTurn = game.startTurn(game.currentPlayer(), "Ocean Animals", DiceResult.HIT)
        firstTurn.result(rob, TurnResult.HIT) // Rob had Shark
        firstTurn.result(tom, TurnResult.MISS) // Tom didn't have Shark

        assertEquals(1, ian.getPlayerPoints()) // Ian gets a point because Rob had his word
        assertEquals(1, rob.getPlayerPoints()) // Rob gets a point for having Ian's word
        assertEquals(0, tom.getPlayerPoints()) // Tom get no points because he didn't have Ian's word

        val secondTurn = game.startTurn(game.nextPlayer(), "Fruits", DiceResult.HIT)

        secondTurn.result(tom, TurnResult.HIT) // Tom had Apple

        secondTurn.result(ian, TurnResult.MISS) // ian didn't have Apple

        assertEquals(1, ian.getPlayerPoints())
        assertEquals(2, rob.getPlayerPoints())
        assertEquals(1, tom.getPlayerPoints())
    }

    @Test
    fun `play a turn with a miss`() {
        val game = Game("testGameId", "ian", false, Players(mutableMapOf("aaaa" to Player("ian")))) // Ian is the host of the game
        val ian = game.userMapForSerialization().values.first()
        val rob = game.addUser("rob")  // Rob and Tom are other players of the game
        val tom = game.addUser("tom")
        val timmy = game.addUser("timmy")
        game.startForTest()

        // Play the first turn of the round
        // Ian's turn is first and randomly selects a category
        // Ian rolled the dice and shared a word
        val ianTurn = game.startTurn(game.currentPlayer(), "Ocean Animals", DiceResult.MISS)
        ianTurn.result(rob, TurnResult.MISS) // Rob did not have Shark
        ianTurn.result(tom, TurnResult.MISS) // Tom did not have Shark
        ianTurn.result(timmy, TurnResult.HIT)//Timmy did have Shark


        assertEquals(2, ian.getPlayerPoints()) // Ian gets a point because Rob had his word
        assertEquals(0, rob.getPlayerPoints()) // Rob gets a point for having Ian's word
        assertEquals(0, tom.getPlayerPoints()) // Tom get no points because he didn't have Ian's word
        assertEquals(3, timmy.getPlayerPoints())

        val Turn = game.startTurn(game.nextPlayer(), "Veggies", DiceResult.MISS)
        Turn.result(tom, TurnResult.HIT)
        Turn.result(timmy, TurnResult.MISS)
        Turn.result(ian, TurnResult.HIT)


        assertEquals(5, ian.getPlayerPoints())
        assertEquals(1, rob.getPlayerPoints())
        assertEquals(3, tom.getPlayerPoints())
        assertEquals(3, timmy.getPlayerPoints())
    }
}
