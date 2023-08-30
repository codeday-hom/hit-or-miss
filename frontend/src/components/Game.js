import {useState} from "react";
import {useLocation, useParams} from "react-router-dom";
import useGameWebSocket from "../hooks/useGameWebSocket";
import CategoryPicker from "./CategoryPicker";
import {WsMessageTypes} from "../constants/wsMessageTypes";

export default function Game() {
  const { gameId } = useParams();
  const initialPlayer = useLocation().state.currentPlayer;
  const [currentPlayer, setCurrentPlayer] = useState(initialPlayer);

  const { sendMessage } = useGameWebSocket(gameId, message => {
      if (message.type === WsMessageTypes.NEXT_PLAYER) {
          setCurrentPlayer(message.data);
      }
  });

  const handleClick = () => {
    sendMessage(JSON.stringify({type: WsMessageTypes.NEXT_PLAYER, data: ""}))
  };

  return (
    <div>
      <h1>Game has started!</h1>
      <p>{currentPlayer} is choosing a category</p>
      <button onClick={handleClick}>Next Player</button>
      <CategoryPicker gameId = {gameId}/>
    </div>
  );
}
