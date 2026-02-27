package logic;

/**
 * Zentrale Konstanten für Richtungen auf dem Spielfeld.
 * <p>
 * Diese Klasse konsolidiert alle Richtungs-Arrays, die für die Navigation
 * und Kombinations-Erkennung auf dem Board verwendet werden.
 * </p>
 * <p>
 * <b>Verwendung:</b>
 * <pre>
 * for (int k = 0; k < Direction.ORTHOGONAL_COUNT; k++) {
 *     Position neighbor = new Position(
 *         pos.row() + Direction.ORTHOGONAL_DR[k],
 *         pos.col() + Direction.ORTHOGONAL_DC[k]
 *     );
 * }
 * </pre>
 * </p>
 */
public final class Direction {

    // Privater Konstruktor verhindert Instanziierung
    private Direction() {}

    // ===========================================
    // Orthogonale Richtungen (4): Oben, Unten, Links, Rechts
    // ===========================================

    /** Anzahl orthogonaler Richtungen. */
    public static final int ORTHOGONAL_COUNT = 4;

    /** Zeilen-Offsets für orthogonale Nachbarn (oben, unten, links, rechts). */
    public static final int[] ORTHOGONAL_DR = {-1, 1, 0, 0};

    /** Spalten-Offsets für orthogonale Nachbarn (oben, unten, links, rechts). */
    public static final int[] ORTHOGONAL_DC = {0, 0, -1, 1};

    // ===========================================
    // Diagonale Richtungen (4): Ecken
    // ===========================================

    /** Anzahl diagonaler Richtungen. */
    public static final int DIAGONAL_COUNT = 4;

    /** Zeilen-Offsets für diagonale Nachbarn (oben-links, oben-rechts, unten-links, unten-rechts). */
    public static final int[] DIAGONAL_DR = {-1, -1, 1, 1};

    /** Spalten-Offsets für diagonale Nachbarn (oben-links, oben-rechts, unten-links, unten-rechts). */
    public static final int[] DIAGONAL_DC = {-1, 1, -1, 1};

    // ===========================================
    // Alle 8 Richtungen (orthogonal + diagonal)
    // ===========================================

    /** Anzahl aller Richtungen (orthogonal + diagonal). */
    public static final int ALL_COUNT = 8;

    /** Zeilen-Offsets für alle 8 Nachbarn. */
    public static final int[] ALL_DR = {-1, -1, -1, 0, 0, 1, 1, 1};

    /** Spalten-Offsets für alle 8 Nachbarn. */
    public static final int[] ALL_DC = {-1, 0, 1, -1, 1, -1, 0, 1};
}