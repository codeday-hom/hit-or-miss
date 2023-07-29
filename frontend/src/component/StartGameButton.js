import { useState } from 'react';
import { useNavigate } from "react-router-dom";

export default function StartGameButton() {
    const [gameId, setGameId] = useState(null);
    const navigate = useNavigate();

    const createNewGame = async () => {
        try {
          const response = await fetch('http://localhost:8080/new-game', {
            method: 'POST',
          });

          if (response.ok) {
            const gameId = await response.text();
            setGameId(gameId);
            navigate(`/game/${gameId}/lobby`);
          } else {
            console.log('Failed to create a new game.');
          }
        } catch (error) {
          console.error('Error:', error);
        }
      };

    return (
        <div>
           <button onClick={createNewGame}>Create New Game</button>
        </div>
    );
}