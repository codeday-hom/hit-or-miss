package com.game.main.hitormiss

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class PlayersTest {

    @Test
    fun `can get current player`() {
        val players = playersWithUnshuffledOrder(mutableMapOf("id" to Player("name")))
        assertEquals("name", players.currentPlayer().id)
    }

    @Test
    fun `can get next player`() {
        val players = playersWithUnshuffledOrder(mutableMapOf("id" to Player("name"), "secondId" to Player("secondName")))
        assertEquals("secondName", players.nextPlayer().id)
        assertEquals("secondName", players.currentPlayer().id)
        assertEquals("name", players.nextPlayer().id)
        assertEquals("name", players.currentPlayer().id)
    }

    @Test
    fun `shuffles orders`() {
        val players = Players()
        players.addPlayer("name")
        players.addPlayer("secondName")
        players.addPlayer("thirdName")
        val seededRandom = Random(1235863L)
        players.shufflePlayerOrders(seededRandom)
        assertEquals(listOf("thirdName", "secondName", "name"), players.playersInOrder())
    }

    @Test
    fun `counts players`() {
        assertEquals(0, Players().count())
        assertEquals(1, Players().apply { addPlayer("name") }.count())
        assertEquals(2, Players().apply {
            addPlayer("name")
            addPlayer("name2")
        }.count())
        assertEquals(3, Players().apply {
            addPlayer("name")
            addPlayer("name2")
            addPlayer("name3")
        }.count())
    }

    private fun playersWithUnshuffledOrder(playerMap: MutableMap<String, Player>): Players {
        val players = Players()
        playerMap.forEach {
            players.addPlayer(it.value.id)
        }
        return players.apply { useUnshuffledOrder() }
    }
}