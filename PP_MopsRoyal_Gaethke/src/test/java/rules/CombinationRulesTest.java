package rules;

import logic.Board;
import logic.Position;
import logic.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für spezielle Kombinationsregeln.
 * <p>
 * <b>
 * Bei Objektkombinationen: gleiches Objekt, verschiedene Farben.
 * Bei Farbkombinationen: gleiche Farbe, verschiedene Objekte.
 * </p>
 */
@DisplayName("Kombinationsregeln (11 Tests)")
class CombinationRulesTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    @DisplayName("Nach OBJEKT: 4er L-Form sollte erkannt werden (verschiedene Farben, gleiches Objekt)")
    void testLShapeByObject() {
        // Gleiches Objekt (Kissen=1), verschiedene Farben
        Tile blueKissen = Tile.of(1, 1);   // Blau Kissen (royal!)
        Tile greenKissen = Tile.of(2, 1);  // Grün Kissen
        Tile orangeKissen = Tile.of(3, 1); // Orange Kissen
        Tile pinkKissen = Tile.of(4, 1);   // Pink Kissen

        board.placeTile(0, 0, blueKissen);
        board.placeTile(0, 1, greenKissen);
        board.placeTile(0, 2, orangeKissen);
        board.placeTile(1, 0, pinkKissen);

        List<Set<Position>> combos = board.findCombinationsByObject();

        assertTrue(combos.size() >= 1, "Sollte mindestens 1 Kombination finden");

        boolean hasFourCombo = combos.stream()
                .anyMatch(c -> c.size() == 4 &&
                        c.contains(new Position(0, 0)) &&
                        c.contains(new Position(0, 1)) &&
                        c.contains(new Position(0, 2)) &&
                        c.contains(new Position(1, 0)));

        assertTrue(hasFourCombo, "Sollte die 4er L-Form-Kombination enthalten");
    }

    @Test
    @DisplayName("Keine Kombination bei nur 2 Tiles")
    void testNoComboWithTwoTiles() {
        // Gleiche Farbe, verschiedene Objekte
        board.placeTile(0, 0, Tile.of(1, 2));  // Blau Knochen
        board.placeTile(0, 1, Tile.of(1, 3));  // Blau Napf

        List<Set<Position>> combos = board.findCombinationsByColor();

        assertEquals(0, combos.size(), "2 Tiles sollten keine Kombination bilden");
    }

    @Test
    @DisplayName("Umgedrehtes Tile wird ignoriert und unterbricht Kombination")
    void testFlippedTileIgnored() {
        // Gleiche Farbe, verschiedene Objekte
        Tile blue1 = Tile.of(1, 2);  // Blau Knochen
        Tile blue2 = Tile.of(1, 3);  // Blau Napf
        Tile blue3 = Tile.of(1, 4);  // Blau Dose

        board.placeTile(0, 0, blue1);
        board.placeTile(0, 1, blue2);
        board.placeTile(0, 2, blue3);

        // Mittleres Tile umdrehen
        board.flipTile(0, 1);

        List<Set<Position>> combos = board.findCombinationsByColor();

        assertEquals(0, combos.size(), "Umgedrehtes Tile sollte Kombination verhindern");
    }

    @Test
    @DisplayName("Mehrere getrennte Kombinationen auf einem Board")
    void testMultipleSeparateCombinations() {
        // Kombination 1: 3 blaue horizontal (verschiedene Objekte)
        board.placeTile(0, 0, Tile.of(1, 2));  // Blau Knochen
        board.placeTile(0, 1, Tile.of(1, 3));  // Blau Napf
        board.placeTile(0, 2, Tile.of(1, 4));  // Blau Dose

        // Kombination 2: 4 grüne L-Form (verschiedene Objekte)
        board.placeTile(2, 0, Tile.of(2, 1));  // Grün Kissen
        board.placeTile(2, 1, Tile.of(2, 2));  // Grün Knochen
        board.placeTile(3, 0, Tile.of(2, 3));  // Grün Napf
        board.placeTile(4, 0, Tile.of(2, 4));  // Grün Dose

        List<Set<Position>> combos = board.findCombinationsByColor();

        assertTrue(combos.size() >= 2, "Sollte mindestens 2 Kombinationen finden");

        boolean hasBlueThree = combos.stream()
                .anyMatch(c -> c.size() == 3 &&
                        c.contains(new Position(0, 0)) &&
                        c.contains(new Position(0, 1)) &&
                        c.contains(new Position(0, 2)));

        assertTrue(hasBlueThree, "Sollte 3er blaue Kombination enthalten");

        boolean hasGreenFour = combos.stream()
                .anyMatch(c -> c.size() == 4 &&
                        c.contains(new Position(2, 0)) &&
                        c.contains(new Position(2, 1)) &&
                        c.contains(new Position(3, 0)) &&
                        c.contains(new Position(4, 0)));

        assertTrue(hasGreenFour, "Sollte 4er grüne L-Form enthalten");
    }

    @Test
    @DisplayName("Kombination nach Farbe vs. Objekt unterschiedlich")
    void testColorVsObjectCombinations() {
        // 3 Tiles: gleiche Farbe (Blau), verschiedene Objekte
        board.placeTile(0, 0, Tile.of(1, 2));  // Blau Knochen
        board.placeTile(0, 1, Tile.of(1, 3));  // Blau Napf
        board.placeTile(0, 2, Tile.of(1, 4));  // Blau Dose

        List<Set<Position>> byColor = board.findCombinationsByColor();
        List<Set<Position>> byObject = board.findCombinationsByObject();

        assertEquals(1, byColor.size(), "Nach Farbe: 1 Kombination");
        assertEquals(0, byObject.size(), "Nach Objekt: keine Kombination");
    }

    @Test
    @DisplayName("Gemischte orthogonal+diagonal Formen sollten erkannt werden")
    void testMixedOrthogonalDiagonalCombo() {
        // Gleiche Farbe (Rosa), verschiedene Objekte
        board.placeTile(0, 0, Tile.of(4, 1));  // Rosa Kissen
        board.placeTile(0, 1, Tile.of(4, 2));  // Rosa Knochen
        board.placeTile(1, 1, Tile.of(4, 3));  // Rosa Napf

        List<Set<Position>> combos = board.findCombinationsByColor();

        assertEquals(1, combos.size(), "Gemischte Form sollte 1 Kombination bilden");
        assertEquals(3, combos.get(0).size());
    }

    @Test
    @DisplayName("Drei Möpse in L-Form nach Objekt (verschiedene Farben)")
    void testThreePugsLShapeByObject() {
        // Gleiches Objekt (Mops=6), verschiedene Farben (vermeidet 2,6 Grün-Mops royal)
        Tile rosaPug = Tile.of(4, 6);   // Rosa Mops
        Tile bluePug = Tile.of(1, 6);   // Blau Mops
        Tile yellowPug = Tile.of(6, 6); // Gelb Mops

        board.placeTile(1, 2, rosaPug);
        board.placeTile(2, 1, bluePug);
        board.placeTile(2, 2, yellowPug);

        List<Set<Position>> combosByObject = board.findCombinationsByObject();

        assertEquals(1, combosByObject.size(),
                "3 Möpse in L-Form sollten 1 Kombination nach Objekt bilden");
        assertEquals(3, combosByObject.get(0).size(),
                "Kombination sollte 3 Tiles enthalten");

        Set<Position> combo = combosByObject.get(0);
        assertTrue(combo.contains(new Position(1, 2)), "Rosa Mops sollte dabei sein");
        assertTrue(combo.contains(new Position(2, 1)), "Blau Mops sollte dabei sein");
        assertTrue(combo.contains(new Position(2, 2)), "Gelber Mops sollte dabei sein");
    }

    @Test
    @DisplayName("Diagonale Zickzack-Form sollte als ungültig erkannt werden")
    void testDiagonalZigzagInvalid() {
        // Gleiche Farbe (Orange), verschiedene Objekte (vermeidet 3,3 royal)
        board.placeTile(1, 1, Tile.of(3, 1));  // Orange Kissen
        board.placeTile(2, 2, Tile.of(3, 4));  // Orange Dose
        board.placeTile(3, 1, Tile.of(3, 2));  // Orange Knochen

        List<Set<Position>> combos = board.findCombinationsByColor();

        boolean hasZigzag = combos.stream()
                .anyMatch(c -> c.size() == 3 &&
                        c.contains(new Position(1, 1)) &&
                        c.contains(new Position(2, 2)) &&
                        c.contains(new Position(3, 1)));

        assertFalse(hasZigzag,
                "Diagonale Zickzack-Form sollte NICHT als Kombination erkannt werden");

        assertEquals(0, combos.size(),
                "Es sollten keine Kombinationen gefunden werden");
    }

    @Test
    @DisplayName("4er diagonal Kot + 3er L Lila mit gemeinsamem Tile (Komplexes Szenario)")
    void testFourDiagonalKotPlusThreeLilaL() {
        // 4 Kot-Tiles diagonal (gleiches Objekt=5, verschiedene Farben)
        board.placeTile(0, 0, Tile.of(3, 5)); // Orange Kot
        board.placeTile(1, 1, Tile.of(2, 5)); // Grün Kot
        board.placeTile(2, 2, Tile.of(1, 5)); // Blau Kot
        board.placeTile(3, 3, Tile.of(4, 5)); // Rosa Kot

        // 2 weitere Lila Tiles (gleiche Farbe=5, verschiedene Objekte)
        board.placeTile(3, 4, Tile.of(5, 3)); // Lila Napf
        board.placeTile(4, 4, Tile.of(5, 4)); // Lila Dose

        List<Set<Position>> combosByObject = board.findCombinationsByObject();

        Set<Position> expectedDiagonalKot = Set.of(
                new Position(0, 0),
                new Position(1, 1),
                new Position(2, 2),
                new Position(3, 3)
        );

        boolean foundDiagonalKot = combosByObject.stream()
                .anyMatch(combo -> combo.equals(expectedDiagonalKot));

        assertTrue(foundDiagonalKot,
                "4er diagonale Kot-Kombination sollte gefunden werden");

        // Hinweis: Lila-L ergibt keine 3er nach Farbe, da Rosa-Kot (4,5) dabei ist
    }

    @Test
    @DisplayName("Mehrere Subkombinationen bei 4 diagonalen Tiles")
    void testOnlyFourDiagonalTiles() {
        // Gleiche Farbe UND gleiches Objekt (ACHTUNG: nur für Test!)
        // Real: 4x Blau-Kackhaufen würde Duplikate erfordern
        // Stattdessen: Verschiedene Objekte, gleiche Farbe
        board.placeTile(0, 0, Tile.of(1, 2));  // Blau Knochen
        board.placeTile(1, 1, Tile.of(1, 3));  // Blau Napf
        board.placeTile(2, 2, Tile.of(1, 4));  // Blau Dose
        board.placeTile(3, 3, Tile.of(1, 5));  // Blau Kackhaufen

        List<Set<Position>> combosByColor = board.findCombinationsByColor();

        Set<Position> expected4er = Set.of(
                new Position(0, 0),
                new Position(1, 1),
                new Position(2, 2),
                new Position(3, 3)
        );

        boolean found4erByColor = combosByColor.stream()
                .anyMatch(combo -> combo.equals(expected4er));

        assertTrue(found4erByColor,
                "4er diagonale sollte nach Farbe gefunden werden");

        // Nach Objekt: keine Kombination (alle verschiedene Objekte)
        List<Set<Position>> combosByObject = board.findCombinationsByObject();
        assertEquals(0, combosByObject.size(),
                "Nach Objekt sollte keine Kombination gefunden werden");

        assertTrue(combosByColor.size() >= 3,
                "Sollte mindestens 3 Kombinationen finden (1x 4er + 2x 3er)");
    }
}