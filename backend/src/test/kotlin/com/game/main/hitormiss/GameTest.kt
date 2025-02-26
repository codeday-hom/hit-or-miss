package com.game.main.hitormiss

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GameTest {

    @Test
    fun `play a turn with a hit`() {
        val game = Game("testGameId")
        val ian = game.addPlayer("ian")
        val rob = game.addPlayer("rob")
        val tom = game.addPlayer("tom")
        game.startForTest()
        game.startRound()

        game.startTurn(DiceResult.HIT)
        game.turnResult("rob", TurnResult.HIT)
        game.turnResult("tom", TurnResult.MISS)

        assertEquals(1, ian.getPlayerPoints())
        assertEquals(1, rob.getPlayerPoints())
        assertEquals(0, tom.getPlayerPoints())

        game.nextTurn()
        game.startTurn(DiceResult.HIT)
        game.turnResult("ian", TurnResult.MISS)
        game.turnResult("tom", TurnResult.HIT)

        assertEquals(1, ian.getPlayerPoints())
        assertEquals(2, rob.getPlayerPoints())
        assertEquals(1, tom.getPlayerPoints())
    }

    @Test
    fun `play a turn with a miss`() {
        val game = Game("testGameId")
        val ian = game.addPlayer("ian")
        val rob = game.addPlayer("rob")
        val tom = game.addPlayer("tom")
        val timmy = game.addPlayer("timmy")
        game.startForTest()
        game.startRound()

        game.startTurn(DiceResult.MISS)
        game.turnResult("rob", TurnResult.MISS)
        game.turnResult("tom", TurnResult.MISS)
        game.turnResult("timmy", TurnResult.HIT)

        assertEquals(2, ian.getPlayerPoints())
        assertEquals(0, rob.getPlayerPoints())
        assertEquals(0, tom.getPlayerPoints())
        assertEquals(3, timmy.getPlayerPoints())

        game.nextTurn()
        game.startTurn(DiceResult.MISS)
        game.turnResult("ian", TurnResult.HIT)
        game.turnResult("tom", TurnResult.MISS)
        game.turnResult("timmy", TurnResult.HIT)

        assertEquals(5, ian.getPlayerPoints())
        assertEquals(1, rob.getPlayerPoints())
        assertEquals(0, tom.getPlayerPoints())
        assertEquals(6, timmy.getPlayerPoints())
    }
}
