import { useState } from "react";
import "./WordList.css";
import useGameWebSocket from "../hooks/useGameWebSocket";
import { WsMessageTypes } from "../constants/wsMessageTypes";

export default function WordList({ gameId }) {
  const wordlist = ["Soccer", "Swimming", "Tennis", "Fencing"];
  const [selectedWord, setSelectedWord] = useState("");
  const { sendMessage } = useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageTypes.SELECTED_WORD) {
      setSelectedWord(message.data);
    }
  });

  const handleWordClick = (word) => {
    if (selectedWord) return;
    sendMessage(
      JSON.stringify({ type: WsMessageTypes.SELECTED_WORD, data: word })
    );
  };

  return (
    <div className="word-list">
      <ul>
        {wordlist.map((word, index) => (
          <li
            key={index}
            className={word === selectedWord ? "selected" : ""}
            onClick={() => handleWordClick(word)}
          >
            {word}
          </li>
        ))}
      </ul>
    </div>
  );
}
