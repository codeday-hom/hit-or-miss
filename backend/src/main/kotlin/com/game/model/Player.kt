package com.game.model

class Player(val name: String) {
    private var userName = name
    private var points = 0
    fun getPlayerPoints():Int {
        return points
    }
    fun addPlayerPoints(amount: Int){
        points += amount
    }

    fun getUserName() = this.userName

}
