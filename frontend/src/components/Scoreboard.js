import {useEffect, useState} from "react";
import "./Scoreboard.css";

export default function Scoreboard({ scoreboardData }) {
  const [sortedPlayers, setSortedPlayers] = useState([]);

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
            <th>Rank</th>
            <th>Player</th>
            <th>Score</th>
          </tr>
        </thead>
        <tbody>
          {sortedPlayers.map((player, index) => (
            <tr key={player.username}>
              <td>{index + 1}</td>
              <td>{player.username}</td>
              <td>{player.score}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
