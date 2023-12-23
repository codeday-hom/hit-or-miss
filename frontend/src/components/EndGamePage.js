import React from "react";

export default function EndGamePage({winningPlayers}) {

  function tada(s) {
    return `🎉 ${s} 🎉`
  }

  function sparkly(name) {
    return `✨${name}!✨`
  }

  function congratulations() {
    if (winningPlayers.length === 0) {
      // This should never happen
      return tada("Everybody lost")
    }

    if (winningPlayers.length === 2) {
      let jointWinners = winningPlayers.map((name) => sparkly(name))
      return tada(`Congratulations to the joint winners: ${jointWinners[0]} and ${jointWinners[1]}`)
    }

    if (winningPlayers.length > 2) {
      let jointWinners = winningPlayers.map((name) => sparkly(name)).join(", ")
      return tada(`Congratulations to the joint winners: ${jointWinners}`)
    }

    return tada(`Congratulations to the winner: ${sparkly(winningPlayers[0])}`)
  }

  return (
    <div>
      <h2>Game Over!</h2>
      <p>{congratulations()}</p>
    </div>
  )
}
