import Dice from "./Dice";
import React from "react";

export default function RollDicePage({gameId, currentPlayer, clientPlayer, currentSelectedCategory, onDiceResult}) {
  return (
    <div>
      <p>Current category is: {currentSelectedCategory}</p>
      <Dice
        gameId={gameId}
        currentPlayer={currentPlayer}
        clientPlayer={clientPlayer}
        onDiceResult={onDiceResult}
      />
    </div>
  )
}
