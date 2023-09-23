import { useState, useEffect } from "react";
import { useLocation, useParams } from "react-router-dom";
import useGameWebSocket from "../hooks/useGameWebSocket";
import useWebsocketHeartbeat from "../hooks/useWebsocketHeartbeat";
import CategoryPicker from "./CategoryPicker";
import { WsMessageTypes } from "../constants/wsMessageTypes";
import Dice from "./Dice";

export default function Game() {
  const { gameId } = useParams();
  const location = useLocation();
  const clientUsername = location.state.clientUsername;
  const initialPlayer = location.state.currentPlayer;
  const [currentPlayer, setCurrentPlayer] = useState(initialPlayer);

  const { sendMessage } = useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageTypes.NEXT_PLAYER) {
      setCurrentPlayer(message.data);
    }
  });

  useWebsocketHeartbeat(sendMessage);
  const handleClick = () => {
    sendMessage(JSON.stringify({ type: WsMessageTypes.NEXT_PLAYER, data: "" }));
  };

  return (
    <div>
      <h1>Game has started!</h1>
      <p>{currentPlayer} is choosing a category</p>
      {currentPlayer === clientUsername ? (
        <button onClick={handleClick}>Next Player</button>
      ) : null}
      <CategoryPicker
        gameId={gameId}
        clientUsername={clientUsername}
        currentPlayer={currentPlayer}
      />
      <Dice
        gameId={gameId}
        currentPlayer={currentPlayer}
        clientUsername={clientUsername}
      />
    </div>
  );
}
