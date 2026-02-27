package logic;

/**
 * Repräsentiert einen Spieler in Mops Royal.
 * <p>
 * Jeder Spieler besitzt:
 * <ul>
 *   <li>Ein eigenes {@link Board} (HashMap-basiert, max. 5×5)</li>
 *   <li>Ein Startplättchen (unterschiedlich für jeden Spieler)</li>
 *   <li>Einen Punktestand</li>
 * </ul>
 * </p>
 */
public class Player {

    /** Name des Spielers. */
    private final String name;

    /** Aktueller Punktestand des Spielers. */
    private int score;

    /**
     * Anzahl der tatsächlich gelegten Plättchen (ohne Startplättchen).
     * <p>
     * Wird nach jedem erfolgreichen Platzieren inkrementiert.
     * Maximum ist 24 (da Startplättchen nicht mitzählt, aber Board = 5x5 = 25 Felder).
     * </p>
     */
    private int tilesPlaced = 0;

    /** Das eigene Board des Spielers. */
    private final Board board;

    /**
     * Das Startplättchen dieses Spielers.
     * <p>
     * Jeder Spieler erhält ein unterschiedliches Startplättchen (ohne Krone).
     * Wird bei (0,0) auf dem Board platziert.
     * </p>
     */
    private Tile startTile;

    /**
     * Erstellt einen neuen Spieler mit eigenem Board.
     *
     * @param name Spielername (nicht {@code null})
     */
    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.board = new Board();
        this.startTile = null;
    }

    /**
     * Gibt den Namen des Spielers zurück.
     *
     * @return Spielername (nicht {@code null})
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt den aktuellen Punktestand zurück.
     *
     * @return Punktestand (≥ 0)
     */
    public int getScore() {
        return score;
    }

    /**
     * Erhöht den Punktestand des Spielers.
     *
     * @param delta Punkteänderung
     */
    public void addScore(int delta) {
        this.score += delta;
    }

    /**
     * Setzt den Punktestand (z.B. beim Laden eines Spielstands).
     *
     * @param score neuer Punktestand (≥ 0)
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Gibt das eigene Board des Spielers zurück.
     *
     * @return das Board (nicht {@code null})
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Gibt das Startplättchen des Spielers zurück.
     *
     * @return das Startplättchen oder {@code null} wenn noch nicht gesetzt
     */
    public Tile getStartTile() {
        return startTile;
    }

    /**
     * Setzt das Startplättchen dieses Spielers.
     * <p>
     * Das Startplättchen wird zu Beginn des Spiels vergeben und dient als
     * Startkarte. Wenn der Spieler später diese Karte aus dem Deck zieht,
     * muss er aussetzen ({@link GameEvent#PLAYER_SKIP}).
     * </p>
     *
     * @param startTile das Startplättchen (nicht {@code null})
     */
    public void setStartTile(Tile startTile) {
        this.startTile = startTile;
    }

    /**
     * Gibt die Anzahl der gelegten Plättchen zurück (ohne Startplättchen).
     *
     * @return Anzahl gelegter Plättchen (0..24)
     */
    public int getTilesPlaced() {
        return tilesPlaced;
    }

    /**
     * Inkrementiert den Zähler für gelegte Plättchen.
     * <p>
     * Wird von {@link Game} nach jedem erfolgreichen Platzieren aufgerufen.
     * </p>
     */
    public void incrementTilesPlaced() {
        this.tilesPlaced++;
    }

    /**
     * Setzt den Zähler für gelegte Plättchen (beim Laden eines Spielstands).
     *
     * @param tilesPlaced neue Anzahl (0..24)
     */
    public void setTilesPlaced(int tilesPlaced) {
        this.tilesPlaced = tilesPlaced;
    }

    /**
     * Platziert das Startplättchen bei Position (0, 0).
     * <p>
     * Wird von {@link Game} beim Spielstart aufgerufen.
     * Das Startplättchen darf keine Krone haben.
     * </p>
     *
     * @param startTile das Startplättchen (nicht {@code null})
     * @throws NullPointerException wenn {@code startTile} null ist
     * @throws IllegalStateException wenn Position (0,0) bereits belegt ist
     */
    public void placeStartTile(Tile startTile) {
        java.util.Objects.requireNonNull(startTile, "startTile");

        // Startplättchen speichern
        this.startTile = startTile;

        // Startplättchen bei (0,0) platzieren
        board.placeTile(0, 0, startTile);
    }
}