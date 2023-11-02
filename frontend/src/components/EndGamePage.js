import Scoreboard from "./Scoreboard";
import React, {useState} from "react";

export default function EndGamePage({currentPlayer}) {

    return (
    <body>
    <title>Game Over</title>
    <h1>Game Over</h1>
    <p>Congratulations, you've reached the end of the game!</p>
    <div className="game-scoreboard">
        <p> {currentPlayer} has won!</p>
        <Scoreboard clientUsername={clientUsername} scores={scores}/>
    </div>
    <p>Thank you for playing.</p>
    </body>
)
}

