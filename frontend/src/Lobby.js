import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Cookies from 'js-cookie';

export default function Lobby() {
  const [isHost, setIsHost] = useState(false);
  const [userId, setUserId] = useState("");
  const [userIds, setUserIds] = useState([]);
  const { gameId } = useParams();

//  const generateUserId = () => {
//    const randimals = require('randimals');
//    const randomAnimal = randimals().split(' ')[1];
//    const randomDigits = Math.floor(Math.random() * 900) + 100;
//    return randomAnimal + '-' + randomDigits;
//  };

    const checkIfHost = () => {
        const hostGameId = Cookies.get('game_host');
        const currentUrl = window.location.href;
        setIsHost(hostGameId && currentUrl.includes("/game/" + hostGameId + "/lobby"));
      }

    useEffect(() => {
        const url = `http://localhost:8080/game/${gameId}/lobby`;

        fetch(url, {
        method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ gameId }),
        })
          .then(response => response.json())
          .then(data => {
            checkIfHost();
            setUserIds(data.userIds);
            setUserId(data.userId);
          })
          .catch(error => {
            console.log('Error fetching data:', error);
          });
      }, [gameId]);


  const handleStartGame = () => {
    console.log("Game started!");
  };

  return (
    <div>
      <h1>Welcome to the Game Lobby!</h1>
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
