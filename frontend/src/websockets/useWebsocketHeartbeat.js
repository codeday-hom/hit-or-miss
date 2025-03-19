import {useEffect} from "react";
import {WsMessageType} from "./WsMessageType";

const HEARTBEAT_INTERVAL = 1000 * 5;

export default function useWebsocketHeartbeat(gameId, clientPlayer, sendMessage) {
  useEffect(() => {
    const heartbeatInterval = setInterval(() => {
      sendMessage(
        JSON.stringify({
          gameId,
          player: clientPlayer,
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
