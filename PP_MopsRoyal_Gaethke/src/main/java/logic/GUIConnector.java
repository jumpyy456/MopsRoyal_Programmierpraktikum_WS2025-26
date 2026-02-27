package logic;

import java.util.List;
import java.util.Map;

/**
 * Schnittstelle zwischen Spiellogik und GUI.
 * <p>
 *     Definiert alle Methoden, die die GUI implementieren muss,
 *     um vom Game über Zustandsänderungen informiert zu werden.
 * </p>
 *
 * <p>
 *     Wichtig: Diese Schnittstelle übergibt keine Texte, sondern nur typisierte Enums (ErrorType, InfoType, GameEvent).
 *     Die Übersetzung in benutzerfreundliche Texte erfolgt in der GUI-Implementierung.
 * </p>
 *
 * <p>
 *     Implementierungen müssen thread-safe sein,
 *     da Aufrufe sowohl vom JavaFX Application Thread als auch von anderen Threads erfolgen können.
 * </p>
 */
public interface GUIConnector {

    // ===========================================
    // Board-Darstellung
    // ===========================================

    /**
     * Rendert das Spielfeld in der Benutzeroberfläche.
     * <p>
     * Das übergebene Array enthält den aktuellen Zustand eines Spieler-Boards.
     * Leere Felder sind null.
     * </p>
     * <p>
     * WICHTIG: Das Array-Index [0][0] entspricht der Board-Position origin,
     * nicht der Board-Position (0,0)!
     * </p>
     *
     * @param tiles 2D-Array der Tiles (5×5, nicht @code null)
     * @param origin Position der oberen linken Ecke (minRow, minCol) oder null wenn leer
     */
    void renderBoard(Tile[][] tiles, Position origin);

    /**
     * Hebt gültige Anlegepositionen im Spielfeld hervor.
     *
     * @param positions Liste gültiger Positionen (nicht null, kann leer sein)
     */
    void highlightPositions(List<Position> positions);

    // ===========================================
    // Spieler-Anzeige
    // ===========================================

    /**
     * Zeigt den aktuell aktiven Spieler an.
     *
     * @param player der aktive Spieler (nicht null)
     */
    void showActivePlayer(Player player);

    /**
     * Aktualisiert die Punkteübersicht aller Spieler.
     *
     * @param scores Mapping Spieler -> Punktestand (nicht null)
     */
    void showScores(Map<Player, Integer> scores);

    /**
     * Aktualisiert die Mini-Board-Ansicht aller Spieler.
     *
     * @param players Liste aller Spieler in Sitzreihenfolge (nicht null)
     */
    void updatePlayerBoards(List<Player> players);

    // ===========================================
    // Tile-Anzeige
    // ===========================================

    /**
     * Zeigt das nächste zu legende Tile an.
     *
     * @param tile das nächste Tile oder null, wenn Vorrat leer
     */
    void showNextTile(Tile tile);

    // ===========================================
    // Meldungen (Enum-basiert, !keine Strings!)
    // ===========================================

    /**
     * Zeigt eine Informationsmeldung in der GUI an.
     * Die GUI übersetzt den Informationstyp in eine nutzerfreundliche Meldung.
     *
     * @param infoType Meldungstext (nicht null)
     */
    void showInfo(InfoType infoType);

    /**
     * Zeigt eine Informationsmeldung mit zusätzlichem Kontext an.
     * Der Kontext kann z.B. ein Dateiname sein, der in die Meldung eingefügt wird.
     * Die GUI übersetzt den Informationstyp und fügt den Kontext passend ein.
     *
     * @param infoType der Informationstyp (nicht null)
     * @param context zusätzlicher Kontext (z.B. Dateiname, nicht null)
     */
    void showInfo(InfoType infoType, String context);

    /**
     * Zeigt einen Benutzerfehler an.
     * <p>
     * Die GUI übersetzt den Fehlertyp in eine nutzerfreundliche Fehlermeldung.
     * </p>
     *
     * @param errorType der Fehlertyp (nicht null)
     */
    void showUserError(ErrorType errorType);

    /**
     * Zeigt einen Benutzerfehler mit zusätzlichem Kontext an.
     * Der Kontext kann z.B. eine technische Fehlermeldung sein.
     * Die GUI übersetzt den Fehlertyp und zeigt den Kontext ggf. als Details.
     *
     * @param errorType
     * @param context
     */
    void showUserError(ErrorType errorType, String context);

    // ===========================================
    // Spiel-Events
    // ===========================================

    /**
     * Zeigt ein Spiel-Event als Dialog an.
     * <p>
     * Der Dialog wird nicht-blockierend angezeigt.
     * </p>
     *
     * @param event das anzuzeigende Event (nicht null)
     * @param player der betroffene Spieler (nicht code null)
     */
    void showGameEvent(GameEvent event, Player player);

    // ===========================================
    // Wertungsphase
    // ===========================================

    /**
     * Zeigt die Wertungsansicht mit allen möglichen Kombinationen an.
     * <p>
     * Der Spieler kann eine Kombination auswählen, ein Plättchen zum Umdrehen wählen
     * und die Wertung bestätigen oder die Wertung komplett überspringen.
     * </p>
     *
     * @param player der Spieler, der werten darf (nicht null)
     * @param combinations Liste aller möglichen Kombinationen (nicht null, nicht leer)
     * @param onScoringComplete Callback, der nach Abschluss der Wertung aufgerufen wird (nicht null)
     */
    void showScoringView(Player player, List<Combination> combinations, Runnable onScoringComplete);

    /**
     * Blendet die Wertungsansicht aus.
     */
    void hideScoringView();

    /**
     * Plant den Wechsel zum nächsten Spieler nach einer kurzen Pause.
     * <p>
     * Diese Methode wird aufgerufen, wenn keine Wertung stattfindet.
     * Die GUI soll eine kurze Animation/Pause (z.B. 0.5 Sekunden) zeigen,
     * danach wird der Callback ausgeführt.
     * </p>
     *
     * @param onComplete Callback nach der Pause (nicht null)
     */
    void scheduleNextPlayer(Runnable onComplete);

    // ===========================================
    // Spielende
    // ===========================================

    /**
     * Zeigt den Sieger oder Gleichstand an.
     * <p>
     * Bei einem eindeutigen Sieger enthält die Liste nur einen Spieler.
     * Bei Gleichstand enthält die Liste alle Spieler mit dem höchsten Score.
     * </p>
     *
     * @param winners Liste der Gewinner-Spieler (nicht null, nicht leer)
     * @param score der Gewinner-Score
     */
    void showWinner(List<Player> winners, int score);

    // ===========================================
    // UI-Steuerung
    // ===========================================

    /**
     * Aktiviert den Speichern-Button nach Spielstart.
     * <p>
     * Wird von Game nach erfolgreichem startNewGame() aufgerufen.
     * </p>
     */
    void enableSaveButton();
}