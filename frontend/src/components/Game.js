import React, { useState, useEffect } from "react";
import { useLocation, useParams } from "react-router-dom";
import useGameWebSocket from "../hooks/useGameWebSocket";
import useWebsocketHeartbeat from "../hooks/useWebsocketHeartbeat";
import CategoryPicker from "./CategoryPicker";
import { WsMessageTypes } from "../constants/wsMessageTypes";
import Dice from "./Dice";
import WordList from "./Wordlist";
import CountdownTimer from "./CountdownTimer";
import {GamePhases} from "../constants/gamePhases";

export default function Game() {
  const { gameId } = useParams();
  const location = useLocation();
  const clientUsername = location.state.clientUsername;
  const initialPlayer = location.state.currentPlayer;
  const [currentPlayer, setCurrentPlayer] = useState(initialPlayer);
  const [gamePhase, setGamePhase] = useState(GamePhases.CATEGORY_SELECTION);

  const [currentSelectedCategory, setCurrentSelectedCategory] = useState(null);
  const [isCountdownFinished, setIsCountdownFinished] = useState(false);
  const [diceResult, setDiceResult] = useState("");
  const [selectedWord, setSelectedWord] = useState("");

  const { sendMessage } = useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageTypes.NEXT_PLAYER) {
      setCurrentPlayer(message.data);
    } else if (message.type === WsMessageTypes.CATEGORY_CHOSEN) {
      setCurrentSelectedCategory(message.data);
      setGamePhase(GamePhases.COUNTDOWN)
    } else if (message.type === WsMessageTypes.SELECTED_WORD) {
      setSelectedWord(message.data);
    }
  });

  useWebsocketHeartbeat(sendMessage);

  /**
   * At the start of the game, there is a player whose turn is first.
   * 1. Category selection: This player first selects a category for the round.
   * 2. Countdown: Once everyone is aware of the selected category, they all get 30 seconds to write down as many words as they can.
   *
   * After the countdown, the game enters a round-based structure using the fixed category. The player who chose the category goes first.
   * 1. Dice rolling: When the 30-second countdown is finished, the player whose turn it is, rolls the dice for all to see.
   * 2. Word selection: The player whose turn it is then chooses a word from their list, taking their dice roll into account of course.
   * 3. Hit or miss reporting: All the players see the word chosen, and report whether they have the word or not.
   * 4. Scores shown: Scores are tallied up and shown to all players before the start of the next round.
   *
   * These steps repeat for all players until every player has done a round for this category.
   * Then, it becomes the next player's turn to select a category.
   * This continues until a round has been played for each player selecting a category, or until everyone gets bored.
   */
  function conditionalGameState() {
    if (gamePhase === GamePhases.CATEGORY_SELECTION) {
      return (
          <div>
            {currentPlayer === clientUsername
                ? <CategoryPicker gameId={gameId} />
                : <p>{currentPlayer} is choosing a category</p>}
          </div>
      )
    } else if (gamePhase === GamePhases.COUNTDOWN) {
      return (
          <div>
            <p>Current category is: {currentSelectedCategory}</p>
            {!isCountdownFinished ? (
                <CountdownTimer onTimeout={() => {
                    setIsCountdownFinished(true)
                    setGamePhase(GamePhases.DICE_ROLLING)
                }}/>
            ) : null}
          </div>
      )
    } else if (gamePhase === GamePhases.DICE_ROLLING) {
        return (
            <div>
                <p>Current category is: {currentSelectedCategory}</p>
                <Dice
                    gameId={gameId}
                    currentPlayer={currentPlayer}
                    clientUsername={clientUsername}
                    onDiceResult={(diceResult) => {
                        setDiceResult(diceResult)
                        setGamePhase(GamePhases.WORD_SELECTION)
                    }}
                />
            </div>
        )
    } else if (gamePhase === GamePhases.WORD_SELECTION) {
        return (
            <div>
                <p>Current category is: {currentSelectedCategory}</p>
                <p>{currentPlayer === clientUsername ? "You" : currentPlayer} rolled the dice and got: {diceResult}</p>
                <div>Current word: {selectedWord}</div>
                {currentPlayer === clientUsername ? (
                    <WordList gameId={gameId} />
                ) : null}
                {/* TODO: Upon word selection, all players will be prompted to select "hit" or "miss". */}
            </div>
        )
    }
  }

  return (
    <div>
      <h1>Hit or Miss!</h1>
      {conditionalGameState()}
    </div>
  );
}
