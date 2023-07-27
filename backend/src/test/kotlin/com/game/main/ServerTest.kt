package com.game.main

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServerTest {

    private val testApiResponse = Response(Status.OK).body("Api!")
    private val testLobbyHandler = { _: Request -> Response(Status.OK) }
    private val testApiHandler = { _: Request -> testApiResponse }
    private val app = gameServerHandler("src/test/resources/test-frontend-assets/", testLobbyHandler, testApiHandler)

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

    @Test
    fun `json requests and responses`() {
        data class MyRequest(val x: String, val l: List<String>, val i: Int)
        data class MyResponse(val foo: String, val bar: List<String>, val baz: Int)
        val handler = { request: Request ->
            val requestBodyString = request.bodyString()
            println("Request body: $requestBodyString")
            val myRequest = Jackson.asA(requestBodyString, MyRequest::class)
            val responseBody = MyResponse(myRequest.x.uppercase(), myRequest.l.reversed(), myRequest.i + 100)
            Response(Status.OK).body(Jackson.asInputStream(responseBody))
        }

        val response = handler(
            Request(Method.GET, "/whatever").body(
                """{"x": "something", "l": ["a", "b", "c"], "i": 5 }"""
            )
        )

        val responseBodyString = response.bodyString()
        println("Response body: $responseBodyString")
        val myResponse = Jackson.asA(responseBodyString, MyResponse::class)

        assertEquals("SOMETHING", myResponse.foo)
        assertEquals(listOf("c", "b", "a"), myResponse.bar)
        assertEquals(105, myResponse.baz)
    }

    private fun get(path: String) = app(Request(Method.GET, path))
}
