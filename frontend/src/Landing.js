import './Landing.css';

export default function Landing() {
  let version = process.env.REACT_APP_GIT_VERSION
  let versionLinkPart = version.replace("-snapshot", "")
  return (
    <div>
      <form action="/api/new-game" method="post">
        <button type="submit">Create new game</button>
      </form>
      <a href="/kit"><button>Categories and Dice</button></a>
      <br/>
      <a href={`https://github.com/codeday-hom/hit-or-miss/tree/${versionLinkPart}`}>Version {version}</a>
    </div>
  );
}
