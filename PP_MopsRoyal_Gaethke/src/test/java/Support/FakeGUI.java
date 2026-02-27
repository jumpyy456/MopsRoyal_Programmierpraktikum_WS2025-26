package Support;

import logic.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fake-Implementierung des GUIConnector für Tests.
 * <p>
 * <b>WICHTIG:</b> Diese Datei gehört in das <b>Test-Paket</b>:
 * </p>
 * <p>
 * Diese Klasse implementiert alle Methoden des GUIConnector-Interfaces,
 * führt aber keine tatsächlichen GUI-Operationen aus. Stattdessen
 * protokolliert sie alle Aufrufe in einer Liste, die in Tests
 * überprüft werden kann.
 * </p>
 *
 * <h3>Verwendung in Tests:</h3>
 * <pre>
 * FakeGUI fakeGui = new FakeGUI();
 * Game game = new Game(fakeGui);
 *
 * // ... Testoperationen ...
 *
 * // Prüfen, ob bestimmte Methoden aufgerufen wurden
 * assertTrue(fakeGui.calls.contains("showActivePlayer: Alice"));
 * assertEquals(fakeGui.lastActivePlayer.getName(), "Alice");
 * </pre>
 */
public class FakeGUI implements GUIConnector {

    // --- Öffentliche Felder für Test-Assertions ---

    /**
     * Liste aller aufgerufenen Methoden mit ihren Parametern.
     * Format: "methodName: parameter"
     */
    public List<String> calls = new ArrayList<>();

    /**
     * Der zuletzt über showActivePlayer() angezeigte Spieler.
     */
    public Player lastActivePlayer;

    /**
     * Das zuletzt über showNextTile() angezeigte Tile.
     */
    public Tile lastNextTile;

    /**
     * Die zuletzt über renderBoard() übergebenen Tiles.
     */
    public Tile[][] lastRenderedBoard;

    /**
     * Die zuletzt über renderBoard() übergebene Origin-Position.
     */
    public Position lastOrigin;

    /**
     * Die zuletzt über highlightPositions() übergebenen Positionen.
     */
    public List<Position> lastHighlightedPositions;

    /**
     * Die zuletzt über showScores() übergebenen Scores.
     */
    public Map<Player, Integer> lastScores;

    /**
     * Der zuletzt über showInfo() übergebene InfoType.
     */
    public InfoType lastInfoType;

    /**
     * Der zuletzt über showInfo() übergebene Kontext (z.B. Dateiname).
     */
    public String lastInfoContext;

    /**
     * Das zuletzt über showGameEvent() übergebene Event.
     */
    public GameEvent lastGameEvent;

    /**
     * Der zuletzt über showGameEvent() übergebene Spieler.
     */
    public Player lastGameEventPlayer;

    /**
     * Der zuletzt über showUserError() übergebene ErrorType.
     */
    public ErrorType lastUserError;

    /**
     * Der zuletzt über showUserError() übergebene Kontext (z.B. Exception-Message).
     */
    public String lastErrorContext;

    /**
     * Die zuletzt über showWinner() übergebenen Gewinner.
     */
    public List<Player> lastWinners;

    /**
     * Der zuletzt über showWinner() übergebene Highscore.
     */
    public int lastWinnerScore;

    /**
     * Die zuletzt über showScoringView() übergebenen Kombinationen.
     */
    public List<Combination> lastCombinations;

    /**
     * Der zuletzt über showScoringView() übergebene Spieler.
     */
    public Player lastScoringPlayer;

    // --- GUIConnector-Implementierung ---

    @Override
    public void renderBoard(Tile[][] tiles, Position origin) {
        calls.add("renderBoard");
        this.lastRenderedBoard = tiles;
        this.lastOrigin = origin;
    }

    @Override
    public void highlightPositions(List<Position> positions) {
        calls.add("highlightPositions: " + positions.size() + " positions");
        this.lastHighlightedPositions = positions;
    }

    @Override
    public void showActivePlayer(Player player) {
        String playerName = (player != null) ? player.getName() : "null";
        calls.add("showActivePlayer: " + playerName);
        this.lastActivePlayer = player;
    }

    @Override
    public void showScores(Map<Player, Integer> scores) {
        calls.add("showScores");
        this.lastScores = scores;
    }

    @Override
    public void showInfo(InfoType infoType) {
        calls.add("showInfo: " + infoType);
        this.lastInfoType = infoType;
        this.lastInfoContext = null;
    }

    @Override
    public void showInfo(InfoType infoType, String context) {
        calls.add("showInfo: " + infoType + " [" + context + "]");
        this.lastInfoType = infoType;
        this.lastInfoContext = context;
    }

    @Override
    public void showUserError(ErrorType errorType) {
        calls.add("showUserError: " + errorType);
        this.lastUserError = errorType;
        this.lastErrorContext = null;
    }

    @Override
    public void showUserError(ErrorType errorType, String context) {
        calls.add("showUserError: " + errorType + " [" + context + "]");
        this.lastUserError = errorType;
        this.lastErrorContext = context;
    }

    @Override
    public void showNextTile(Tile tile) {
        String tileDesc = (tile != null)
                ? "Tile(" + tile.getColor() + "," + tile.getObject() + ")"
                : "null";
        calls.add("showNextTile: " + tileDesc);
        this.lastNextTile = tile;
    }

    @Override
    public void updatePlayerBoards(List<Player> players) {
        calls.add("updatePlayerBoards: " + players.size() + " players");
    }

    @Override
    public void showScoringView(Player player, List<Combination> combinations, Runnable onScoringComplete) {
        String playerName = (player != null) ? player.getName() : "null";
        calls.add("showScoringView: " + playerName + ", " + combinations.size() + " combinations");
        this.lastScoringPlayer = player;
        this.lastCombinations = combinations;

        // Rufe Callback sofort auf (im Test kein Dialog nötig)
        if (onScoringComplete != null) {
            onScoringComplete.run();
        }
    }

    @Override
    public void hideScoringView() {
        calls.add("hideScoringView");
    }

    @Override
    public void showGameEvent(GameEvent event, Player player) {
        this.lastGameEvent = event;
        this.lastGameEventPlayer = player;
        String playerName = (player != null) ? player.getName() : "null";
        calls.add("showGameEvent: " + event + " [" + playerName + "]");
    }

    @Override
    public void showWinner(List<Player> winners, int score) {
        calls.add("showWinner: " + winners.size() + " winner(s), Score: " + score);
        this.lastWinners = winners;
        this.lastWinnerScore = score;
    }

    @Override
    public void scheduleNextPlayer(Runnable onComplete) {
        calls.add("scheduleNextPlayer");
        // In Tests: sofort ausführen (keine Animation)
        if (onComplete != null) {
            onComplete.run();
        }
    }

    @Override
    public void enableSaveButton() {
        calls.add("enableSaveButton");
    }

    // --- Hilfsmethoden für Tests ---

    /**
     * Prüft, ob eine bestimmte Methode aufgerufen wurde.
     *
     * @param methodName Name der Methode (ohne Parameter)
     * @return true, wenn Methode aufgerufen wurde
     */
    public boolean wasCalled(String methodName) {
        return calls.stream().anyMatch(call -> call.startsWith(methodName));
    }

    /**
     * Gibt die Anzahl der Aufrufe einer Methode zurück.
     *
     * @param methodName Name der Methode (ohne Parameter)
     * @return Anzahl der Aufrufe
     */
    public int getCallCount(String methodName) {
        return (int) calls.stream()
                .filter(call -> call.startsWith(methodName))
                .count();
    }

    /**
     * Löscht alle gespeicherten Aufrufe und Zustände.
     * Nützlich, um zwischen Test-Phasen aufzuräumen.
     */
    public void reset() {
        calls.clear();
        lastActivePlayer = null;
        lastNextTile = null;
        lastRenderedBoard = null;
        lastOrigin = null;
        lastHighlightedPositions = null;
        lastScores = null;
        lastInfoType = null;
        lastInfoContext = null;
        lastGameEvent = null;
        lastGameEventPlayer = null;
        lastUserError = null;
        lastErrorContext = null;
        lastWinners = null;
        lastWinnerScore = 0;
        lastCombinations = null;
        lastScoringPlayer = null;
    }

    /**
     * Gibt alle Aufrufe als formatierten String zurück.
     * Nützlich für Debug-Ausgaben in Tests.
     *
     * @return mehrzeiliger String mit allen Aufrufen
     */
    public String getCallHistory() {
        if (calls.isEmpty()) {
            return "(keine Aufrufe)";
        }
        return String.join("\n", calls);
    }
}