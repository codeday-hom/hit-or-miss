import {render, screen} from '@testing-library/react';
import React from "react";
import Lobby from "./Lobby";
import {MemoryRouter, Route, Routes} from "react-router-dom";

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
                <Route path={pathPattern} element={element} />
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
