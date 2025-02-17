import {useLocation, useParams} from "react-router-dom";
import Game from "./Game";

export default function ContextualGame() {
  const {gameId} = useParams();
  const location = useLocation();
  const clientUsername = location.state.clientUsername;
  const initialPlayer = location.state.currentPlayer;
  const playerNames = location.state.playerNames;

  return <Game gameId={gameId} clientUsername={clientUsername} initialPlayer={initialPlayer} playerNames={playerNames}/>
}
