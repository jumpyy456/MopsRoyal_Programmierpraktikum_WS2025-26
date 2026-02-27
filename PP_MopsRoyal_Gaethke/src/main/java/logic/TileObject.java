package logic;

/**
 * Plättchen-Objekte für die 3-stellige Tile-Kodierung.
 * <p>
 * Verwendung: Zweite Ziffer der 3-stelligen Kodierung (z.B. 610 = Gelb-PILLOW).
 * </p>
 */
public enum TileObject {
    /** Kissen (Code 1) */
    PILLOW(1),

    /** Knochen (Code 2) */
    BONE(2),

    /** Napf (Code 3) */
    BOWL(3),

    /** Dose (Code 4) */
    CAN(4),

    /** Kackhaufen (Code 5) */
    POOP(5),

    /** Mops (Code 6) */
    PUG(6);

    private final int code;

    TileObject(int code) {
        this.code = code;
    }

    /**
     * Gibt den numerischen Objektcode zurück.
     *
     * @return Objektcode (1-6)
     */
    public int code() {
        return code;
    }
}