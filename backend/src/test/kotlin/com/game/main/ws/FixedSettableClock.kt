package com.game.main.ws

import java.time.Duration
import java.time.Instant
import java.time.InstantSource

class FixedSettableClock(private var instant: Instant) : InstantSource {
    override fun instant(): Instant {
        return instant
    }

    fun windForward(duration: Duration) {
        instant = instant.plus(duration)
    }
}