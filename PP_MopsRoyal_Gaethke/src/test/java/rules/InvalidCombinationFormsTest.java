package rules;

import logic.Board;
import logic.Position;
import logic.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests für ungültige Kombinationsformen und Edge Cases.
 */
@DisplayName("Invalide Kombinationsformen und Edge Cases (12 Tests)")
class InvalidCombinationFormsTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    @DisplayName("V-Form mit gespreizten Beinen sollte NICHT valide sein")
    void testVShapeWithSpreadLegs() {
        board.placeTile(0, 2, Tile.of(1, 2));
        board.placeTile(1, 1, Tile.of(1, 3));
        board.placeTile(1, 3, Tile.of(1, 4));
        board.placeTile(2, 0, Tile.of(1, 5));
        board.placeTile(2, 4, Tile.of(1, 6));

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasInvalidFiveCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertFalse(hasInvalidFiveCombo);
    }

    @Test
    @DisplayName("Zickzack mit nur schwachen Verbindungen sollte NICHT valide sein")
    void testZigZagWithWeakConnections() {
        board.placeTile(0, 0, Tile.of(2, 1));
        board.placeTile(1, 1, Tile.of(2, 2));
        board.placeTile(2, 0, Tile.of(2, 3));
        board.placeTile(3, 1, Tile.of(2, 4));
        board.placeTile(4, 0, Tile.of(2, 5));

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasInvalidFiveCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertFalse(hasInvalidFiveCombo);
    }

    @Test
    @DisplayName("Große L-Form (4x3 Bounding Box) sollte NICHT valide sein")
    void testLargeLShape4x3() {
        board.placeTile(0, 0, Tile.of(3, 1));
        board.placeTile(1, 0, Tile.of(3, 2));
        board.placeTile(2, 0, Tile.of(3, 4));
        board.placeTile(3, 0, Tile.of(3, 5));
        board.placeTile(3, 2, Tile.of(3, 6));

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasInvalidFiveCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertFalse(hasInvalidFiveCombo);
    }

    @Test
    @DisplayName("Umgekehrte T-Form mit zu großer Bounding Box (3x4) sollte NICHT valide sein")
    void testInvertedTShape3x4() {
        board.placeTile(0, 0, Tile.of(4, 1));
        board.placeTile(0, 1, Tile.of(4, 2));
        board.placeTile(0, 2, Tile.of(4, 3));
        board.placeTile(1, 1, Tile.of(4, 5));
        board.placeTile(3, 1, Tile.of(4, 6));

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasInvalidFiveCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertFalse(hasInvalidFiveCombo);
    }

    @Test
    @DisplayName("Verstreute Form in 4x4 Bounding Box sollte NICHT valide sein")
    void testScatteredFormIn4x4() {
        board.placeTile(0, 0, Tile.of(5, 1));
        board.placeTile(0, 3, Tile.of(5, 3));
        board.placeTile(1, 1, Tile.of(5, 4));
        board.placeTile(1, 2, Tile.of(5, 5));
        board.placeTile(2, 3, Tile.of(5, 6));

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasInvalidFiveCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertFalse(hasInvalidFiveCombo);
    }

    @Test
    @DisplayName("Haken-Form mit schwacher Verbindung sollte NICHT valide sein")
    void testHookShapeWithWeakConnection() {
        board.placeTile(0, 0, Tile.of(6, 1));
        board.placeTile(0, 1, Tile.of(6, 2));
        board.placeTile(0, 2, Tile.of(6, 3));
        board.placeTile(1, 2, Tile.of(6, 4));
        board.placeTile(2, 1, Tile.of(6, 6));

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasInvalidFiveCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertFalse(hasInvalidFiveCombo);
    }

    @Test
    @DisplayName("3x3 Form ohne stark verbundenes Zentrum sollte NICHT valide sein")
    void test3x3WithoutStrongCenter() {
        board.placeTile(0, 0, Tile.of(1, 2));
        board.placeTile(0, 1, Tile.of(1, 3));
        board.placeTile(1, 2, Tile.of(1, 4));
        board.placeTile(2, 0, Tile.of(1, 5));
        board.placeTile(2, 1, Tile.of(1, 6));

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasInvalidFiveCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertFalse(hasInvalidFiveCombo);
    }

    @Test
    @DisplayName("Gebrochene Linie mit Gap sollte NICHT valide sein")
    void testBrokenLineWithGap() {
        board.placeTile(0, 0, Tile.of(2, 1));
        board.placeTile(0, 1, Tile.of(2, 2));
        board.placeTile(0, 3, Tile.of(2, 3));
        board.placeTile(0, 4, Tile.of(2, 4));

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasInvalidFiveCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertFalse(hasInvalidFiveCombo);
    }

    @Test
    @DisplayName("VALIDE: Gerade 5er-Linie horizontal sollte erkannt werden")
    void testValidStraightLineHorizontal() {
        board.placeTile(0, 0, Tile.of(3, 1));
        board.placeTile(0, 1, Tile.of(3, 2));
        board.placeTile(0, 2, Tile.of(3, 4));
        board.placeTile(0, 3, Tile.of(3, 5));
        board.placeTile(0, 4, Tile.of(3, 6));

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasFiveCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertTrue(hasFiveCombo);
    }

    @Test
    @DisplayName("VALIDE: Plus-Form sollte erkannt werden")
    void testValidPlusShape() {
        board.placeTile(0, 1, Tile.of(4, 1));
        board.placeTile(1, 0, Tile.of(4, 2));
        board.placeTile(1, 1, Tile.of(4, 3));
        board.placeTile(1, 2, Tile.of(4, 5));
        board.placeTile(2, 1, Tile.of(4, 6));

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasFiveCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertTrue(hasFiveCombo);
    }

    @Test
    @DisplayName("VALIDE: T-Form sollte erkannt werden")
    void testValidTShape() {
        board.placeTile(0, 0, Tile.of(5, 1));
        board.placeTile(0, 1, Tile.of(5, 3));
        board.placeTile(0, 2, Tile.of(5, 4));
        board.placeTile(1, 1, Tile.of(5, 5));
        board.placeTile(2, 1, Tile.of(5, 6));

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasFiveCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertTrue(hasFiveCombo);
    }

    @Test
    @DisplayName("VALIDE: Diagonale 5er-Linie sollte erkannt werden")
    void testValidDiagonalLine() {
        board.placeTile(0, 0, Tile.of(6, 1));
        board.placeTile(1, 1, Tile.of(6, 2));
        board.placeTile(2, 2, Tile.of(6, 3));
        board.placeTile(3, 3, Tile.of(6, 4));
        board.placeTile(4, 4, Tile.of(6, 6));

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasFiveCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertTrue(hasFiveCombo);
    }
}