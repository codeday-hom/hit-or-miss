import React from 'react';

class StartGameButton extends React.Component {

    render() {
        return (
            <form action="http://localhost:8080/api/new-game" method="post">
                <button type="submit">Create new game</button>
            </form>
        );
    }
}

export default StartGameButton;