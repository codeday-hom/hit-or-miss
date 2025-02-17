import {useEffect} from "react";
import {WsMessageType} from "./WsMessageType";

const HEARTBEAT_INTERVAL = 1000 * 5;

export default function useWebsocketHeartbeat(gameId, clientUsername, sendMessage) {
  useEffect(() => {
    const heartbeatInterval = setInterval(() => {
      sendMessage(
        JSON.stringify({
          gameId,
          player: clientUsername,
          type: WsMessageType.HEARTBEAT,
          data: {},
        })
      );
    }, HEARTBEAT_INTERVAL);

    return () => {
      clearInterval(heartbeatInterval);
    };
  }, [sendMessage]);
}
