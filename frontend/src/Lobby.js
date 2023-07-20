import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
//import Cookies from 'js-cookie';

export default function Lobby() {
  const [isHost, setIsHost] = useState(false);
  const [userIds, setUserIds] = useState([]);
  const { gameId } = useParams();

  const generateUserId = () => {
    const randimals = require('randimals');
    const randomAnimal = randimals().split(' ')[1];
    const randomDigits = Math.floor(Math.random() * 900) + 100;
    return randomAnimal + '-' + randomDigits;
  };

//  const getGameId = () => {
//    const gameId = Cookies.get("game_id");
//    console.log("gameID from cookie:", gameId)
//  }

   const getUserIdsFromBackend = async () => {
     try {
       const response = await fetch(`http://localhost:8080/game/${gameId}/lobby`);
       if (response.ok) {
         const data = await response.json(); // Assuming the backend returns JSON data
         console.log('Received data:', data);
         if (data.userIds !== null) {
             setUserIds(data.userIds);
         }
       } else {
         console.log('Failed to get userIds from the backend. Status:', response.status);
       }
     } catch (error) {
       console.error('Error:', error);
     }
   };

         
  const sendUserIdsToBackend = async (gameId, userIds) => {
      try {
        const response = await fetch(`http://localhost:8080/game/${gameId}/lobby`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ gameId, userIds }),
        });

        if (response.ok) {
          console.log('UserIds successfully sent to the backend!');
        } else {
          console.log('Failed to send userIds to the backend.');
        }
      } catch (error) {
        console.error('Error sending userIds:', error);
      }
    };

//    useEffect(() => {
//      const newUserId = generateUserId();
//      setUserIds((prevUserIds) => [...prevUserIds, newUserId]);
//      sendUserIdsToBackend([...userIds, newUserId]);
//    }, []);
    useEffect(() => {
        getUserIdsFromBackend();

      const newUserId1 = generateUserId();
      const newUserId2 = generateUserId();

      setUserIds((prevUserIds) => [...prevUserIds, newUserId1, newUserId2]);

      const updatedUserIds = [...userIds, newUserId1, newUserId2];
      sendUserIdsToBackend(gameId, updatedUserIds);
    }, [gameId]);


  const handleStartGame = () => {
    console.log("Game started!");
  };

  return (
    <div>
      <h1>Welcome to the Game Lobby!</h1>
//      {isHost && <button onClick={handleStartGame}>Start Game</button>}
        <button onClick={handleStartGame}>Start Game</button>
      <h2>User IDs:</h2>
      <ul>
        {userIds.map(userId => (
          <li key={userId}>{userId}</li>
        ))}
      </ul>
    </div>
  );
}
