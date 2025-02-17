import {useState} from "react";
import "./WordEntry.css";
import {WsMessageType} from "../websockets/WsMessageType";

export default function WordEntry({gameId, clientUsername, sendWebSocketMessage}) {
  const [inputWord, setInputWord] = useState("");
  const [wordSelected, setWordSelected] = useState(false);

  const send = () => {
    if (!wordSelected) {
      sendWebSocketMessage(JSON.stringify({
        gameId,
        player: clientUsername,
        type: WsMessageType.SELECTED_WORD,
        data: {selectedWord: inputWord}
      }));
      setWordSelected(true)
    }
  };

  return (
    <div className="word-list">
      <input
        type="text"
        value={inputWord}
        onChange={(e) => setInputWord(e.target.value)}
        placeholder="Enter a word..."
        disabled={wordSelected}
      />
      <button onClick={send}>Send</button>
    </div>
  );
}
