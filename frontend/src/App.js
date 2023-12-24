import StartGameButton from './components/StartGameButton'
import './App.css';

export default App;

function App() {
  let version = process.env.REACT_APP_GIT_VERSION
  let versionLinkPart = version.replace("-snapshot", "")
  return (
    <div>
      <StartGameButton/>
      <a href={`https://github.com/codeday-hom/hit-or-miss/tree/${versionLinkPart}`}>Version {version}</a>
    </div>
  );
}
