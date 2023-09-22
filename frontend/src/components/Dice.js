import { useState, useEffect } from "react";
import useGameWebSocket from "../hooks/useGameWebSocket";
import "./Dice.css";
import { WsMessageTypes } from "../constants/wsMessageTypes";

export default function Dice({
  gameId,
  currentPlayer,
  clientUsername,
  onDiceRolled,
}) {
  const [diceTransform, setDiceTransform] = useState("");
  const [wildcardOption, setWildcardOption] = useState(false);
  const [diceResult, setDiceResult] = useState();
  const [isDiceRolled, setIsDiceRolled] = useState(false);
  const [hitOrMiss, setHitOrMiss] = useState("");
  const [displayHitOrMiss, setDisplayHitOrMiss] = useState(false);
  const { sendMessage } = useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageTypes.ROLL_DICE_RESULT) {
      setDiceResult(message.data);
    } else if (message.type === WsMessageTypes.HIT_OR_MISS) {
      setHitOrMiss(message.data);
    }
  });

  const computeDiceRotation = (diceResult) => {
    let randomSpin = 3 + Math.floor(Math.random() * 5);
    let endRotationValueX = 0;
    let endRotationValueY = 0;
    let endRotationValueZ = 0;

    switch (diceResult) {
      case 1:
        endRotationValueX = 720 + 360 * randomSpin;
        endRotationValueZ = -720 - 360 * randomSpin;
        break;
      case 2:
        endRotationValueY = 900 + 360 * randomSpin;
        endRotationValueZ = -1080 - 360 * randomSpin;
        break;
      case 3:
        endRotationValueY = 810 + 360 * randomSpin;
        endRotationValueZ = 720 + 360 * randomSpin;
        break;
      case 4:
        endRotationValueY = -450 + 360 * randomSpin;
        endRotationValueZ = -1440 - 360 * randomSpin;
        break;
      case 5:
        endRotationValueX = -810 + 360 * randomSpin;
        endRotationValueZ = -1080 - 360 * randomSpin;
        break;
      case 6:
        endRotationValueX = 450 + 360 * randomSpin;
        endRotationValueZ = -720 - 360 * randomSpin;
        break;
      default:
        break;
    }

    return `rotateX(${endRotationValueX}deg) rotateY(${endRotationValueY}deg) rotateZ(${endRotationValueZ}deg)`;
  };

  const sendHitOrMissMessage = (diceResult) => {
    if (diceResult <= 3) {
      sendMessage(
        JSON.stringify({ type: WsMessageTypes.HIT_OR_MISS, data: "Hit" })
      );
    } else if (diceResult < 6) {
      sendMessage(
        JSON.stringify({ type: WsMessageTypes.HIT_OR_MISS, data: "Miss" })
      );
    }
  };

  const processDiceResult = (diceResult) => {
    const rotation = computeDiceRotation(diceResult);
    setDiceTransform(rotation);

    if (diceResult === 6) {
      setWildcardOption(true);
    } else {
      sendHitOrMissMessage(diceResult);
    }
  };
  useEffect(() => {
    if (diceResult) {
      processDiceResult(diceResult);
    }
  }, [diceResult]);

  const displayHitOrMissWithDelay = () => {
    const timer = setTimeout(() => {
      setDisplayHitOrMiss(true);
      onDiceRolled();
    }, 1500);
    return () => clearTimeout(timer);
  };

  useEffect(() => {
    if (hitOrMiss) displayHitOrMissWithDelay();
    else setDisplayHitOrMiss(false);
  }, [hitOrMiss]);

  const handleRollDice = () => {
    sendMessage(JSON.stringify({ type: WsMessageTypes.ROLL_DICE, data: "" }));
    setIsDiceRolled(true);
  };

  const handleWildcardOption = (choice) => {
    setWildcardOption(false);

    sendMessage(
      JSON.stringify({ type: WsMessageTypes.HIT_OR_MISS, data: choice })
    );
  };

  return (
    <div>
      <div className="container">
        <div className="dice" style={{ transform: diceTransform }}>
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
        {displayHitOrMiss
          ? `Current choice: ${hitOrMiss}`
          : "Rolling the Dice..."}
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
