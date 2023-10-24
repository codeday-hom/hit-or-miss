import {act, render, screen} from '@testing-library/react';
import React from "react";
import GameContext from "./GameContext";
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
let webSocketOnMessageFunction = null;
let sentMessages = []
jest.mock("../websockets/useGameWebSocket", () => function (gameId, onMessageFunction) {
  webSocketGameId = gameId;
  webSocketOnMessageFunction = onMessageFunction;
  return {sendMessage: (m) => sentMessages.push(m)}
})

function receiveWebSocketMessage(message) {
  act(() => webSocketOnMessageFunction(message))
}

let gameId = "abcdef"

// The component is rendered within a router, because this is important to the component's behaviour.
function renderGame() {
  render(
    <MemoryRouter initialEntries={[{
      pathname: `/game/${gameId}`,
      state: {
        clientUsername: "Alice",
        currentPlayer: "Bob",
        playerNames: ["Alice", "Bob", "Charlie"]
      }
    }]}>
      <Routes>
        <Route path={"/game/:gameId"} element={<GameContext/>}/>
      </Routes>
    </MemoryRouter>
  )
}

function categorySelected(category) {
  receiveWebSocketMessage({type: WsMessageType.CATEGORY_SELECTED, data: category})
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

test('renders page header', async () => {
  renderGame()

  expect(screen.getByText(/Hit or Miss!/i)).toBeInTheDocument();
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

  await screen.findByText(/Bob is rolling the dice.../i)
});

test('fourth state is word selection', async () => {
  window['useTestTimeouts'] = true
  renderGame()

  categorySelected("Types of Category")
  await waitForCountdown()
  await rollDiceAndGetHit()

  await screen.findByText(/Bob is choosing a word.../i)
});
