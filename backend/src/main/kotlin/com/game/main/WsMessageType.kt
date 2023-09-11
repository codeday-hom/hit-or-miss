package com.game.main

enum class WsMessageType {
    USER_JOINED,
    GAME_START,
    NEXT_PLAYER,
    ERROR,
    CATEGORY_SELECTED,
    CATEGORY_CHOSEN,
    HEARTBEAT,
    HEARTBEAT_ACK,
    ROLL_DICE,
    ROLL_DICE_RESULT,
    HIT_OR_MISS, // Current player select Hit or Miss after rolling the dice
    PLAYER_CHOSE_HIT_OR_MISS // Player chose Hit or Miss after they know the selected word
}