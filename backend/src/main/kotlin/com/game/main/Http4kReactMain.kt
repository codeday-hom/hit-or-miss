package com.game.main

import com.game.model.Game
import com.game.repository.GameRepository
import com.game.services.GameService
import org.http4k.core.*
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.format.Jackson
import org.http4k.routing.*
import org.http4k.routing.ResourceLoader.Companion.Directory
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.ws.bind
import org.http4k.routing.websockets
import org.http4k.server.PolyHandler

fun apiHandler(req: Request): Response {
    return Response(OK)
}

fun lobbyHandler(req: Request, wsHandler: GameWebSocket): Response {
    val requestBodyString = req.bodyString()
    println("Request body: $requestBodyString")
    val lobbyRequest = Jackson.asA(requestBodyString, lobbyRequest::class)
    val gameId = lobbyRequest.gameId

    val game = GameRepository.addUserToGame(gameId)

    wsHandler.sendUserJoinedMessages(gameId, game.userIds)

    val responseBody = lobbyResponse(gameId, game.hostId, game.userIds)
    return Response(OK).body(Jackson.asInputStream(responseBody))
}

fun gameServerHandler(assetsPath: String, apiHandler: HttpHandler, wsHandler: GameWebSocket): RoutingHttpHandler {
    return routes(
        "/api/{rest:.*}" bind apiHandler,
        "/new-game" bind POST to { _: Request -> createNewGame()},
        "/game/{rest:.*}" bind POST to { req: Request -> lobbyHandler(req, wsHandler)},
        singlePageApp(Directory(assetsPath))
    )
}

fun createNewGame(): Response {
    val gameId = GameService().createGame()
    val game = Game(gameId, "", mutableListOf())
    GameRepository.createGame(gameId, game)
    return Response(OK).body(gameId).cookie(Cookie("game_host", gameId))
}

fun main() {
    val frontendBuild = "../frontend/build/"
    val wsHandler = GameWebSocket()
    val ws = websockets("/ws/game/{gameId}" bind wsHandler.gameWsHandler())
    val server = PolyHandler(gameServerHandler(frontendBuild, ::apiHandler, wsHandler), ws).asServer(Jetty(8080)).start()
    val localAddress = "http://localhost:" + server.port()
    println("Server started on $localAddress")
}
