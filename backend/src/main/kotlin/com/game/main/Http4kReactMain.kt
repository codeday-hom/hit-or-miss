package com.game.main

import com.game.services.GameService
import com.game.repository.GameRepository
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.core.cookie.*
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.HttpHandler
import org.http4k.routing.ResourceLoader
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK
import java.util.*
import org.http4k.routing.singlePageApp
import org.http4k.routing.ResourceLoader.Companion.Directory
import org.http4k.filter.DebuggingFilters.PrintRequestAndResponse
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.core.then

fun main() {
    val frontendBuild = "../frontend/build/"
    val server = gameServerHandler(frontendBuild, apiHandler()).asServer(Jetty(8080)).start()
    val localAddress = "http://localhost:" + server.port()
    println("Server started on $localAddress")
}


fun apiHandler(): RoutingHttpHandler = routes(
    "/new-game" bind POST to { _: Request -> createNewGame() }
)


fun gameServerHandler(assetsPath: String, apiHandler: RoutingHttpHandler): RoutingHttpHandler {
    return routes(
        "/api" bind apiHandler,
        singlePageApp(Directory(assetsPath))
    )
}

fun createNewGame(): Response {
    val gameId = GameService().createGame()
    return Response(Status.SEE_OTHER)
        .header("Location", "/game/$gameId/lobby").cookie(Cookie("game_host", gameId, path = "/"))
}

