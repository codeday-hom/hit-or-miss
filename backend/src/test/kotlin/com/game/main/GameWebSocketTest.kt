import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.game.main.GameWebSocket
import com.game.main.WsMessageType
import com.game.model.Game
import com.game.repository.GameRepository

import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.routing.websockets
import org.http4k.routing.ws.bind
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.websocket.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.properties.Delegates

class GameWebSocketTest {
    private val wsHandler = GameWebSocket()
    private val testApp: WsHandler = websockets("/{gameId}" bind wsHandler.gameWsHandler())
    private lateinit var server: Http4kServer
    private var port by Delegates.notNull<Int>()

    @BeforeEach
    fun before() {
        server = testApp.asServer(Jetty(0))
        server.start()
        port = server.port()
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
        val client = WebsocketClient.blocking(Uri.of("ws://localhost:$port/$gameId"))
        client.send(WsMessage("Hello from client!"))
        val messages = client.received().take(1).toList()
        val expectedData = mapOf("testId1" to "testUser1", "testId2" to "testUser2")
        val expectedMessage = mapOf("type" to WsMessageType.USER_JOINED.name, "data" to expectedData)
        val expected = WsMessage(jacksonObjectMapper().writeValueAsString(expectedMessage))
        assertEquals(listOf(expected), messages)
    }

    @Test
    fun `receives error message when the game has already started`() {
        val gameId = "testGameId"

        GameRepository.getGame(gameId)?.start()

        val client = WebsocketClient.blocking(Uri.of("ws://localhost:$port/$gameId"))
        client.send(WsMessage("Hello from client!"))
        val messages = client.received().take(1).toList()
        val expectedMessage = mapOf("type" to WsMessageType.ERROR.name, "data" to "Game already started")
        val expected = WsMessage(jacksonObjectMapper().writeValueAsString(expectedMessage))
        assertEquals(listOf(expected), messages)
    }

    @Test
    fun `receives error message when the game is not found`() {
        val gameId = "invalidGameId"

        val client = WebsocketClient.blocking(Uri.of("ws://localhost:$port/$gameId"))
        client.send(WsMessage("Hello from client!"))
        val messages = client.received().take(1).toList()
        val expectedMessage = mapOf("type" to WsMessageType.ERROR.name, "data" to "Game not found")
        val expected = WsMessage(jacksonObjectMapper().writeValueAsString(expectedMessage))
        assertEquals(listOf(expected), messages)
    }
}
