import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import useGameWebSocket from "../hooks/useGameWebSocket";
import "./Dice.css";
import { WsMessageTypes } from "../constants/wsMessageTypes";

export default function Dice({ currentPlayer, clientUsername }) {
  const { gameId } = useParams();
  const [diceState, setDiceState] = useState("");
  const [wildcardOption, setWildcardOption] = useState(false);
  const [diceResult, setDiceResult] = useState();
  const [isDiceRolled, setIsDiceRolled] = useState(false);
  const [hitOrMiss, setHitOrMiss] = useState("");
  const { sendMessage } = useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageTypes.ROLL_DICE_RESULT) {
      setDiceResult(message.data);
    } else if (message.type === WsMessageTypes.HIT_OR_MISS) {
      setHitOrMiss(message.data);
    }
  });

  useEffect(() => {
    if (diceResult) {
      if (diceResult === 6) {
        setWildcardOption(true);
      } else if (diceResult <= 3) {
        sendMessage(
          JSON.stringify({ type: WsMessageTypes.HIT_OR_MISS, data: "Hit" })
        );
      } else {
        sendMessage(
          JSON.stringify({ type: WsMessageTypes.HIT_OR_MISS, data: "Miss" })
        );
      }
      setDiceState("show-" + diceResult);
    }
  }, [diceResult]);

  function handleRollDice() {
    sendMessage(JSON.stringify({ type: WsMessageTypes.ROLL_DICE, data: "" }));
    setIsDiceRolled(true);
  }

  function handleWildcardOption(option) {
    setWildcardOption(false);
    if (hitOrMiss === "Hit") {
      sendMessage(
        JSON.stringify({ type: WsMessageTypes.HIT_OR_MISS, data: "Hit" })
      );
    } else {
      sendMessage(
        JSON.stringify({ type: WsMessageTypes.HIT_OR_MISS, data: "Miss" })
      );
    }
  }

  return (
    <div className={diceState}>
      <div className="container">
        <div className="dice">
          <div className="side">
            <div className="hit-text">Hit</div>
          </div>
          <div className="side">
            <div className="hit-text">Hit</div>
          </div>
          <div className="side">
            <div className="hit-text">Hit</div>
          </div>
          <div className="side">
            <div className="miss-text">Miss</div>
          </div>
          <div className="side">
            <div className="miss-text">Miss</div>
          </div>
          <div className="side">
            <div className="wildcard-text">Wildcard</div>
          </div>
        </div>
      </div>
      <div className="roll-button">
        {currentPlayer === clientUsername && (
          <button onClick={handleRollDice} disabled={isDiceRolled}>
            Roll dice
          </button>
        )}
      </div>

      <div className="dice-result">
        {hitOrMiss ? `Current choice: ${hitOrMiss}` : "Rolling the Dice..."}
      </div>

      {wildcardOption && currentPlayer === clientUsername && (
        <div className="wildcard">
          <div className="wildcard-content">
            <h2>Wildcard!</h2>
            <p>Hit or Miss?</p>
            <button onClick={() => handleWildcardOption("Hit")}>Hit</button>
            <button onClick={() => handleWildcardOption("Miss")}>Miss</button>
          </div>
        </div>
      )}
    </div>
  );
}
