import com.game.main.GameWebSocket
import com.game.model.Game
import com.game.repository.GameRepository

import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.routing.websockets
import org.http4k.routing.ws.bind
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.websocket.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameWebSocketTest {
    private val wsHandler = GameWebSocket()
    private val testApp: WsHandler = websockets("/{gameId}" bind wsHandler.gameWsHandler())
    fun client(gameId: String) =  WebsocketClient.blocking(Uri.of("ws://localhost:8000/$gameId"))
    private val server = testApp.asServer(Jetty(8000))

    @BeforeEach
    fun before() {
        server.start()
        val gameId = "testGameId"
        val userIds = mutableMapOf<String, String>()
        userIds["testId1"] = "testUser1"
        userIds["testId2"] = "testUser2"
        val game = Game(gameId, "testId1", userIds)
        GameRepository.createGame(gameId, game)
    }

    @AfterEach
    fun after() {
        server.stop()
    }

    @Test
    fun `receives userJoined message when a new user connects`() {
        val gameId = "testGameId"
        val client = client(gameId)
        client.send(WsMessage("Hello from client!"))
        val messages = client.received().take(1).toList()
        val expected = WsMessage("""{"type":"userJoined","data":{"testId1":"testUser1","testId2":"testUser2"}}""")
        assertEquals(listOf(expected), messages)
    }
}
