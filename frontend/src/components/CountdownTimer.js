import React, {useEffect, useState} from 'react';
import {buildStyles, CircularProgressbar} from 'react-circular-progressbar';
import 'react-circular-progressbar/dist/styles.css';

function CountdownTimer({ onTimeout }) {
    // Seconds for main countdown.
    const countdownDuration = window['useTestTimeouts'] ? 0.1 : 30

    // Seconds between "Ready", "Set" and "Go".
    const phaseInterval = window['useTestTimeouts'] ? 0.1 : 2

    const [phase, setPhase] = useState('ready'); // ready / set / go / timeout
    const [secondsLeft, setSecondsLeft] = useState(countdownDuration);

    function tick() {
        setSecondsLeft(s => s - 1);
    }

    // Converts seconds to millis.
    const seconds = (n) => n * 1000;

    useEffect(() => {
        if (phase === 'ready') {
            setTimeout(() => setPhase('set'), seconds(phaseInterval)); // "Ready" phase
            setTimeout(() => setPhase('go'), seconds(phaseInterval * 2)); // "Set" phase
            setTimeout(() => {
                setPhase('timeout')
                onTimeout()
            }, seconds(countdownDuration + (phaseInterval * 2))); // "Go" phase
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
        }
    }

    return (
        <div>
            <h2>Countdown!</h2>
            <div className="message">{getPhaseMessage()}</div>
            {conditionalDisplay()}
        </div>
    );
}

export default CountdownTimer;
