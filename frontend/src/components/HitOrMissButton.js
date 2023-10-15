import React, {useState} from 'react';
import {WsMessageType} from "../websockets/wsMessageType";
import useGameWebSocket from "../websockets/useGameWebSocket"

export default function HitOrMissButton({ gameId, clientUsername, currentPlayer }) {
    const [wordStatus, setWordStatus] = useState(null);
    const [updatedScore, setUpdatedScore] = useState();
    const { sendMessage } = useGameWebSocket(gameId, message => {
        if (message.type === WsMessageType.PLAYER_CHOSE_HIT_OR_MISS) {
            setUpdatedScore(message.data[clientUsername]);
        }
    });

    const handleHitOrMiss = (status) => {
        setWordStatus(status);
        sendMessage(JSON.stringify({type: WsMessageType.PLAYER_CHOSE_HIT_OR_MISS, data:{username: clientUsername, data: status}}));
    }

    const hitOrMissButton = () => {
        if (currentPlayer !== clientUsername) {

        } else {
            return null
        }

        if (currentPlayer === clientUsername) {
            return null
        }

        if (!wordStatus) {
            return <div>
                <button onClick={() => handleHitOrMiss("HIT")}>
                    Hit
                    <p><small>I have the word</small></p>
                </button>
                <button onClick={() => handleHitOrMiss("MISS")}>
                    Miss
                    <p><small>I don't have the word</small></p>
                </button>
            </div>
        } else if (!updatedScore) { return <p>"Waiting for other users to select hit or miss..."</p> }
    }

    return (
        <div>
           <h2> Choose Hit Or Miss </h2>
           {hitOrMissButton()}
           {wordStatus ? <p>You picked: {wordStatus}</p> : null}
           {updatedScore? <p>Your score is: {updatedScore}</p> : <p>Your score is: 0</p>}
        </div>
    );
}