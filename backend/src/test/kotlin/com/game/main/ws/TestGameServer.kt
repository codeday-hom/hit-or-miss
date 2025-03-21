package com.game.main.ws

import com.game.main.api.GameRepository
import com.game.main.hitormiss.Game
import java.time.InstantSource
import org.http4k.routing.websockets
import org.http4k.routing.ws.bind
import org.http4k.server.Jetty
import org.http4k.server.asServer

class TestGameServer(clock: InstantSource) {

    private val websocket = GameWebSocket(clock)
    private var server = websockets(
        "/ws/game/{gameId}" bind websocket.handler(),
        "/ws/game/{gameId}/{playerId}" bind websocket.handler()
    ).asServer(Jetty(0))

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop()
    }

    fun createLobby(game: Game, vararg players: TestPlayer) {
        GameRepository.registerGame(game)
        players.forEach { it.connect(server, game) }
    }

    fun connectWithError(player: TestPlayer, game: Game) {
        player.connect(server, game, skipConnectionAssertion = true)
    }
}
