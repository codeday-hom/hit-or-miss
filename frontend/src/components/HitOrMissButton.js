import React, { useState, useEffect } from 'react';
import {WsMessageTypes} from "../constants/wsMessageTypes";
import useGameWebSocket from "../hooks/useGameWebSocket"

export default function HitOrMissButton({ gameId, clientUsername, currentPlayer }) {
    const [selectedWord, setSelectedWord] = useState(null);
    const [wordStatus, setWordStatus] = useState(null);
    const [message, setMessage] = useState("");
    const { sendMessage } = useGameWebSocket(gameId, message => {
        if (message.type === "scoreUpdate") {
            setMessage(message.data);
        }
    });

//    useEffect(() => {
//        socket.onmessage = (event) => {
//            const data = JSON.parse(event.data);

//            if (data.type === "wordChosen") {
//                setSelectedWord(data.data.word);
//                setWordStatus(data.data.status);
//                setMessage('');
//            } else if (data.type === "scoreUpdate") {
//                setMessage(data.data);
//            }
//        };
//    }, []);

    const handleHitOrMiss = (status) => {
        setWordStatus(status);
        sendMessage(JSON.stringify({type: WsMessageTypes.PLAYER_CHOSE_HIT_OR_MISS, data: {status}}));
    }

    const hitOrMissButton = () => {
        if (currentPlayer != clientUsername) {
            if (!wordStatus) {
                return <div>
                    <button onClick={() => handleHitOrMiss("hit")}>
                       Hit
                       <p><small>I have the word</small></p>
                   </button>
                   <button onClick={() => handleHitOrMiss("miss")}>
                       Miss
                       <p><small>"I don't have the word"</small></p>
                   </button>
               </div>
           } else { return null }
        } else {
            return <p>"Waiting for other users to select hit or miss..."</p>
        }
    }

    return (
        <div>
           <h2> Choose Hit Or Miss </h2>
           {hitOrMissButton()}
           {wordStatus ? <p>Your picked: {wordStatus}</p> : null}
        </div>
    );
}