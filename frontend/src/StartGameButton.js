import React from 'react';

class StartGameButton extends React.Component {

    render() {
        return (
            <form action="/api/new-game" method="post">
                <button type="submit">Create new game</button>
            </form>
        );
    }
}

export default StartGameButton;