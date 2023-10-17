import { useEffect } from "react";
import { WsMessageType } from "./WsMessageType";

const HEARTBEAT_INTERVAL = 1000 * 5;

export default function useWebsocketHeartbeat(sendMessage) {
  let timeoutId;

  useEffect(() => {
    const heartbeatInterval = setInterval(() => {
      sendMessage(
        JSON.stringify({
          type: WsMessageType.HEARTBEAT,
          data: {},
        })
      );
      timeoutId = setTimeout(() => {
        heartbeatInterval();
      }, HEARTBEAT_INTERVAL + 2000);
    }, HEARTBEAT_INTERVAL);

    return () => {
      clearInterval(heartbeatInterval);
      clearTimeout(timeoutId);
    };
  }, [sendMessage]);
}
