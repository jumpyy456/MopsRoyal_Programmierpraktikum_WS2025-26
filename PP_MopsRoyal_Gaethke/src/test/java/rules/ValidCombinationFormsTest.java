package rules;

import logic.Board;
import logic.Position;
import logic.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests für alle 18 validen Kombinationsformen.
 * <p>
 * Diese Klasse enthält exhaustive Tests für jede gültige Kombinationsform:
 * <ul>
 *   <li>3er-Kombinationen: 6 Formen (gerade Linien + L-Formen)</li>
 *   <li>4er-Kombinationen: 6 Formen (gerade Linien + L, T, Z)</li>
 *   <li>5er-Kombinationen: 6 Formen (gerade Linien + Plus, T, U)</li>
 * </ul>
 * </p>
 * <p>
 * Bei Farbkombinationen: gleiche Farbe, verschiedene Objekte.
 * Bei Objektkombinationen: gleiches Objekt, verschiedene Farben.
 * </p>
 */
@DisplayName("Valide Kombinationsformen (18 Tests)")
class ValidCombinationFormsTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    // ========== 3er-Kombinationen (6 Formen) ==========

    @Test
    @DisplayName("3er: Gerade Linie horizontal (gleiche Farbe, verschiedene Objekte)")
    void test3TilesStraightLineHorizontal() {
        // Blau mit verschiedenen Objekten
        board.placeTile(0, 0, Tile.of(1, 1));  // Blau Kissen
        board.placeTile(0, 1, Tile.of(1, 2));  // Blau Knochen
        board.placeTile(0, 2, Tile.of(1, 3));  // Blau Napf

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 3);
        assertTrue(hasCombo, "3er gerade Linie horizontal sollte valide sein");
    }

    @Test
    @DisplayName("3er: Gerade Linie vertikal (gleiche Farbe, verschiedene Objekte)")
    void test3TilesStraightLineVertical() {
        // Grün mit verschiedenen Objekten
        board.placeTile(0, 0, Tile.of(2, 1));  // Grün Kissen
        board.placeTile(1, 0, Tile.of(2, 2));  // Grün Knochen
        board.placeTile(2, 0, Tile.of(2, 3));  // Grün Napf

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 3);
        assertTrue(hasCombo, "3er gerade Linie vertikal sollte valide sein");
    }

    @Test
    @DisplayName("3er: Gerade Linie diagonal (gleiche Farbe, verschiedene Objekte)")
    void test3TilesStraightLineDiagonal() {
        // Orange mit verschiedenen Objekten
        board.placeTile(0, 0, Tile.of(3, 1));  // Orange Kissen
        board.placeTile(1, 1, Tile.of(3, 2));  // Orange Knochen
        board.placeTile(2, 2, Tile.of(3, 4));  // Orange Dose (3,3 wäre royal!)

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 3);
        assertTrue(hasCombo, "3er gerade Linie diagonal sollte valide sein");
    }

    @Test
    @DisplayName("3er: L-Form (Ecke links oben, gleiche Farbe)")
    void test3TilesLShapeTopLeft() {
        // Rosa mit verschiedenen Objekten
        board.placeTile(0, 0, Tile.of(4, 1));  // Rosa Kissen
        board.placeTile(0, 1, Tile.of(4, 2));  // Rosa Knochen
        board.placeTile(1, 0, Tile.of(4, 3));  // Rosa Napf

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 3);
        assertTrue(hasCombo, "3er L-Form (Ecke links oben) sollte valide sein");
    }

    @Test
    @DisplayName("3er: L-Form (Ecke rechts oben, gleiche Farbe)")
    void test3TilesLShapeTopRight() {
        // Lila mit verschiedenen Objekten
        board.placeTile(0, 0, Tile.of(5, 1));  // Lila Kissen
        board.placeTile(0, 1, Tile.of(5, 3));  // Lila Napf (5,2 wäre royal!)
        board.placeTile(1, 1, Tile.of(5, 4));  // Lila Dose

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 3);
        assertTrue(hasCombo, "3er L-Form (Ecke rechts oben) sollte valide sein");
    }

    @Test
    @DisplayName("3er: L-Form (Ecke links unten, gleiche Farbe)")
    void test3TilesLShapeBottomLeft() {
        // Gelb mit verschiedenen Objekten
        board.placeTile(0, 0, Tile.of(6, 1));  // Gelb Kissen
        board.placeTile(1, 0, Tile.of(6, 2));  // Gelb Knochen
        board.placeTile(1, 1, Tile.of(6, 3));  // Gelb Napf

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 3);
        assertTrue(hasCombo, "3er L-Form (Ecke links unten) sollte valide sein");
    }

    // ========== 4er-Kombinationen (6 Formen) ==========

    @Test
    @DisplayName("4er: Gerade Linie horizontal (gleiche Farbe, verschiedene Objekte)")
    void test4TilesStraightLineHorizontal() {
        // Blau mit 4 verschiedenen Objekten
        board.placeTile(0, 0, Tile.of(1, 2));  // Blau Knochen (vermeidet 1,1 royal)
        board.placeTile(0, 1, Tile.of(1, 3));  // Blau Napf
        board.placeTile(0, 2, Tile.of(1, 4));  // Blau Dose
        board.placeTile(0, 3, Tile.of(1, 5));  // Blau Kackhaufen

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 4);
        assertTrue(hasCombo, "4er gerade Linie horizontal sollte valide sein");
    }

    @Test
    @DisplayName("4er: Gerade Linie vertikal (gleiche Farbe, verschiedene Objekte)")
    void test4TilesStraightLineVertical() {
        // Grün mit 4 verschiedenen Objekten
        board.placeTile(0, 0, Tile.of(2, 1));  // Grün Kissen
        board.placeTile(1, 0, Tile.of(2, 2));  // Grün Knochen
        board.placeTile(2, 0, Tile.of(2, 3));  // Grün Napf
        board.placeTile(3, 0, Tile.of(2, 4));  // Grün Dose

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 4);
        assertTrue(hasCombo, "4er gerade Linie vertikal sollte valide sein");
    }

    @Test
    @DisplayName("4er: Gerade Linie diagonal (gleiche Farbe, verschiedene Objekte)")
    void test4TilesStraightLineDiagonal() {
        // Orange mit 4 verschiedenen Objekten
        board.placeTile(0, 0, Tile.of(3, 1));  // Orange Kissen
        board.placeTile(1, 1, Tile.of(3, 2));  // Orange Knochen
        board.placeTile(2, 2, Tile.of(3, 4));  // Orange Dose (vermeidet 3,3 royal)
        board.placeTile(3, 3, Tile.of(3, 5));  // Orange Kackhaufen

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 4);
        assertTrue(hasCombo, "4er gerade Linie diagonal sollte valide sein");
    }

    @Test
    @DisplayName("4er: L-Form groß (gleiche Farbe, verschiedene Objekte)")
    void test4TilesLargeL() {
        // Rosa mit 4 verschiedenen Objekten
        board.placeTile(0, 0, Tile.of(4, 1));  // Rosa Kissen
        board.placeTile(0, 1, Tile.of(4, 2));  // Rosa Knochen
        board.placeTile(0, 2, Tile.of(4, 3));  // Rosa Napf
        board.placeTile(1, 0, Tile.of(4, 5));  // Rosa Kackhaufen (vermeidet 4,4 royal)

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 4);
        assertTrue(hasCombo, "4er große L-Form sollte valide sein");
    }

    @Test
    @DisplayName("4er: T-Form (gleiche Farbe, verschiedene Objekte)")
    void test4TilesTShape() {
        // Lila mit 4 verschiedenen Objekten
        board.placeTile(0, 0, Tile.of(5, 1));  // Lila Kissen
        board.placeTile(0, 1, Tile.of(5, 3));  // Lila Napf (vermeidet 5,2 royal)
        board.placeTile(0, 2, Tile.of(5, 4));  // Lila Dose
        board.placeTile(1, 1, Tile.of(5, 5));  // Lila Kackhaufen

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 4);
        assertTrue(hasCombo, "4er T-Form sollte valide sein");
    }

    @Test
    @DisplayName("4er: Z-Form (Zickzack, gleiche Farbe)")
    void test4TilesZShape() {
        // Gelb mit 4 verschiedenen Objekten
        board.placeTile(0, 0, Tile.of(6, 1));  // Gelb Kissen
        board.placeTile(0, 1, Tile.of(6, 2));  // Gelb Knochen
        board.placeTile(1, 1, Tile.of(6, 3));  // Gelb Napf
        board.placeTile(1, 2, Tile.of(6, 4));  // Gelb Dose

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 4);
        assertTrue(hasCombo, "4er Z-Form sollte valide sein");
    }

    // ========== 5er-Kombinationen (6 Formen) ==========

    @Test
    @DisplayName("5er: Gerade Linie horizontal (gleiche Farbe, 5 verschiedene Objekte)")
    void test5TilesStraightLineHorizontal() {
        // Blau mit 5 verschiedenen Objekten (vermeidet 1,1 royal)
        board.placeTile(0, 0, Tile.of(1, 2));  // Blau Knochen
        board.placeTile(0, 1, Tile.of(1, 3));  // Blau Napf
        board.placeTile(0, 2, Tile.of(1, 4));  // Blau Dose
        board.placeTile(0, 3, Tile.of(1, 5));  // Blau Kackhaufen
        board.placeTile(0, 4, Tile.of(1, 6));  // Blau Mops

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertTrue(hasCombo, "5er gerade Linie horizontal sollte valide sein");
    }

    @Test
    @DisplayName("5er: Gerade Linie vertikal (gleiche Farbe, 5 verschiedene Objekte)")
    void test5TilesStraightLineVertical() {
        // Grün mit 5 verschiedenen Objekten (vermeidet 2,6 royal)
        board.placeTile(0, 0, Tile.of(2, 1));  // Grün Kissen
        board.placeTile(1, 0, Tile.of(2, 2));  // Grün Knochen
        board.placeTile(2, 0, Tile.of(2, 3));  // Grün Napf
        board.placeTile(3, 0, Tile.of(2, 4));  // Grün Dose
        board.placeTile(4, 0, Tile.of(2, 5));  // Grün Kackhaufen

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertTrue(hasCombo, "5er gerade Linie vertikal sollte valide sein");
    }

    @Test
    @DisplayName("5er: Gerade Linie diagonal (gleiche Farbe, 5 verschiedene Objekte)")
    void test5TilesStraightLineDiagonal() {
        // Orange mit 5 verschiedenen Objekten (vermeidet 3,3 royal)
        board.placeTile(0, 0, Tile.of(3, 1));  // Orange Kissen
        board.placeTile(1, 1, Tile.of(3, 2));  // Orange Knochen
        board.placeTile(2, 2, Tile.of(3, 4));  // Orange Dose
        board.placeTile(3, 3, Tile.of(3, 5));  // Orange Kackhaufen
        board.placeTile(4, 4, Tile.of(3, 6));  // Orange Mops

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertTrue(hasCombo, "5er gerade Linie diagonal sollte valide sein");
    }

    @Test
    @DisplayName("5er: Plus-Form (Kreuz, gleiche Farbe)")
    void test5TilesPlusShape() {
        // Rosa mit 5 verschiedenen Objekten (vermeidet 4,4 royal)
        board.placeTile(0, 1, Tile.of(4, 1));  // Rosa Kissen
        board.placeTile(1, 0, Tile.of(4, 2));  // Rosa Knochen
        board.placeTile(1, 1, Tile.of(4, 3));  // Rosa Napf (Zentrum)
        board.placeTile(1, 2, Tile.of(4, 5));  // Rosa Kackhaufen
        board.placeTile(2, 1, Tile.of(4, 6));  // Rosa Mops

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertTrue(hasCombo, "5er Plus-Form sollte valide sein");
    }

    @Test
    @DisplayName("5er: T-Form (gleiche Farbe, 5 verschiedene Objekte)")
    void test5TilesTShape() {
        // Lila mit 5 verschiedenen Objekten (vermeidet 5,2 royal)
        board.placeTile(0, 0, Tile.of(5, 1));  // Lila Kissen
        board.placeTile(0, 1, Tile.of(5, 3));  // Lila Napf
        board.placeTile(0, 2, Tile.of(5, 4));  // Lila Dose
        board.placeTile(1, 1, Tile.of(5, 5));  // Lila Kackhaufen
        board.placeTile(2, 1, Tile.of(5, 6));  // Lila Mops

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertTrue(hasCombo, "5er T-Form sollte valide sein");
    }

    @Test
    @DisplayName("5er: U-Form (gleiche Farbe, 5 verschiedene Objekte)")
    void test5TilesUShape() {
        // Gelb mit 5 verschiedenen Objekten (vermeidet 6,5 royal)
        board.placeTile(0, 0, Tile.of(6, 1));  // Gelb Kissen
        board.placeTile(0, 1, Tile.of(6, 2));  // Gelb Knochen
        board.placeTile(0, 2, Tile.of(6, 3));  // Gelb Napf
        board.placeTile(1, 0, Tile.of(6, 4));  // Gelb Dose
        board.placeTile(1, 2, Tile.of(6, 6));  // Gelb Mops

        List<Set<Position>> combos = board.findCombinationsByColor();
        boolean hasCombo = combos.stream().anyMatch(c -> c.size() == 5);
        assertTrue(hasCombo, "5er U-Form sollte valide sein");
    }
}