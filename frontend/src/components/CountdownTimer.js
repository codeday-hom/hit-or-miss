import React, {useEffect, useState} from 'react';
import {buildStyles, CircularProgressbar} from 'react-circular-progressbar';
import 'react-circular-progressbar/dist/styles.css';

function CountdownTimer({countdownTimerStart, onTimeout}) {

  const secondsToMillis = (n) => n * 1000;

  const countdownDurationSeconds = window['useTestTimeouts'] ? 0.1 : 30
  const initialSecondsLeft = Math.max(1, Math.floor(((countdownTimerStart.getTime() + (secondsToMillis(countdownDurationSeconds))) - new Date().getTime()) / 1000))

  // Seconds between "Ready", "Set" and "Go".
  const phaseInterval = window['useTestTimeouts'] ? 0.1 : 2

  const [phase, setPhase] = useState('ready'); // ready / set / go / timeout
  const [secondsLeft, setSecondsLeft] = useState(initialSecondsLeft);

  function tick() {
    setSecondsLeft(s => s - 1);
  }

  useEffect(() => {
    if (phase === 'ready') {
      setTimeout(() => setPhase('set'), secondsToMillis(phaseInterval)); // "Ready" phase
      setTimeout(() => setPhase('go'), secondsToMillis(phaseInterval * 2)); // "Set" phase
      setTimeout(() => {
        setPhase('timeout')
        onTimeout()
      }, secondsToMillis(initialSecondsLeft + (phaseInterval * 2))); // "Go" phase
    }

    if (phase === 'go') {
      const interval = setInterval(() => tick(), secondsToMillis(1));
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

  const percentage = Math.round((countdownDurationSeconds - secondsLeft) / countdownDurationSeconds * 100);

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
