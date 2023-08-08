export default function StartGameButton() {
    return (
        <form action="/api/new-game" method="post">
            <button type="submit">Create new game</button>
        </form>
    );
}