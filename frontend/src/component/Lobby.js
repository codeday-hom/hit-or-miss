import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import Cookies from "js-cookie";
import useGameWebSocket from "../hook/useGameWebSocket";

export default function Lobby() {
  const [isHost, setIsHost] = useState(false);
  //  const [userId, setUserId] = useState("");
  const { gameId } = useParams();
  const { userIds } = useGameWebSocket(gameId);

  const [name, setName] = useState("");
  const [username, setUsername] = useState("");
  const [invalidNameWarning, setInvalidNameWarning] = useState("");
  const [validName, setValidName] = useState(false);

  const checkIfHost = () => {
    const hostGameId = Cookies.get("game_host");
    const currentUrl = window.location.href;
    setIsHost(
      hostGameId && currentUrl.includes("/game/" + hostGameId + "/lobby")
    );
  };

  const handleStartGame = () => {
    console.log("Game started!");
  };

  const handleNameChange = (e) => {
    setName(e.target.value);
  };

  const handleNameSave = () => {
    const formattedUsername = name.trim();
    if (formattedUsername === "") {
      setInvalidNameWarning("Please enter a valid name.");
      return;
    }

    if (usernames.includes(formattedUsername)) {
       setInvalidNameWarning("This username is already taken");
       return;
    }
    setUsername(formattedUsername);
    setName("");
    setValidName(true);
    sendUserNameToBackend(formattedUsername);
  };

  const sendUserNameToBackend = async (username) => {
    const url = `http://localhost:8080/api/join-game/${gameId}`;
    fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ gameId }),
    })
      .then((response) => {
        if (response.status !== 200) {
          console.log("Error response: ", response)
          throw Error("Unsuccessful response")
        }
        console.log("Response: ", response)
        return response.json()
      })
      .then((data) => {
        // data: gameId and hostId
        checkIfHost();
        //  setUserId(data.userId);
      })
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
      {isHost && <button onClick={handleStartGame}>Start Game</button>}
      <h2>User IDs:</h2>
      <ul>
        {userIds.map((userId, index) => (
          <li key={index}>{userId}</li>
        ))}
      </ul>
    </div>
  );
}
