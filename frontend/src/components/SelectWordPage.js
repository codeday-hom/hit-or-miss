import WordEntry from "./WordEntry";
import React from "react";
import useGameWebSocket from "../hooks/useGameWebSocket";
import {WsMessageTypes} from "../constants/wsMessageTypes";

export default function SelectWordPage({gameId, currentPlayer, clientUsername, currentSelectedCategory, diceResult, onWordSelected}) {
    const {sendMessage} = useGameWebSocket(gameId, (message) => {
        if (message.type === WsMessageTypes.SELECTED_WORD) {
            onWordSelected(message.data)
        }
    });

    return (
        <div>
            <p>Current category is: {currentSelectedCategory}</p>
            <p>{currentPlayer === clientUsername ? "You" : currentPlayer} rolled the dice and got: {diceResult}</p>
            {currentPlayer === clientUsername && <WordEntry sendWebSocketMessage={sendMessage}/>}
            {/* TODO: Upon word selection, all players will be prompted to select "hit" or "miss". */}
        </div>
    )
}
