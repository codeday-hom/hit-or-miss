package com.game.main

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IdGeneratorTest {

    @Test
    fun `generated ids have a fixed length`() {
        assertEquals(IdGenerator.LENGTH, IdGenerator.generateId().length)
    }
}
