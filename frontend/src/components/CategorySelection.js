import CategoryPicker from "./CategoryPicker";
import React from "react";
import useGameWebSocket from "../hooks/useGameWebSocket";
import {WsMessageTypes} from "../constants/wsMessageTypes";

export default function CategorySelection({ gameId, currentPlayer, clientUsername, onCategoryChosen }) {
    const { sendMessage } = useGameWebSocket(gameId, (message) => {
        if (message.type === WsMessageTypes.CATEGORY_CHOSEN) {
            onCategoryChosen(message.data);
        }
    });

    return (
        <div>
            {currentPlayer === clientUsername
                ? <CategoryPicker gameId={gameId} sendWebSocketMessage={sendMessage} />
                : <p>{currentPlayer} is choosing a category</p>}
        </div>
    )
}
