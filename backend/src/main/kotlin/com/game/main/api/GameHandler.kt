package com.game.main.api

import com.game.main.api.GameRepository.getGame
import com.game.main.hitormiss.Game
import com.game.main.ws.GameWebSocket
import com.game.main.ws.WsMessageType
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
        val joinGameRequest = Jackson.asA(req.bodyString(), JoinGameRequest::class)
        val gameId = joinGameRequest.gameId
        val game = getGame(gameId) ?: return Response(NOT_FOUND).body("Game not found: $gameId")
        val playerId = joinGameRequest.playerId
        game.addPlayer(playerId)
        websocket.broadcast(game, WsMessageType.USER_JOINED, game.playerListForSerialization())
        val isStarted = game.isStarted()
        val responseBody = JoinGameResponse(gameId, game.hostPlayerId, game.playerListForSerialization(), isStarted)
        return Response(OK).body(Jackson.asInputStream(responseBody))
            .cookie(Cookie(gameId, playerId, path = "/"))
    }

    private fun startGame(req: Request, websocket: GameWebSocket): Response {
        val gameId = Path.of("gameId")(req)
        val game = getGame(gameId) ?: return Response(NOT_FOUND).body("Game not found: $gameId")
        game.start()
        val currentPlayer = game.currentPlayer().id
        websocket.broadcast(game, WsMessageType.GAME_START, currentPlayer)
        val responseBody = """{ "message": "Game started", "currentPlayer": "$currentPlayer" }"""
        return Response(OK).body(responseBody)
    }
}