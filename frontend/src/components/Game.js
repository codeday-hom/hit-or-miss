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
import {GamePhase} from "./GamePhase";

export default function Game({gameId, clientPlayer, initialPlayer, players, initialPhase, phaseData}) {
  const [currentPlayer, setCurrentPlayer] = useState(initialPlayer);
  const [gamePhase, setGamePhase] = useState(initialPhase);

  const [currentSelectedCategory, setCurrentSelectedCategory] = useState(phaseData.category ? phaseData.category : null);
  const [countdownTimerStart, setCountdownTimerStart] = useState(phaseData.countdownTimerStart ? new Date(phaseData.countdownTimerStart) : null);
  const [diceResult, setDiceResult] = useState(phaseData.diceResult ? phaseData.diceResult : "");
  const [selectedWord, setSelectedWord] = useState(phaseData.selectedWord ? phaseData.selectedWord : "");
  const [scores, setScores] = useState(phaseData.scores ? phaseData.scores : players.map(playerId => ({playerId: playerId, score: 0})));
  const [disconnectedPlayers, setDisconnectedPlayers] = useState([]);

  const {sendMessage} = useGameWebSocket(gameId, clientPlayer, (message) => {
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
    } else if (message.type === WsMessageType.USER_DISCONNECTED) {
      setDisconnectedPlayers(l => l.concat([message.data]))
    } else if (message.type === WsMessageType.USER_RECONNECTED) {
      setDisconnectedPlayers(l => l.filter(p => p !== message.data))
    }
  });


  useWebsocketHeartbeat(gameId, clientPlayer, sendMessage);

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
      return <SelectCategoryPage gameId={gameId} currentPlayer={currentPlayer} clientPlayer={clientPlayer}
                                 onCategorySelected={(categorySelection) => {
                                   setCurrentSelectedCategory(categorySelection.category);
                                   setCountdownTimerStart(new Date(categorySelection.countdownTimerStart))
                                   setGamePhase(GamePhase.WAIT_FOR_COUNTDOWN)
                                 }}/>
    } else if (gamePhase === GamePhase.WAIT_FOR_COUNTDOWN) {
      return <WaitForCountdownPage currentSelectedCategory={currentSelectedCategory} countdownTimerStart={countdownTimerStart}
                                   onTimeout={() => {
                                     setGamePhase(GamePhase.ROLL_DICE)
                                   }}/>
    } else if (gamePhase === GamePhase.ROLL_DICE) {
      return <RollDicePage gameId={gameId} currentPlayer={currentPlayer} clientPlayer={clientPlayer}
                           currentSelectedCategory={currentSelectedCategory}
                           onDiceResult={(diceResult => {
                             setDiceResult(diceResult)
                             setGamePhase(GamePhase.SELECT_WORD)
                           })}/>
    } else if (gamePhase === GamePhase.SELECT_WORD) {
      return <SelectWordPage gameId={gameId} currentPlayer={currentPlayer} clientPlayer={clientPlayer}
                             currentSelectedCategory={currentSelectedCategory} diceResult={diceResult}
                             onWordSelected={(word) => {
                               setSelectedWord(word)
                               setGamePhase(GamePhase.SELECT_HIT_OR_MISS)
                             }}/>
    } else if (gamePhase === GamePhase.SELECT_HIT_OR_MISS) {
      return <HitOrMissButtonPage gameId={gameId} currentSelectedCategory={currentSelectedCategory}
                                  diceResult={diceResult} selectedWord={selectedWord}
                                  currentPlayer={currentPlayer} clientPlayer={clientPlayer}
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
        .map(p => p.playerId)
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
        <Scoreboard clientPlayer={clientPlayer} scores={scores} disconnectedPlayers={disconnectedPlayers}/>
      </div>
    </div>
  );
}
