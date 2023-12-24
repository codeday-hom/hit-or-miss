import "../Dice.css";
import {useEffect, useState} from "react";

export default function StandaloneDice() {

  const [diceTransform, setDiceTransform] = useState("");
  const [wildcardOption, setWildcardOption] = useState(false);
  const [diceResult, setDiceResult] = useState();
  const [hitOrMiss, setHitOrMiss] = useState("");
  const [displayHitOrMiss, setDisplayHitOrMiss] = useState(false);

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
        setHitOrMiss("Hit");
      } else {
        setHitOrMiss("Miss");
      }
    }
  }, [diceResult]);

  const displayHitOrMissWithDelay = () => {
    const delayMillis = window['useTestTimeouts'] ? 100 : 1500
    const timer = setTimeout(() => {
      setDisplayHitOrMiss(true);
    }, delayMillis);
    return () => clearTimeout(timer);
  };

  useEffect(() => {
    if (hitOrMiss) displayHitOrMissWithDelay();
    else setDisplayHitOrMiss(false);
  }, [hitOrMiss]);

  const handleRollDice = () => {
    // noinspection JSCheckFunctionSignatures
    setDiceResult(1 + Math.floor(Math.random() * 6));
  };

  const handleWildcardOption = (hitOrMiss) => {
    setWildcardOption(false);
    if (hitOrMiss === "Hit") {
      setHitOrMiss("Hit");
    } else {
      setHitOrMiss("Miss");
    }
  };

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
        <button onClick={handleRollDice}>Roll dice</button>
      </div>

      {displayHitOrMiss && (<div className="dice-result">Result: {hitOrMiss}</div>)}

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
