package com.game.main

import com.game.main.api.GameHandler
import com.game.main.ws.GameWebSocket
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
    val gameHandler = GameHandler()
    val server = PolyHandler(
        gameServerHandler(frontendBuild, gameHandler.apiHandler(websocket)),
        websockets("/ws/game/{gameId}" bind websocket.handler())
    ).asServer(Jetty(8080)).start()
    val localAddress = "http://localhost:" + server.port()
    LOGGER.info("Server started on $localAddress")
}

fun gameServerHandler(assetsPath: String, apiHandler: RoutingHttpHandler): RoutingHttpHandler {
    return routes(
        "/api" bind apiHandler,
        singlePageApp(Directory(assetsPath))
    )
}
