package gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import logic.*;

import java.util.*;

/**
 * Controller für die Wertungsansicht.
 * <p>
 * Zeigt alle möglichen Kombinationen an und erlaubt dem Spieler:
 * <ul>
 *   <li>Eine Kombination auszuwählen</li>
 *   <li>Ein Plättchen zum Umdrehen zu wählen</li>
 *   <li>Die Wertung zu bestätigen</li>
 *   <li>Oder die Wertung zu überspringen</li>
 * </ul>
 * </p>
 */
public class ScoringViewController {

    @FXML private VBox combinationsContainer;

    /** Referenz zur Game-Instanz. */
    private Game game;

    /** Der Spieler, der gerade wertet. */
    private Player player;

    /** Liste aller möglichen Kombinationen. */
    private List<Combination> combinations;

    /** Callback, der nach Abschluss der Wertung aufgerufen wird. */
    private Runnable onComplete;

    /** Aktuell ausgewählte Position zum Umdrehen. */
    private Position selectedPosition;

    /**
     * Initialisiert den Controller mit Daten.
     *
     * @param game die Game-Instanz (nicht {@code null})
     * @param player der wertende Spieler (nicht {@code null})
     * @param combinations alle möglichen Kombinationen (nicht {@code null})
     * @param onComplete Callback nach Abschluss (nicht {@code null})
     */
    public void init(Game game, Player player, List<Combination> combinations, Runnable onComplete) {
        this.game = Objects.requireNonNull(game, "game");
        this.player = Objects.requireNonNull(player, "player");
        this.combinations = Objects.requireNonNull(combinations, "combinations");
        this.onComplete = Objects.requireNonNull(onComplete, "onComplete");

        buildCombinationsView();
    }

    /**
     * Baut die Ansicht für alle Kombinationen auf.
     */
    private void buildCombinationsView() {
        combinationsContainer.getChildren().clear();

        for (Combination combo : combinations) {
            VBox comboBox = createCombinationView(combo);
            combinationsContainer.getChildren().add(comboBox);
        }
    }

    /**
     * Erstellt die Ansicht für eine einzelne Kombination.
     *
     * @param combo die Kombination
     * @return VBox mit Kombinations-Ansicht
     */
    private VBox createCombinationView(Combination combo) {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-background-color: #f9f9f9; -fx-padding: 10;");

        // Button "Kombination werten"
        Button scoreButton = new Button("Kombination werten");
        scoreButton.setMaxWidth(Double.MAX_VALUE);
        scoreButton.setDisable(true); // Erst aktivieren wenn Plättchen gewählt
        scoreButton.setOnAction(e -> handleScoreCombination(combo));

        // Grid für Kombination (dynamische Größe basierend auf Bounding Box)
        GridPane grid = createCombinationGrid(combo, scoreButton);

        container.getChildren().addAll(scoreButton, grid);
        return container;
    }

    /**
     * Erstellt ein Grid für eine Kombination.
     * <p>
     * Das Grid zeigt nur die relevanten Positionen der Kombination.
     * Innere Plättchen (umdrehbar) werden rot markiert, das ausgewählte grün.
     * </p>
     *
     * @param combo die Kombination
     * @param scoreButton der zugehörige "Werten"-Button (wird aktiviert bei Auswahl)
     * @return GridPane mit Kombinations-Ansicht
     */
    private GridPane createCombinationGrid(Combination combo, Button scoreButton) {
        // Bounding Box der Kombination berechnen
        Set<Position> positions = combo.positions();
        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;

        for (Position pos : positions) {
            minRow = Math.min(minRow, pos.row());
            maxRow = Math.max(maxRow, pos.row());
            minCol = Math.min(minCol, pos.col());
            maxCol = Math.max(maxCol, pos.col());
        }

        int rows = maxRow - minRow + 1;
        int cols = maxCol - minCol + 1;

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setStyle("-fx-background-color: white;");

        // Grid-Größe (max 250x250)
        double cellSize = Math.min(250.0 / Math.max(rows, cols), 80);

        // Spalten/Zeilen erstellen
        for (int c = 0; c < cols; c++) {
            ColumnConstraints col = new ColumnConstraints(cellSize);
            grid.getColumnConstraints().add(col);
        }
        for (int r = 0; r < rows; r++) {
            RowConstraints row = new RowConstraints(cellSize);
            grid.getRowConstraints().add(row);
        }

        // Tiles platzieren
        Board board = player.getBoard();
        List<Position> flippable = combo.flippablePositions();

        for (Position pos : positions) {
            int gridRow = pos.row() - minRow;
            int gridCol = pos.col() - minCol;

            Tile tile = board.getTile(pos);
            if (tile == null) continue;

            // StackPane für Tile + Overlay
            StackPane cell = new StackPane();

            // Tile-Bild
            String path = TileImageHelper.getImagePath(tile);
            try {
                Image img = new Image(
                        Objects.requireNonNull(getClass().getResourceAsStream(path),
                                "Bilddatei nicht gefunden: " + path)
                );
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(cellSize - 4);
                imgView.setFitHeight(cellSize - 4);
                imgView.setPreserveRatio(true);
                cell.getChildren().add(imgView);
            } catch (Exception e) {
                System.err.println("Fehler beim Laden: " + path);
            }

            // Overlay für umdrehbare Plättchen
            if (flippable.contains(pos)) {
                Rectangle overlay = new Rectangle(cellSize - 4, cellSize - 4);
                overlay.setFill(Color.web("#FF0000", 0.3)); // Rot mit 30% Transparenz
                overlay.setStroke(Color.RED);
                overlay.setStrokeWidth(2);
                overlay.setMouseTransparent(false);

                // Klick-Handler
                overlay.setOnMouseClicked(e -> selectFlippableTile(pos, overlay, scoreButton));

                cell.getChildren().add(overlay);

                // Speichere Referenz für spätere Zurücksetzung
                overlay.setUserData(pos);
            }

            grid.add(cell, gridCol, gridRow);
        }

        return grid;
    }

    /**
     * Handler für Auswahl eines umdrehbaren Plättchens.
     *
     * @param pos die ausgewählte Position
     * @param overlay das Overlay-Rechteck
     * @param scoreButton der "Werten"-Button
     */
    private void selectFlippableTile(Position pos, Rectangle overlay, Button scoreButton) {
        // Zurücksetzen aller anderen Overlays dieser Kombination
        resetAllOverlays();

        // Diese Position auswählen
        // Aktuell ausgewählte Kombination.
        selectedPosition = pos;

        // Overlay grün färben
        overlay.setFill(Color.web("#00FF00", 0.5)); // Grün mit 50% Transparenz
        overlay.setStroke(Color.GREEN);

        // Button aktivieren
        scoreButton.setDisable(false);
    }

    /**
     * Setzt alle Overlays auf rot zurück.
     */
    private void resetAllOverlays() {
        // Durchsuche alle Grids und setze Overlays zurück
        for (Node node : combinationsContainer.getChildren()) {
            if (node instanceof VBox comboBox) {
                for (Node child : comboBox.getChildren()) {
                    if (child instanceof GridPane grid) {
                        resetGridOverlays(grid);
                    }
                }
            }
        }
    }

    /**
     * Setzt alle Overlays in einem Grid auf rot zurück.
     *
     * @param grid das Grid
     */
    private void resetGridOverlays(GridPane grid) {
        for (Node node : grid.getChildren()) {
            if (node instanceof StackPane stack) {
                for (Node child : stack.getChildren()) {
                    if (child instanceof Rectangle overlay) {
                        overlay.setFill(Color.web("#FF0000", 0.3));
                        overlay.setStroke(Color.RED);
                    }
                }
            }
        }
    }

    /**
     * Handler für "Kombination werten"-Button.
     *
     * @param combo die zu wertende Kombination
     */
    private void handleScoreCombination(Combination combo) {
        if (selectedPosition == null) {
            return; // Sollte nicht passieren, da Button deaktiviert ist
        }

        // Wertung an Game delegieren
        game.scoreCombinationFromGUI(combo, selectedPosition);

        // Callback ausführen
        if (onComplete != null) {
            onComplete.run();
        }
    }

    /**
     * Handler für "keine Wertung vornehmen"-Button.
     */
    @FXML
    private void handleSkipScoring() {
        game.skipScoring();

        if (onComplete != null) {
            onComplete.run();
        }
    }
}