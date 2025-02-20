import {useLocation, useParams} from "react-router-dom";
import Game from "./Game";

export default function ContextualGame() {
  const {gameId} = useParams();
  const location = useLocation();
  const clientPlayer = location.state.clientPlayer;
  const initialPlayer = location.state.currentPlayer;
  const players = location.state.players;

  return <Game gameId={gameId} clientPlayer={clientPlayer} initialPlayer={initialPlayer} players={players}/>
}
