package com.game.services

import com.game.utils.IdGenerator

class GameService {

    fun createGame(): String {
        return IdGenerator.generateId()
    }
}
