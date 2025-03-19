import {useLocation, useParams} from "react-router-dom";
import Game from "./Game";
import ReconnectingGame from "./ReconnectingGame";
import {GamePhase} from "./GamePhase";

export default function ContextualGame() {
  const {gameId} = useParams();

  const location = useLocation();
  if (location.state !== null) {
    const clientPlayer = location.state.clientPlayer;
    const initialPlayer = location.state.initialPlayer;
    const players = location.state.players;
    return <Game
      gameId={gameId}
      clientPlayer={clientPlayer}
      initialPlayer={initialPlayer}
      players={players}
      initialPhase={GamePhase.SELECT_CATEGORY}
      phaseData={{}}
    />
  } else {
    return <ReconnectingGame gameId={gameId}/>
  }
}
