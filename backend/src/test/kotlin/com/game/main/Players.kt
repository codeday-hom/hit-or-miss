package org.http4k.main

class Players(val name: String, var points: Int) {
    fun getPlayerPoints():Int {
        return points
    }
    fun addPlayerPoints(amount: Int){
        points += amount
    }

}
