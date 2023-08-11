package com.game.main

import com.game.model.Game
import com.game.repository.GameRepository
import io.mockk.mockk
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.format.Jackson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServerTest {

    private val wsHandlerMock = mockk<GameWebSocket>(relaxed = true)
    private val app = gameServerHandler("src/test/resources/test-frontend-assets/", apiHandler(wsHandlerMock))

    @Test
    fun `serves files`() {
        assertEquals(Status.OK, get("/").status)
        assertEquals(Status.OK, get("/game/123/lobby").status)
        assertEquals(Status.OK, get("/anything").status)
    }

    @Test
    fun `lobbyHandler adds a new userId each time`() {
        val gameId = "randomGameId"
        val game = Game(gameId, "", mutableListOf())
        GameRepository.createGame(gameId, game)

        val requestBody = LobbyRequest(gameId)
        val request = Request(Method.POST, "/api/join-game/$gameId")
            .body(Jackson.asInputStream(requestBody))

        lobbyHandler(request, wsHandlerMock)
        assert(GameRepository.getGame(gameId)!!.userIds.size == 1)
        assert(GameRepository.getGame(gameId)!!.hostId == GameRepository.getGame(gameId)!!.userIds.first())

        lobbyHandler(request, wsHandlerMock)
        assert(GameRepository.getGame(gameId)!!.userIds.size == 2)
        assert(GameRepository.getGame(gameId)!!.hostId == GameRepository.getGame(gameId)!!.userIds.first())

        lobbyHandler(request, wsHandlerMock)
        lobbyHandler(request, wsHandlerMock)
        lobbyHandler(request, wsHandlerMock)
        assert(GameRepository.getGame(gameId)!!.userIds.size == 5)
        assert(GameRepository.getGame(gameId)!!.hostId == GameRepository.getGame(gameId)!!.userIds.first())
    }

     @Test
    fun `should return 404 if game not found`() {
        val gameId = "randomGameId"
        val requestBody = LobbyRequest(gameId)
        val request = Request(Method.POST, "/api/join-game/$gameId")
            .body(Jackson.asInputStream(requestBody))
        val response = lobbyHandler(request, wsHandlerMock)
        assertEquals(Status.NOT_FOUND, response.status)
    }

    private fun get(path: String) = app(Request(Method.GET, path))
}
