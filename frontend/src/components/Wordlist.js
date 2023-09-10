import { useState } from "react";
import "./WordList.css";
import useGameWebSocket from "../hooks/useGameWebSocket";
import { WsMessageTypes } from "../constants/wsMessageTypes";

export default function WordList({ gameId }) {
  const [inputWord, setInputWord] = useState("");
  const { sendMessage } = useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageTypes.SELECTED_WORD) {
      setInputWord(message.data);
    }
  });

  const handleSendClick = () => {
    if (!inputWord) return;
    sendMessage(
      JSON.stringify({ type: WsMessageTypes.SELECTED_WORD, data: inputWord })
    );
    setInputWord("");
  };

  return (
    <div className="word-list">
      <input
        type="text"
        value={inputWord}
        onChange={(e) => setInputWord(e.target.value)}
        placeholder="Enter a word..."
      />
      <button onClick={handleSendClick}>Send</button>
    </div>
  );
}
