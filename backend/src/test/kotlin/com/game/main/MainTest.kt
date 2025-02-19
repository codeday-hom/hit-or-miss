package com.game.main

import com.game.main.api.GameHandler
import com.game.main.api.GameRepository
import com.game.main.api.JoinGameRequest
import com.game.main.hitormiss.Game
import com.game.main.ws.GameWebSocket
import io.mockk.clearMocks
import io.mockk.mockk
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.format.Jackson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.InputStream

class MainTest {

    private val wsHandlerMock = mockk<GameWebSocket>(relaxed = true)
    private val gameHandler = GameHandler()
    private val app = gameServerHandler("src/test/resources/test-frontend-assets/", gameHandler.apiHandler(wsHandlerMock))

    @BeforeEach
    fun setup() {
        GameRepository.reset()
    }

    @AfterEach
    fun teardown() {
        clearMocks(wsHandlerMock)
    }

    @Test
    fun `serves files`() {
        assertEquals(Status.OK, get("/").status)
        assertEquals(Status.OK, get("/lobby/123").status)
        assertEquals(Status.OK, get("/game/456").status)
        assertEquals(Status.OK, get("/anything").status)
    }

    @Test
    fun `Game is updated whenever a new player joins`() {
        val gameId = "randomGameId"
        GameRepository.createGame(gameId, Game(gameId))
        val game = GameRepository.getGame(gameId)!!

        val username1 = "testUser1"
        post("api/game/$gameId/join", Jackson.asInputStream(JoinGameRequest(gameId, username1)))
        val players1 = game.playerListForSerialization()
        assert(players1.size == 1)
        assert(game.countPlayers() == 1)
        assert(game.hostId == username1)
        assert(players1.contains(username1))

        val username2 = "testUser2"
        post("api/game/$gameId/join", Jackson.asInputStream(JoinGameRequest(gameId, username2)))
        val players2 = game.playerListForSerialization()
        assert(players2.size == 2)
        assert(game.countPlayers() == 2)
        assert(game.hostId == username1)
        assert(players2.contains(username1))
        assert(players2.contains(username2))
    }

    @Test
    fun `should return 404 if game not found`() {
        val gameId = "notFoundGameId"
        val response = post("api/game/$gameId/join", Jackson.asInputStream(JoinGameRequest(gameId, "username")))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    private fun get(path: String) = app(Request(Method.GET, path))
    private fun post(path: String, body: InputStream) = app(Request(Method.POST, path).body(body))
}
