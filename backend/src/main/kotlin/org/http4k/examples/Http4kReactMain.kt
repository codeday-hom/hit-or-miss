package org.http4k.examples

import org.http4k.services.GameService
import org.http4k.repository.GameRepository
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.core.Method
import org.http4k.core.Body
import org.http4k.core.Response
import org.http4k.core.Request
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.OK


fun main() {

    val app = routes(
        "/" bind static(ResourceLoader.Companion.Directory("../frontend/build/")),
        "/new-game" bind POST to { _: Request -> createNewGame() })

    val server = app.asServer(SunHttp(8080)).start()

    println("Server started on " + server.port())
}

fun createNewGame(): Response {
    val gameId = GameService().createGame()
//    To be implemented
//    val hostId = retrieveHostId(request)
    val hostId = "placeholder"
    GameRepository.createGame(gameId, hostId)
    return Response(OK).body(gameId)
}

