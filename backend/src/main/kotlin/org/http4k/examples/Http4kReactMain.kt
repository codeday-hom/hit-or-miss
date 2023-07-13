package org.http4k.examples

import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.SunHttp
import org.http4k.server.asServer


fun main() {
    val app = routes(
        "/" bind static(ResourceLoader.Companion.Directory("../frontend/build/"))
    )

    val server = app.asServer(SunHttp(8080)).start()

    println("Server started on http://localhost:" + server.port())
}

