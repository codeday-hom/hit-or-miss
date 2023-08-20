import { useState, useEffect } from "react";
import useWebSocket from "react-use-websocket";
import { useNavigate } from "react-router-dom";
import { WsMessageTypes } from "../constants/wsMessageTypes";

export default function useGameWebSocket(gameId) {
  const [userIds, setUserIds] = useState([]);
  const [usernames, setUsernames] = useState([]);
  const WS_URL = `ws://localhost:8080/ws/game/${gameId}`;
  const navigate = useNavigate();
  const [currentPlayer, setCurrentPlayer] = useState("");

  const { sendMessage, lastMessage } = useWebSocket(WS_URL, {
    onOpen: () => {
      console.log("WebSocket connection established.");
    },

    onMessage: (event) => {
      if (typeof event.data === "string") {
        try {
          const message = JSON.parse(event.data);
          console.log("Received a message:", message);

          if (message.type === WsMessageTypes.USER_JOINED) {
            setUserIds((prevUserIds) => {
              const newUserIds = Object.keys(message.data).filter(
                (id) => !prevUserIds.includes(id)
              );
              return [...prevUserIds, ...newUserIds];
            });
            setUsernames((prevUsernames) => {
              const newUsernames = Object.values(message.data).filter(
                (name) => !prevUsernames.includes(name)
              );
              return [...prevUsernames, ...newUsernames];
            });
          } else if (message.type === WsMessageTypes.GAME_START) {
            navigate(`/game/${gameId}`, {
              state: { currentPlayer: message.data },
            });
          } else if (message.type === WsMessageTypes.NEXT_PLAYER) {
            setCurrentPlayer(message.data);
          }
        } catch (e) {
          console.log("Error parsing message:", e);
        }
      }
    },

    onSend: (data) => {
      console.log("Sent a message:", data);
    },
  });

  const sendNextPlayerMessage = () => {
    sendMessage(WsMessageTypes.NEXT_PLAYER);
  };

  useEffect(() => {
    sendMessage("Hello from client!");
  }, []);

  return { userIds, usernames, currentPlayer, sendNextPlayerMessage, isCountdown, setIsCountdown, startCountdown };
}