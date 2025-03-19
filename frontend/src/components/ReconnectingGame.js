import useGameWebSocket from "../websockets/useGameWebSocket";
import {useState} from "react";
import {WsMessageType} from "../websockets/WsMessageType";
import Game from "./Game";

export default function ReconnectingGame({gameId}) {
  const [clientPlayer, setClientPlayer] = useState(null);
  const [candidateClientPlayers, setCandidateClientPlayers] = useState(null);
  const [gameState, setGameState] = useState(null);

  useGameWebSocket(gameId, clientPlayer, (message) => {
    if (message.type === WsMessageType.DISCONNECTED_PLAYER_IDS) {
      let disconnectedPlayerIds = message.data.disconnectedPlayerIds
      if (disconnectedPlayerIds.length === 1) {
        setClientPlayer(disconnectedPlayerIds[0])
      } else {
        setCandidateClientPlayers(disconnectedPlayerIds)
      }
    } else if (message.type === WsMessageType.GAME_JOINABLE) {
      setGameState(message.data)
    }
  })

  if (clientPlayer === null) {
    if (candidateClientPlayers === null) {
      return <div>
        <h1>Awaiting confirmation from the game server that you can join the game...</h1>
      </div>
    } else {
      return <div>
        <h1>There are multiple disconnected players - press the button corresponding to you.</h1>
        <ul aria-label="disconnected-players">
          {candidateClientPlayers.map((playerId, index) => (
            <li key={index}>
              <button onClick={() => setClientPlayer(playerId)}>Rejoin as '{playerId}'</button>
            </li>
          ))}
        </ul>
      </div>
    }
  } else if (gameState !== null) {
    const currentPlayer = gameState.currentPlayer;
    const players = gameState.players;
    const scores = gameState.scores;
    const phase = gameState.phase;
    const phaseData = {...gameState.phaseData, scores: scores};
    return <Game
      gameId={gameId}
      clientPlayer={clientPlayer}
      initialPlayer={currentPlayer}
      players={players}
      initialPhase={phase}
      phaseData={phaseData}
    />
  } else {
    return <div>
      <h1>Awaiting information from the game server about the current state of the game...</h1>
    </div>
  }
}
