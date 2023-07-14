package org.http4k.services

import org.http4k.utils.IdGenerator

class GameService {

    fun createGame(): String {
        return IdGenerator.generateId()
    }
}
