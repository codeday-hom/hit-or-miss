import React from 'react';

class GameButton extends React.Component {
    createNewGame() {
        fetch('http://localhost:8080/new-game', {
            method: 'POST'
        })
            .then(response => response.text())
            .then(gameId => {
                // Redirect to the game lobby page
                window.location.href = '/game/' + gameId + '/lobby';
            })
            .catch(error => console.error('Error:', error));
    }

    render() {
        return (
            <button onClick={() => this.createNewGame()}>
                Create New Game
            </button>
        );
    }
}

export default GameButton;