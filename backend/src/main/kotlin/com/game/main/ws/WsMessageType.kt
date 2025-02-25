package com.game.main.ws

enum class WsMessageType {
    USER_JOINED,
    GAME_START,
    NEXT_TURN, // The next person should roll the dice
    NEXT_ROUND, // The next person should select a category
    ERROR,
    CATEGORY_SELECTED,
    HEARTBEAT,
    HEARTBEAT_ACK,
    ROLL_DICE,
    ROLL_DICE_RESULT,
    ROLL_DICE_HIT_OR_MISS,
    SELECTED_WORD,
    PLAYER_CHOSE_HIT_OR_MISS, // Player chose Hit or Miss after they know the selected word
    SCORES,
    GAME_OVER,
    USER_DISCONNECTED,
    USER_RECONNECTED,
    GAME_JOINABLE,
}