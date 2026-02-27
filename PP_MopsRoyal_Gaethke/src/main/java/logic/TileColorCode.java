package logic;

/**
 * Plättchen-Farbcodes für die 3-stellige Tile-Kodierung.
 * <p>
 * Diese Farben repräsentieren die <b>Plättchenfarben</b> (1-6) und sind
 * NICHT identisch mit den UI-Spielerfarben aus {@link PlayerColor}.
 * </p>
 * <p>
 * Verwendung: Erste Ziffer der 3-stelligen Kodierung (z.B. 610 = YELLOW-Kissen).
 * </p>
 */
public enum TileColorCode {
    /** Blau (Code 1) */
    BLUE(1),

    /** Grün (Code 2) */
    GREEN(2),

    /** Orange (Code 3) */
    ORANGE(3),

    /** Pink/Rosa (Code 4) */
    PINK(4),

    /** Lila/Violett (Code 5) */
    PURPLE(5),

    /** Gelb (Code 6) */
    YELLOW(6);

    private final int code;

    TileColorCode(int code) {
        this.code = code;
    }

    /**
     * Gibt den numerischen Farbcode zurück.
     *
     * @return Farbcode (1-6)
     */
    public int code() {
        return code;
    }
}