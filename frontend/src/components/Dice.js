import { useState } from "react";
import "./Dice.css";

function Dice() {
  const [diceState, setDiceState] = useState("");

  function rollDice() {
    const diceResult = Math.floor(Math.random() * 6) + 1;
    console.log(diceResult);
    setDiceState("show-" + diceResult);
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
            <div className="miss-text">Miss</div>
          </div>
        </div>
      </div>
      <div className="roll-button">
        <button onClick={rollDice}>Roll dice</button>
      </div>
    </div>
  );
}

export default Dice;
