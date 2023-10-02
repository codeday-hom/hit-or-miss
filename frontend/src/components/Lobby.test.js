import {act, fireEvent, render, screen, waitFor, within} from '@testing-library/react';
import React from "react";
import Lobby from "./Lobby";
import {MemoryRouter, Route, Routes} from "react-router-dom";
import Cookies from "js-cookie";
import {WsMessageType} from "../websockets/WsMessageType";

let stubGameId = null;
let stubOnMessageFunction = null;

jest.mock("../websockets/useGameWebSocket", () => function (gameId, onMessageFunction) {
    stubGameId = gameId;
    stubOnMessageFunction = onMessageFunction;
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

function renderLobby(gameId) {
    renderAtRoute("/game/:gameId/lobby", `/game/${gameId}/lobby`, <Lobby/>)
}

test('renders page header', async () => {
    renderLobby("foo")

    expect(screen.getByText(/Welcome to the Game Lobby!/i)).toBeInTheDocument();
});

test('connects to websocket with provided game id', async () => {
    const gameId = "abcdef"
    renderLobby(gameId)

    expect(stubGameId).toBe(gameId)
});

function stubFetch(gameId) {
    global.fetch = jest.fn(() => {
        Cookies.set("game_host", gameId)
        return Promise.resolve({
            json: () => Promise.resolve({})
        })
    });
}

function enterName(name) {
    const nameInput = screen.getByPlaceholderText(/Choose your name/i)
    fireEvent.change(nameInput, {target: {value: name}})

    const saveNameButton = screen.getByText(/Save/i)
    fireEvent.click(saveNameButton)
}

test('shows your name after entering it', async () => {
    const gameId = "abcdef"
    stubFetch(gameId)

    renderLobby(gameId)

    enterName("Zuno")

    await waitFor(() => expect(screen.getByText(/Your name: Zuno/i)).toBeInTheDocument())
});

test('new players are shown as they join', async () => {
    const gameId = "abcdef"
    stubFetch(gameId)

    renderLobby(gameId)

    act(() => stubOnMessageFunction({type: WsMessageType.USER_JOINED, data: {ignored1: "Grace", ignored2: "Ian"}}))

    const otherPlayersList = screen.getByRole("list", {name: /other-players/i})
    expect(otherPlayersList).toBeInTheDocument()

    await waitFor(() => expect(within(otherPlayersList)
        .getAllByRole("listitem")
        .map(item => item.textContent.trim()))
        .toEqual(["Grace", "Ian"]))
});

test('start game button is shown', async () => {
    const gameId = "abcdef"
    stubFetch(gameId)

    renderLobby(gameId)

    enterName("Zuno")

    act(() => stubOnMessageFunction({type: WsMessageType.USER_JOINED, data: {ignored1: "Grace", ignored2: "Ian"}}))

    await waitFor(() => !expect(screen.getByText(/Start Game/i)).toBeInTheDocument())
});
