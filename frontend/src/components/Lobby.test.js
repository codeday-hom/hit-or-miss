import {act, fireEvent, render, screen, waitFor, within} from '@testing-library/react';
import React from "react";
import Lobby from "./Lobby";
import {MemoryRouter, Route, Routes} from "react-router-dom";
import Cookies from "js-cookie";
import {WsMessageType} from "../websockets/WsMessageType";

// Tests should fail if there are any warnings
global.console.warn = (message) => {
    throw message
}

// Mock web socket connection
let webSocketGameId = null;
let webSocketOnMessageFunction = null;
jest.mock("../websockets/useGameWebSocket", () => function (gameId, onMessageFunction) {
    webSocketGameId = gameId;
    webSocketOnMessageFunction = onMessageFunction;
})
function receiveWebSocketMessage(message) {
    act(() => webSocketOnMessageFunction(message))
}
function otherPlayersJoin(otherPlayerNames) {
    receiveWebSocketMessage({type: WsMessageType.USER_JOINED, data: otherPlayerNames})
}
function gameStarted() {
    receiveWebSocketMessage({type: WsMessageType.GAME_START, data: "someone"})
}

// Mock HTTP responses
let isHost = null
let gameId = "abcdef"
let requests = []
function stubFetch() {
    global.fetch = jest.fn((url, options) => {
        requests.push({ url, options })
        if (isHost) {
            Cookies.set("game_host", gameId)
        }
        return Promise.resolve({
            json: () => Promise.resolve({})
        })
    });
}

// Clear the cookie that is used by the Lobby component. There is only one.
function clearCookies() {
    Cookies.remove("game_host")
}

// Before each test:
// - Set isHost to its default value in these tests: true.
// - Ensure that http response mocking is in place.
// - Ensure that cookies from previous test runs are cleared.
beforeEach(() => {
    isHost = true
    stubFetch()
    clearCookies()
})

// The component is rendered within a router, because this is important to the component's behaviour.
function renderLobby() {
    render(
      <MemoryRouter initialEntries={[{pathname: `/game/${gameId}/lobby`}]}>
          <Routes>
              <Route path={"/game/:gameId/lobby"} element={<Lobby/>}/>

              {/* This route is set up to capture the case where the game is started and players are redirected. */}
              <Route path={"/game/:gameId"} element={<div>The client was redirected to the game screen</div>}/>
          </Routes>
      </MemoryRouter>
    )
}

// Simulates the client choosing their name in the lobby and clicking the "Save" button.
// This function is async because it needs to wait for the state update following the button click.
async function enterName(name, awaitSuccess = true) {
    const nameInput = screen.getByPlaceholderText(/Choose your name/i)
    fireEvent.change(nameInput, {target: {value: name}})

    const saveNameButton = screen.getByText(/Save/i)
    fireEvent.click(saveNameButton)
    if (awaitSuccess) {
        await screen.findByText(`Your name: ${name}`)
    }
}

test('renders page header', async () => {
    renderLobby()

    expect(screen.getByText(/Welcome to the Game Lobby!/i)).toBeInTheDocument();
});

test('connects to websocket with provided game id', async () => {
    renderLobby()

    expect(webSocketGameId).toBe(gameId)
});

test('shows your name after entering it', async () => {
    renderLobby()

    await enterName("Zuno")

    expect(screen.getByText(/Your name: Zuno/i)).toBeInTheDocument()
});

test('sends your name to the server after entering it', async () => {
    renderLobby()

    await enterName("Zuno")

    const request = requests.filter(it => it.url === `/api/join-game/${gameId}`).at(0)
    const requestBody = JSON.parse(request.options.body)
    expect(requestBody.gameId).toEqual(gameId)
    expect(requestBody.username).toEqual("Zuno")
});

test('shows invalid name warning if name is empty', async () => {
    renderLobby()

    await enterName("", false)

    expect(screen.getByText(/Please enter a valid name/i)).toBeInTheDocument()
});

test('shows invalid name warning if name is already taken', async () => {
    renderLobby()

    otherPlayersJoin(["Rob"])

    await enterName("Rob", false)

    expect(screen.getByText(/This username is already taken/i)).toBeInTheDocument()
});

test('new players are shown as they join', async () => {
    renderLobby()

    otherPlayersJoin(["Grace", "Ian"])

    const otherPlayersList = screen.getByRole("list", {name: /other-players/i})
    expect(otherPlayersList).toBeInTheDocument()

    await waitFor(() => expect(within(otherPlayersList)
        .getAllByRole("listitem")
        .map(item => item.textContent.trim()))
        .toEqual(["Grace", "Ian"]))
});

test('start game button is shown', async () => {
    renderLobby()

    await enterName("Zuno")
    otherPlayersJoin(["Grace", "Ian"])

    const startGameButton = await screen.findByText(/Start Game/i)
    expect(startGameButton).toBeInTheDocument()
});

test('start game button is not shown if not the host', async () => {
    isHost = false
    renderLobby()

    await enterName("Zuno")
    otherPlayersJoin(["Grace", "Ian"])

    const startGameButton = screen.queryByText(/Start Game/i)
    expect(startGameButton).toBeNull()
});

test('clicking the start game button causes a request to the server', async () => {
    renderLobby()

    await enterName("Zuno")
    otherPlayersJoin(["Grace", "Ian"])

    const startGameButton = await screen.findByText(/Start Game/i)
    fireEvent.click(startGameButton)

    expect(requests.map(it => it.url)).toContain(`/api/start-game/${gameId}`)
});

test('client is redirected when the game starts', async () => {
    renderLobby()

    gameStarted()

    expect(screen.getByText(/The client was redirected to the game screen/i)).toBeInTheDocument()
});
