import "./Scoreboard.css";

export default function Scoreboard({clientPlayer, scores, disconnectedPlayers}) {
  const sortedScores = [...scores].sort((a, b) => b.score - a.score);

  function playerIdSuffix(playerId) {
    if (playerId === clientPlayer) {
      return " (you)"
    } else if (disconnectedPlayers.includes(playerId)) {
      return " (disconnected)"
    }

    return ""
  }

  return (
    <div className="scoreboard">
      <table>
        <thead>
        <tr>
          <th>Player</th>
          <th>Score</th>
        </tr>
        </thead>
        <tbody>
        {sortedScores.map(player => (
          <tr key={player.playerId}>
            <td>{player.playerId + playerIdSuffix(player.playerId)}</td>
            <td>{player.score}</td>
          </tr>
        ))}
        </tbody>
      </table>
    </div>
  );
}
