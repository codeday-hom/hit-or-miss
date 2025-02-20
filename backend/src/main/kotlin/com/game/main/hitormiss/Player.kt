package com.game.main.hitormiss

class Player(val id: String) {
    private var points = 0

    fun getPlayerPoints(): Int {
        return this.points
    }

    fun addPlayerPoints(amount: Int) {
        this.points += amount
    }
}
