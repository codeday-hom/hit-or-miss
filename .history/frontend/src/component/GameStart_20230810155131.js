import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import useGameWebSocket from "../hook/useGameWebSocket";

export default function GameStart() {
  const { gameId } = useParams();
  const { userIds, usernames } = useGameWebSocket(gameId);

  return (
    <div>
      <h1>Game has started!</h1>
      <p>{usernames[0]} is choosing a category</p>
    </div>
  );
}
