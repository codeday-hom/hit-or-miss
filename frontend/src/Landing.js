import './Landing.css';

export default function Landing() {
  let version = process.env.REACT_APP_GIT_VERSION
  let versionLinkPart = version.replace("-snapshot", "")
  return (
    <div className="buttonbox">
      <form action="/api/game/new" method="post">
        <button type="submit">Play online with friends</button>
      </form>
      <a href="/kit"><button>Dice and categories for playing in person</button></a>
      <br/>
      <a href={`https://github.com/codeday-hom/hit-or-miss/tree/${versionLinkPart}`}>Version {version}</a>
    </div>
  );
}
