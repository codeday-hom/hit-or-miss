package org.http4k.examples

import org.http4k.core.*
import org.http4k.routing.*
import org.http4k.routing.ResourceLoader.Companion.Directory
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun apiHandler(req: Request): Response {
    return Response(Status.OK)
}

fun handler(assetsPath: String, apiHandler: HttpHandler): RoutingHttpHandler {
    return routes(
        "/api/{rest:.*}" bind apiHandler,
        singlePageApp(Directory(assetsPath))
    )
}

fun main() {
    val frontendBuild = "../frontend/build/"
    val server = handler(frontendBuild, ::apiHandler).asServer(Jetty(8080)).start()
    val localAddress = "http://localhost:" + server.port()
    println("Server started on $localAddress")
}

