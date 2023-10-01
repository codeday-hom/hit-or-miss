import {render, screen} from '@testing-library/react';
import React from "react";
import Lobby from "./Lobby";
import {MemoryRouter, Route, Routes} from "react-router-dom";

jest.mock("../websockets/useGameWebSocket", () => function (gameId, onMessageFunction) {
    console.log(`gameId: ${gameId}`)
})

test('renders header', async () => {
    const gameId = "abcdef"
    render(
        <MemoryRouter initialEntries={[{pathname: `/game/${gameId}/lobby`}]}>
            <Routes>
                <Route path={"/game/:gameId/lobby"} element={<Lobby/>} />
            </Routes>
        </MemoryRouter>
    )

    expect(screen.getByText(/Welcome to the Game Lobby!/i)).toBeInTheDocument();
});
