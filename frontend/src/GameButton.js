import React from 'react';

class GameButton extends React.Component {
    // createNewGame() {
    //     fetch('http://localhost:8080/new-game', {
    //         method: 'POST',
    //         redirect: 'follow'
    //     })
    //         // .then(response => {
    //         //     if (response.ok) {
    //         //         window.location.href = response.url;
    //         //     } else {
    //         //         console.error('Error creating new game:', response);
    //         //     }
    //         // })
    //         .catch(error => console.error('Error:', error));
    // }

    render() {
        return (
            // <button onClick={() => this.createNewGame()}>
            //     Create New Game
            // </button>
            <form action="http://localhost:8080/api/new-game" method="post">
                <button type="submit">Create new game</button>
            </form>
        );
    }
}

export default GameButton;