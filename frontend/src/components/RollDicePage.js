import Dice from "./Dice";
import React from "react";

export default function RollDicePage({ gameId, currentPlayer, clientUsername, currentSelectedCategory, onDiceResult }) {
    return (
        <div>
            <p>Current category is: {currentSelectedCategory}</p>
            <Dice
                gameId={gameId}
                currentPlayer={currentPlayer}
                clientUsername={clientUsername}
                onDiceResult={onDiceResult}
            />
        </div>
    )
}
