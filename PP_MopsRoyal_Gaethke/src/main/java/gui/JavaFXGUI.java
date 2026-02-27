package gui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import logic.*;

import java.util.List;
import java.util.Map;

/**
 * Adapter zwischen der Spiellogik und dem UserInterfaceController.
 * <p>
 * Diese Klasse implementiert {@link GUIConnector} und übersetzt alle
 * Logik-Events in konkrete JavaFX-Aktionen. Alle Aufrufe werden über
 * {@code Platform.runLater()} auf den JavaFX Application Thread delegiert.
 * </p>
 * <p>
 * <b>Text-Übersetzung:</b> Die Methoden {@link #showInfo(InfoType)} und
 * {@link #showUserError(ErrorType)} übersetzen Enum-Werte in benutzerfreundliche
 * deutsche Texte. Bei einer Lokalisierung müssten nur diese Methoden angepasst werden.
 * </p>
 */
public class JavaFXGUI implements GUIConnector {

    /** UI-Controller, der die konkreten Anzeigeaktionen bereitstellt. */
    private final UserInterfaceController fxml;

    /**
     * Erstellt die JavaFX-UI-Brücke.
     *
     * @param fxml UI-Controller (nicht null)
     * @throws NullPointerException wenn {@code fxml} null ist
     */
    public JavaFXGUI(UserInterfaceController fxml) {
        if (fxml == null) {
            throw new NullPointerException("fxml darf nicht null sein");
        }
        this.fxml = fxml;
    }

    // ===========================================
    // Board-Darstellung
    // ===========================================

    /**
     * Rendert das Spielfeld in der UI.
     *
     * @param tiles 2D-Array der Tiles (nicht null)
     * @param origin Position der oberen linken Ecke
     */
    @Override
    public void renderBoard(Tile[][] tiles, Position origin) {
        Platform.runLater(() -> fxml.renderBoard(tiles, origin));
    }

    /**
     * Hebt erlaubte/aktive Positionen hervor.
     *
     * @param positions Positionen, die markiert werden sollen (nicht null)
     */
    @Override
    public void highlightPositions(List<Position> positions) {
        Platform.runLater(() -> fxml.showHighlights(positions));
    }

    // ===========================================
    // Spieler-Anzeige
    // ===========================================

    /**
     * Zeigt den aktuell aktiven Spieler an.
     *
     * @param player aktiver Spieler (nicht null)
     */
    @Override
    public void showActivePlayer(Player player) {
        Platform.runLater(() -> fxml.setActivePlayerLabel(player.getName()));
    }

    /**
     * Aktualisiert die Punkteübersicht.
     *
     * @param scores Mapping Spieler → Punkte (nicht null)
     */
    @Override
    public void showScores(Map<Player, Integer> scores) {
        Platform.runLater(() -> fxml.updateScoreView(scores));
    }

    /**
     * Aktualisiert die Mini-Board-Ansicht aller Spieler.
     * <p>
     * Zeigt für jeden Spieler in der rechten Seitenleiste:
     * <ul>
     *   <li>Spielername</li>
     *   <li>Aktueller Board-Zustand (5×5 Mini-Grid)</li>
     * </ul>
     * Container für nicht teilnehmende Spieler (bei weniger als 4 Spielern) werden ausgeblendet.
     * </p>
     *
     * @param players Liste aller Spieler im Spiel (nicht {@code null}, max. 4)
     */
    @Override
    public void updatePlayerBoards(List<Player> players) {
        Platform.runLater(() -> fxml.updatePlayerBoards(players));
    }

    // ===========================================
    // Tile-Anzeige
    // ===========================================

    /**
     * Zeigt das nächste zu legende Tile in der GUI an.
     * <p>
     * Das Tile wird in der linken Seitenleiste angezeigt, damit der aktive Spieler
     * sieht, welches Plättchen als nächstes gelegt werden muss.
     * </p>
     *
     * @param tile das anzuzeigende Tile, oder {@code null} um die Anzeige zu leeren
     */
    @Override
    public void showNextTile(Tile tile) {
        Platform.runLater(() -> fxml.showNextTile(tile));
    }

    // ===========================================
    // Meldungen (Enum → Text Übersetzung)
    // ===========================================

    /**
     * Übersetzt einen InfoType in einen benutzerfreundlichen Text.
     *
     * @param infoType der Informationstyp
     * @return übersetzter Text
     */
    private String translateInfoType(InfoType infoType) {
        return switch (infoType) {
            case GAME_LOADED -> "Spielstand wurde geladen.";
            case GAME_SAVED -> "Spielstand wurde gespeichert.";
        };
    }

    /**
     * Übersetzt einen InfoType mit Kontext in einen benutzerfreundlichen Text.
     *
     * @param infoType der Informationstyp
     * @param context  zusätzlicher Kontext (z.B. Dateiname)
     * @return übersetzter Text mit Kontext
     */
    private String translateInfoType(InfoType infoType, String context) {
        return switch (infoType) {
            case GAME_LOADED -> "Spiel geladen: " + context;
            case GAME_SAVED -> "Spiel gespeichert: " + context;
        };
    }

    /**
     * Übersetzt einen ErrorType in einen benutzerfreundlichen Text.
     *
     * @param errorType der Fehlertyp
     * @return übersetzter Text
     */
    private String translateErrorType(ErrorType errorType) {
        return switch (errorType) {
            case INVALID_POSITION ->
                    "Ungültige Position!\nBitte wähle eine markierte Position.";
            case INVALID_SELECTION ->
                    "Ungültige Auswahl!\nDieses Plättchen kann nicht umgedreht werden.";
            case TILE_PLACEMENT_FAILED ->
                    "Fehler beim Platzieren des Plättchens.";
            case LOAD_FAILED ->
                    "Laden fehlgeschlagen.";
            case SAVE_FAILED ->
                    "Speichern fehlgeschlagen.";
            case INVALID_SAVE_FILE ->
                    "Ungültige Spielstandsdatei.";
            case NO_GAME_STARTED ->
                    "Kein Spiel gestartet!\nBitte starte erst ein neues Spiel.";
            case ACTION_DURING_SCORING ->
                    "Aktion nicht möglich!\nBitte schließe erst die Wertungsansicht ab.";
            case DIALOG_LOAD_FAILED ->
                    "Dialog konnte nicht geladen werden.";
        };
    }

    /**
     * Übersetzt einen ErrorType mit Kontext in einen benutzerfreundlichen Text.
     *
     * @param errorType der Fehlertyp
     * @param context   zusätzlicher Kontext (z.B. Exception-Message)
     * @return übersetzter Text mit Kontext
     */
    private String translateErrorType(ErrorType errorType, String context) {
        return translateErrorType(errorType) + "\n\nDetails: " + context;
    }

    /**
     * Zeigt einen Info-Alert an.
     *
     * @param message die anzuzeigende Nachricht
     */
    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Zeigt einen Fehler-Alert an.
     *
     * @param message die anzuzeigende Fehlermeldung
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void showInfo(InfoType infoType) {
        String message = translateInfoType(infoType);
        Platform.runLater(() -> showInfoAlert(message));
    }

    @Override
    public void showInfo(InfoType infoType, String context) {
        String message = translateInfoType(infoType, context);
        Platform.runLater(() -> showInfoAlert(message));
    }

    @Override
    public void showUserError(ErrorType errorType) {
        String message = translateErrorType(errorType);
        Platform.runLater(() -> showErrorAlert(message));
    }

    @Override
    public void showUserError(ErrorType errorType, String context) {
        String message = translateErrorType(errorType, context);
        Platform.runLater(() -> showErrorAlert(message));
    }

    // ===========================================
    // Spiel-Events
    // ===========================================

    /**
     * Zeigt ein Spiel-Event als Dialog an.
     *
     * @param event das anzuzeigende Event (nicht {@code null})
     * @param player der betroffene Spieler (nicht {@code null})
     */
    @Override
    public void showGameEvent(GameEvent event, Player player) {
        Platform.runLater(() -> fxml.showGameEvent(event, player));
    }

    // ===========================================
    // Wertungsphase
    // ===========================================

    /**
     * Zeigt die Wertungsansicht als Overlay an.
     * <p>
     * Die Wertungsansicht wird rechts im Hauptfenster eingeblendet und zeigt:
     * <ul>
     *   <li>Alle gefundenen Kombinationen des Spielers</li>
     *   <li>Möglichkeit zur Auswahl, welche Kombination gewertet werden soll</li>
     *   <li>Auswahl des umzudrehenden Plättchens</li>
     * </ul>
     * Nach Abschluss der Wertung wird {@code onScoringComplete} aufgerufen.
     * </p>
     *
     * @param player der Spieler, der die Wertung durchführt (nicht {@code null})
     * @param combinations Liste der gefundenen Kombinationen (nicht {@code null}, nicht leer)
     * @param onScoringComplete Callback nach Abschluss der Wertung (nicht {@code null})
     */
    @Override
    public void showScoringView(Player player, List<Combination> combinations, Runnable onScoringComplete) {
        Platform.runLater(() -> fxml.showScoringOverlay(player, combinations, onScoringComplete));
    }

    /**
     * Blendet die Wertungsansicht aus.
     * <p>
     * Entfernt das Wertungs-Overlay aus der Szene und gibt Ressourcen frei.
     * Diese Methode wird automatisch nach Abschluss der Wertung aufgerufen.
     * </p>
     */
    @Override
    public void hideScoringView() {
        Platform.runLater(fxml::hideScoringOverlay);
    }

    /**
     * Plant den Übergang zum nächsten Spieler mit einer kurzen Verzögerung.
     * <p>
     * Diese Methode wird aufgerufen, wenn ein Spieler sein Plättchen gelegt hat,
     * aber keine Wertung stattfindet. Die Verzögerung (ca. 0,5 Sekunden) gibt dem
     * Benutzer Zeit, das gelegte Plättchen zu sehen, bevor der nächste Spieler dran ist.
     * </p>
     *
     * @param onComplete Callback nach Ablauf der Verzögerung (nicht {@code null})
     */
    @Override
    public void scheduleNextPlayer(Runnable onComplete) {
        Platform.runLater(() -> fxml.scheduleNextPlayerTransition(onComplete));
    }

    // ===========================================
    // Spielende
    // ===========================================

    /**
     * Zeigt den Gewinner-Dialog am Ende des Spiels an.
     * <p>
     * Der Dialog zeigt:
     * <ul>
     *   <li>Bei einem Gewinner: Name und Punktzahl</li>
     *   <li>Bei Gleichstand: Alle Spieler mit der höchsten Punktzahl</li>
     * </ul>
     * Das Spiel ist nach Anzeige dieses Dialogs beendet.
     * </p>
     *
     * @param winners Liste der Gewinner (nicht {@code null}, nicht leer)
     * @param score die Gewinner-Punktzahl
     */
    @Override
    public void showWinner(List<Player> winners, int score) {
        Platform.runLater(() -> fxml.showWinnerDialog(winners));
    }

    // ===========================================
    // UI-Steuerung
    // ===========================================

    /**
     * Aktiviert den Speichern-Button nach Spielstart.
     * <p>
     * Delegiert an den UserInterfaceController.
     * </p>
     */
    @Override
    public void enableSaveButton() {
        Platform.runLater(() -> fxml.enableSaveButton());
    }
}