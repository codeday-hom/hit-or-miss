import { useState, useEffect } from "react";
import useWebSocket from "react-use-websocket";

export default function useGameWebSocket(gameId) {
  const [userIds, setUserIds] = useState([]);
  const WS_URL = `ws://localhost:8080/ws/game/${gameId}`;

  const { sendMessage, lastMessage } = useWebSocket(WS_URL, {
    onOpen: () => {
      console.log("WebSocket connection established.");
    },
    onMessage: (event) => {
      if (typeof event.data === "string") {
        try {
          const message = JSON.parse(event.data);
          console.log("Received a message:", message);

          if (message.type === "userJoined") {
            setUserIds((prevUserIds) => {
              const newUserIds = message.data.filter(
                (id) => !prevUserIds.includes(id)
              );
              return [...prevUserIds, ...newUserIds];
            });
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

  useEffect(() => {
    sendMessage("Hello from client!");
  }, []);

  return { userIds };
}
