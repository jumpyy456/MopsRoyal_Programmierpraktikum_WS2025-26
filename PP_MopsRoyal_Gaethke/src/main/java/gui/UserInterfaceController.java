package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.beans.binding.Bindings;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.io.File;
import java.util.*;
import logic.*;



/**
 * Haupt-Controller für die JavaFX-Benutzeroberfläche von Mops Royal.
 * <p>
 * Diese Klasse ist verantwortlich für:
 * <ul>
 *   <li>Initialisierung und Verwaltung aller GUI-Komponenten</li>
 *   <li>Darstellung des Hauptspielfelds und der Mini-Boards</li>
 *   <li>Verarbeitung von Benutzer-Eingaben (Klicks, Zoom, etc.)</li>
 *   <li>Aktualisierung der Anzeige basierend auf Spielzustand</li>
 *   <li>Menü-Aktionen (Neu, Laden, Speichern, Hilfe)</li>
 * </ul>
 * </p>
 */
public class UserInterfaceController {

    // --- Konstanten ---
    private static final String COLOR_HEX_BLUE = "#0066cc";
    private static final String COLOR_HEX_GREEN = "#00cc66";
    private static final String COLOR_HEX_RED = "#cc0066";
    private static final String COLOR_HEX_YELLOW = "#cccc00";
    private static final double MIN_SCALE = 0.38;
    private static final double MAX_SCALE = 3.0;

    // --- SPIELLOGIK-VERBINDUNG ---
    private Game game;
    private JavaFXGUI view;

    // FXML-Komponenten: Hauptfeld
    @FXML private GridPane gridPane;
    @FXML private ScrollPane scrollPane;

    // FXML-Komponenten: Links (Infos + Nächstes Tile)
    @FXML private Label activePlayerLabel;
    @FXML private VBox scoresBox;
    @FXML private ImageView nextTileView;
    @FXML private MenuItem saveMenuItem;

    // FXML-Komponenten: Rechts (Mini-Boards)
    @FXML private VBox miniBoard1Container;
    @FXML private VBox miniBoard2Container;
    @FXML private VBox miniBoard3Container;
    @FXML private VBox miniBoard4Container;

    @FXML private Label miniBoard1Label;
    @FXML private Label miniBoard2Label;
    @FXML private Label miniBoard3Label;
    @FXML private Label miniBoard4Label;

    @FXML private GridPane miniBoard1;
    @FXML private GridPane miniBoard2;
    @FXML private GridPane miniBoard3;
    @FXML private GridPane miniBoard4;

    // --- Zustandsvariablen ---
    private final List<Rectangle> currentHighlights = new ArrayList<>();
    private int uiRowOffset;
    private int uiColOffset;
    private boolean isScoringActive = false;
    private ScoringViewController scoringController;
    private VBox scoringViewRoot;

    // ===========================================
    // Initialisierung
    // ===========================================

    /**
     * Initialisiert die GUI-Komponenten nach dem Laden der FXML.
     * <p>
     * Wird automatisch von JavaFX aufgerufen. Führt folgende Schritte aus:
     * <ol>
     *   <li>Konfiguriert GridPane und ScrollPane (Größe, Pannable)</li>
     *   <li>Erstellt JavaFXGUI und Game (zentrale Komponenten)</li>
     *   <li>Registriert Scene-Listener für Offset-Berechnung</li>
     *   <li>Initialisiert Mini-Boards (5×5 Struktur)</li>
     *   <li>Deaktiviert Speichern-Button (wird später aktiviert)</li>
     *   <li>Zeigt "Neues Spiel"-Dialog</li>
     * </ol>
     * </p>
     * <p>
     * Saubere Trennung: UserInterfaceController erstellt hier sowohl
     * JavaFXGUI als auch Game. Dies ermöglicht volle Kontrolle über die
     * Komponenten ohne dass ApplicationMain.start() davon wissen muss.
     * </p>
     */
    @FXML
    public void initialize() {
        // Grid/ScrollPane Setup
        gridPane.setPrefWidth(scrollPane.getMaxWidth() * 2.5);
        gridPane.setPrefHeight(scrollPane.getMaxHeight() * 2.5);
        gridPane.setMinWidth(scrollPane.getMaxWidth());
        gridPane.setMinHeight(scrollPane.getMaxHeight());

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);

        // Mini-Boards vorbereiten (5×5 Struktur) - vor Komponenten-Erstellung
        initializeMiniBoard(miniBoard1);
        initializeMiniBoard(miniBoard2);
        initializeMiniBoard(miniBoard3);
        initializeMiniBoard(miniBoard4);

        // Speichern-Button initial deaktivieren
        if (saveMenuItem != null) {
            saveMenuItem.setDisable(true);
        }

        // Listener für Offset-Berechnung wenn Scene verfügbar ist
        gridPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(this::computeUiCenterAndOffsetsSafely);
            }
        });

        // Zentral: JavaFXGUI und Game erstellen
        this.view = new JavaFXGUI(this);
        this.game = new Game(view);

        // Nach Komponenten-Erstellung: Dialog anzeigen
        Platform.runLater(() -> {
            setInitialZoomAndCenter();
            showNewGameDialog();
        });
    }

    /**
     * Initialisiert ein Mini-Board mit 5×5 Struktur.
     *
     * @param grid das zu initialisierende GridPane (nicht null)
     */
    private void initializeMiniBoard(GridPane grid) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        // 5 Spalten
        for (int i = 0; i < 5; i++) {
            javafx.scene.layout.ColumnConstraints col = new javafx.scene.layout.ColumnConstraints();
            col.setPercentWidth(20);
            grid.getColumnConstraints().add(col);
        }

        // 5 Zeilen
        for (int i = 0; i < 5; i++) {
            javafx.scene.layout.RowConstraints row = new javafx.scene.layout.RowConstraints();
            row.setPercentHeight(20);
            grid.getRowConstraints().add(row);
        }
    }

    /**
     * Berechnet Zentrum und Offsets zwischen UI-Grid und logischem Board.
     * <p>
     * Benötigt, um Board-Koordinaten (0..4) auf UI-Koordinaten zu mappen.
     * Wird nach UI-Initialisierung aufgerufen.
     * </p>
     */
    private void computeUiCenterAndOffsetsSafely() {
        int cols = gridPane.getColumnCount();
        int rows = gridPane.getRowCount();

        // Fallback auf ColumnConstraints/RowConstraints
        if (cols <= 0) cols = (gridPane.getColumnConstraints() != null) ? gridPane.getColumnConstraints().size() : 0;
        if (rows <= 0) rows = (gridPane.getRowConstraints() != null) ? gridPane.getRowConstraints().size() : 0;

        if (cols == 0 || rows == 0) return;

        // UI-Zentrum
        int uiCenterCol = cols / 2;
        int uiCenterRow = rows / 2;

        // für HashMap-Board mit Startplättchen bei 0,0
        int boardCenterCol = 0;
        int boardCenterRow = 0;

        uiColOffset = uiCenterCol - boardCenterCol;
        uiRowOffset = uiCenterRow - boardCenterRow;
    }

    /**
     * Setzt initialen Zoom auf 3-4 Reihen/Spalten und zentriert.
     * <p>
     * Wird nur einmal beim ersten Programmstart aufgerufen.
     * </p>
     */
    private void setInitialZoomAndCenter() {
        Platform.runLater(() -> {
            // Scale zurücksetzen
            gridPane.setScaleX(1.0);
            gridPane.setScaleY(1.0);

            // ScrollPane viewport size
            double viewportWidth = scrollPane.getViewportBounds().getWidth();
            double viewportHeight = scrollPane.getViewportBounds().getHeight();

            // Grid-Größe ermitteln
            int cols = gridPane.getColumnConstraints().size();
            int rows = gridPane.getRowConstraints().size();

            if (cols == 0 || rows == 0) {
                cols = 9;
                rows = 9;
            }

            // Ziel: 3.5 Zellen sollen viewport füllen
            double targetCellsVisible = 3.5;
            double targetGridWidth = viewportWidth * (cols / targetCellsVisible);
            double targetGridHeight = viewportHeight * (rows / targetCellsVisible);

            gridPane.setPrefWidth(targetGridWidth);
            gridPane.setPrefHeight(targetGridHeight);

            // Nach Layout-Update: zentrieren
            Platform.runLater(() -> {
                scrollPane.setHvalue((scrollPane.getHmax() - scrollPane.getHmin()) / 2.0);
                scrollPane.setVvalue((scrollPane.getVmax() - scrollPane.getVmin()) / 2.0);
            });
        });
    }

    // ===========================================
    // Rendering (Hauptfeld)
    // ===========================================

    /**
     * Fügt ein Tile-Bild zum Haupt-GridPane hinzu.
     * <p>
     * Das Bild wird dynamisch skaliert und an die Grid-Größe gebunden.
     * </p>
     *
     * @param tile das anzuzeigende Tile (nicht null)
     * @param col UI-Spalte (nach Offset-Anpassung)
     * @param row UI-Zeile (nach Offset-Anpassung)
     */
    private void addImageToGridPane(Tile tile, int col, int row) {
        int cellWidth = (int) gridPane.getWidth() / gridPane.getColumnCount();
        int cellHeight = (int) gridPane.getHeight() / gridPane.getRowCount();

        String path = TileImageHelper.getImagePath(tile);
        Image image = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream(path),
                        "Fehler: Bilddatei nicht gefunden: " + path)
        );

        ImageView imgView = new ImageView(image);
        imgView.setFitWidth(cellWidth);
        imgView.setFitHeight(cellHeight);
        imgView.setPreserveRatio(true);
        imgView.setSmooth(true);

        gridPane.add(imgView, col, row);

        // Dynamisches Binding an Grid-Größe
        imgView.fitWidthProperty().bind(
                gridPane.widthProperty().divide(gridPane.getColumnCount())
                        .subtract(gridPane.getHgap())
        );
        imgView.fitHeightProperty().bind(
                gridPane.heightProperty().divide(gridPane.getRowCount())
                        .subtract(gridPane.getVgap())
        );
    }

    /**
     * Fügt ein Tile-Bild zu einem Mini-Board hinzu.
     * <p>
     * Kleinere Version für die Mini-Ansicht rechts.
     * </p>
     *
     * @param miniGrid das Mini-GridPane (nicht null)
     * @param tile das anzuzeigende Tile (kann null sein)
     * @param col Spalte im Mini-Grid (0..4)
     * @param row Zeile im Mini-Grid (0..4)
     */
    private void addImageToMiniBoard(GridPane miniGrid, Tile tile, int col, int row) {
        if (tile == null) return;

        String path = TileImageHelper.getImagePath(tile);
        Image image = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream(path),
                        "Fehler: Bilddatei nicht gefunden: " + path)
        );

        ImageView imgView = new ImageView(image);
        imgView.setPreserveRatio(true);
        imgView.setSmooth(true);

        // Dynamische Größenanpassung an Mini-Grid-Zelle
        imgView.fitWidthProperty().bind(
                miniGrid.widthProperty().divide(5).subtract(2)
        );
        imgView.fitHeightProperty().bind(
                miniGrid.heightProperty().divide(5).subtract(2)
        );

        miniGrid.add(imgView, col, row);
    }

    // ===========================================
    // Öffentliche Anzeigemethoden (von JavaFXGUI aufgerufen)
    // ===========================================

    /**
     * Rendert das Hauptspielfeld.
     * <p>
     * <b>WICHTIG:</b> Das übergebene Array stammt von snapshotTiles(),
     * bei dem die Bounding-Box nach (0,0) verschoben wurde.
     * Der origin-Parameter gibt die echten Board-Koordinaten der
     * Array-Position [0][0] an.
     * </p>
     *
     * @param tiles 2D-Array des Board-Zustands (5×5, kann null-Einträge haben)
     * @param origin Board-Position der oberen linken Ecke (kann null sein wenn Board leer)
     */
    public void renderBoard(Tile[][] tiles, Position origin) {
        if (tiles == null) return;

        // Alte ImageViews entfernen
        gridPane.getChildren().removeIf(node -> node instanceof ImageView);

        if (origin == null) {
            return;
        }

        for (int r = 0; r < tiles.length; r++) {
            for (int c = 0; c < tiles[r].length; c++) {
                Tile t = tiles[r][c];
                if (t == null) continue;

                // Snapshot-Array-Index [r][c] → Board-Koordinaten
                int boardRow = origin.row() + r;
                int boardCol = origin.col() + c;

                // Board-Koordinaten → UI-Koordinaten
                int uiCol = boardCol + uiColOffset;
                int uiRow = boardRow + uiRowOffset;

                addImageToGridPane(t, uiCol, uiRow);
            }
        }
    }

    /**
     * Zeigt Highlight-Markierungen für gültige Positionen.
     * <p>
     * Alte Highlights werden entfernt, neue als halbtransparente Rechtecke hinzugefügt.
     * </p>
     *
     * @param positions Liste gültiger Positionen (kann null oder leer sein)
     */
    public void showHighlights(List<Position> positions) {
        // Alte Highlights entfernen
        if (!currentHighlights.isEmpty()) {
            gridPane.getChildren().removeAll(currentHighlights);
            currentHighlights.clear();
        }

        if (positions == null || positions.isEmpty()) {
            return;
        }

        final int cols = Math.max(1, gridPane.getColumnCount());
        final int rows = Math.max(1, gridPane.getRowCount());

        // Neue Highlights erstellen
        for (Position p : positions) {
            int uiRow = p.row() + uiRowOffset;
            int uiCol = p.col() + uiColOffset;

            if (uiCol < 0 || uiCol >= cols || uiRow < 0 || uiRow >= rows) continue;

            Rectangle rect = new Rectangle();
            rect.setMouseTransparent(true); // Klicks durchlassen
            rect.setFill(Color.web("#CFEA8C", 0.45));

            // Dynamisches Binding an Grid-Größe
            rect.widthProperty().bind(
                    Bindings.max(0.0, gridPane.widthProperty().divide(cols).subtract(gridPane.getHgap()))
            );
            rect.heightProperty().bind(
                    Bindings.max(0.0, gridPane.heightProperty().divide(rows).subtract(gridPane.getVgap()))
            );

            gridPane.add(rect, uiCol, uiRow);
            currentHighlights.add(rect);
        }
    }

    /**
     * Setzt das Label für den aktiven Spieler.
     *
     * @param name Name des aktiven Spielers (nicht null)
     */
    public void setActivePlayerLabel(String name) {
        if (activePlayerLabel == null) return;

        activePlayerLabel.setText(name);

        if (game != null && game.getPlayers().length > 0) {
            int currentIndex = game.getCurrentPlayerIndex();
            PlayerColor currentColor = game.getPlayerColor(currentIndex);
            String borderColor = getPlayerColorHex(currentColor);


            activePlayerLabel.setStyle(
                    "-fx-font-size: 16; " +
                            "-fx-border-color: " + borderColor + "; " +
                            "-fx-border-width: 2px; " +
                            "-fx-border-radius: 5px; " +
                            "-fx-padding: 8px; " +
                            "-fx-background-color: " + borderColor + "20;" +  // 20 = 12% Transparenz
                            "-fx-font-weight: bold;"
            );

            scrollPane.setStyle("-fx-border-color: " + borderColor + "; -fx-border-width: 2px;");
        }
    }

    /**
     * Aktualisiert die Punkteanzeige aller Spieler.
     * <p>
     * Löscht alle alten Labels und erstellt neue für jeden Spieler.
     * </p>
     *
     * @param scores Mapping Spieler -> Punktestand (nicht null)
     */
    public void updateScoreView(Map<Player, Integer> scores) {
        if (scoresBox == null) return;

        scoresBox.getChildren().clear();

        for (Map.Entry<Player, Integer> entry : scores.entrySet()) {
            Label scoreLabel = new Label(entry.getKey().getName() + ": " + entry.getValue());
            scoresBox.getChildren().add(scoreLabel);
        }
    }

    /**
     * Zeigt das nächste zu legende Tile an.
     * <p>
     * Wird links neben dem Hauptfeld angezeigt.
     * </p>
     *
     * @param tile das nächste Tile oder null (dann wird Bild geleert)
     */
    public void showNextTile(Tile tile) {
        if (nextTileView == null) return;

        if (tile == null) {
            nextTileView.setImage(null);
            return;
        }

        String path = TileImageHelper.getImagePath(tile);
        try {
            Image image = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(path),
                            "Fehler: Bilddatei nicht gefunden: " + path)
            );
            nextTileView.setImage(image);
        } catch (Exception e) {
            System.err.println("Fehler beim Laden des Tiles: " + e.getMessage());
        }
    }

    /**
     * Aktualisiert alle Mini-Boards (rechte Seite).
     * <p>
     * Versteckt Container für nicht teilnehmende Spieler (max. 4).
     * </p>
     *
     * @param players Liste aller Spieler (nicht null, max. 4)
     */
    public void updatePlayerBoards(List<Player> players) {
        if (players == null || players.isEmpty()) return;

        GridPane[] miniBoards = {miniBoard1, miniBoard2, miniBoard3, miniBoard4};
        VBox[] containers = {miniBoard1Container, miniBoard2Container,
                miniBoard3Container, miniBoard4Container};
        Label[] labels = {miniBoard1Label, miniBoard2Label, miniBoard3Label, miniBoard4Label};

        // Alle Container verstecken
        for (VBox container : containers) {
            if (container != null) {
                container.setVisible(false);
            }
        }

        // Nur aktive Spieler anzeigen (max. 4)
        for (int i = 0; i < players.size() && i < 4; i++) {
            Player player = players.get(i);
            GridPane miniGrid = miniBoards[i];
            VBox container = containers[i];
            Label label = labels[i];

            if (miniGrid == null || container == null) continue;

            // Container sichtbar machen
            container.setVisible(true);

            // Label aktualisieren
            if (label != null) {
                label.setText(player.getName());
            }

            // Board rendern (mit Origin)
            Board board = player.getBoard();
            renderMiniBoard(miniGrid, board.snapshotTiles());
        }
    }

    /**
     * Konvertiert PlayerColor zu Hex-Farbe für CSS.
     *
     * @param color die Spielerfarbe
     * @return Hex-Farbe als String (passend zu den Spielerfarben)
     */
    private String getPlayerColorHex(PlayerColor color) {
        return switch (color) {
            case BLUE -> COLOR_HEX_BLUE;
            case GREEN -> COLOR_HEX_GREEN;
            case RED -> COLOR_HEX_RED;
            case YELLOW -> COLOR_HEX_YELLOW;
        };
    }

    /**
     * Rendert ein 5×5 Board in einem Mini-Grid.
     * <p>
     * Entfernt alte Bilder und fügt neue hinzu.
     * Im Gegensatz zum Haupt-Grid wird hier das komplette 5×5 Array 1:1 dargestellt.
     * </p>
     *
     * @param miniGrid das Mini-GridPane (nicht null)
     * @param tiles Board-Zustand (5×5, kann null-Einträge haben)
     */
    private void renderMiniBoard(GridPane miniGrid, Tile[][] tiles) {
        if (miniGrid == null || tiles == null) return;

        // Alte ImageViews entfernen
        miniGrid.getChildren().removeIf(node -> node instanceof ImageView);

        // Tiles einfügen (1:1 Mapping, keine Offset-Berechnung nötig)
        for (int r = 0; r < tiles.length && r < 5; r++) {
            for (int c = 0; c < tiles[r].length && c < 5; c++) {
                Tile t = tiles[r][c];
                if (t != null) {
                    addImageToMiniBoard(miniGrid, t, c, r);
                }
            }
        }
    }

    // ===========================================
    // Meldungen / Dialoge (Enum-basiert)
    // ===========================================

    /**
     * Übersetzt ErrorType in benutzerfreundlichen Text.
     *
     * @param errorType
     * @return
     */
    private String translateErrorType(ErrorType errorType) {
        return switch (errorType) {
            case INVALID_POSITION -> "Ungültige Position!\nBitte wähle eine markierte Position.";
            case INVALID_SELECTION -> "Ungültige Auswahl!\nDieses Plättchen kann nicht umgedreht werden.";
            case TILE_PLACEMENT_FAILED -> "Fehler beim Platzieren des Plättchens.";
            case LOAD_FAILED -> "Laden fehlgeschlagen.";
            case SAVE_FAILED -> "Speichern fehlgeschlagen.";
            case INVALID_SAVE_FILE -> "Ungültige Spielstandsdatei.";
            case NO_GAME_STARTED -> "Kein Spiel gestartet!\nBitte starte erst ein neues Spiel.";
            case ACTION_DURING_SCORING -> "Aktion nicht möglich!\nBitte schließe erst die Wertungsansicht ab.";
            case DIALOG_LOAD_FAILED -> "Dialog konnte nicht geladen werden.";
        };
    }

    /**
     * Übersetzt InfoType in benutzerfreundlichen Text.
     *
     * @param infoType
     * @param context
     * @return
     */
    private String translateInfoType(InfoType infoType, String context) {
        return switch (infoType) {
            case GAME_LOADED -> "Spiel geladen: " + context;
            case GAME_SAVED -> "Spiel gespeichert: " + context;
        };
    }

    /**
     * Zeigt einen Benutzerfehler als Dialog an.
     *
     * @param errorType
     */
    public void showUserError(ErrorType errorType) {
        String message = translateErrorType(errorType);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Zeigt einen Benutzerfehler mit Kontext als Dialog an.
     *
     * @param errorType
     * @param context
     */
    private void showUserErrorWithContext(ErrorType errorType, String context) {
        String message = translateErrorType(errorType) + "\n\nDetails: " + context;
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Zeigt eine Info-Meldung mit Kontext als Dialog an.
     *
     * @param infoType
     * @param context
     */
    private void showInfoWithContext(InfoType infoType, String context) {
        String message = translateInfoType(infoType, context);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Zeigt ein Spiel-Event als Dialog an.
     *
     * @param event
     * @param player
     */
    public void showGameEvent(GameEvent event, Player player) {
        String title;
        String content;

        switch (event) {
            case PLAYER_SKIP:
                title = "Spieler setzt aus";
                content = player.getName() + " muss aussetzen und erhält eine Nachholrunde!\n\n" +
                        "(Eigene Startkarte gezogen)";
                break;

            case CATCH_UP_ROUND_START:
                title = "Nachholrunde";
                content = player.getName() + ", jetzt ist deine Nachholrunde!\n\n";
                break;

            case PLAYER_MUST_SKIP_FULL_BOARD:
                title = "Spielfeld voll";
                content = player.getName() + " muss aussetzen!\n\n" +
                        "Dein Spielfeld hat bereits 25 Plättchen (Maximum).";
                break;

            default:
                title = "Spiel-Event";
                content = "Event: " + event;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Zeigt einen Dialog mit dem Spielergebnis an.
     *
     * @param winners
     */
    public void showWinnerDialog(List<Player> winners) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Spielende");
        alert.setHeaderText("🏆 Spiel beendet! 🏆");

        List<Player> allPlayers = Arrays.asList(game != null ? game.getPlayers() : new Player[0]);
        if (allPlayers.isEmpty()) {
            alert.setContentText("Keine Spieler gefunden.");
            alert.showAndWait();
            return;
        }

        List<Player> sortedPlayers = new ArrayList<>(allPlayers);
        sortedPlayers.sort((p1, p2) -> {
            int scoreComp = Integer.compare(p2.getScore(), p1.getScore());
            if (scoreComp != 0) return scoreComp;
            return Integer.compare(p1.getBoard().countFlippedTiles(), p2.getBoard().countFlippedTiles());
        });

        StringBuilder content = new StringBuilder();

        if (winners.size() == 1) {
            Player winner = winners.getFirst();
            content.append("🎉 Herzlichen Glückwunsch! 🎉\n");
            content.append(winner.getName()).append(" hat gewonnen!\n\n");
        } else {
            content.append("⭐ Unentschieden! ⭐\n");
            content.append("Folgende Spieler teilen sich den Sieg:\n");
            for (Player w : winners) {
                content.append("• ").append(w.getName()).append("\n");
            }
            content.append("\n");
        }

        content.append("═══════════════════════════════\n");
        content.append("ENDSTAND\n");
        content.append("═══════════════════════════════\n\n");

        int currentRank = 1;
        int lastScore = -1;
        int lastFlipped = -1;
        int displayRank = 1;

        for (Player p : sortedPlayers) {
            int score = p.getScore();
            int flipped = p.getBoard().countFlippedTiles();

            if (score != lastScore || flipped != lastFlipped) {
                displayRank = currentRank;
            }

            String line = String.format("%-3s %-15s | %2d Pkt. | %2d umgedreht\n",
                    displayRank + ".",
                    p.getName(),
                    score,
                    flipped);

            content.append(line);

            lastScore = score;
            lastFlipped = flipped;
            currentRank++;
        }

        alert.setContentText(content.toString());
        alert.getDialogPane().setMinWidth(500);
        alert.showAndWait();
    }

    // ===========================================
    // Event-Handler (FXML-gebunden)
    // ===========================================

    /**
     * Handler für Mausrad-Zoom auf dem GridPane.
     *
     * @param scrollEvent Scroll-Event (nicht  null)
     */
    @FXML
    private void onScrollWheelGridPane(ScrollEvent scrollEvent) {
        scrollEvent.consume();

        if (scrollEvent.getDeltaY() == 0) {
            return;
        }

        final double SCALE_DELTA = 1.1;
        double scaleFactor = (scrollEvent.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;

        double newScaleX = gridPane.getScaleX() * scaleFactor;
        double newScaleY = gridPane.getScaleY() * scaleFactor;

        // Min/Max Grenzen prüfen
        if (newScaleX < MIN_SCALE || newScaleX > MAX_SCALE) {
            return; // Zoom-Limit erreicht
        }

        gridPane.setScaleX(newScaleX);
        gridPane.setScaleY(newScaleY);
    }

    /**
     * Handler für Rechtsklick auf GridPane (Debug-Ausgabe).
     * <p>
     * Gibt angeklickte Zell-Koordinaten auf der Konsole aus.
     * </p>
     *
     * @param mouseEvent Maus-Event (nicht null)
     */
    @FXML
    private void onMouseClickedGridPane(MouseEvent mouseEvent) {
        // Nur bei klarem Klick (kein Drag)
        if (!mouseEvent.isStillSincePress()) {
            return;
        }

        double cellWidth = gridPane.getWidth() / gridPane.getColumnCount();
        double cellHeight = gridPane.getHeight() / gridPane.getRowCount();
        int uiCol = (int) (mouseEvent.getX() / cellWidth);
        int uiRow = (int) (mouseEvent.getY() / cellHeight);

        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            int boardCol = uiCol - uiColOffset;
            int boardRow = uiRow - uiRowOffset;

            if (game != null) {
                game.onBoardClick(boardRow, boardCol);
            }

            mouseEvent.consume();
        }
    }

    /**
     * Handler für Menü: "Neues Spiel".
     */
    @FXML
    private void handelNewGame() {
        if (isScoringActive) {
            showUserError(ErrorType.ACTION_DURING_SCORING);
            return;
        }

        if (!confirmSaveBeforeNewGame()) {
            return;
        }
        showNewGameDialog();
    }

    /**
     * Handler für Menü: "Spielstand laden".
     * <p>
     * Öffnet einen FileChooser zum Auswählen einer JSON-Datei und lädt den gespeicherten
     * Spielstand. Bei Erfolg wird das aktuelle Spiel durch den geladenen Zustand ersetzt
     * und die GUI aktualisiert (inkl. Zoom-Reset).
     * </p>
     */
    @FXML
    private void handleLoadGame() {
        if (game != null && game.isInScoringPhase()) {
            showUserError(ErrorType.ACTION_DURING_SCORING);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Spiel laden");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON-Dateien", "*.json")
        );

        var window = scrollPane.getScene() != null ? scrollPane.getScene().getWindow() : null;
        File file = chooser.showOpenDialog(window);

        if (file != null) {
            try {
                SaveGameReader.GameSaveData data = SaveGameReader.read(file);

                List<Game.PlayerData> playerDataList = new ArrayList<>();
                for (SaveGameReader.PlayerSaveData pj : data.players) {
                    playerDataList.add(new Game.PlayerData(pj.name, pj.points, pj.cards, pj.initial));
                }

                this.game = new Game(view, playerDataList, data.turn, data.nextCard);

                Platform.runLater(() -> {
                    setInitialZoomAndCenter();
                    game.checkForCombinationsAfterLoad();
                });

                showInfoWithContext(InfoType.GAME_LOADED, file.getName());

            } catch (IOException ex) {
                showUserErrorWithContext(ErrorType.LOAD_FAILED, ex.getMessage());
            } catch (IllegalArgumentException ex) {
                showUserErrorWithContext(ErrorType.INVALID_SAVE_FILE, ex.getMessage());
            } catch (Exception ex) {
                showUserErrorWithContext(ErrorType.LOAD_FAILED, ex.getMessage());
            }
        }
    }

    /**
     * Handler für Menü: "Spielstand speichern".
     * <p>
     * Öffnet einen FileChooser zum Speichern des aktuellen Spielstands als JSON-Datei.
     * </p>
     * <p>
     * Verhindert Speichern wenn:
     * <ul>
     *   <li>Kein Spiel initialisiert wurde</li>
     *   <li>Gerade eine Wertung stattfindet</li>
     * </ul>
     * </p>
     *
     * @return true wenn erfolgreich gespeichert oder vom Benutzer abgebrochen wurde
     *         (bei Fehler wird Fehlerdialog gezeigt und false zurückgegeben)
     */
    @FXML
    private boolean handleSaveGame() {
        // Prüfe ob Wertung aktiv
        if (isScoringActive) {
            showUserError(ErrorType.ACTION_DURING_SCORING);
            return false;
        }

        // Prüfe ob Spiel initialisiert
        if (game == null || !game.isGameInitialized()) {
            showUserError(ErrorType.NO_GAME_STARTED);
            return false;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Spiel speichern");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON-Dateien", "*.json"));

        // Vorgeschlagener Dateiname im Format: MopsRoyal_YYYYMMDD_HHMMSS.json
        String suggestedFilename = generateSaveFilename();
        chooser.setInitialFileName(suggestedFilename);

        var window = scrollPane.getScene() != null ? scrollPane.getScene().getWindow() : null;
        File file = chooser.showSaveDialog(window);

        if (file == null) {
            return true;
        }

        try {
            game.saveGameToFile(file);
            showInfoWithContext(InfoType.GAME_SAVED, file.getName());
            return true;
        } catch (Exception ex) {
            showUserErrorWithContext(ErrorType.SAVE_FAILED, ex.getMessage());
            return false;
        }
    }

    /**
     * Generiert einen Dateinamen im Format MopsRoyal_YYYYMMDD_HHMMSS.json
     *
     * @return vorgeschlagener Dateiname
     */
    private String generateSaveFilename() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return "MopsRoyal_" + now.format(formatter) + ".json";
    }

    /**
     * Handler für Menü: "Beenden".
     * <p>
     * Ruft den gleichen Dialog wie beim X-Button auf.
     * </p>
     */
    @FXML
    protected void handelExitGame() {
        handleWindowClose();
    }

    /**
     * Behandelt den Schließen-Request des Fensters (X-Button).
     * <p>
     * Zeigt je nach Spiel-Status unterschiedliche Dialoge an:
     * <ul>
     *   <li><b>Wertung aktiv:</b> Warnt, dass Speichern nicht möglich ist (Wertung läuft)</li>
     *   <li><b>Spiel unkonfiguriert:</b> Informiert, dass Spiel noch nicht bereit ist (kein Startplättchen)</li>
     *   <li><b>Spiel konfiguriert:</b> Fragt, ob das Spiel vor dem Beenden gespeichert werden soll</li>
     * </ul>
     * </p>
     * <p>
     * In allen Fällen kann der Benutzer mit "Abbrechen" zum Spiel zurückkehren.
     */
    public void handleWindowClose() {
        if (isScoringActive) {
            showScoringActiveDialog();
        } else if (!game.isGameConfigured()) {
            showGameNotConfiguredDialog();
        } else {
            showSaveGameDialog();
        }
    }

    /**
     * Zeigt einen Dialog an, wenn der Benutzer während einer aktiven Wertung das Fenster schließen möchte.
     * <p>
     * Dialog-Text: "Wertung läuft - Speichern nicht möglich"
     * </p>
     * <p>
     * Der Benutzer kann wählen:
     * <ul>
     *   <li><b>Nicht speichern</b> - Beendet das Spiel sofort ohne Speichern</li>
     *   <li><b>Abbrechen</b> - Kehrt zur Wertungsansicht zurück</li>
     * </ul>
     * </p>
     */
    private void showScoringActiveDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Spiel beenden");
        alert.setHeaderText("Wertung läuft - Speichern nicht möglich");
        alert.setContentText("Das Spiel kann während der Wertung nicht gespeichert werden.");

        ButtonType buttonNotSave = new ButtonType("Nicht speichern");
        ButtonType buttonCancel = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonNotSave, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == buttonNotSave) {
            Platform.exit();
        }
    }

    /**
     * Zeigt einen Dialog an, wenn das Spiel noch nicht vollständig konfiguriert ist.
     * <p>
     * Das Spiel gilt als unkonfiguriert, wenn:
     * <ul>
     *   <li>Weniger als 2 Spieler ausgewählt wurden, ODER</li>
     *   <li>Noch kein Startplättchen auf einem Board gelegt wurde</li>
     * </ul>
     * </p>
     * <p>
     * Dialog-Text: "Spielkonfiguration unvollständig - Speichern nicht möglich"
     * </p>
     * <p>
     * Der Benutzer kann wählen:
     * <ul>
     *   <li><b>Nicht speichern</b> - Beendet das Spiel sofort ohne Speichern</li>
     *   <li><b>Abbrechen</b> - Kehrt zum Setup zurück zum Konfigurieren</li>
     * </ul>
     * </p>
     *
     * @see #handleWindowClose()
     * @see Game#isGameConfigured()
     */
    private void showGameNotConfiguredDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Spiel beenden");
        alert.setHeaderText("Spielkonfiguration unvollständig - Speichern nicht möglich");
        alert.setContentText(
                "Das Spiel ist noch nicht vollständig konfiguriert.\n" +
                        "Das Spiel kann nicht gespeichert werden."
        );

        ButtonType buttonNotSave = new ButtonType("Nicht speichern");
        ButtonType buttonCancel = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonNotSave, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == buttonNotSave) {
            Platform.exit();
        }
    }

    /**
     * Zeigt einen Dialog an, wenn das Spiel konfiguriert und einsatzbereit ist.
     * <p>
     * Fragt den Benutzer, ob er das aktuelle Spiel speichern möchte, bevor es beendet wird.
     * </p>
     * <p>
     * Der Benutzer kann wählen:
     * <ul>
     *   <li><b>Speichern</b> - Speichert das aktuelle Spiel und beendet dann die Anwendung</li>
     *   <li><b>Nicht speichern</b> - Beendet die Anwendung sofort ohne Speichern</li>
     *   <li><b>Abbrechen</b> - Kehrt zum Spiel zurück</li>
     * </ul>
     * </p>
     * <p>
     * Wenn beim Speichern ein Fehler auftritt, wird eine Fehlermeldung angezeigt
     * und das Fenster bleibt offen.
     * </p>
     */
    private void showSaveGameDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Spiel beenden");
        alert.setHeaderText("Spiel speichern?");
        alert.setContentText("Möchten Sie das aktuelle Spiel speichern?");

        ButtonType buttonSave = new ButtonType("Speichern");
        ButtonType buttonDontSave = new ButtonType("Nicht speichern");
        ButtonType buttonCancel = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonSave, buttonDontSave, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == buttonSave) {
                boolean savedSuccessfully = handleSaveGame();
                if (savedSuccessfully) {
                    Platform.exit();
                }
            } else if (result.get() == buttonDontSave) {
                Platform.exit();
            }
        }
    }

    /**
     * Aktiviert den Speichern-Button nach Spielstart.
     * <p>
     * Wird von Game nach erfolgreichem startNewGame() aufgerufen.
     * </p>
     */
    public void enableSaveButton() {
        if (saveMenuItem != null) {
            saveMenuItem.setDisable(false);
        }
    }

    /**
     * Handler für Menü: "Hilfe → Bedienungsanleitung".
     * <p>
     * Zeigt Platzhalter-Dialog mit Bedienungshinweisen.
     * </p>
     */
    @FXML
    protected void handelHelpManual() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hilfe");
        alert.setHeaderText("MopsRoyal Bedienungsanleitung");
        alert.setContentText(
                "ZIEL: Sammle durch Kombinationen die meisten Punkte!\n\n" +

                        "SO SPIELST DU:\n" +
                        "1. Klicke auf eine grün markierte Position\n" +
                        "2. Bei Kombinationen (3-5 gleiche Farbe/Objekt):\n" +
                        "   → Wähle Kombination + Plättchen zum Umdrehen\n" +
                        "   → Oder überspringe die Wertung\n" +
                        "3. Automatischer Spielerwechsel\n\n" +

                        "REGELN:\n" +
                        "• Plättchen müssen waagerecht/senkrecht anliegen\n" +
                        "• Max. 5 Plättchen pro Reihe\n" +
                        "• Spielbrett: 5×5 Felder\n\n" +

                        "PUNKTE:\n" +
                        "3 Plättchen = 2 Pkt. | 4 Plättchen = 4 Pkt. | 5 Plättchen = 7 Pkt.\n" +
                        "+1 Bonuspunkt pro Krone\n\n" +

                        "STEUERUNG:\n" +
                        "Mausrad = Zoom | Ziehen = Verschieben | Klick = Platzieren\n\n" +

                        "SPIELENDE:\n" +
                        "Alle Spieler mit 25 Plättchen = Spielende\n" +
                        "→ Höchste Punktzahl gewinnt!"
        );
        alert.showAndWait();
    }

    /**
     * Handler für Menü: "Hilfe -> Kombinationen".
     * <p>
     * Zeigt Bild mit allen gültigen Kombinationen.
     * </p>
     */
    @FXML
    protected void handelHelpCombos() {
        HelpWindow.showAtImageSize("/gui/helpResponse/Kombinationen.png", "Kombinationen");
    }

    // ===========================================
    // Dialoge
    // ===========================================

    /**
     * Zeigt den "Neues Spiel"-Dialog.
     * <p>
     * Lädt PlayerSetupDialog.fxml und verarbeitet Spieler-Setup.
     * Bei Bestätigung wird {@link Game#startNewGame(List, boolean[])} aufgerufen.
     * </p>
     */
    void showNewGameDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/PlayerSetupDialog.fxml"));
            DialogPane pane = loader.load();
            PlayerSetupDialogController dlgCtl = loader.getController();

            Dialog<ButtonType> dlg = new Dialog<>();
            dlg.setTitle("Neues Spiel");
            dlg.setDialogPane(pane);

            Node okBtn = pane.lookupButton(ButtonType.OK);
            okBtn.addEventFilter(ActionEvent.ACTION, ev -> {
                try {
                    boolean[] takesPart = dlgCtl.getTakesPart();
                    List<String> names = dlgCtl.getNames();

                    PlayerSetupValidator.validateCount(takesPart);
                    PlayerSetupValidator.validateNames(names, takesPart);

                    game.startNewGame(names, takesPart);

                    Platform.runLater(this::setInitialZoomAndCenter);

                } catch (IllegalArgumentException ex) {
                    ev.consume();
                    showUserErrorWithContext(ErrorType.INVALID_SAVE_FILE, ex.getMessage());
                }
            });

            dlg.showAndWait();
        } catch (IOException io) {
            showUserErrorWithContext(ErrorType.DIALOG_LOAD_FAILED, io.getMessage());
        }
    }

    /**
     * Fragt vor "Neues Spiel", ob der aktuelle Spielstand gespeichert werden soll.
     * <p>
     * Zeigt Bestätigungsdialog mit 3 Optionen:
     * <ul>
     *   <li><b>Speichern</b> - Speichert aktuelles Spiel, dann weiter</li>
     *   <li><b>Nicht speichern</b> - Direkt weiter ohne Speichern</li>
     *   <li><b>Abbrechen</b> - Aktion abbrechen</li>
     * </ul>
     * </p>
     *
     * @return true wenn fortgefahren werden soll, false bei Abbruch
     */
    private boolean confirmSaveBeforeNewGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Neues Spiel");
        alert.setHeaderText("Aktuellen Spielstand speichern?");
        alert.setContentText("Wenn du fortfährst, wird ein neues Spiel gestartet.");

        ButtonType save = new ButtonType("Speichern");
        ButtonType dont = new ButtonType("Nicht speichern");
        ButtonType cancel = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(save, dont, cancel);
        ButtonType res = alert.showAndWait().orElse(cancel);

        if (res == save) {
            handleSaveGame();
            return true;
        } else return res == dont;
    }

    // ===========================================
    // Wertungsansicht
    // ===========================================

    /**
     * Zeigt die Wertungsansicht als Overlay rechts im Hauptfenster an.
     * <p>
     * Lädt ScoringView.fxml, initialisiert den Controller und fügt die Ansicht
     * der Hauptszene hinzu. Der Spieler kann eine Kombination auswählen, ein Plättchen
     * zum Umdrehen markieren und die Wertung bestätigen.
     * </p>
     * <p>
     * Nach Abschluss wird onComplete aufgerufen und die Ansicht kann mit
     * {@link #hideScoringOverlay()} entfernt werden.
     * </p>
     *
     * @param player der Spieler, der die Wertung durchführt (nicht null)
     * @param combinations Liste der gefundenen Kombinationen (nicht null, nicht leer)
     * @param onComplete Callback nach Abschluss der Wertung (nicht null)
     */
    public void showScoringOverlay(Player player, List<Combination> combinations, Runnable onComplete) {
        isScoringActive = true;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ScoringView.fxml"));
            scoringViewRoot = loader.load();
            scoringController = loader.getController();

            scoringController.init(game, player, combinations, onComplete);

            StackPane.setAlignment(scoringViewRoot, javafx.geometry.Pos.CENTER_RIGHT);
            StackPane.setMargin(scoringViewRoot, new javafx.geometry.Insets(80, 20, 80, 0));

            if (gridPane.getScene() != null && gridPane.getScene().getRoot() instanceof StackPane root) {
                root.getChildren().add(scoringViewRoot);
            }

        } catch (Exception e) {
            showUserErrorWithContext(ErrorType.DIALOG_LOAD_FAILED, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Entfernt die Wertungsansicht aus der Szene.
     *
     * <p>
     * Wird automatisch nach Abschluss der Wertung aufgerufen.
     * </p>
     */
    public void hideScoringOverlay() {
        isScoringActive = false;

        if (scoringViewRoot != null && gridPane.getScene() != null) {
            Node root = gridPane.getScene().getRoot();
            if (root instanceof StackPane) {
                ((StackPane) root).getChildren().remove(scoringViewRoot);
            }
            scoringViewRoot = null;
            scoringController = null;
        }
    }

    /**
     * Führt nach einer kurzen Pause den übergebenen Callback aus.
     * <p>
     * Wird von der Spiellogik aufgerufen, wenn keine Wertung stattfindet,
     * um dem Benutzer Zeit zu geben, das gelegte Plättchen zu sehen.
     * </p>
     *
     * @param onComplete Callback nach 0.5 Sekunden (nicht null)
     */
    public void scheduleNextPlayerTransition(Runnable onComplete) {
        javafx.animation.PauseTransition pause =
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(0.5));

        pause.setOnFinished(event -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });

        pause.play();
    }
}