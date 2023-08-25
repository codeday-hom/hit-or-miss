import logo from './logo.svg';
import StartGameButton from './components/StartGameButton'
import './App.css';
import CountdownTimer from "./components/CountdownTimer";

export default App;

function App() {
    return (
        <div className="App">
            <header className="App-header">
                <img src={logo} className="App-logo" alt="logo"/>
                <p>
                    Edit <code>src/App.js</code> and save to reload.
                </p>
                <a
                    className="App-link"
                    href="https://reactjs.org"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    Learn React
                </a>
                <StartGameButton/>
                <CountdownTimer/>
            </header>
        </div>
    );
}

