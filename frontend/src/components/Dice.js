import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import useGameWebSocket from "../hooks/useGameWebSocket";
import "./Dice.css";
import { WsMessageTypes } from "../constants/wsMessageTypes";

export default function Dice() {
  const { gameId } = useParams();
  const [diceState, setDiceState] = useState("");
  const [wildcardOption, setWildcardOption] = useState(false);
  const [diceResult, setDiceResult] = useState();
  const { sendMessage } = useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageTypes.ROLL_DICE_RESULT) {
      setDiceResult(message.data);
    }
  });

  useEffect(() => {
    if (diceResult) {
      if (diceResult === 6) {
        setWildcardOption(true);
      }
      setDiceState("show-" + diceResult);
    }
  }, [diceResult]);

  function handleRollDice() {
    sendMessage(JSON.stringify({ type: WsMessageTypes.ROLL_DICE, data: "" }));
  }

  function handleWildcardOption(option) {
    console.log("Player chose", option);
    setWildcardOption(false);
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
        <button onClick={handleRollDice}>Roll dice</button>
      </div>

      {wildcardOption && (
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
