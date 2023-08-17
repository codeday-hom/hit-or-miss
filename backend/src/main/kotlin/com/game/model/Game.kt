package com.game.model

data class Game(val gameId: String,
                var hostId: String,
                val users: MutableMap<String, String> = mutableMapOf(),
                // sortedMap will maintain the sorting order provided by the given comparator.
                // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/to-sorted-map.html
                var started: Boolean = false,
                val playerOrders: MutableList<String> = mutableListOf(),
                var currentPlayerIndex: Int = 0)