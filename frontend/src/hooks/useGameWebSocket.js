import useWebSocket from "react-use-websocket";

export default function useGameWebSocket(gameId, onMessageFunction) {
  const WS_URL = `ws://localhost:8080/ws/game/${gameId}`;

  return useWebSocket(WS_URL, {
    onOpen: () => {
      console.log("WebSocket connection established for gameId '" + gameId + "'.");
    },

    onMessage: (event) => {
      if ((typeof event.data === "string") || (typeof event.data === "object")) {
        try {
          const message = JSON.parse(event.data);
          console.log("Received a message:", message);
          onMessageFunction(message);
        } catch (e) {
          console.log("Error parsing message:", e);
        }
      } else {
        console.log("Received a event that couldn't be parsed:", event);
      }
    },

    onSend: (data) => {
      console.log("Sent a message:", data);
    },
  });
}
