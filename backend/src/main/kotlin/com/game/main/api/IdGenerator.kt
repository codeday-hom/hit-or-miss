package com.game.main.api

import org.apache.commons.codec.binary.Base32
import java.util.Random

object IdGenerator {
    private val random = Random()
    private val base32 = Base32(true, 'z'.code.toByte())

    /**
     * The length of generated ids.
     */
    const val LENGTH = 8

    fun generateId(): String {
        return base32.encodeAsString(random.nextInt(1000000, 2000000).toString().toByteArray()).lowercase().substring(0, LENGTH)
    }
}

