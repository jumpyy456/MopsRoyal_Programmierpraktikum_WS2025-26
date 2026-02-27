package unit;

import logic.Board;
import logic.Tile;
import logic.TileColorCode;
import logic.TileObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für Board-Snapshot und Speichern/Laden.
 * <p>
 * Fokus auf:
 * <ul>
 *   <li>Snapshot für GUI (snapshotTiles)</li>
 *   <li>Speichern (toSaveFormat)</li>
 *   <li>Laden (loadFromSaveFormat, fromSaveFormat)</li>
 *   <li>Roundtrip-Tests (Speichern → Laden → Vergleich)</li>
 * </ul>
 * </p>
 * <p>
 * Verwendet den int-Array-Konstruktor für bessere Lesbarkeit
 * </p>
 */
@DisplayName("Board-Snapshot und Persistierung")
class BoardSnapshotTest {

    private Board board;
    private Tile blueKissen;
    private Tile greenNapf;
    private Tile orangeDose;

    @BeforeEach
    void setUp() {
        board = new Board();
        blueKissen = Tile.of(TileColorCode.BLUE, TileObject.PILLOW);   // 1,1
        greenNapf = Tile.of(TileColorCode.GREEN, TileObject.BOWL);     // 2,3
        orangeDose = Tile.of(TileColorCode.ORANGE, TileObject.CAN);    // 3,4
    }

    // --- Snapshot (GUI-Support) ---

    @Test
    @DisplayName("snapshotTiles() leeres Board")
    void testSnapshotEmptyBoard() {
        Tile[][] snapshot = board.snapshotTiles();

        assertNotNull(snapshot);
        assertEquals(5, snapshot.length);
        assertEquals(5, snapshot[0].length);

        // Alle Zellen sollten null sein
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                assertNull(snapshot[r][c], "Leeres Board: Alle Zellen sollten null sein");
            }
        }
    }

    @Test
    @DisplayName("snapshotTiles() mit Startplättchen")
    void testSnapshotWithStartTile() {
        board.placeTile(0, 0, blueKissen);

        Tile[][] snapshot = board.snapshotTiles();

        // Startplättchen sollte bei (0,0) im Array sein
        assertNotNull(snapshot[0][0],
                "Position (0,0) sollte ein Tile haben");
        assertEquals(blueKissen.getColor(), snapshot[0][0].getColor(),
                "Farbe sollte gleich sein");
        assertEquals(blueKissen.getObject(), snapshot[0][0].getObject(),
                "Objekt sollte gleich sein");

        // Rest sollte null sein
        assertNull(snapshot[0][1]);
        assertNull(snapshot[1][0]);
    }

    @Test
    @DisplayName("snapshotTiles() mit negativen Koordinaten")
    void testSnapshotWithNegativeCoordinates() {
        // Platziere unterschiedliche Tiles bei negativen Koordinaten
        board.placeTile(-1, -1, blueKissen);
        board.placeTile(-1, 0, greenNapf);
        board.placeTile(0, -1, orangeDose);

        Tile[][] snapshot = board.snapshotTiles();

        // Bounding-Box wird nach (0,0) verschoben
        assertEquals(blueKissen.getColor(), snapshot[0][0].getColor(), "(-1,-1) → (0,0) Farbe");
        assertEquals(greenNapf.getColor(), snapshot[0][1].getColor(), "(-1,0) → (0,1) Farbe");
        assertEquals(orangeDose.getColor(), snapshot[1][0].getColor(), "(0,-1) → (1,0) Farbe");
    }

    @Test
    @DisplayName("snapshotTiles() mit 5×5 Board (voll) - via int-Array")
    void testSnapshotFullBoard() {
        // Nutze int-Array-Konstruktor für bessere Lesbarkeit
        int[][] field = {
                {110, 120, 130, 140, 150},
                {210, 220, 230, 240, 250},
                {310, 320, 330, 340, 350},
                {410, 420, 430, 440, 450},
                {510, 520, 530, 540, 550}
        };
        Board fullBoard = Board.fromSaveFormat(field);

        Tile[][] snapshot = fullBoard.snapshotTiles();

        // Alle 25 Zellen sollten belegt sein
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                assertNotNull(snapshot[r][c],
                        "Position (" + r + "," + c + ") sollte belegt sein");
            }
        }
    }

    // --- Speichern ---

    @Test
    @DisplayName("toSaveFormat() leeres Board")
    void testToSaveFormatEmpty() {
        int[][] saveData = board.toSaveFormat();

        assertEquals(5, saveData.length);
        assertEquals(5, saveData[0].length);

        // Alle Zellen sollten 990 sein (leer)
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                assertEquals(990, saveData[r][c], "Leere Zelle sollte 990 sein");
            }
        }
    }

    @Test
    @DisplayName("toSaveFormat() mit Startplättchen")
    void testToSaveFormatWithStartTile() {
        // Blau (1) Kissen (1), nicht umgedreht → 110
        board.placeTile(0, 0, blueKissen);

        int[][] saveData = board.toSaveFormat();

        assertEquals(110, saveData[0][0], "Blau-Kissen sollte 110 sein");
        assertEquals(990, saveData[0][1], "Leere Zelle");
    }

    @Test
    @DisplayName("toSaveFormat() mit umgedrehtem Tile")
    void testToSaveFormatWithFlippedTile() {
        board.placeTile(0, 0, blueKissen);
        board.flipTile(0, 0);

        int[][] saveData = board.toSaveFormat();

        assertEquals(111, saveData[0][0], "Umgedrehtes Tile sollte letzte Ziffer 1 haben");
    }

    // --- Laden ---

    @Test
    @DisplayName("loadFromSaveFormat() lädt korrekt")
    void testLoadFromSaveFormat() {
        // Nutze int-Array-Konstruktor
        int[][] saveData = {
                {110, 230, 990, 990, 990},  // Blau-Kissen, Grün-Napf
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };

        board.loadFromSaveFormat(saveData);

        assertEquals(2, board.getTileCount(), "2 Tiles sollten geladen sein");
        assertTrue(board.isOccupied(0, 0));
        assertTrue(board.isOccupied(0, 1));

        Tile t1 = board.getTile(0, 0);
        assertEquals(1, t1.getColor(), "Farbe Blau");
        assertEquals(1, t1.getObject(), "Objekt Kissen");
        assertFalse(t1.isFlipped());

        Tile t2 = board.getTile(0, 1);
        assertEquals(2, t2.getColor(), "Farbe Grün");
        assertEquals(3, t2.getObject(), "Objekt Napf");
    }

    @Test
    @DisplayName("loadFromSaveFormat() mit umgedrehtem Tile")
    void testLoadFromSaveFormatWithFlippedTile() {
        int[][] saveData = {
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 451, 990},  // Rosa (4) Kackhaufen (5), umgedreht (1)
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };

        board.loadFromSaveFormat(saveData);

        Tile t = board.getTile(2, 3);
        assertNotNull(t);
        assertEquals(4, t.getColor());
        assertEquals(5, t.getObject());
        assertTrue(t.isFlipped(), "Tile sollte umgedreht sein");
    }

    @Test
    @DisplayName("loadFromSaveFormat() wirft Exception bei ungültigem Code")
    void testLoadFromSaveFormatInvalidCode() {
        int[][] saveData = {
                {790, 990, 990, 990, 990},  // Ungültige Farbe 7
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };

        assertThrows(IllegalArgumentException.class, () -> board.loadFromSaveFormat(saveData),
                "Ungültiger Code sollte Exception werfen");
    }

    @Test
    @DisplayName("fromSaveFormat() statischer Konstruktor")
    void testFromSaveFormatStaticConstructor() {
        int[][] saveData = {
                {990, 990, 990, 990, 990},
                {990, 990, 340, 990, 990},  // Orange Dose bei (1,2)
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };

        Board loaded = Board.fromSaveFormat(saveData);

        assertNotNull(loaded);
        assertEquals(1, loaded.getTileCount());
        assertTrue(loaded.isOccupied(1, 2));
    }

    // --- Roundtrip ---

    @Test
    @DisplayName("Roundtrip: toSaveFormat() → loadFromSaveFormat()")
    void testSaveLoadRoundtrip() {
        // Ausgangszustand: L-Form mit verschiedenen Tiles
        Tile pinkKnochen = Tile.of(TileColorCode.PINK, TileObject.BONE);    // 4,2
        Tile purplePoop = Tile.of(TileColorCode.PURPLE, TileObject.POOP);   // 5,5

        board.placeTile(0, 0, blueKissen);
        board.placeTile(0, 1, greenNapf);
        board.placeTile(0, 2, pinkKnochen);
        board.placeTile(1, 0, purplePoop);

        // Speichern
        int[][] saveData = board.toSaveFormat();

        // Neues Board laden
        Board loaded = Board.fromSaveFormat(saveData);

        // Vergleich
        assertEquals(board.getTileCount(), loaded.getTileCount());

        Tile[][] original = board.snapshotTiles();
        Tile[][] restored = loaded.snapshotTiles();

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (original[r][c] == null) {
                    assertNull(restored[r][c], "Leere Zelle sollte leer bleiben");
                } else {
                    assertNotNull(restored[r][c], "Belegte Zelle sollte belegt bleiben");
                    assertEquals(original[r][c].getColor(), restored[r][c].getColor());
                    assertEquals(original[r][c].getObject(), restored[r][c].getObject());
                }
            }
        }
    }
}