import "./Scoreboard.css";

export default function Scoreboard({clientPlayer, scores}) {
  const sortedScores = [...scores].sort((a, b) => b.score - a.score);

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
            <td>{player.playerId + (player.playerId === clientPlayer ? " (you)" : "")}</td>
            <td>{player.score}</td>
          </tr>
        ))}
        </tbody>
      </table>
    </div>
  );
}
