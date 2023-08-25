package com.game.main

class Player(val name: String) {

    private var points = 0
    fun getPlayerPoints():Int {
        return points
    }
    fun addPlayerPoints(amount: Int){
        points += amount
    }

}
