import {useEffect, useState} from "react";
import useGameWebSocket from "../websockets/useGameWebSocket";
import "./Dice.css";
import {WsMessageType} from "../websockets/WsMessageType";
import {DiceResult} from "./DiceResult";

export default function Dice(
  {
    gameId,
    currentPlayer,
    clientPlayer,
    onDiceResult
  }
) {
  const [diceTransform, setDiceTransform] = useState("");
  const [wildcardOption, setWildcardOption] = useState(false);
  const [diceResult, setDiceResult] = useState();
  const [isDiceRolled, setIsDiceRolled] = useState(false);
  const [hitOrMiss, setHitOrMiss] = useState("");
  const [displayHitOrMiss, setDisplayHitOrMiss] = useState(false);
  const {sendMessage} = useGameWebSocket(gameId, clientPlayer, (message) => {
    if (message.type === WsMessageType.ROLL_DICE_RESULT) {
      setDiceResult(message.data);
    } else if (message.type === WsMessageType.ROLL_DICE_HIT_OR_MISS) {
      setHitOrMiss(message.data);
    }
  });

  const sendDiceResult = (diceResult) => {
    sendMessage(JSON.stringify({
      gameId,
      player: clientPlayer,
      type: WsMessageType.ROLL_DICE_HIT_OR_MISS,
      data: {diceResult: diceResult},
    }));
  };
  const sendHit = () => sendDiceResult(DiceResult.HIT)
  const sendMiss = () => sendDiceResult(DiceResult.MISS)

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

      setDiceTransform(`rotateX(${endRotationValueX}deg) rotateY(${endRotationValueY}deg) rotateZ(${endRotationValueZ}deg)`);

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
    const delayMillis = window['useTestTimeouts'] ? 100 : 1500
    const timer = setTimeout(() => {
      setDisplayHitOrMiss(true);
      onDiceResult(hitOrMiss)
    }, delayMillis);
    return () => clearTimeout(timer);
  };

  useEffect(() => {
    if (hitOrMiss) displayHitOrMissWithDelay();
    else setDisplayHitOrMiss(false);
  }, [hitOrMiss]);

  const handleRollDice = () => {
    sendMessage(JSON.stringify({
      gameId,
      player: clientPlayer,
      type: WsMessageType.ROLL_DICE,
      data: {}
    }));
    setIsDiceRolled(true);
  };

  const handleWildcardOption = (hitOrMiss) => {
    setWildcardOption(false);
    if (hitOrMiss === DiceResult.HIT) {
      sendHit();
    } else {
      sendMiss();
    }
  };

  function titlecase(s) {
    return String(s).charAt(0).toUpperCase() + String(s).slice(1).toLowerCase();
  }

  return (
    <div>
      <div className="container">
        <div className="dice" style={{transform: diceTransform}}>
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
        {currentPlayer === clientPlayer && !isDiceRolled && (
          <button onClick={handleRollDice}>Roll dice</button>
        )}
      </div>

      {currentPlayer !== clientPlayer && (
        <div>{currentPlayer} is rolling the dice...</div>
      )}

      {displayHitOrMiss && <div className="dice-result">Result: ${titlecase(hitOrMiss)}</div>}

      {wildcardOption && currentPlayer === clientPlayer && (
        <div className="wildcard">
          <div className="wildcard-content">
            <h2>Wildcard!</h2>
            <p>Hit or Miss?</p>
            <button onClick={() => handleWildcardOption(DiceResult.HIT)}>Hit</button>
            <button onClick={() => handleWildcardOption(DiceResult.MISS)}>Miss</button>
          </div>
        </div>
      )}
    </div>
  );
}
