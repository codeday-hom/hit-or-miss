import useWebSocket from "react-use-websocket";

export default function useGameWebSocket(gameId, onMessageFunction) {
  const WS_URL = `ws://localhost:8080/ws/game/${gameId}`;

  return useWebSocket(WS_URL, {
    onOpen: () => {
      console.log("WebSocket connection established.");
    },

    onMessage: (event) => {
      if (typeof event.data === "string") {
        try {
          const message = JSON.parse(event.data);
          console.log("Received a message:", message);
          onMessageFunction(message)
        } catch (e) {
          console.log("Error parsing message:", e);
        }
      }
    },

    onSend: (data) => {
      console.log("Sent a message:", data);
    },
  });
}