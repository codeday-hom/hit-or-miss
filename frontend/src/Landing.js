import StartGameButton from './components/StartGameButton'
import './Landing.css';

export default function Landing() {
  let version = process.env.REACT_APP_GIT_VERSION
  let versionLinkPart = version.replace("-snapshot", "")
  return (
    <div>
      <StartGameButton/>
      <a href={`https://github.com/codeday-hom/hit-or-miss/tree/${versionLinkPart}`}>Version {version}</a>
    </div>
  );
}
