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
import org.http4k.routing.*
import org.http4k.routing.ResourceLoader.Companion.Directory
import org.http4k.routing.ws.bind
import org.http4k.server.Jetty
import org.http4k.server.PolyHandler
import org.http4k.server.asServer
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException

private val LOGGER = LoggerFactory.getLogger("Main")

fun main() {
    val frontendBuild = System.getProperty("react.build.dir")
    if (frontendBuild.isNullOrBlank()) {
        throw IllegalArgumentException("Illegal frontend assets path given: '$frontendBuild'")
    } else if (!File(frontendBuild).exists()) {
        throw FileNotFoundException("Frontend assets not found at path '$frontendBuild'")
    }
    LOGGER.info("Serving frontend assets from $frontendBuild")

    val websocket = GameWebSocket()
    val ws = websockets("/ws/game/{gameId}" bind websocket.handler())
    val server = PolyHandler(gameServerHandler(frontendBuild, apiHandler(websocket)), ws).asServer(Jetty(8080)).start()
    val localAddress = "http://localhost:" + server.port()
    LOGGER.info("Server started on $localAddress")
}

fun gameServerHandler(assetsPath: String, apiHandler: RoutingHttpHandler): RoutingHttpHandler {
    return routes(
        "/api" bind apiHandler,
        singlePageApp(Directory(assetsPath))
    )
}

fun apiHandler(websocket: GameWebSocket): RoutingHttpHandler = routes(
    "/new-game" bind POST to { _: Request -> createNewGame() },
    "/join-game/{gameId}" bind POST to { req: Request -> joinGameHandler(req, websocket) },
    "/start-game/{gameId}" bind POST to { req: Request -> startGameHandler(req, websocket) },
)

fun createNewGame(): Response {
    val gameId = IdGenerator.generateId()
    val game = Game(gameId)
    GameRepository.createGame(gameId, game)
    return Response(Status.SEE_OTHER)
        .header("Location", "/lobby/$gameId")
        .cookie(Cookie("${gameId}_host", "1", path = "/"))
}

fun startGameHandler(req: Request, websocket: GameWebSocket): Response {
    val gameId = Path.of("gameId")(req)
    val game = getGame(gameId) ?: return Response(NOT_FOUND).body("Game not found: $gameId")
    game.start()
    val currentPlayer = game.currentPlayer().name
    websocket.broadcast(game, WsMessageType.GAME_START, currentPlayer)
    val responseBody = """{ "message": "Game started", "currentPlayer": "$currentPlayer" }"""
    return Response(OK).body(responseBody)
}

fun joinGameHandler(req: Request, websocket: GameWebSocket): Response {
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
