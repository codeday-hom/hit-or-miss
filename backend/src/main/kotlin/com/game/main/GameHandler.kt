package com.game.main

import com.game.main.GameRepository.getGame
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.format.Jackson
import org.http4k.lens.Path
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger(GameHandler::class.java.simpleName)

class GameHandler {

    fun apiHandler(websocket: GameWebSocket): RoutingHttpHandler = routes(
        "/game/new" bind POST to { _: Request -> newGame() },
        "/game/{gameId}/join" bind POST to { req: Request -> joinGame(req, websocket) },
        "/game/{gameId}/start" bind POST to { req: Request -> startGame(req, websocket) }
    )

    private fun newGame(): Response {
        val gameId = IdGenerator.generateId()
        val game = Game(gameId)
        GameRepository.createGame(gameId, game)
        return Response(Status.SEE_OTHER)
            .header("Location", "/lobby/$gameId")
            .cookie(Cookie("${gameId}_host", "1", path = "/"))
    }

    private fun joinGame(req: Request, websocket: GameWebSocket): Response {
        val requestBodyString = req.bodyString()
        LOGGER.info("Request body: $requestBodyString")
        val joinGameRequest = Jackson.asA(requestBodyString, JoinGameRequest::class)
        val gameId = joinGameRequest.gameId
        val game = getGame(gameId) ?: return Response(NOT_FOUND).body("Game not found: $gameId")
        val username = joinGameRequest.username
        game.addPlayer(username)
        websocket.broadcast(game, WsMessageType.USER_JOINED, game.playerListForSerialization())
        val isStarted = game.isStarted()
        val responseBody = JoinGameResponse(gameId, game.hostId, game.playerListForSerialization(), isStarted)
        return Response(OK).body(Jackson.asInputStream(responseBody))
            .cookie(Cookie(gameId, username, path = "/"))
    }

    private fun startGame(req: Request, websocket: GameWebSocket): Response {
        val gameId = Path.of("gameId")(req)
        val game = getGame(gameId) ?: return Response(NOT_FOUND).body("Game not found: $gameId")
        game.start()
        val currentPlayer = game.currentPlayer().name
        websocket.broadcast(game, WsMessageType.GAME_START, currentPlayer)
        val responseBody = """{ "message": "Game started", "currentPlayer": "$currentPlayer" }"""
        return Response(OK).body(responseBody)
    }
}