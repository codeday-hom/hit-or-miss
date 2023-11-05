import StartGameButton from './components/StartGameButton'
import './App.css';

export default App;

function App() {
  return (
    <div>
      <StartGameButton/>
      <p>Version {process.env.REACT_APP_GIT_VERSION}</p>
    </div>
  );
}
