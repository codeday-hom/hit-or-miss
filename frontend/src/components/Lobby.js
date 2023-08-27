import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import Cookies from "js-cookie";
import useGameWebSocket from "../hooks/useGameWebSocket";
import {WsMessageTypes} from "../constants/wsMessageTypes";

export default function Lobby() {
  const [isHost, setIsHost] = useState(false);
  const { gameId } = useParams();
  const [usernames, setUsernames] = useState([]);
  const [username, setUsername] = useState("");
  const [name, setName] = useState("");
  const [invalidNameWarning, setInvalidNameWarning] = useState("");
  const [validName, setValidName] = useState(false);
  const navigate = useNavigate();

  useGameWebSocket(gameId, message => {
    if (message.type === WsMessageTypes.USER_JOINED) {
      setUsernames((prevUsernames) => {
        const newUsernames = Object.values(message.data).filter(
            (name) => !prevUsernames.includes(name)
        );
        return [...prevUsernames, ...newUsernames];
      });
    } else if (message.type === WsMessageTypes.GAME_START) {
      navigate(`/game/${gameId}`, {
        state: { clientUsername: username, currentPlayer: message.data },
      });
    }
  });

    const checkIfHost = () => {
    const hostGameId = Cookies.get("game_host");
    const currentUrl = window.location.href;
    setIsHost(hostGameId && currentUrl.includes("/game/" + hostGameId + "/lobby"));
  };

  const handleStartGame = () => {
    const url = `http://localhost:8080/api/start-game/${gameId}`;

    fetch(url, {
      method: "POST",
    }).catch((error) => {
      console.log("Error starting the game:", error);
    });
  };

  const handleNameChange = (e) => {
    setName(e.target.value);
  };

  const handleNameSave = async () => {
    const formattedUsername = name.trim();
    if (formattedUsername === "") {
      setInvalidNameWarning("Please enter a valid name.");
      return;
    }

    if (usernames.includes(formattedUsername)) {
      setInvalidNameWarning("This username is already taken");
      return;
    }
    setName("");
    setValidName(true);
    await sendUserNameToBackend(formattedUsername);
    setUsername(formattedUsername)
  };

  const sendUserNameToBackend = async (username) => {
    const url = `http://localhost:8080/api/join-game/${gameId}`;

    fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ gameId, username }),
    })
      .then((response) => response.json())
      .then(() => checkIfHost())
      .catch((error) => {
        console.log("Error fetching data:", error);
      });
  };

  return (
    <div>
      <h1>Welcome to the Game Lobby!</h1>
      {!validName && (
        <div>
          <input
            type="text"
            value={name}
            onChange={handleNameChange}
            placeholder="Choose your name"
          />
          <button onClick={handleNameSave}>Save</button>
          {invalidNameWarning && <div>{invalidNameWarning}</div>}
        </div>
      )}
      {isHost && (
        <button onClick={handleStartGame} disabled={usernames.length < 2}>
          Start Game
        </button>
      )}
      <h2>Your name: {username}</h2>
      <h2>Players in the lobby:</h2>
      <ul>
        {usernames.map((name, index) => (
          <li key={index}>{name}</li>
        ))}
      </ul>
    </div>
  );
}