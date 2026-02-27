package logic;

/**
 * Spielerfarben in fester Sitzreihenfolge.
 * <p>
 * Diese Farben werden für die UI-Darstellung der Spieler verwendet (z.B. farbige
 * Rahmen um Mini-Boards). Sie sind NICHT identisch mit den Plättchenfarben aus
 * {@link TileColorCode}.
 * </p>
 * <p>
 * Die Reihenfolge entspricht der Spieler-Nummerierung (0-3 bzw. 1-4).
 * </p>
 */
public enum PlayerColor {
    /** Spieler 1 - Blau */
    BLUE,

    /** Spieler 2 - Grün */
    GREEN,

    /** Spieler 3 - Rot */
    RED,

    /** Spieler 4 - Gelb */
    YELLOW
}