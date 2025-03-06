import CountdownTimer from "./CountdownTimer";
import React, {useState} from "react";

export default function WaitForCountdownPage({currentSelectedCategory, countdownTimerStart, onTimeout}) {
  const [isCountdownFinished, setIsCountdownFinished] = useState(false);

  return (
    <div>
      <p>Current category is: {currentSelectedCategory}</p>
      {!isCountdownFinished ? (
        <CountdownTimer countdownTimerStart={countdownTimerStart} onTimeout={() => {
          setIsCountdownFinished(true)
          onTimeout()
        }}/>
      ) : null}
    </div>
  )
}
