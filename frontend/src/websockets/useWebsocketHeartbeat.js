import {useEffect} from "react";
import {WsMessageType} from "./WsMessageType";

const HEARTBEAT_INTERVAL = 1000 * 5;

export default function useWebsocketHeartbeat(sendMessage) {
  useEffect(() => {
    const heartbeatInterval = setInterval(() => {
      sendMessage(
        JSON.stringify({
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
