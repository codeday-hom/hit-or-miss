package com.game.main

import com.game.model.DiceResult
import com.game.model.Game
import com.game.model.TurnResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GameTest {

    @Test
    fun `play a turn with a hit`() {
        val game = Game("testGameId")
        val ian = game.addUser("ian")
        val rob = game.addUser("rob")
        val tom = game.addUser("tom")
        game.startForTest()

        game.updateDiceResult(DiceResult.HIT)
        game.turnResult(rob, TurnResult.HIT)
        game.turnResult(tom, TurnResult.MISS)

        assertEquals(1, ian.getPlayerPoints())
        assertEquals(1, rob.getPlayerPoints())
        assertEquals(0, tom.getPlayerPoints())

        game.nextTurn()
        game.updateDiceResult(DiceResult.HIT)
        game.turnResult(ian, TurnResult.MISS)
        game.turnResult(tom, TurnResult.HIT)

        assertEquals(1, ian.getPlayerPoints())
        assertEquals(2, rob.getPlayerPoints())
        assertEquals(1, tom.getPlayerPoints())
    }

    @Test
    fun `play a turn with a miss`() {
        val game = Game("testGameId")
        val ian = game.addUser("ian")
        val rob = game.addUser("rob")
        val tom = game.addUser("tom")
        val timmy = game.addUser("timmy")
        game.startForTest()

        game.updateDiceResult(DiceResult.MISS)
        game.turnResult(rob, TurnResult.MISS)
        game.turnResult(tom, TurnResult.MISS)
        game.turnResult(timmy, TurnResult.HIT)

        assertEquals(2, ian.getPlayerPoints())
        assertEquals(0, rob.getPlayerPoints())
        assertEquals(0, tom.getPlayerPoints())
        assertEquals(3, timmy.getPlayerPoints())

        game.nextTurn()
        game.updateDiceResult(DiceResult.MISS)
        game.turnResult(ian, TurnResult.HIT)
        game.turnResult(tom, TurnResult.MISS)
        game.turnResult(timmy, TurnResult.HIT)

        assertEquals(5, ian.getPlayerPoints())
        assertEquals(1, rob.getPlayerPoints())
        assertEquals(0, tom.getPlayerPoints())
        assertEquals(6, timmy.getPlayerPoints())
    }
}
