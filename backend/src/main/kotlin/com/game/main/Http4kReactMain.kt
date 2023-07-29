package com.game.main

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsResponse
import java.util.*
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.Path
import org.http4k.routing.ws.bind
import org.http4k.routing.websockets
import org.http4k.server.PolyHandler
import org.http4k.websocket.WsMessage
import java.util.concurrent.ConcurrentHashMap

fun apiHandler(req: Request): Response {
    return Response(Status.OK)
}
val wsConnections = ConcurrentHashMap<String, MutableList<Websocket>>()

fun lobbyHandler(req: Request): Response {
    val requestBodyString = req.bodyString()
    println("Request body: $requestBodyString")
    val lobbyRequest = Jackson.asA(requestBodyString, lobbyRequest::class)
    val gameId = lobbyRequest.gameId;
    val game = GameRepository.getGame(gameId)!!

    var userId = UUID.randomUUID().toString()
    if (game.hostId.isEmpty()) {
        game.hostId = userId
        game.userIds.add(userId)
    } else {
        game.userIds.add(userId)
    }
    wsConnections[gameId]?.forEach { ws ->
        sendWsMessage(ws, "userJoined", game.userIds)
    }

    GameRepository.createGame(gameId, game)
    val responseBody = lobbyResponse(gameId, game.hostId, game.userIds)
    return Response(Status.OK).body(Jackson.asInputStream(responseBody))
}



fun gameServerHandler(assetsPath: String, apiHandler: HttpHandler): RoutingHttpHandler {

    return routes(
        "/api/{rest:.*}" bind apiHandler,
        "/new-game" bind POST to { _: Request -> createNewGame()},
        "/game/{rest:.*}" bind POST to { req: Request -> lobbyHandler(req)},
        singlePageApp(Directory(assetsPath))
    )
}

fun createNewGame(): Response {
    val gameId = GameService().createGame()
    val game = Game(gameId, "", mutableListOf())
    GameRepository.createGame(gameId, game);
    return Response(OK).body(gameId).cookie(Cookie("game_host", gameId))
}

fun sendWsMessage(ws: Websocket, type: String, data: Any) {
    val message = mapOf("type" to type, "data" to data)
    val mapper = ObjectMapper()
    val messageJson = mapper.writeValueAsString(message)
    println("Sending a message: $message")
    ws.send(WsMessage(messageJson))
}
fun main() {
    val ws = websockets(
        "/ws/game/{gameId}" bind { req: Request ->
            WsResponse { ws: Websocket ->
                val gameId = Path.of("gameId")(req)
                wsConnections.getOrPut(gameId) { mutableListOf() }.add(ws)
//                sendWsMessage(ws, "gameId", "$gameId")
//                ws.send(WsMessage("This is gameId: $gameId"))
                ws.onMessage {
                    println("Received a message: ${it.bodyString()}")
//                    ws.send(WsMessage("$gameId is connecting"))
//                    println("Sent a message: $gameId is connecting")
                    val userIds = GameRepository.getGame(gameId)!!.userIds
                    println("Sending user IDs: $userIds")
//                    sendWsMessage(ws, "userIds", userIds)
                    sendWsMessage(ws, "userJoined", userIds)
                }

                ws.onClose {
                    println("$gameId is closing")
                    wsConnections[gameId]?.remove(ws)
                }
            }
        }
    )

    val frontendBuild = "../frontend/build/"
//    val server = gameServerHandler(frontendBuild, ::apiHandler).asServer(Jetty(8080)).start()
    val server = PolyHandler(gameServerHandler(frontendBuild, ::apiHandler), ws).asServer(Jetty(8080)).start()
    val localAddress = "http://localhost:" + server.port()
    println("Server started on $localAddress")
}
