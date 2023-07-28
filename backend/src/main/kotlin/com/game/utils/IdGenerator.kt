package com.game.utils

import org.apache.commons.codec.binary.Base32
import java.util.Random

object IdGenerator {
    private val random = Random()
    private val base32 = Base32()

    fun generateId(): String {
        val randomLong = random.nextLong()
        return base32.encodeAsString(randomLong.toString().toByteArray())
    }
}

