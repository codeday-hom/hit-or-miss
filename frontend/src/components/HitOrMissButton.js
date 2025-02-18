import React, {useState} from 'react';
import {WsMessageType} from "../websockets/wsMessageType";

export default function HitOrMissButton({gameId, clientUsername, currentPlayer, sendWebSocketMessage}) {
  const [wordStatus, setWordStatus] = useState(null);

  const handleHitOrMiss = (status) => {
    setWordStatus(status);
    sendWebSocketMessage(JSON.stringify({
      gameId,
      player: clientUsername,
      type: WsMessageType.PLAYER_CHOSE_HIT_OR_MISS,
      data: {hitOrMiss: status}
    }));
  }

  const hitOrMissButton = () => {
    if (currentPlayer === clientUsername || wordStatus) {
      return null
    }

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
  }

  return (
    <div>
      <h2> Choose Hit Or Miss </h2>
      {hitOrMissButton()}
      {wordStatus ? <p>You picked {wordStatus}</p> : null}
      {wordStatus ? <p>Waiting for all players to pick hit or miss...</p> : null}
    </div>
  );
}