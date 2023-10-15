package com.game.model

class Player(val name: String) {
    private var userName = name
    private var points = 0

    fun getPlayerPoints(): Int {
        return this.points
    }

    fun addPlayerPoints(amount: Int){ // every time we call the function will somehow reset the points to be 0
        this.points += amount
    }

    fun getUserName() = this.userName

}
