import { useState } from "react";
import "./WordList.css";

function WordList({ onSelectWord }) {
  const [text, setText] = useState("");
  const [selectedWord, setSelectedWord] = useState("");

  function handleTextChange(e) {
    setText(e.target.value);
  }

  function handleWordClick(word) {
    setSelectedWord(word);
    onSelectWord(word);
  }

  const words = text.split("\n").filter(word => word.trim() !== "");

  return (
    <div className="word-list">
      <textarea
        placeholder="Write down your words here..."
        value={text}
        onChange={handleTextChange}
      />
      <ul>
        {words.map((word, index) => (
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

export default WordList;
