import CountdownTimer from "./CountdownTimer";
import React, {useState} from "react";

export default function Countdown({ currentSelectedCategory, onTimeout }) {
    const [isCountdownFinished, setIsCountdownFinished] = useState(false);

    return (
        <div>
            <p>Current category is: {currentSelectedCategory}</p>
            {!isCountdownFinished ? (
                <CountdownTimer onTimeout={() => {
                    setIsCountdownFinished(true)
                    onTimeout()
                }}/>
            ) : null}
        </div>
    )
}
