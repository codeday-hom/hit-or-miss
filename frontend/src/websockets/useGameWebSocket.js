import useWebSocket from "react-use-websocket";

export default function useGameWebSocket(gameId, onMessageFunction) {
  const url = new URL(`/ws/game/${gameId}`, window.location.href);
  url.protocol = url.protocol.replace('http', 'ws');

  return useWebSocket(url.href, {
    onOpen: () => {
      console.log("WebSocket connection established for gameId '" + gameId + "'.");
    },

    onMessage: (event) => {
      if (typeof event.data === "string") {
        try {
          const message = JSON.parse(event.data);
          console.log("Received a message:", message);
          onMessageFunction(message);
        } catch (e) {
          console.log("Error parsing message:", e);
        }
      } else {
        console.log("Received a non-string event:", event);
      }
    },

    onSend: (data) => {
      console.log("Sent a message:", data);
    },
  });
}
