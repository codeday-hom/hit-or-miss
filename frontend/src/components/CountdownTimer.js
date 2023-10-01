import React, {useEffect, useState} from 'react';
import {buildStyles, CircularProgressbar} from 'react-circular-progressbar';
import 'react-circular-progressbar/dist/styles.css';

function CountdownTimer({ onTimeout }) {
    const countdownDuration = 5

    const [phase, setPhase] = useState('ready'); // ready / set / go / timeout
    const [secondsLeft, setSecondsLeft] = useState(countdownDuration);

    function tick() {
        setSecondsLeft(s => s - 1);
    }

    // Converts seconds to millis.
    const seconds = (n) => n * 1000;

    useEffect(() => {
        if (phase === 'ready') {
            setTimeout(() => setPhase('set'), seconds(1)); // "Ready" phase
            setTimeout(() => setPhase('go'), seconds(2)); // "Set" phase
            setTimeout(() => setPhase('timeout'), seconds(countdownDuration + 2)); // "Go" phase
        }

        if (phase === 'go') {
            const interval = setInterval(() => tick(), seconds(1));
            return () => clearInterval(interval);
        }
    }, [phase]);

    const getPhaseMessage = () => {
        switch (phase) {
            case 'ready':
                return 'Ready...';
            case 'set':
                return 'Set...';
            case 'go':
                return 'Go!';
            case 'timeout':
                return "Time's up!";
            default:
                return '';
        }
    };

    const percentage = Math.round((countdownDuration - secondsLeft) / countdownDuration * 100);

    function conditionalDisplay() {
        if (phase === 'go') {
            return (
                <div className="timer">
                    <CircularProgressbar
                        value={percentage}
                        text={secondsLeft.toString()}
                        styles={buildStyles({
                            textColor: '#fff',
                            pathColor: '#f4d35e',
                            tailColor: '#fff'
                        })}
                    />
                </div>
            )
        } else if (phase === 'timeout') {
            return <button onClick={onTimeout}>Continue</button>
        }
    }

    return (
        <div>
            <div className="message">{getPhaseMessage()}</div>
            {conditionalDisplay()}
        </div>
    );
}

export default CountdownTimer;
