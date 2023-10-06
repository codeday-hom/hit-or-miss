package com.game.main

import com.game.model.Player
import com.game.model.Players
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class PlayersTest {

    @Test
    fun `can get current player`() {
        val players = playersWithUnshuffledOrder(mutableMapOf("id" to Player("name")))
        assertEquals("name", players.currentPlayer().getUserName())
    }

    @Test
    fun `can get next player`() {
        val players = playersWithUnshuffledOrder(mutableMapOf("id" to Player("name"), "secondId" to Player("secondName")))
        assertEquals("secondName", players.nextPlayer().getUserName())
        assertEquals("secondName", players.currentPlayer().getUserName())
        assertEquals("name", players.nextPlayer().getUserName())
        assertEquals("name", players.currentPlayer().getUserName())
    }

    @Test
    fun `updates username`() {
        val players = playersWithUnshuffledOrder(mutableMapOf("id" to Player("name"), "secondId" to Player("secondName")))
        players.addPlayer("id", "newName")
        assertEquals("newName", players.currentPlayer().getUserName())
    }

    @Test
    fun `shuffles orders`() {
        val players = Players(mutableMapOf("id" to Player("name"), "secondId" to Player("secondName"), "thirdId" to Player("thirdName")))
        val seededRandom = Random(1235863L)
        players.shufflePlayerOrders(seededRandom)
        assertEquals(listOf("thirdName", "secondName", "name"), players.playersInOrder())
    }

    @Test
    fun `counts players`() {
        assertEquals(0, Players(mutableMapOf()).count())
        assertEquals(1, Players(mutableMapOf("id" to Player("name"))).count())
        assertEquals(2, Players(mutableMapOf("id" to Player("name"), "secondId" to Player("secondName"))).count())
        assertEquals(3, Players(mutableMapOf("id" to Player("name"), "secondId" to Player("secondName"), "thirdId" to Player("thirdName"))).count())
    }

    private fun playersWithUnshuffledOrder(playerMap: MutableMap<String, Player>) = Players(playerMap).apply { useUnshuffledOrder() }
}