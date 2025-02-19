package com.game.main.hitormiss

class Player(val name: String) {
    private var username = name
    private var points = 0

    fun getPlayerPoints(): Int {
        return this.points
    }

    fun addPlayerPoints(amount: Int) {
        this.points += amount
    }

    fun getUsername() = this.username

}
