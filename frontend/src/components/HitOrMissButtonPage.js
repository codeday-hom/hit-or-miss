import HitOrMissButton from "./HitOrMissButton";
import React from "react";

export default function HitOrMissButtonPage(
  {
    gameId,
    currentSelectedCategory,
    diceResult,
    selectedWord,
    currentPlayer,
    clientPlayer,
    sendWebSocketMessage
  }
) {
  return (
    <div>
      <div>Category: {currentSelectedCategory}</div>
      <div>Dice result: {diceResult}</div>
      <div>Word: {selectedWord}</div>
      <HitOrMissButton
        gameId={gameId}
        currentPlayer={currentPlayer}
        clientPlayer={clientPlayer}
        sendWebSocketMessage={sendWebSocketMessage}
      />
    </div>
  )
}
