import WordEntry from "./WordEntry";
import React from "react";
import useGameWebSocket from "../websockets/useGameWebSocket";
import {WsMessageType} from "../websockets/WsMessageType";

export default function SelectWordPage({gameId, currentPlayer, clientUsername, currentSelectedCategory, diceResult, onWordSelected}) {
  const {sendMessage} = useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageType.SELECTED_WORD) {
      onWordSelected(message.data)
    }
  });

  return (
    <div>
      <p>Current category is: {currentSelectedCategory}</p>
      <p>{currentPlayer === clientUsername ? "You" : currentPlayer} rolled the dice and got: {diceResult}</p>
      <p>{currentPlayer === clientUsername ? "You are" : `${currentPlayer} is`} choosing a word...</p>
      {currentPlayer === clientUsername && <WordEntry sendWebSocketMessage={sendMessage}/>}
      {/* TODO: Upon word selection, all players will be prompted to select "hit" or "miss". */}
    </div>
  )
}
