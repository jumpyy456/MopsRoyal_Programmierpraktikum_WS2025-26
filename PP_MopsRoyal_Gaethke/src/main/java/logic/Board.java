package logic;

import java.util.*;

/**
 * Repräsentiert das Spielfeld eines Spielers in Mops Royal.
 * <p>
 * Ein Board verwaltet Tiles in einer HashMap mit Position als Key.
 * Dies ermöglicht flexible Platzierung während des Spiels.
 * Am Ende muss alles in ein 5×5-Feld passen.
 * </p>
 *
 * <h3>Hauptfunktionen:</h3>
 * <ul>
 *   <li>Platzieren und Verwalten von Tiles</li>
 *   <li>Berechnen gültiger Anlegepositionen (mit 5×5-Check)</li>
 *   <li>Speichern/Laden (JSON-Format)</li>
 * </ul>
 *
 * <p>
 * <b>Kombinations-Erkennung:</b> Wurde in {@link CombinationFinder} ausgelagert.
 * </p>
 */
public class Board {

    // --- Konstanten ---
    /** Maximale Anzahl der Zeilen (5×5 Endgröße). */
    public static final int ROWS = 5;
    /** Maximale Anzahl der Spalten (5×5 Endgröße). */
    public static final int COLS = 5;
    /** Leere Zelle. */
    public static final int EMPTY_CELL_CODE = 990;

    // --- Felder ---
    /** HashMap für flexible Tile-Platzierung. Key: Position, Value: Tile */
    private final Map<Position, Tile> tiles = new HashMap<>();

    /**
     * Erstellt ein neues, leeres Board.
     */
    public Board() {}

    // ===========================================
    // Queries (prüfen/lesen, keine Seiteneffekte)
    // ===========================================

    /**
     * Prüft, ob an einer Position ein Tile liegt.
     *
     * @param pos Position (nicht {@code null})
     * @return {@code true}, wenn Position belegt ist
     */
    public boolean isOccupied(Position pos) {
        return tiles.containsKey(pos);
    }

    /**
     * Prüft, ob an einer Position ein Tile liegt.
     *
     * @param r Zeilenindex
     * @param c Spaltenindex
     * @return {@code true}, wenn Position belegt ist
     */
    public boolean isOccupied(int r, int c) {
        return isOccupied(new Position(r, c));
    }

    /**
     * Prüft, ob eine Position leer ist.
     *
     * @param pos Position (nicht {@code null})
     * @return {@code true}, wenn Position leer ist
     */
    public boolean isEmpty(Position pos) {
        return !isOccupied(pos);
    }

    /**
     * Prüft, ob eine Position leer ist.
     *
     * @param r Zeilenindex
     * @param c Spaltenindex
     * @return {@code true}, wenn Position leer ist
     */
    public boolean isEmpty(int r, int c) {
        return isEmpty(new Position(r, c));
    }

    /**
     * Gibt das Tile an einer Position zurück.
     *
     * @param pos Position (nicht {@code null})
     * @return das {@link Tile} oder {@code null}, wenn leer
     */
    public Tile getTile(Position pos) {
        return tiles.get(pos);
    }

    /**
     * Gibt das Tile an einer Position zurück.
     *
     * @param r Zeilenindex
     * @param c Spaltenindex
     * @return das {@link Tile} oder {@code null}, wenn leer
     */
    public Tile getTile(int r, int c) {
        return getTile(new Position(r, c));
    }

    /**
     * Gibt die Anzahl der platzierten Tiles zurück.
     *
     * @return Anzahl der Tiles (≥ 0)
     */
    public int getTileCount() {
        return tiles.size();
    }

    // ===========================================
    // Mutations (verändern den Board-Zustand)
    // ===========================================

    /**
     * Platziert ein Tile auf dem Spielfeld.
     * <p>
     * <b>WICHTIG:</b> Es wird eine Kopie des Tiles gespeichert, nicht das Original.
     * </p>
     *
     * @param pos Position (nicht {@code null})
     * @param tile zu platzierendes Plättchen (nicht {@code null})
     * @throws IllegalStateException wenn Feld bereits belegt ist
     */
    public void placeTile(Position pos, Tile tile) {
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(tile, "tile");

        if (isOccupied(pos)) {
            throw new IllegalStateException("Feld bereits belegt bei " + pos);
        }

        tiles.put(pos, tile.copy());
    }

    /**
     * Platziert ein Tile auf dem Spielfeld.
     *
     * @param r Zeilenindex
     * @param c Spaltenindex
     * @param tile zu platzierendes Plättchen (nicht {@code null})
     */
    public void placeTile(int r, int c, Tile tile) {
        placeTile(new Position(r, c), tile);
    }

    /**
     * Dreht das Plättchen an der gegebenen Position um.
     *
     * @param pos Position (nicht {@code null})
     * @throws IllegalStateException wenn Feld leer ist
     */
    public void flipTile(Position pos) {
        Tile t = getTile(pos);
        if (t == null) {
            throw new IllegalStateException("Kein Tile an " + pos);
        }
        t.flip();
    }

    /**
     * Dreht das Plättchen an der gegebenen Position um.
     *
     * @param r Zeilenindex
     * @param c Spaltenindex
     */
    public void flipTile(int r, int c) {
        flipTile(new Position(r, c));
    }

    // ===========================================
    // Berechnungen
    // ===========================================

    /**
     * Zählt die Anzahl der umgedrehten Plättchen.
     * <p>
     * Wird für den Tie-Breaker bei Punktgleichstand verwendet.
     * </p>
     *
     * @return Anzahl der umgedrehten Tiles (0-25)
     */
    public int countFlippedTiles() {
        int count = 0;
        for (Tile tile : tiles.values()) {
            if (tile.isFlipped()) {
                count++;
            }
        }
        return count;
    }
    /**
     * Berechnet alle aktuell gültigen Anlegepositionen.
     * <p>
     * Eine Position gilt als regelkonform, wenn sie:
     * <ul>
     *   <li>orthogonal an mindestens ein bestehendes Tile angrenzt,</li>
     *   <li>noch nicht belegt ist und</li>
     *   <li>nach Platzierung alle Tiles noch in ein 5×5-Feld passen.</li>
     * </ul>
     * </p>
     *
     * @return Liste aller gültigen Positionen
     */
    public List<Position> computeValidPositions() {
        Set<Position> candidates = new LinkedHashSet<>();

        for (Position occupied : tiles.keySet()) {
            for (int k = 0; k < Direction.ORTHOGONAL_COUNT; k++) {
                Position neighbor = new Position(
                        occupied.row() + Direction.ORTHOGONAL_DR[k],
                        occupied.col() + Direction.ORTHOGONAL_DC[k]
                );

                if (isEmpty(neighbor)) {
                    candidates.add(neighbor);
                }
            }
        }

        List<Position> result = new ArrayList<>();
        for (Position candidate : candidates) {
            if (fitsIn5x5WithNewTile(candidate)) {
                result.add(candidate);
            }
        }

        return result;
    }

    /**
     * Ermittelt alle aktuell belegbaren Positionen für das nächste Plättchen.
     *
     * @return Menge aller legalen Zielpositionen
     */
    public Set<Position> getPlayablePositions() {
        return new LinkedHashSet<>(computeValidPositions());
    }

    // ===========================================
    // Kombinationen (delegiert an CombinationFinder)
    // ===========================================

    /**
     * Findet alle gültigen Kombinationen mit <b>gleicher Farbe</b>.
     *
     * @return Liste von Mengen mit je 3-5 Positionen
     */
    public List<Set<Position>> findCombinationsByColor() {
        return CombinationFinder.findCombinationsByColor(this);
    }

    /**
     * Findet alle gültigen Kombinationen mit <b>gleichem Objekt</b>.
     *
     * @return Liste von Mengen mit je 3-5 Positionen
     */
    public List<Set<Position>> findCombinationsByObject() {
        return CombinationFinder.findCombinationsByObject(this);
    }

    /**
     * Ermittelt alle umdrehbaren Plättchen in einer Kombination.
     *
     * @param combo die zu prüfende Kombination (nicht {@code null})
     * @return Liste der Positionen umdrehbarer Plättchen
     */
    public List<Position> getFlippableTilesInCombination(Set<Position> combo) {
        return CombinationFinder.getFlippableTilesInCombination(combo);
    }

    // ===========================================
    // Bounding Box (intern)
    // ===========================================

    private int[] getBoundingBox() {
        if (tiles.isEmpty()) return null;

        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;

        for (Position pos : tiles.keySet()) {
            minRow = Math.min(minRow, pos.row());
            maxRow = Math.max(maxRow, pos.row());
            minCol = Math.min(minCol, pos.col());
            maxCol = Math.max(maxCol, pos.col());
        }

        return new int[]{minRow, maxRow, minCol, maxCol};
    }

    /**
     * Prüft, ob alle Tiles + eine neue Position in ein 5×5-Feld passen.
     */
    private boolean fitsIn5x5WithNewTile(Position newPos) {
        if (tiles.isEmpty()) return true;

        int[] box = getBoundingBox();
        if (box == null) return true;

        int minRow = Math.min(box[0], newPos.row());
        int maxRow = Math.max(box[1], newPos.row());
        int minCol = Math.min(box[2], newPos.col());
        int maxCol = Math.max(box[3], newPos.col());

        int height = maxRow - minRow + 1;
        int width = maxCol - minCol + 1;

        return height <= ROWS && width <= COLS;
    }

    // ===========================================
    // GUI-Support
    // ===========================================

    /**
     * Gibt die Position der oberen linken Ecke der Bounding-Box zurück.
     *
     * @return Position (minRow, minCol) oder {@code null} wenn Board leer
     */
    public Position getSnapshotOrigin() {
        if (tiles.isEmpty()) return null;

        int[] box = getBoundingBox();
        if (box == null) return null;

        return new Position(box[0], box[2]);
    }

    /**
     * Erstellt ein 5×5 Array für die GUI-Darstellung.
     *
     * @return 5×5 Array mit Tiles (nicht {@code null})
     */
    public Tile[][] snapshotTiles() {
        Tile[][] result = new Tile[ROWS][COLS];

        if (tiles.isEmpty()) {
            return result;
        }

        int[] box = getBoundingBox();
        if (box == null) return result;

        int minRow = box[0];
        int minCol = box[2];

        for (Map.Entry<Position, Tile> entry : tiles.entrySet()) {
            Position pos = entry.getKey();
            Tile tile = entry.getValue();

            int arrayRow = pos.row() - minRow;
            int arrayCol = pos.col() - minCol;

            if (arrayRow >= 0 && arrayRow < ROWS && arrayCol >= 0 && arrayCol < COLS) {
                result[arrayRow][arrayCol] = tile;
            }
        }

        return result;
    }

    // ===========================================
    // Speichern/Laden Support
    // ===========================================

    /**
     * Konvertiert Board zu 5×5 int-Array für JSON-Speicherung.
     * <p>
     * Format pro Zelle: {@code Farbe*100 + Objekt*10 + Flipped}
     * <br>Leere Zellen: {@code 990}
     * </p>
     *
     * @return 5×5 int-Array (nicht {@code null})
     */
    public int[][] toSaveFormat() {
        int[][] result = new int[ROWS][COLS];

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                result[r][c] = EMPTY_CELL_CODE;
            }
        }

        if (tiles.isEmpty()) {
            return result;
        }

        int[] box = getBoundingBox();
        if (box == null) return result;

        int minRow = box[0];
        int minCol = box[2];

        for (Map.Entry<Position, Tile> entry : tiles.entrySet()) {
            Position pos = entry.getKey();
            Tile tile = entry.getValue();

            int arrayRow = pos.row() - minRow;
            int arrayCol = pos.col() - minCol;

            if (arrayRow >= 0 && arrayRow < ROWS && arrayCol >= 0 && arrayCol < COLS) {
                int code = tile.getColor() * 100 + tile.getObject() * 10 + (tile.isFlipped() ? 1 : 0);
                result[arrayRow][arrayCol] = code;
            }
        }

        return result;
    }

    /**
     * Lädt Board aus 5×5 int-Array (aus JSON).
     *
     * @param data 5×5 int-Array (nicht {@code null})
     * @throws IllegalArgumentException bei ungültigem Format
     */
    public void loadFromSaveFormat(int[][] data) {
        Objects.requireNonNull(data, "data");

        if (data.length != ROWS) {
            throw new IllegalArgumentException("Array muss " + ROWS + " Zeilen haben");
        }

        tiles.clear();

        for (int r = 0; r < ROWS; r++) {
            if (data[r].length != COLS) {
                throw new IllegalArgumentException("Zeile " + r + " muss " + COLS + " Spalten haben");
            }

            for (int c = 0; c < COLS; c++) {
                int code = data[r][c];

                if (code == EMPTY_CELL_CODE) {
                    continue;
                }

                int color = code / 100;
                int object = (code % 100) / 10;
                boolean flipped = (code % 10) == 1;

                if (color < 1 || color > 6 || object < 1 || object > 6) {
                    throw new IllegalArgumentException(
                            "Ungültiger Code " + code + " an Position (" + r + "," + c + ")"
                    );
                }

                Tile tile = Tile.of(color, object);
                if (flipped) {
                    tile.flip();
                }

                tiles.put(new Position(r, c), tile);
            }
        }
    }

    /**
     * Konstruktor für Laden aus Spielstand.
     *
     * @param data 5×5 int-Array (nicht {@code null})
     * @return neues Board mit geladenen Tiles
     */
    public static Board fromSaveFormat(int[][] data) {
        Board board = new Board();
        board.loadFromSaveFormat(data);
        return board;
    }
}