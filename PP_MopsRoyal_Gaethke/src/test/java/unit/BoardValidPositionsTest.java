package unit;

import logic.Board;
import logic.Position;
import logic.Tile;
import logic.TileColorCode;
import logic.TileObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für gültige Anlegepositionen (computeValidPositions).
 * <p>
 * Fokus auf:
 * <ul>
 *   <li>Orthogonale Nachbarn (nicht diagonal)</li>
 *   <li>5×5 Limit (BoundingBox)</li>
 *   <li>Ausschluss belegter Positionen</li>
 *   <li>Property-basierte Tests mit Zufallspositionen</li>
 * </ul>
 * </p>
 * <p>
 * Verwendet unterschiedliche Tiles (keine Duplikate)
 * </p>
 */
@DisplayName("Board-ValidPositions")
class BoardValidPositionsTest {

    private Board board;
    private Tile blueKissen;
    private Tile greenNapf;
    private Tile orangeDose;

    @BeforeEach
    void setUp() {
        board = new Board();
        // Verschiedene Tiles für jeden Test
        blueKissen = Tile.of(TileColorCode.BLUE, TileObject.PILLOW);
        greenNapf = Tile.of(TileColorCode.GREEN, TileObject.BOWL);
        orangeDose = Tile.of(TileColorCode.ORANGE, TileObject.CAN);
    }

    // --- Basisfälle ---

    @Test
    @DisplayName("Leeres Board → keine gültigen Positionen")
    void emptyBoard_hasNoValidPositions() {
        List<Position> v = board.computeValidPositions();
        assertTrue(v.isEmpty(), "Leeres Board muss 0 gültige Positionen liefern");
    }

    @Test
    @DisplayName("Ein Tile in der Mitte → 4 orthogonale Positionen")
    void centerTile_hasFourNeighbors() {
        board.placeTile(2, 2, blueKissen);

        // Erwartet: oben, unten, links, rechts
        Set<Position> expected = Set.of(
                new Position(1, 2),
                new Position(3, 2),
                new Position(2, 1),
                new Position(2, 3)
        );

        List<Position> v = board.computeValidPositions();
        assertEquals(expected, new LinkedHashSet<>(v));
    }

    @Test
    @DisplayName("Tile in der Ecke (0,0) → 4 orthogonale Positionen (negative Koordinaten OK)")
    void cornerTile_hasFourNeighbors() {
        board.placeTile(0, 0, blueKissen);

        Set<Position> expected = Set.of(
                new Position(-1, 0),  // oben
                new Position(1, 0),   // unten
                new Position(0, -1),  // links
                new Position(0, 1)    // rechts
        );

        List<Position> v = board.computeValidPositions();
        assertEquals(expected, new LinkedHashSet<>(v));
    }

    @Test
    @DisplayName("Tile am Rand (0,2) → 4 orthogonale Positionen")
    void edgeTile_hasFourNeighbors() {
        board.placeTile(0, 2, blueKissen);

        Set<Position> expected = Set.of(
                new Position(-1, 2),  // oben
                new Position(1, 2),   // unten
                new Position(0, 1),   // links
                new Position(0, 3)    // rechts
        );

        List<Position> v = board.computeValidPositions();
        assertEquals(expected, new LinkedHashSet<>(v));
    }

    // --- Zusammengesetzte Fälle ---

    @Test
    @DisplayName("Zwei benachbarte verschiedene Tiles → Vereinigungsmenge ohne Duplikate/Belegte")
    void twoAdjacentTiles_unionWithoutDuplicates() {
        board.placeTile(2, 2, blueKissen);
        board.placeTile(2, 3, greenNapf); // rechts daneben (anderes Tile!)

        Set<Position> expected = Set.of(
                new Position(1, 2), new Position(3, 2), new Position(2, 1),
                new Position(1, 3), new Position(3, 3), new Position(2, 4)
        );

        List<Position> v = board.computeValidPositions();
        assertEquals(expected, new LinkedHashSet<>(v));
    }

    @Test
    @DisplayName("Belegtes Feld erscheint NICHT als valide Position")
    void occupiedCell_isNotValid() {
        board.placeTile(2, 2, blueKissen);
        board.placeTile(1, 2, greenNapf); // oben belegt (verschiedenes Tile)

        List<Position> v = board.computeValidPositions();
        assertFalse(v.contains(new Position(1, 2)));
    }

    // --- Kanten-/Linien-Konstellationen ---

    @Test
    @DisplayName("Drei verschiedene Tiles entlang einer Kante (Zeile 0)")
    void threeTilesInRow_allNeighbors() {
        board.placeTile(0, 1, blueKissen);
        board.placeTile(0, 2, greenNapf);
        board.placeTile(0, 3, orangeDose);

        Set<Position> expected = Set.of(
                // Oben:
                new Position(-1, 1), new Position(-1, 2), new Position(-1, 3),
                // Unten:
                new Position(1, 1), new Position(1, 2), new Position(1, 3),
                // Links/Rechts:
                new Position(0, 0), new Position(0, 4)
        );

        List<Position> v = board.computeValidPositions();
        assertEquals(expected, new LinkedHashSet<>(v));
    }

    // --- Valide Positionen nach Startplättchen ---

    @Test
    @DisplayName("Startplättchen bei (0,0): 4 valide Nachbarn")
    void testValidPositionsAfterStartTile() {
        board.placeTile(0, 0, blueKissen);

        List<Position> valid = board.computeValidPositions();

        assertEquals(4, valid.size(), "Startplättchen sollte 4 orthogonale Nachbarn haben");
        assertTrue(valid.contains(new Position(-1, 0)), "Oben sollte valide sein");
        assertTrue(valid.contains(new Position(1, 0)), "Unten sollte valide sein");
        assertTrue(valid.contains(new Position(0, -1)), "Links sollte valide sein");
        assertTrue(valid.contains(new Position(0, 1)), "Rechts sollte valide sein");
    }

    @Test
    @DisplayName("Nur nach links legen: sollte funktionieren")
    void testLayingOnlyToTheLeft() {
        board.placeTile(0, 0, blueKissen);

        List<Position> valid1 = board.computeValidPositions();
        assertTrue(valid1.contains(new Position(0, -1)), "Links sollte valide sein");
        board.placeTile(0, -1, greenNapf);

        List<Position> valid2 = board.computeValidPositions();
        assertTrue(valid2.contains(new Position(0, -2)), "Weiter links sollte valide sein");
        board.placeTile(0, -2, orangeDose);

        List<Position> valid3 = board.computeValidPositions();
        assertTrue(valid3.contains(new Position(0, -3)), "Noch weiter links sollte valide sein");

        assertEquals(3, board.getTileCount(), "3 Tiles sollten platziert sein");
    }

    // --- 5×5 Limit ---

    @Test
    @DisplayName("5×5-Limit: Horizontal (5 verschiedene Tiles in einer Reihe)")
    void testFiveInARowHorizontalLimit() {
        Tile pinkKnochen = Tile.of(TileColorCode.PINK, TileObject.BONE);
        Tile purplePoop = Tile.of(TileColorCode.PURPLE, TileObject.POOP);

        board.placeTile(0, 0, blueKissen);
        board.placeTile(0, -1, greenNapf);
        board.placeTile(0, 1, orangeDose);
        board.placeTile(0, -2, pinkKnochen);
        board.placeTile(0, 2, purplePoop);

        List<Position> valid = board.computeValidPositions();

        assertFalse(valid.contains(new Position(0, -3)),
                "Position links außen sollte nicht valide sein (5×5 Limit)");
        assertFalse(valid.contains(new Position(0, 3)),
                "Position rechts außen sollte nicht valide sein (5×5 Limit)");

        // Aber vertikal sollte noch Platz sein
        assertTrue(valid.contains(new Position(-1, 0)), "Oben sollte valide sein");
        assertTrue(valid.contains(new Position(1, 0)), "Unten sollte valide sein");
    }

    @Test
    @DisplayName("5×5-Limit: Vertikal (5 verschiedene Tiles in einer Spalte)")
    void testFiveInAColumnVerticalLimit() {
        Tile pinkKnochen = Tile.of(TileColorCode.PINK, TileObject.BONE);
        Tile purplePoop = Tile.of(TileColorCode.PURPLE, TileObject.POOP);

        board.placeTile(0, 0, blueKissen);
        board.placeTile(-1, 0, greenNapf);
        board.placeTile(1, 0, orangeDose);
        board.placeTile(-2, 0, pinkKnochen);
        board.placeTile(2, 0, purplePoop);

        List<Position> valid = board.computeValidPositions();

        assertFalse(valid.contains(new Position(-3, 0)),
                "Position oben außen sollte nicht valide sein");
        assertFalse(valid.contains(new Position(3, 0)),
                "Position unten außen sollte nicht valide sein");

        // Horizontal sollte noch Platz sein
        assertTrue(valid.contains(new Position(0, -1)), "Links sollte valide sein");
        assertTrue(valid.contains(new Position(0, 1)), "Rechts sollte valide sein");
    }

    @Test
    @DisplayName("5×5-Limit: L-Form mit 4 verschiedenen Tiles")
    void testLShapeValidPositions() {
        Tile pinkKnochen = Tile.of(TileColorCode.PINK, TileObject.BONE);

        board.placeTile(0, 0, blueKissen);
        board.placeTile(0, 1, greenNapf);
        board.placeTile(0, 2, orangeDose);
        board.placeTile(1, 0, pinkKnochen);

        List<Position> valid = board.computeValidPositions();

        assertTrue(valid.contains(new Position(-1, 0)), "Oben von (0,0)");
        assertTrue(valid.contains(new Position(0, -1)), "Links von (0,0)");
        assertTrue(valid.contains(new Position(2, 0)), "Unten von (1,0)");
        assertTrue(valid.contains(new Position(1, 1)), "Rechts von (1,0)");

        assertTrue(valid.contains(new Position(0, 3)), "Weiter rechts");
        assertTrue(valid.contains(new Position(1, -1)), "Weitere Expansion");
    }

    @Test
    @DisplayName("8 verschiedene Tiles in O-Form (Mitte frei)")
    void testOShapeWithHoleInMiddle() {
        // Nutze int-Array für komplexes Setup
        int[][] field = {
                {110, 120, 130, 990, 990},  // Blau-Kissen, Blau-Knochen, Blau-Napf
                {210, 990, 230, 990, 990},  // Grün-Kissen, (leer), Grün-Napf
                {310, 320, 330, 990, 990},  // Orange-Kissen, Orange-Knochen, Orange-Napf
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };
        Board oBoard = Board.fromSaveFormat(field);

        List<Position> valid = oBoard.computeValidPositions();

        assertTrue(valid.contains(new Position(1, 1)),
                "Mitte des O sollte valide sein");

        assertEquals(8, oBoard.getTileCount(), "8 Tiles sollten platziert sein");
    }

    @Test
    @DisplayName("Diagonale Nachbarn sind NICHT valide für Anlegung")
    void testDiagonalNeighborsAreNotValidForPlacement() {
        board.placeTile(0, 0, blueKissen);

        List<Position> valid = board.computeValidPositions();

        assertFalse(valid.contains(new Position(-1, -1)), "Diagonal oben-links nicht für Anlegung");
        assertFalse(valid.contains(new Position(-1, 1)), "Diagonal oben-rechts nicht für Anlegung");
        assertFalse(valid.contains(new Position(1, -1)), "Diagonal unten-links nicht für Anlegung");
        assertFalse(valid.contains(new Position(1, 1)), "Diagonal unten-rechts nicht für Anlegung");
    }

    // --- Property-basierter Test ---

    @Test
    @DisplayName("Property: Jede orthogonale leere Nachbar ist im Ergebnis enthalten (Stichprobe)")
    void property_everyOrthogonalEmptyNeighborIsIncluded() {
        for (int seed = 1; seed <= 5; seed++) {
            Board b = new Board();
            java.util.Random rnd = new java.util.Random(seed);

            int placements = 5 + rnd.nextInt(6);
            int placed = 0;
            while (placed < placements) {
                int r = rnd.nextInt(5);
                int c = rnd.nextInt(5);
                if (b.getTile(r, c) == null) {
                    // Verschiedene Tiles basierend auf Position
                    int color = (r + c) % 6 + 1;
                    int object = (r * c + 1) % 6 + 1;
                    b.placeTile(r, c, Tile.of(color, object));
                    placed++;
                }
            }

            List<Position> valid = b.computeValidPositions();
            Set<Position> validSet = new LinkedHashSet<>(valid);

            // 1) Ergebnis enthält KEINE belegten Felder
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 5; c++) {
                    if (b.getTile(r, c) != null) {
                        assertFalse(validSet.contains(new Position(r, c)),
                                "Valide Menge darf belegte Zelle nicht enthalten: (" + r + "," + c + ")");
                    }
                }
            }

            // 2) Für jede belegte Zelle sind alle orthogonalen leeren Nachbarn enthalten
            int[] dr = {-1, 1, 0, 0};
            int[] dc = {0, 0, -1, 1};
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 5; c++) {
                    if (b.getTile(r, c) == null) continue;

                    for (int k = 0; k < 4; k++) {
                        int nr = r + dr[k];
                        int nc = c + dc[k];
                        if (nr >= 0 && nr < 5 && nc >= 0 && nc < 5 && b.getTile(nr, nc) == null) {
                            Position p = new Position(nr, nc);
                            assertTrue(validSet.contains(p),
                                    "Fehlender orthogonaler Nachbar in validSet: " + p + " von (" + r + "," + c + ")");
                        }
                    }
                }
            }
        }
    }
}