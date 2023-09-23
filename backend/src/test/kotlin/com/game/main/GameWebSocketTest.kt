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
import org.http4k.websocket.WsClient
import org.http4k.websocket.WsHandler
import org.http4k.websocket.WsMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import kotlin.properties.Delegates

class GameWebSocketTest {
    private val websocket = GameWebSocket()
    private val testApp: WsHandler = websockets("/{gameId}" bind websocket.handler())
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
        game = Game(gameId, "aaaa", mutableMapOf("aaaa" to "zuno", "bbbb" to "grace"))
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
        assertFirstReplyEquals(mapOf("type" to WsMessageType.USER_JOINED.name, "data" to mutableMapOf("aaaa" to "zuno", "bbbb" to "grace")))
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
        client.send(WsMessage("Nonsense"))
        assertNthReplyEquals(2, mapOf("type" to WsMessageType.ERROR.name, "data" to "Invalid message"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies with game not found if game disappears`() {
        // Wait for the websocket connection to be open
        assertFirstReplyEquals(mapOf("type" to WsMessageType.USER_JOINED.name, "data" to mutableMapOf("aaaa" to "zuno", "bbbb" to "grace")))
        // Then make the game disappear
        GameRepository.reset()

        send(mapOf("type" to WsMessageType.CATEGORY_SELECTED.name, "data" to "Science"))

        assertFirstReplyEquals(mapOf("type" to WsMessageType.ERROR.name, "data" to "Game not found"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies to next-player message with the next player in the game`() {
        game.start()
        val currentPlayer = game.currentPlayer()
        val nextPlayer = mutableListOf<String>().run {
            addAll(game.users.values)
            remove(currentPlayer)
            first()
        }

        send(mapOf("type" to WsMessageType.NEXT_PLAYER.name, "data" to ""))

        assertNthReplyEquals(2, mapOf("type" to WsMessageType.NEXT_PLAYER.name, "data" to nextPlayer))
    }

    @Test
    @Timeout(value = 4)
    fun `replies to card-selected message with a category chosen response`() {
        game.start()

        send(mapOf("type" to WsMessageType.CATEGORY_SELECTED.name, "data" to "Science"))

        assertNthReplyEquals(2, mapOf("type" to WsMessageType.CATEGORY_CHOSEN.name, "data" to "Science"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies with heartbeat ack message when receives a heartbeat message`() {
        game.start()

        send(mapOf("type" to WsMessageType.HEARTBEAT.name, "data" to ""))

        assertNthReplyEquals(2, mapOf("type" to WsMessageType.HEARTBEAT_ACK.name, "data" to ""))
    }

    @Test
    @Timeout(value = 4)
    fun `replies with dice result when receives a roll dice message`() {
        game.start()
        send(mapOf("type" to WsMessageType.ROLL_DICE.name, "data" to ""))
        assertNthReplyWithinRange(2, WsMessageType.ROLL_DICE_RESULT, 1..6)
    }

    @Test
    @Timeout(value = 4)
    fun `replies with a hit message when receives a hit message`() {
        game.start()
        send(mapOf("type" to WsMessageType.HIT_OR_MISS.name, "data" to "Hit"))
        assertNthReplyEquals(2, mapOf("type" to WsMessageType.HIT_OR_MISS.name, "data" to "Hit"))
    }

    @Test
    @Timeout(value = 4)
    fun `replies with a miss message when receives a miss message`() {
        game.start()
        send(mapOf("type" to WsMessageType.HIT_OR_MISS.name, "data" to "Miss"))
        assertNthReplyEquals(2, mapOf("type" to WsMessageType.HIT_OR_MISS.name, "data" to "Miss"))
    }


    private fun wsClient(gameId: String): WsClient = WebsocketClient.blocking(Uri.of("ws://localhost:$port/$gameId"))

    private fun send(body: Map<String, String>) {
        client.send(WsMessage(jacksonObjectMapper().writeValueAsString(body)))
    }

    private fun assertFirstReplyEquals(expectedMessage: Map<String, Any>) {
        assertNthReplyEquals(1, expectedMessage)
    }

    private fun assertNthReplyEquals(n: Int, expectedMessage: Map<String, Any>) {
        val reply = client.received().take(n).last()
        val expected = WsMessage(jacksonObjectMapper().writeValueAsString(expectedMessage))
        assertEquals(expected, reply)
    }

    private fun assertNthReplyWithinRange(n: Int, expectedType: WsMessageType, range: IntRange) {
        val reply = client.received().take(n).last()
        val expected = jacksonObjectMapper().readValue(reply.bodyString(), Map::class.java)
        val type = expected["type"]
        val dataValue = expected["data"] as Int
        assert(type == expectedType.name) { "Expected type: ${expectedType.name}, but was: $type" }
        assert(dataValue in range) { "Expected data value to be in $range, but was: $dataValue" }
    }


}
