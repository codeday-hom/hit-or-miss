import {useEffect, useState} from "react";
import "./Scoreboard.css";

export default function Scoreboard({playerNames, scoreboardData}) {
  const initialScoreboard = playerNames.map(name => ({username: name, score: 0}))
  const [sortedPlayers, setSortedPlayers] = useState(initialScoreboard);

  useEffect(() => {
    if (scoreboardData) {
      const sorted = [...scoreboardData].sort(
        (a, b) => b.score - a.score
      );
      setSortedPlayers(sorted);
    }
  }, [scoreboardData]);

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
        {sortedPlayers.map(player => (
          <tr key={player.username}>
            <td>{player.username}</td>
            <td>{player.score}</td>
          </tr>
        ))}
        </tbody>
      </table>
    </div>
  );
}
