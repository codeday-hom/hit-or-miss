package org.http4k.examples

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.ResourceLoader.Companion.Directory
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun apiHandler(req: Request): Response {
    return Response(Status.OK)
}

fun gameServerHandler(assetsPath: String, apiHandler: HttpHandler): RoutingHttpHandler {
    return routes(
        "/api/{rest:.*}" bind apiHandler,
        singlePageApp(Directory(assetsPath))
    )
}

fun main() {
    val frontendBuild = "../frontend/build/"
    val server = gameServerHandler(frontendBuild, ::apiHandler).asServer(Jetty(8080)).start()
    val localAddress = "http://localhost:" + server.port()
    println("Server started on $localAddress")
}

