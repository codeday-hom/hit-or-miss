package com.game.main

import java.io.ByteArrayInputStream
import org.junit.jupiter.api.Test

class GameTest {

    @Test
    fun `play game`() {
        val game = Gameplay()
        val mockInput = "my choice"
        System.setIn(ByteArrayInputStream(mockInput.toByteArray()))
        println(game.hitOrMiss())
        println(game.categories())
        game.timer()
    }
}
