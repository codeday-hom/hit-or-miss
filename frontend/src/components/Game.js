import React, {useState} from "react";
import useGameWebSocket from "../websockets/useGameWebSocket";
import useWebsocketHeartbeat from "../websockets/useWebsocketHeartbeat";
import {WsMessageType} from "../websockets/WsMessageType";
import SelectCategoryPage from "./SelectCategoryPage";
import WaitForCountdownPage from "./WaitForCountdownPage";
import RollDicePage from "./RollDicePage";
import SelectWordPage from "./SelectWordPage";
import HitOrMissButtonPage from "./HitOrMissButtonPage";
import Scoreboard from "./Scoreboard";
import "./Game.css";
import EndGamePage from "./EndGamePage";

const GamePhase = {
  SELECT_CATEGORY: "CATEGORY_SELECTION",
  WAIT_FOR_COUNTDOWN: "COUNTDOWN",
  ROLL_DICE: "DICE_ROLLING",
  SELECT_WORD: "WORD_SELECTION",
  SELECT_HIT_OR_MISS: "SELECT_HIT_OR_MISS",
  GAME_OVER: "GAME_OVER"
};

export default function Game({gameId, clientUsername, initialPlayer, playerNames}) {
  const [currentPlayer, setCurrentPlayer] = useState(initialPlayer);
  const [gamePhase, setGamePhase] = useState(GamePhase.SELECT_CATEGORY);

  const [currentSelectedCategory, setCurrentSelectedCategory] = useState(null);
  const [diceResult, setDiceResult] = useState("");
  const [selectedWord, setSelectedWord] = useState("");
  const [scores, setScores] = useState(playerNames.map(name => ({username: name, score: 0})));

  const {sendMessage} = useGameWebSocket(gameId, (message) => {
    if (message.type === WsMessageType.NEXT_TURN) {
      setCurrentPlayer(message.data);
      setGamePhase(GamePhase.ROLL_DICE);
    } else if (message.type === WsMessageType.NEXT_ROUND) {
      setCurrentPlayer(message.data);
      setGamePhase(GamePhase.SELECT_CATEGORY);
    } else if (message.type === WsMessageType.SCORES) {
      setScores(message.data);
    } else if (message.type === WsMessageType.GAME_OVER) {
      setScores(message.data);
      setGamePhase(GamePhase.GAME_OVER)
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
                                 onCategorySelected={(category) => {
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
      return <HitOrMissButtonPage gameId={gameId} currentSelectedCategory={currentSelectedCategory}
                                  diceResult={diceResult} selectedWord={selectedWord}
                                  currentPlayer={currentPlayer} clientUsername={clientUsername}
                                  sendWebSocketMessage={sendMessage}/>
    } else if (gamePhase === GamePhase.GAME_OVER) {
      let winningPlayers = scores
        .reduce((accumulator, current) => {
            if (accumulator.length === 0) {
              return [current]
            } else if (current.score > accumulator[0].score) {
              return [current]
            } else if (current.score === accumulator[0].score) {
              return accumulator.concat([current])
            } else {
              return accumulator
            }
          },
          []
        )
        .map(p => p.username)
      return <EndGamePage winningPlayers={winningPlayers}/>
    }
  }

  return (
    <div className="game-container">
      <div className="game-content">
        <h1>Hit or Miss!</h1>
        {conditionalGameState()}
      </div>
      <div className="game-scoreboard">
        <Scoreboard clientUsername={clientUsername} scores={scores}/>
      </div>
    </div>
  );
}
