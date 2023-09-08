package com.game.main

import com.game.model.Players
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class PlayersTest {

    @Test
    fun `can get current player`() {
        val players = playersWithUnshuffledOrder(mutableMapOf("id" to "name"))
        assertEquals("name", players.currentPlayer())
    }

    @Test
    fun `can get next player`() {
        val players = playersWithUnshuffledOrder(mutableMapOf("id" to "name", "secondId" to "secondName"))
        assertEquals("secondName", players.nextPlayer())
        assertEquals("secondName", players.currentPlayer())
        assertEquals("name", players.nextPlayer())
        assertEquals("name", players.currentPlayer())
    }

    @Test
    fun `updates username`() {
        val players = playersWithUnshuffledOrder(mutableMapOf("id" to "name", "secondId" to "secondName"))
        players.addPlayer("id", "newName")
        assertEquals("newName", players.currentPlayer())
    }

    @Test
    fun `shuffles orders`() {
        val players = Players(mutableMapOf("id" to "name", "secondId" to "secondName", "thirdId" to "thirdName"))
        val seededRandom = Random(1235863L)
        players.shufflePlayerOrders(seededRandom)
        assertEquals(listOf("thirdName", "secondName", "name"), players.playersInOrder())
    }

    @Test
    fun `counts players`() {
        assertEquals(0, Players(mutableMapOf()).count())
        assertEquals(1, Players(mutableMapOf("id" to "name")).count())
        assertEquals(2, Players(mutableMapOf("id" to "name", "secondId" to "secondName")).count())
        assertEquals(3, Players(mutableMapOf("id" to "name", "secondId" to "secondName", "thirdId" to "thirdName")).count())
    }

    private fun playersWithUnshuffledOrder(playerMap: MutableMap<String, String>) = Players(playerMap).apply { useUnshuffledOrder() }
}