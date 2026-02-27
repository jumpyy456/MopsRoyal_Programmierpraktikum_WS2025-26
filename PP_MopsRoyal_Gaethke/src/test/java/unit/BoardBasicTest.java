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
 * Unit Tests für Board-Grundfunktionen.
 * <p>
 * Fokus auf:
 * <ul>
 *   <li>Platzierung (placeTile)</li>
 *   <li>Abfragen (isEmpty, isOccupied, getTile, getTileCount)</li>
 *   <li>Flip-Operationen (flipTile)</li>
 * </ul>
 * </p>
 * <p>
 * Verwendet unterschiedliche Tiles (keine Duplikate) gemäß Spielregeln.
 * </p>
 */
@DisplayName("Board-Grundfunktionen")
class BoardBasicTest {

    private Board board;
    private Tile blueKissen;   // Royal
    private Tile greenNapf;    // Nicht royal
    private Tile orangeDose;   // Nicht royal

    @BeforeEach
    void setUp() {
        board = new Board();
        // Verschiedene Tiles (keine Duplikate!)
        blueKissen = Tile.of(TileColorCode.BLUE, TileObject.PILLOW);   // 1,1 - royal
        greenNapf = Tile.of(TileColorCode.GREEN, TileObject.BOWL);     // 2,3 - nicht royal
        orangeDose = Tile.of(TileColorCode.ORANGE, TileObject.CAN);    // 3,4 - nicht royal
    }

    // --- Grundfunktionen ---

    @Test
    @DisplayName("Neues Board ist leer")
    void testNewBoardIsEmpty() {
        assertEquals(0, board.getTileCount(), "Neues Board sollte 0 Tiles haben");
        assertTrue(board.isEmpty(0, 0), "Position (0,0) sollte leer sein");
    }

    @Test
    @DisplayName("Tile platzieren bei (0,0)")
    void testPlaceTileAtOrigin() {
        board.placeTile(0, 0, blueKissen);

        assertEquals(1, board.getTileCount());
        assertTrue(board.isOccupied(0, 0));
        assertEquals(blueKissen.getColor(), board.getTile(0, 0).getColor());
        assertEquals(blueKissen.getObject(), board.getTile(0, 0).getObject());
    }

    @Test
    @DisplayName("Tile platzieren an negativer Position")
    void testPlaceTileAtNegativePosition() {
        // Sollte funktionieren (HashMap erlaubt negative Koordinaten)
        board.placeTile(-2, -3, greenNapf);

        assertTrue(board.isOccupied(-2, -3));
        assertEquals(greenNapf.getColor(), board.getTile(-2, -3).getColor());
    }

    @Test
    @DisplayName("Doppelte Platzierung wirft Exception")
    void testPlaceTileTwiceThrowsException() {
        board.placeTile(0, 0, blueKissen);

        assertThrows(IllegalStateException.class, () -> board.placeTile(0, 0, greenNapf),
                "Doppelte Platzierung sollte IllegalStateException werfen");
    }

    @Test
    @DisplayName("Tile umdrehen funktioniert")
    void testFlipTile() {
        board.placeTile(0, 0, orangeDose);
        Tile stored = board.getTile(0, 0);  // Hole die Kopie
        assertFalse(stored.isFlipped());

        board.flipTile(0, 0);
        assertTrue(stored.isFlipped());  // Prüft die Kopie im Board
    }

    @Test
    @DisplayName("Flip auf leerer Position wirft Exception")
    void testFlipEmptyPositionThrowsException() {
        assertThrows(IllegalStateException.class, () -> board.flipTile(5, 5),
                "Flip auf leerer Position sollte Exception werfen");
    }

    @Test
    @DisplayName("Mehrere verschiedene Tiles platzieren")
    void testPlaceMultipleDifferentTiles() {
        board.placeTile(0, 0, blueKissen);
        board.placeTile(0, 1, greenNapf);
        board.placeTile(1, 0, orangeDose);

        assertEquals(3, board.getTileCount(), "3 verschiedene Tiles sollten platziert sein");

        // Verifiziere, dass alle Tiles unterschiedlich sind
        Tile t1 = board.getTile(0, 0);
        Tile t2 = board.getTile(0, 1);
        Tile t3 = board.getTile(1, 0);

        assertNotEquals(t1.getColor() * 10 + t1.getObject(),
                t2.getColor() * 10 + t2.getObject(),
                "Tiles sollten unterschiedlich sein");
        assertNotEquals(t2.getColor() * 10 + t2.getObject(),
                t3.getColor() * 10 + t3.getObject(),
                "Tiles sollten unterschiedlich sein");
    }
}