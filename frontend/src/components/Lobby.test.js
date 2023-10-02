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

let stubGameId = null;
let stubOnMessageFunction = null;

jest.mock("../websockets/useGameWebSocket", () => function (gameId, onMessageFunction) {
    stubGameId = gameId;
    stubOnMessageFunction = onMessageFunction;
})

let isHost = null
let gameId = null

function clearCookies() {
    Cookies.remove("game_host")
}

function stubFetch() {
    global.fetch = jest.fn(() => {
        if (isHost) {
            Cookies.set("game_host", gameId)
        }
        return Promise.resolve({
            json: () => Promise.resolve({})
        })
    });
}

beforeEach(() => {
    isHost = true
    clearCookies()
    stubFetch()
})

function renderAtRoute(pathPattern, path, element) {
    render(
        <MemoryRouter initialEntries={[{pathname: path}]}>
            <Routes>
                <Route path={pathPattern} element={element}/>
            </Routes>
        </MemoryRouter>
    )
}

function renderLobby() {
    renderAtRoute("/game/:gameId/lobby", `/game/${gameId}/lobby`, <Lobby/>)
}

async function enterName(name) {
    const nameInput = screen.getByPlaceholderText(/Choose your name/i)
    fireEvent.change(nameInput, {target: {value: name}})

    const saveNameButton = screen.getByText(/Save/i)
    fireEvent.click(saveNameButton)
    await screen.findByText(`Your name: ${name}`)
}

test('renders page header', async () => {
    renderLobby()

    expect(screen.getByText(/Welcome to the Game Lobby!/i)).toBeInTheDocument();
});

test('connects to websocket with provided game id', async () => {
    gameId = "abcdef"
    renderLobby()

    expect(stubGameId).toBe(gameId)
});

test('shows your name after entering it', async () => {
    gameId = "abcdef"
    renderLobby()

    await enterName("Zuno")

    expect(screen.getByText(/Your name: Zuno/i)).toBeInTheDocument()
});

test('new players are shown as they join', async () => {
    gameId = "abcdef"
    renderLobby()

    act(() => stubOnMessageFunction({type: WsMessageType.USER_JOINED, data: {ignored1: "Grace", ignored2: "Ian"}}))

    const otherPlayersList = screen.getByRole("list", {name: /other-players/i})
    expect(otherPlayersList).toBeInTheDocument()

    await waitFor(() => expect(within(otherPlayersList)
        .getAllByRole("listitem")
        .map(item => item.textContent.trim()))
        .toEqual(["Grace", "Ian"]))
});

test('start game button is shown', async () => {
    gameId = "abcdef"
    renderLobby()

    await enterName("Zuno")
    act(() => stubOnMessageFunction({type: WsMessageType.USER_JOINED, data: {ignored1: "Grace", ignored2: "Ian"}}))

    const startGameButton = await screen.findByText(/Start Game/i)
    expect(startGameButton).toBeInTheDocument()
});

test('start game button is not shown if not the host', async () => {
    gameId = "abcdef"
    isHost = false
    renderLobby()

    await enterName("Zuno")
    act(() => stubOnMessageFunction({type: WsMessageType.USER_JOINED, data: {ignored1: "Grace", ignored2: "Ian"}}))

    const startGameButton = screen.queryByText(/Start Game/i)
    expect(startGameButton).toBeNull()
});
