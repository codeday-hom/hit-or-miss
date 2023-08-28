import React, { useState, useEffect, useRef } from 'react';
import { CircularProgressbar, buildStyles } from 'react-circular-progressbar';
import 'react-circular-progressbar/dist/styles.css';

const red = '#f54e4e';
const green = '#4aec8c';

function CountdownTimer() {
    const [phase, setPhase] = useState('ready'); // ready / set / go / timeout
    const [secondsLeft, setSecondsLeft] = useState(30);

    const secondsLeftRef = useRef(secondsLeft);

    function tick() {
        secondsLeftRef.current--;
        setSecondsLeft(secondsLeftRef.current);
    }

    useEffect(() => {
        if (phase === 'ready') {
            setTimeout(() => setPhase('set'), 1000); // "Ready" phase
            setTimeout(() => setPhase('go'), 2000); // "Set" phase
            setTimeout(() => {
                setPhase('timeout');
                secondsLeftRef.current = 30; // Reset countdown for the next round
            }, 3000);
        }

        if (phase === 'timeout' && secondsLeftRef.current > 0) {
            const interval = setInterval(() => {
                tick();
                if (secondsLeftRef.current === 0) {
                    clearInterval(interval);
                    setPhase('ready'); // Reset the phase for the next round
                }
            }, 1000);

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
                return 'Time\'s up!';
            default:
                return '';
        }
    };

    const percentage = Math.round((30 - secondsLeft) / 30 * 100);
    let seconds = secondsLeft % 60;
    if (seconds < 10) seconds = '0' + seconds;

    return (
        <div>
            <div className="message">{getPhaseMessage()}</div>
            {phase === 'timeout' ? (
                <div className="timer">
                    <CircularProgressbar
                        value={percentage}
                        text={seconds < 10 ? '0' + seconds : seconds}
                        styles={buildStyles({
                            textColor: '#fff',
                            pathColor: red,
                            tailColor: 'rgba(255,255,255,.2)',
                        })}
                    />
                </div>
            ) : null}
        </div>
    );
}

export default CountdownTimer;
