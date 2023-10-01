import React, {useState} from "react";
import {useLocation, useParams} from "react-router-dom";
import useGameWebSocket from "../websockets/useGameWebSocket";
import useWebsocketHeartbeat from "../websockets/useWebsocketHeartbeat";
import {WsMessageType} from "../websockets/WsMessageType";
import SelectCategoryPage from "./CategorySelection";
import WaitForCountdownPage from "./Countdown";
import RollDicePage from "./RollDicePage";
import SelectWordPage from "./SelectWordPage";

const GamePhase = {
    SELECT_CATEGORY: "CATEGORY_SELECTION",
    WAIT_FOR_COUNTDOWN: "COUNTDOWN",
    ROLL_DICE: "DICE_ROLLING",
    SELECT_WORD: "WORD_SELECTION",
    SELECT_HIT_OR_MISS: "SELECT_HIT_OR_MISS"

    // Add more, as more is implemented
};

export default function Game() {
  const { gameId } = useParams();
  const location = useLocation();
  const clientUsername = location.state.clientUsername;
  const initialPlayer = location.state.currentPlayer;
  const [currentPlayer, setCurrentPlayer] = useState(initialPlayer);
  const [gamePhase, setGamePhase] = useState(GamePhase.SELECT_CATEGORY);

  const [currentSelectedCategory, setCurrentSelectedCategory] = useState(null);
  const [diceResult, setDiceResult] = useState("");
  const [selectedWord, setSelectedWord] = useState("");

  const { sendMessage } = useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageType.NEXT_PLAYER) {
      setCurrentPlayer(message.data);
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
    if (gamePhase === GamePhase.SELECT_CATEGORY) {
      return <SelectCategoryPage gameId={gameId} currentPlayer={currentPlayer} clientUsername={clientUsername}
                                 onCategoryChosen={(category) => {
                                     setCurrentSelectedCategory(category);
                                     setGamePhase(GamePhase.WAIT_FOR_COUNTDOWN)
                                 }}/>
    } else if (gamePhase === GamePhase.WAIT_FOR_COUNTDOWN) {
      return <WaitForCountdownPage currentSelectedCategory={currentSelectedCategory}
                                   onTimeout={() => {
                                       setGamePhase(GamePhase.ROLL_DICE)
                                   }}/>
    } else if (gamePhase === GamePhase.ROLL_DICE) {
        return <RollDicePage gameId={gameId} currentPlayer={currentPlayer} clientUsername={clientUsername}
                             currentSelectedCategory={currentSelectedCategory}
                             onDiceResult={(diceResult => {
                                 setDiceResult(diceResult)
                                 setGamePhase(GamePhase.SELECT_WORD)
                             })}/>
    } else if (gamePhase === GamePhase.SELECT_WORD) {
        return <SelectWordPage gameId={gameId} currentPlayer={currentPlayer} clientUsername={clientUsername}
                               currentSelectedCategory={currentSelectedCategory} diceResult={diceResult}
                               onWordSelected={(word) => {
                                   setSelectedWord(word)
                                   setGamePhase(GamePhase.SELECT_HIT_OR_MISS)
                               }}/>
    } else if (gamePhase === GamePhase.SELECT_HIT_OR_MISS) {
        return (
            <div>
                <h1>WIP</h1>
                <div>Category: {currentSelectedCategory}</div>
                <div>Dice result: {diceResult}</div>
                <div>Word: {selectedWord}</div>
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
