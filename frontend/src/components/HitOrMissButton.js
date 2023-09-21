import React, { useState, useEffect } from 'react';
import {WsMessageTypes} from "../constants/wsMessageTypes";
import useGameWebSocket from "../hooks/useGameWebSocket"

export default function HitOrMissButton({ gameId, clientUsername, currentPlayer }) {
    const [selectedWord, setSelectedWord] = useState(null);
    const [wordStatus, setWordStatus] = useState(null);
    const [message, setMessage] = useState("");
    const [updatedScore, setUpdatedScore] = useState();
    const { sendMessage } = useGameWebSocket(gameId, message => {
        if (message.type === WsMessageTypes.PLAYER_CHOSE_HIT_OR_MISS) {
            setUpdatedScore(message.data[clientUsername]);
        }
    });

    const handleHitOrMiss = (status) => {
        setWordStatus(status);
        sendMessage(JSON.stringify({type: WsMessageTypes.PLAYER_CHOSE_HIT_OR_MISS, data: JSON.stringify({username: clientUsername, choice: status})}));
    }

    const hitOrMissButton = () => {
        if (currentPlayer != clientUsername) {
            if (!wordStatus) {
                return <div>
                    <button onClick={() => handleHitOrMiss("HIT")}>
                       Hit
                       <p><small>I have the word</small></p>
                   </button>
                   <button onClick={() => handleHitOrMiss("MISS")}>
                       Miss
                       <p><small>"I don't have the word"</small></p>
                   </button>
               </div>
           } else if (!updatedScore) { return <p>"Waiting for other users to select hit or miss..."</p> }
        } else {
            return null
        }
    }

    return (
        <div>
           <h2> Choose Hit Or Miss </h2>
           {hitOrMissButton()}
           {wordStatus ? <p>Your picked: {wordStatus}</p> : null}
           {updatedScore? <p>Your score is: {updatedScore}</p> : <p>Your score is: 0</p>}
        </div>
    );
}