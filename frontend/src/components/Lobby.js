import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import Cookies from "js-cookie";
import {WsMessageType} from "../websockets/WsMessageType";
import useGameWebSocket from "../websockets/useGameWebSocket";

export default function Lobby() {
  const [isHost, setIsHost] = useState(false);
  const {gameId} = useParams();
  const [players, setPlayers] = useState([]);
  const [savedPlayerId, setSavedPlayerId] = useState("");
  const [typedPlayerId, setTypedPlayerId] = useState("");
  const [invalidPlayerIdWarning, setInvalidPlayerIdWarning] = useState("");
  const [gameStarted, setGameStarted] = useState(false);
  const navigate = useNavigate();

  // From the lobby, we connect without using a playerId, since we don't necessarily have it yet.
  useGameWebSocket(gameId, null, (message) => {
    if (message.type === WsMessageType.USER_JOINED) {
      setPlayers((previousPlayers) => {
        const newPlayers = Object.values(message.data).filter(
          (playerId) => !previousPlayers.includes(playerId)
        );
        return [...previousPlayers, ...newPlayers];
      });
    } else if (message.type === WsMessageType.GAME_START) {
      navigate(`/game/${gameId}`, {
        state: {clientPlayer: savedPlayerId, currentPlayer: message.data, players: players},
      });
    } else if (message.type === WsMessageType.ERROR) {
      setGameStarted(true);
    }
  });

  useEffect(() => {
    let previouslySavedPlayerId = Cookies.get(gameId);
    if (previouslySavedPlayerId !== undefined) {
      setSavedPlayerId(previouslySavedPlayerId)
      checkIfHost()
    }
  }, []);

  const checkIfHost = () => {
    if (Cookies.get(gameId + "_host") !== undefined) {
      setIsHost(true);
    }
  };

  const handleStartGame = () => {
    const url = `/api/game/${gameId}/start`;

    fetch(url, {
      method: "POST",
    }).catch((error) => {
      console.log("Error starting the game:", error);
    });
  };

  const handlePlayerIdChange = (e) => {
    setTypedPlayerId(e.target.value);
  };

  const handlePlayerIdSave = async () => {
    const formattedPlayerId = typedPlayerId.trim();
    if (formattedPlayerId === "") {
      setInvalidPlayerIdWarning("Please enter a valid name.");
      return;
    }

    if (players.includes(formattedPlayerId)) {
      setInvalidPlayerIdWarning("This name is already taken");
      return;
    }

    await sendPlayerIdToBackend(formattedPlayerId).then(() => {
      setTypedPlayerId("");
      setSavedPlayerId(formattedPlayerId);
    });
  };

  const sendPlayerIdToBackend = async (playerId) => {
    const url = `/api/game/${gameId}/join`;

    fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({gameId, playerId}),
    })
      .then((response) => response.json())
      .then(() => checkIfHost())
      .catch((error) => {
        console.log("Error fetching data:", error);
      });
  };

  if (gameStarted) {
    return <div>This game has already started.</div>;
  }

  return (
    <div>
      <h1>Welcome to the Game Lobby!</h1>
      <p>Your friends can join this game by visiting <a href={window.location.href}>this link</a></p>
      {!savedPlayerId && (
        <div>
          <input
            type="text"
            value={typedPlayerId}
            onChange={handlePlayerIdChange}
            placeholder="Choose your name"
          />
          <button onClick={handlePlayerIdSave}>Save</button>
          {invalidPlayerIdWarning && <div>{invalidPlayerIdWarning}</div>}
        </div>
      )}
      {savedPlayerId && isHost && (
        <button onClick={handleStartGame} disabled={players.length < 2}>
          Start Game
        </button>
      )}
      {savedPlayerId && <h2>Your name: {savedPlayerId}</h2>}
      <h2>Players in the lobby:</h2>
      <ul aria-label="other-players">
        {players.map((playerId, index) => (
          <li key={index}>{playerId}  </li>
        ))}
      </ul>
    </div>
  );
}
