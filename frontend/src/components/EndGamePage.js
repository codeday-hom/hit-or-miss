import React from "react";

export default function EndGamePage({winningPlayer}) {

  return (
    <div>
      <h2>Game Over!</h2>
      <p>🎉 Congratulations to the winner: ✨{winningPlayer}!✨ 🎉</p>
    </div>
  )
}
