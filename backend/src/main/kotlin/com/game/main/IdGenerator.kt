package com.game.main

import org.apache.commons.codec.binary.Base32
import java.util.Random

object IdGenerator {
    private val random = Random()
    private val base32 = Base32(true, 'z'.code.toByte())

    fun generateId(): String {
        return base32.encodeAsString(random.nextInt().toString().toByteArray()).lowercase()
    }
}

