package com.game.main

import com.game.repository.GameRepository
import com.game.services.GameService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.format.Jackson.auto
import org.http4k.routing.ResourceLoader.Companion.Directory
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun apiHandler(req: Request): Response {
    val gameId = GameService().createGame()
    GameRepository.createGame(gameId, "", emptyList<String>());
    return Response(OK).body(gameId).cookie(Cookie("game_id", gameId))
//    return Response(Status.OK).body("Hello World!")
}

@Serializable
data class LobbyData(val gameId: String, val userIds: List<String>)
data class LobbyDataBack(val gameId: String, val hostId: String?, val userIds: List<String>?)

fun lobbyHandler(req: Request): Response {
//    println(req.bodyString())
    val requestBody = req.bodyString()
    val lobbyData = Json.decodeFromString<LobbyData>(requestBody)
    val gameId = lobbyData.gameId
    val userIds = lobbyData.userIds
//    println("GameId: $gameId")
//    println("UserIds: $userIds")
    GameRepository.updateUserIds(gameId, userIds)
//    val json = Json.encodeToString(LobbyDataBack(gameId, GameRepository.getHostId(gameId), GameRepository.getUserIds(gameId) ))

    val bodyLens = Body.auto<LobbyDataBack>().toLens()
    val lobbyDataBack = LobbyDataBack(gameId, GameRepository.getHostId(gameId), GameRepository.getUserIds(gameId))
//    println(lobbyDataBack)

    // Return the response with the JSON data in the body
    return Response(Status.OK).with(bodyLens of lobbyDataBack)
}

fun gameServerHandler(assetsPath: String, lobbyHandler: HttpHandler, apiHandler: HttpHandler): RoutingHttpHandler {
    return routes(
        "/game/{rest:.*}" bind lobbyHandler,
        "/api/{rest:.*}" bind apiHandler,
        singlePageApp(Directory(assetsPath))
    )
}

fun main() {
    val frontendBuild = "../frontend/build/"
    val server = gameServerHandler(frontendBuild, ::lobbyHandler, ::apiHandler).asServer(Jetty(8080)).start()
    val localAddress = "http://localhost:" + server.port()
    println("Server started on $localAddress")
}
