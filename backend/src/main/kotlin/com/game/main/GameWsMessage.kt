package com.game.main

import kotlinx.serialization.json.Json


data class GameWsMessage(val type: String, val data: Any) {
}
