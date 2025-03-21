import WordEntry from "./WordEntry";
import React from "react";
import useGameWebSocket from "../websockets/useGameWebSocket";
import {WsMessageType} from "../websockets/WsMessageType";

export default function SelectWordPage({gameId, currentPlayer, clientPlayer, currentSelectedCategory, diceResult, onWordSelected}) {
  const {sendMessage} = useGameWebSocket(gameId, clientPlayer, (message) => {
    if (message.type === WsMessageType.SELECTED_WORD) {
      onWordSelected(message.data)
    }
  });

  return (
    <div>
      <p>Current category is: {currentSelectedCategory}</p>
      <p>{currentPlayer === clientPlayer ? "You" : currentPlayer} rolled the dice and got: {diceResult}</p>
      <p>{currentPlayer === clientPlayer ? "You are" : `${currentPlayer} is`} choosing a word...</p>
      {currentPlayer === clientPlayer && <WordEntry gameId={gameId} clientPlayer={clientPlayer} sendWebSocketMessage={sendMessage}/>}
    </div>
  )
}
