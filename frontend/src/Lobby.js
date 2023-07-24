import React, { Component } from 'react';
import Cookies from 'js-cookie';

class Lobby extends Component {
    constructor(props) {
        super(props);
        // this.state = { gameId: this.props.gameId };
    }

    isHost() {
        const hostGameId = Cookies.get('game_host');
        const currentUrl = window.location.href;
        return hostGameId && currentUrl.includes("/game/" + hostGameId + "/lobby");
    }

    render() {
        return (
            <div>
                <h1>Welcome to the lobby!</h1>

                { this.isHost() &&
                    <button>Start game</button>
                }
            </div>
        );
    }
}

export default Lobby;
