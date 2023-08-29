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
import org.junit.jupiter.api.Timeout
import kotlin.properties.Delegates

class GameWebSocketTest {

    private val wsHandler = GameWebSocket()
    private val testApp: WsHandler = websockets("/{gameId}" bind wsHandler.gameWsHandler())
    private lateinit var server: Http4kServer
    private var port by Delegates.notNull<Int>()

    private lateinit var game: Game
    private lateinit var client: WsClient

    @BeforeEach
    fun before() {
        server = testApp.asServer(Jetty(0))
        server.start()
        port = server.port()
        val gameId = "testGameId"
        game = Game(gameId, "testId1", mutableMapOf("testId1" to "testUser1", "testId2" to "testUser2"))
        GameRepository.createGame(gameId, game)
        client = wsClient(gameId)
    }

    @AfterEach
    fun after() {
        server.stop()
    }

    @Test
    @Timeout(value = 4)
    fun `replies to user-joined message with the current list of players`() {
        client.send(WsMessage("Hello from client!"))

        assertFirstReplyEquals(mapOf("type" to WsMessageType.USER_JOINED.name, "data" to mapOf("testId1" to "testUser1", "testId2" to "testUser2")))
    }

    @Test
    @Timeout(value = 4)
    fun `replies with an error when the game has already started`() {
        game.start()
        client = wsClient(game.gameId)
        assertFirstReplyEquals(mapOf("type" to WsMessageType.ERROR.name, "data" to "Game already started"))
    }

    @Test
    fun `replies with an error when the game is not found`() {
        client = wsClient("non-existing-game")
        assertFirstReplyEquals(mapOf("type" to WsMessageType.ERROR.name, "data" to "Game not found"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies with an error to messages with the wrong format`() {
        client.send(WsMessage("Lacks a type"))
        assertNthReplyEquals(2, mapOf("type" to WsMessageType.ERROR.name, "data" to "Invalid message"))
    }

    //@Test
    //@Timeout(value = 4)
    fun `replies to card-selected message with a category chosen response`() {
        game.start()

        val sentMessage = mapOf("type" to WsMessageType.CATEGORY_SELECTED.name, "data" to "Science")
        client.send(WsMessage(jacksonObjectMapper().writeValueAsString(sentMessage)))

        assertNthReplyEquals(2, mapOf("type" to WsMessageType.CATEGORY_CHOSEN.name, "data" to "Science"))
    }

    private fun wsClient(gameId: String): WsClient = WebsocketClient.blocking(Uri.of("ws://localhost:$port/$gameId"))

    private fun assertFirstReplyEquals(expectedMessage: Map<String, Any>) {
        assertNthReplyEquals(1, expectedMessage)
    }

    private fun assertNthReplyEquals(n: Int, expectedMessage: Map<String, Any>) {
        val reply = client.received().take(n).last()
        val expected = WsMessage(jacksonObjectMapper().writeValueAsString(expectedMessage))
        assertEquals(expected, reply)
    }
}
