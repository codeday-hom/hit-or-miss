import CategoryPicker from "./CategoryPicker";
import React from "react";
import useGameWebSocket from "../websockets/useGameWebSocket";
import {WsMessageType} from "../websockets/WsMessageType";

export default function SelectCategoryPage({gameId, currentPlayer, clientPlayer, onCategorySelected}) {
  const {sendMessage} = useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageType.CATEGORY_SELECTED) {
      onCategorySelected(message.data);
    }
  });

  return (
    <div>
      {currentPlayer === clientPlayer
        ? <CategoryPicker gameId={gameId} clientPlayer={clientPlayer} sendWebSocketMessage={sendMessage}/>
        : <p>{currentPlayer} is choosing a category</p>}
    </div>
  )
}
