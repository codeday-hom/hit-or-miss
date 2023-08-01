package com.game.main

import com.game.model.Game
import com.game.repository.GameRepository
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

class ServerTest {

    private val wsHandlerMock = mockk<GameWebSocket>(relaxed = true)
    private val app = gameServerHandler("src/test/resources/test-frontend-assets/", apiHandler(wsHandlerMock))

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
        assertEquals(Status.OK, get("/game/123/lobby").status)
        assertEquals(Status.OK, get("/anything").status)
    }

    @Test
    fun `lobbyHandler adds a new userId and username each time`() {
        val gameId = "randomGameId"
        val game = Game(gameId, "", mutableMapOf())
        GameRepository.createGame(gameId, game)

        val username1 = "testUser1"
        val username2 = "testUser2"
        val request1 = Request(Method.POST, "api/game/$gameId/")
            .body(Jackson.asInputStream(LobbyRequest(gameId, username1)))
        val request2 = Request(Method.POST, "api/game/$gameId/")
            .body(Jackson.asInputStream(LobbyRequest(gameId, username2)))
        lobbyHandler(request1, wsHandlerMock)
        assert(GameRepository.getGame(gameId)!!.users.size == 1)
        assert(GameRepository.getGame(gameId)!!.hostId == GameRepository.getGame(gameId)!!.users.keys.first())
        assert(GameRepository.getGame(gameId)!!.users.values.contains(username1))

        lobbyHandler(request2, wsHandlerMock)
        assert(GameRepository.getGame(gameId)!!.users.size == 2)
        assert(GameRepository.getGame(gameId)!!.hostId == GameRepository.getGame(gameId)!!.users.keys.first())
        assert(GameRepository.getGame(gameId)!!.users.values.contains(username2))
    }

    @Test
    fun `should return 404 if game not found`() {
        val gameId = "randomGameId"
        val requestBody = LobbyRequest(gameId, "username")
        val request = Request(Method.POST, "/join-game/$gameId")
            .body(Jackson.asInputStream(requestBody))
        val response = lobbyHandler(request, wsHandlerMock)
        assertEquals(Status.NOT_FOUND, response.status)
    }

    private fun get(path: String) = app(Request(Method.GET, path))
}
