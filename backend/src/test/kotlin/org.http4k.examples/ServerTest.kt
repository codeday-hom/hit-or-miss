package org.http4k.examples

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServerTest {

    private val testApiHandler = { req: Request -> Response(Status.OK).body("Api!") }
    private val app = handler("src/test/resources/test-frontend-assets/", testApiHandler)

    @Test
    fun `responds to api requests`() {
        assertEquals(Response(Status.OK).body("Api!"), app(Request(Method.GET, "/api/foo")))
        assertEquals(Response(Status.OK).body("Api!"), app(Request(Method.GET, "/api/something/else")))
    }

    @Test
    fun `serves files`() {
        assertEquals(Status.OK, app(Request(Method.GET, "/")).status)
        assertEquals(Status.OK, app(Request(Method.GET, "/game/123/lobby")).status)
        assertEquals(Status.OK, app(Request(Method.GET, "/anything")).status)
    }
}
