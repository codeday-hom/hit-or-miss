import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import Cookies from "js-cookie";
import useGameWebSocket from "../websockets/useGameWebSocket";
import {WsMessageType} from "../websockets/WsMessageType";

export default function Lobby() {
  const [isHost, setIsHost] = useState(false);
  const {gameId} = useParams();
  const [players, setPlayers] = useState([]);
  const [savedName, setSavedName] = useState("");
  const [typedName, setTypedName] = useState("");
  const [invalidNameWarning, setInvalidNameWarning] = useState("");
  const [gameStarted, setGameStarted] = useState(false);
  const navigate = useNavigate();

  useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageType.USER_JOINED) {
      setPlayers((previousPlayers) => {
        const newPlayers = Object.values(message.data).filter(
          (name) => !previousPlayers.includes(name)
        );
        return [...previousPlayers, ...newPlayers];
      });
    } else if (message.type === WsMessageType.GAME_START) {
      navigate(`/game/${gameId}`, {
        state: {clientUsername: savedName, currentPlayer: message.data, playerNames: players},
      });
    } else if (message.type === WsMessageType.ERROR) {
      setGameStarted(true);
    }
  });

  useEffect(() => {
    let previouslySavedName = Cookies.get(gameId);
    if (previouslySavedName !== undefined) {
      setSavedName(previouslySavedName)
      checkIfHost()
    }
  }, []);

  const checkIfHost = () => {
    if (Cookies.get(gameId + "_host") !== undefined) {
      setIsHost(true);
    }
  };

  const handleStartGame = () => {
    const url = `/api/start-game/${gameId}`;

    fetch(url, {
      method: "POST",
    }).catch((error) => {
      console.log("Error starting the game:", error);
    });
  };

  const handleNameChange = (e) => {
    setTypedName(e.target.value);
  };

  const handleNameSave = async () => {
    const formattedUsername = typedName.trim();
    if (formattedUsername === "") {
      setInvalidNameWarning("Please enter a valid name.");
      return;
    }

    if (players.includes(formattedUsername)) {
      setInvalidNameWarning("This username is already taken");
      return;
    }

    await sendUserNameToBackend(formattedUsername).then(() => {
      setTypedName("");
      setSavedName(formattedUsername);
    });
  };

  const sendUserNameToBackend = async (username) => {
    const url = `/api/join-game/${gameId}`;

    fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({gameId, username}),
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
      {!savedName && (
        <div>
          <input
            type="text"
            value={typedName}
            onChange={handleNameChange}
            placeholder="Choose your name"
          />
          <button onClick={handleNameSave}>Save</button>
          {invalidNameWarning && <div>{invalidNameWarning}</div>}
        </div>
      )}
      {savedName && isHost && (
        <button onClick={handleStartGame} disabled={players.length < 2}>
          Start Game
        </button>
      )}
      {savedName && <h2>Your name: {savedName}</h2>}
      <h2>Players in the lobby:</h2>
      <ul aria-label="other-players">
        {players.map((name, index) => (
          <li key={index}>{name}  </li>
        ))}
      </ul>
    </div>
  );
}
