import { useState, useEffect } from "react";
import useGameWebSocket from "../websockets/useGameWebSocket";
import "./Dice.css";
import { WsMessageType } from "../websockets/WsMessageType";

export default function Dice({
  gameId,
  currentPlayer,
  clientUsername,
  onDiceResult
}) {
  const [diceTransform, setDiceTransform] = useState("");
  const [wildcardOption, setWildcardOption] = useState(false);
  const [diceResult, setDiceResult] = useState();
  const [isDiceRolled, setIsDiceRolled] = useState(false);
  const [hitOrMiss, setHitOrMiss] = useState("");
  const [displayHitOrMiss, setDisplayHitOrMiss] = useState(false);
  const { sendMessage } = useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageType.ROLL_DICE_RESULT) {
      setDiceResult(message.data);
    } else if (message.type === WsMessageType.HIT_OR_MISS) {
      setHitOrMiss(message.data);
    }
  });

  const sendHit = () => {
    sendMessage(
      JSON.stringify({ type: WsMessageType.HIT_OR_MISS, data: "Hit" })
    );
  };
  const sendMiss = () => {
    sendMessage(
      JSON.stringify({ type: WsMessageType.HIT_OR_MISS, data: "Miss" })
    );
  };
  useEffect(() => {
    if (diceResult) {
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

      setDiceTransform(
        `rotateX(${endRotationValueX}deg) rotateY(${endRotationValueY}deg) rotateZ(${endRotationValueZ}deg)`
      );

      if (diceResult === 6) {
        setWildcardOption(true);
      } else if (diceResult <= 3) {
        sendHit();
      } else {
        sendMiss();
      }
    }
  }, [diceResult]);

  const displayHitOrMissWithDelay = () => {
    const timer = setTimeout(() => {
      setDisplayHitOrMiss(true);
      onDiceResult(hitOrMiss)
    }, 1500);
    return () => clearTimeout(timer);
  };

  useEffect(() => {
    if (hitOrMiss) displayHitOrMissWithDelay();
    else setDisplayHitOrMiss(false);
  }, [hitOrMiss]);

  const handleRollDice = () => {
    sendMessage(JSON.stringify({ type: WsMessageType.ROLL_DICE, data: "" }));
    setIsDiceRolled(true);
  };

  const handleWildcardOption = (hitOrMiss) => {
    setWildcardOption(false);
    if (hitOrMiss === "Hit") {
      sendHit();
    } else {
      sendMiss();
    }
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
        {currentPlayer === clientUsername && !isDiceRolled && (
          <button onClick={handleRollDice}>Roll dice</button>
        )}
      </div>

      {currentPlayer !== clientUsername && (
          <div>{currentPlayer} is rolling the dice...</div>
      )}

      {displayHitOrMiss
        ? <div className="dice-result">Result: ${hitOrMiss}</div>
        : null}

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
