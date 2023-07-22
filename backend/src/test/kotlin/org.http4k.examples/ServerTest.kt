package org.http4k.examples

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServerTest {

    private val testApiResponse = Response(Status.OK).body("Api!")
    private val testApiHandler = { _: Request -> testApiResponse }
    private val app = gameServerHandler("src/test/resources/test-frontend-assets/", testApiHandler)

    @Test
    fun `responds to api requests`() {
        assertEquals(testApiResponse, get("/api/foo"))
        assertEquals(testApiResponse, get("/api/something/else"))
    }

    @Test
    fun `serves files`() {
        assertEquals(Status.OK, get("/").status)
        assertEquals(Status.OK, get("/game/123/lobby").status)
        assertEquals(Status.OK, get("/anything").status)
    }

    private fun get(path: String) = app(Request(Method.GET, path))
}
