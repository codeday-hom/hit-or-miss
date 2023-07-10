package com.example

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HelloWorldTest {

    @Test
    fun `App says hello!`() {
        assertEquals(Response(OK).body("hello world!"), app(Request(GET, "/")))
    }

}
