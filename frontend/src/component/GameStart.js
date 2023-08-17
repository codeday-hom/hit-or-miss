import { useState, useEffect } from "react";
import { useParams, useLocation } from "react-router-dom";
import useGameWebSocket from "../hook/useGameWebSocket";

export default function GameStart() {
  const { gameId } = useParams();
  const initialPlayer = useLocation().state.currentPlayer;

  const { currentPlayer: nextPlayer, sendNextPlayerMessage } =
    useGameWebSocket(gameId);
  const [currentPlayer, setCurrentPlayer] = useState(initialPlayer);

  useEffect(() => {
    if (nextPlayer) {
      setCurrentPlayer(nextPlayer);
    }
  }, [nextPlayer]);

  const handleClick = () => {
    sendNextPlayerMessage();
  };

  return (
    <div>
      <h1>Game has started!</h1>
      <p>{currentPlayer} is choosing a category</p>
      <button onClick={handleClick}>Next Player</button>
    </div>
  );
}
