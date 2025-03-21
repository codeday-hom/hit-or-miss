// noinspection JSCheckFunctionSignatures

import {act, fireEvent, prettyDOM, render, screen} from '@testing-library/react';
import React from "react";
import ContextualGame from "./ContextualGame";
import {MemoryRouter, Route, Routes} from "react-router-dom";
import {WsMessageType} from "../websockets/WsMessageType";

// Tests should fail if there are any warnings or errors
global.console.warn = (message) => {
  throw message
}
global.console.error = (message) => {
  throw message
}

// Mock web socket connection
let webSocketGameId = null;
let webSocketOnMessageFunctions = [];
let sentMessages = []
jest.mock("../websockets/useGameWebSocket", () => function (gameId, playerId, onMessageFunction) {
  webSocketGameId = gameId;
  webSocketOnMessageFunctions.push(onMessageFunction)
  // noinspection JSUnusedGlobalSymbols
  return {sendMessage: (m) => sentMessages.push(m)}
})

function receiveWebSocketMessage(message) {
  act(() => webSocketOnMessageFunctions.forEach(it => it(message)))
}

let gameId = "abcdef"

// The component is rendered within a router, because this is important to the component's behaviour.
function renderGame() {
  render(
    <MemoryRouter initialEntries={[{
      pathname: `/game/${gameId}`,
      state: {
        clientPlayer: "Alice",
        initialPlayer: "Bob",
        players: ["Alice", "Bob", "Charlie"]
      }
    }]}>
      <Routes>
        <Route path={"/game/:gameId"} element={<ContextualGame/>}/>
      </Routes>
    </MemoryRouter>
  )
}

let categorySelectionCountdownTimerStart = new Date()
function categorySelected(category) {
  receiveWebSocketMessage({type: WsMessageType.CATEGORY_SELECTED, data: {category, countdownTimerStart: categorySelectionCountdownTimerStart}})
}

async function waitForCountdown() {
  await act(() => new Promise((r) => setTimeout(r, 1000)))
}

async function rollDiceAndGetHit() {
  receiveWebSocketMessage({type: WsMessageType.ROLL_DICE_RESULT, data: 1})
  await act(() => new Promise((r) => setTimeout(r, 500)))

  receiveWebSocketMessage({type: WsMessageType.ROLL_DICE_HIT_OR_MISS, data: 'Hit'})
  await act(() => new Promise((r) => setTimeout(r, 500)))
}

async function wordSelected(word) {
  receiveWebSocketMessage({type: WsMessageType.SELECTED_WORD, data: word})
}

async function selectHit() {
  fireEvent.click(screen.getByRole('button', {name: /Hit/i}))
}

function allPlayersSelectedHitOrMiss(scores) {
  receiveWebSocketMessage({type: WsMessageType.SCORES, data: scores})
}

function gameOver(scores) {
  receiveWebSocketMessage({type: WsMessageType.GAME_OVER, data: scores})
}

function playerDisconnects(playerId) {
  receiveWebSocketMessage({type: WsMessageType.USER_DISCONNECTED, data: playerId})
}

function playerReconnects(playerId) {
  receiveWebSocketMessage({type: WsMessageType.USER_RECONNECTED, data: playerId})
}

function expectScoreboardRows(expectedRows) {
  const rows = screen.getAllByRole('row')

  function findScoreboardRowByContent(playerRowValue, scoreRowValue) {
    const row = rows.find((row) => {
      const playerRow = row.cells.item(0).textContent === playerRowValue
      const scoreRow = row.cells.item(1).textContent === scoreRowValue
      return playerRow && scoreRow
    })
    if (row === undefined) {
      console.error("Couldn't find row matching '" + playerRowValue + ", " + scoreRowValue + "'.\n\n" + prettyDOM())
    }
    return row
  }

  expect(findScoreboardRowByContent("Player", "Score")).toBeInTheDocument()
  expectedRows.forEach((row) => {
    expect(findScoreboardRowByContent(row.player, row.score)).toBeInTheDocument()
  })
}

test('renders page header', async () => {
  renderGame()

  expect(screen.getByText(/Hit or Miss!/i)).toBeInTheDocument();
});

test('renders scoreboard with all player names', async () => {
  renderGame()

  expectScoreboardRows([
    {player: "Alice (you)", score: "0"},
    {player: "Bob", score: "0"},
    {player: "Charlie", score: "0"},
  ])
});

test('first state is category selection', async () => {
  renderGame()

  expect(screen.getByText(/Bob is choosing a category/i)).toBeInTheDocument();
});

test('second state is countdown', async () => {
  renderGame()

  categorySelected("Types of Category")

  expect(screen.getByText(/Countdown!/i)).toBeInTheDocument();
});

test('third state is dice roll', async () => {
  window['useTestTimeouts'] = true

  renderGame()

  categorySelected("Types of Category")
  await waitForCountdown()

  expect(screen.getByText(/Bob is rolling the dice.../i)).toBeInTheDocument()
});

test('fourth state is word selection', async () => {
  window['useTestTimeouts'] = true
  renderGame()

  categorySelected("Types of Category")
  await waitForCountdown()
  await rollDiceAndGetHit()

  expect(screen.getByText(/Bob is choosing a word.../i)).toBeInTheDocument()
});

test('fifth state is hit or miss selection', async () => {
  window['useTestTimeouts'] = true
  renderGame()

  categorySelected("Types of Category")
  await waitForCountdown()
  await rollDiceAndGetHit()
  await wordSelected("Fun categories")

  expect(screen.getByText(/Category: Types of Category/i)).toBeInTheDocument()
  expect(screen.getByText(/Dice result: Hit/i)).toBeInTheDocument()
  expect(screen.getByText(/Word: Fun categories/i)).toBeInTheDocument()

  const buttons = screen.getAllByRole('button')

  function findButtonByContent(title, subtitle) {
    const button = buttons.find((btn) => {
      return btn.textContent.includes(title) && btn.textContent.includes(subtitle)
    })
    if (button === undefined) {
      console.error("Couldn't find button with title '" + title + "' and subtitle '" + subtitle + "'.")
    }
    return button
  }

  expect(findButtonByContent("Hit", "I have the word")).toBeInTheDocument()
  expect(findButtonByContent("Miss", "I don't have the word")).toBeInTheDocument()
});

test('wait for other players after hit or miss selection', async () => {
  window['useTestTimeouts'] = true
  renderGame()

  categorySelected("Types of Category")
  await waitForCountdown()
  await rollDiceAndGetHit()
  await wordSelected("Fun categories")
  await selectHit()

  expect(screen.getByText(/You picked HIT/i)).toBeInTheDocument()
  expect(screen.getByText(/Waiting for all players to pick hit or miss.../i)).toBeInTheDocument()
});

test('scoreboard updates after hit or miss selection', async () => {
  window['useTestTimeouts'] = true
  renderGame()

  categorySelected("Types of Category")
  await waitForCountdown()
  await rollDiceAndGetHit()
  await wordSelected("Fun categories")
  await selectHit()
  allPlayersSelectedHitOrMiss([
    {playerId: "Alice", score: 1},
    {playerId: "Bob", score: 1},
    {playerId: "Charlie", score: 0}
  ])

  expectScoreboardRows([
    {player: "Alice (you)", score: "1"},
    {player: "Bob", score: "1"},
    {player: "Charlie", score: "0"}
  ])
});

test('scoreboard shows disconnected players', async () => {
  renderGame()

  playerDisconnects("Bob")

  expectScoreboardRows([
    {player: "Alice (you)", score: "0"},
    {player: "Bob (disconnected)", score: "0"},
    {player: "Charlie", score: "0"},
  ])
});

test('scoreboard shows reconnected players', async () => {
  renderGame()

  playerDisconnects("Charlie")

  expectScoreboardRows([
    {player: "Alice (you)", score: "0"},
    {player: "Bob", score: "0"},
    {player: "Charlie (disconnected)", score: "0"},
  ])

  playerReconnects("Charlie")

  expectScoreboardRows([
    {player: "Alice (you)", score: "0"},
    {player: "Bob", score: "0"},
    {player: "Charlie", score: "0"},
  ])
});

test('winner is shown once game is over', async () => {
  window['useTestTimeouts'] = true
  renderGame()

  gameOver([
    {playerId: "Alice", score: 2},
    {playerId: "Bob", score: 1},
    {playerId: "Charlie", score: 0}
  ])

  expectScoreboardRows([
    {player: "Alice (you)", score: "2"},
    {player: "Bob", score: "1"},
    {player: "Charlie", score: "0"}
  ])
  expect(screen.getByText(/🎉 Congratulations to the winner, ✨Alice ✨! 🎉/i)).toBeInTheDocument()
});

test('joint winners (2 of them) are shown once game is over', async () => {
  window['useTestTimeouts'] = true
  renderGame()

  gameOver([
    {playerId: "Alice", score: 2},
    {playerId: "Bob", score: 1},
    {playerId: "Charlie", score: 2}
  ])

  expectScoreboardRows([
    {player: "Alice (you)", score: "2"},
    {player: "Bob", score: "1"},
    {player: "Charlie", score: "2"}
  ])
  expect(screen.getByText(/🎉 Congratulations to the joint winners, ✨Alice ✨ and ✨Charlie ✨! 🎉/i)).toBeInTheDocument()
});

test('joint winners (3 of them) are shown once game is over', async () => {
  window['useTestTimeouts'] = true
  renderGame()

  gameOver([
    {playerId: "Alice", score: 2},
    {playerId: "Bob", score: 2},
    {playerId: "Charlie", score: 2}
  ])

  expectScoreboardRows([
    {player: "Alice (you)", score: "2"},
    {player: "Bob", score: "2"},
    {player: "Charlie", score: "2"}
  ])
  expect(screen.getByText(/🎉 Congratulations to the joint winners, ✨Alice ✨, ✨Bob ✨, ✨Charlie ✨! 🎉/i)).toBeInTheDocument()
});

