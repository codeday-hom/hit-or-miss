package org.http4k.utils

import org.apache.commons.codec.binary.Base32
import java.util.Random

object IdGenerator {
    private val random = Random()

    fun generateId(): String {
        val randomLong = random.nextLong()
        val base32 = Base32()
        return base32.encodeAsString(randomLong.toString().toByteArray())
    }
}

