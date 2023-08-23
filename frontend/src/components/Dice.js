import { useState } from "react";
import "./Dice.css";

function Dice() {
  const [diceState, setDiceState] = useState("");
  const [wildcardOption, setWildcardOption] = useState(false);

  function rollDice() {
    const diceResult = Math.floor(Math.random() * 6) + 1;
    console.log(diceResult);
    setDiceState("show-" + diceResult);
    if (diceResult === 6) {
      setWildcardOption(true);
    }
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
        <button onClick={rollDice}>Roll dice</button>
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

export default Dice;
