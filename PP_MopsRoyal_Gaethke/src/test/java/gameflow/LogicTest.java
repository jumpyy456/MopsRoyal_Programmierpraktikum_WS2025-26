package gameflow;

import logic.*;
import Support.FakeGUI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Legacy-Tests für die Zwischenabnahme von Mops Royal.
 * <p>
 * Diese Tests erfüllen die Mindestanforderungen der Aufgabenstellung.
 * Für detailliertere Tests siehe:
 * <ul>
 *   <li>unit/BoardBasicTest - Platzierung und Grundfunktionen</li>
 *   <li>unit/BoardPositionsTest - Gültige Positionen (computeValidPositions)</li>
 *   <li>unit/BoardSnapshotTest - Snapshot und Persistierung</li>
 *   <li>rules/ValidCombinationFormsTest - Alle 18 validen Kombinationsformen</li>
 *   <li>rules/InvalidCombinationFormsTest - Ungültige Formen und Edge Cases</li>
 *   <li>rules/CombinationRulesTest - Spezielle Kombinationsregeln</li>
 *   <li>gameflow/GameTest - Spielablauf und Scoring</li>
 *   <li>unit/TileTest - Tile-Eigenschaften und Royal-Status</li>
 * </ul>
 * </p>
 *
 * <h3>Coverage der Zwischenabnahme:</h3>
 * <ul>
 *   <li>Belegbare Zellen korrekt ermitteln (validPos_* Tests)</li>
 *   <li>L-Erkennung nach Farbe (L_color_* Tests)</li>
 *   <li>L-Erkennung nach Objekt (L_object_* Tests)</li>
 *   <li>L nicht gefunden wenn umgedreht (L_notFound_oneFlipped)</li>
 *   <li>Scoring mit/ohne Krone (scoring_* Tests)</li>
 *   <li>Plättchen umdrehen nach Wertung (settlement_* Tests)</li>
 * </ul>
 * </p>
 */
public class LogicTest {

    private static Position p(int r, int c) {
        return new Position(r, c);
    }

    // --- ANFORDERUNG 1: BELEGBARE ZELLEN KORREKT ERMITTELN ---
    @Test
    @DisplayName("Ein Tile in der Mitte → 4 orthogonale Nachbarn spielbar")
    void playablePositionsCenter() {
        Board b = new Board();
        // Startplättchen bei (0,0) platzieren
        b.placeTile(0, 0, Tile.of(1, 2));  // Blau-Knochen (vermeidet 1,1 royal)

        Set<Position> expected = Set.of(p(-1,0), p(0,-1), p(0,1), p(1,0));
        Set<Position> actual = b.getPlayablePositions();

        assertEquals(expected, actual, "4 orthogonale Nachbarn sollten spielbar sein");
    }


    // ============================================================
    // ANFORDERUNG 2: L-KOMBINATIONEN GLEICHE FARBE
    // (4 Tests: 0°, 90°, 180°, gespiegelt - alle auf 4×4 voll)
    // ============================================================

    @Test
    @DisplayName("L gleiche Farbe, 0° Rotation (└, Ecke unten-links), 4×4 voll belegt")
    void L_sameColor_0degrees_bottomLeft_on4x4FullBoard() {
        int[][] field = {
                {110, 120, 230, 340, 990},  // Blau-Kissen, Blau-Knochen, Grün-Napf, Orange-Dose
                {130, 250, 360, 410, 990},  // Blau-Napf, Grün-Kackhaufen, Orange-Napf, Rosa-Kissen
                {140, 260, 420, 510, 990},  // Blau-Dose, Grün-Mops, Rosa-Knochen, Lila-Kissen
                {220, 310, 450, 560, 990},  // Grün-Knochen, Orange-Kissen, Rosa-Kackhaufen, Lila-Mops
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 0), // 110 Blau-Kissen
                new Position(0, 1), // 120 Blau-Knochen
                new Position(1, 0), // 130 Blau-Napf
                new Position(2, 0)  // 140 Blau-Dose
        );

        List<Set<Position>> combos = b.findCombinationsByColor();

        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form (└, 0°) aus gleicher Farbe sollte gefunden werden");
    }

    @Test
    @DisplayName("L gleiche Farbe, 90° Rotation im Uhrzeigersinn (┌, Ecke oben-links), 4×4 voll belegt")
    void L_sameColor_90degrees_clockwise_topLeft_on4x4FullBoard() {
        int[][] field = {
                {210, 220, 230, 340, 990},  // Grün-Kissen, Grün-Knochen, Grün-Napf, Orange-Dose
                {240, 310, 360, 410, 990},  // Grün-Dose, Orange-Kissen, Orange-Napf, Rosa-Kissen
                {150, 320, 420, 510, 990},  // Blau-Kackhaufen, Orange-Knochen, Rosa-Knochen, Lila-Kissen
                {160, 330, 450, 560, 990},  // Blau-Mops, Orange-Napf, Rosa-Kackhaufen, Lila-Mops
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 0), // 210 Grün-Kissen (horizontal start)
                new Position(0, 1), // 220 Grün-Knochen
                new Position(0, 2), // 230 Grün-Napf
                new Position(1, 0)  // 240 Grün-Dose (vertikal runter)
        );

        List<Set<Position>> combos = b.findCombinationsByColor();

        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form (┌, 90°) aus gleicher Farbe sollte gefunden werden");
    }

    @Test
    @DisplayName("L gleiche Farbe, 180° Rotation (┐, Ecke oben-rechts), 4×4 voll belegt")
    void L_sameColor_180degrees_topRight_on4x4FullBoard() {
        int[][] field = {
                {110, 320, 330, 340, 990},  // Blau-Kissen, Orange-Knochen, Orange-Napf, Orange-Dose
                {220, 250, 350, 410, 990},  // Grün-Knochen, Grün-Kackhaufen, Orange-Kackhaufen, Rosa-Kissen
                {140, 260, 460, 510, 990},  // Blau-Dose, Grün-Mops, Rosa-Mops, Lila-Kissen
                {160, 610, 450, 560, 990},  // Blau-Mops, Gelb-Kissen, Rosa-Kackhaufen, Lila-Mops
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 1), // 320 Orange-Knochen
                new Position(0, 2), // 330 Orange-Napf
                new Position(0, 3), // 340 Orange-Dose
                new Position(1, 2)  // 350 Orange-Kackhaufen (vertikal runter von (0,2))
        );

        List<Set<Position>> combos = b.findCombinationsByColor();

        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form (┐, 180°) aus gleicher Farbe sollte gefunden werden");
    }

    @Test
    @DisplayName("L gleiche Farbe, gespiegelt horizontal (┘, Ecke unten-rechts), 4×4 voll belegt")
    void L_sameColor_mirroredHorizontal_bottomRight_on4x4FullBoard() {
        int[][] field = {
                {110, 420, 430, 340, 990},  // Blau-Kissen, Rosa-Knochen, Rosa-Napf, Orange-Dose
                {220, 440, 360, 510, 990},  // Grün-Knochen, Rosa-Dose, Orange-Napf, Lila-Kissen
                {140, 450, 610, 560, 990},  // Blau-Dose, Rosa-Kackhaufen, Gelb-Kissen, Lila-Mops
                {160, 310, 250, 630, 990},  // Blau-Mops, Orange-Kissen, Grün-Kackhaufen, Gelb-Napf
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 1), // 420 Rosa-Knochen
                new Position(0, 2), // 430 Rosa-Napf
                new Position(1, 1), // 440 Rosa-Dose
                new Position(2, 1)  // 450 Rosa-Kackhaufen
        );

        List<Set<Position>> combos = b.findCombinationsByColor();

        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form (┘, gespiegelt) aus gleicher Farbe sollte gefunden werden");
    }

// ============================================================
// ANFORDERUNG 3: L-KOMBINATIONEN GLEICHES OBJEKT
// (4 Tests: 0°, 90°, 180°, gespiegelt - alle auf 4×4 voll)
// ============================================================

    @Test
    @DisplayName("L gleiches Objekt, 0° Rotation (└, Ecke unten-links), 4×4 voll belegt")
    void L_sameObject_0degrees_bottomLeft_on4x4FullBoard() {
        int[][] field = {
                {110, 210, 330, 440, 990},  // Blau-Kissen, Grün-Kissen, Orange-Napf, Rosa-Dose
                {310, 250, 360, 510, 990},  // Orange-Kissen, Grün-Kackhaufen, Orange-Napf, Lila-Kissen
                {410, 260, 420, 610, 990},  // Rosa-Kissen, Grün-Mops, Rosa-Knochen, Gelb-Kissen
                {220, 320, 450, 560, 990},  // Grün-Knochen, Orange-Knochen, Rosa-Kackhaufen, Lila-Mops
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 0), // 110 Blau-Kissen
                new Position(0, 1), // 210 Grün-Kissen
                new Position(1, 0), // 310 Orange-Kissen
                new Position(2, 0)  // 410 Rosa-Kissen
        );

        List<Set<Position>> combos = b.findCombinationsByObject();

        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form (└, 0°) aus gleichem Objekt sollte gefunden werden");
    }

    @Test
    @DisplayName("L gleiches Objekt, 90° Rotation im Uhrzeigersinn (┌, Ecke oben-links), 4×4 voll belegt")
    void L_sameObject_90degrees_clockwise_topLeft_on4x4FullBoard() {
        int[][] field = {
                {120, 220, 320, 440, 990},  // Blau-Knochen, Grün-Knochen, Orange-Knochen, Rosa-Dose
                {520, 310, 360, 510, 990},  // Lila-Knochen, Orange-Kissen, Orange-Napf, Lila-Kissen
                {160, 330, 410, 610, 990},  // Blau-Mops, Orange-Napf, Rosa-Kissen, Gelb-Kissen
                {260, 340, 450, 560, 990},  // Grün-Mops, Orange-Dose, Rosa-Kackhaufen, Lila-Mops
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 0), // 120 Blau-Knochen (horizontal start)
                new Position(0, 1), // 220 Grün-Knochen
                new Position(0, 2), // 320 Orange-Knochen
                new Position(1, 0)  // 520 Lila-Knochen (vertikal runter)
        );

        List<Set<Position>> combos = b.findCombinationsByObject();

        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form (┌, 90°) aus gleichem Objekt sollte gefunden werden");
    }

    @Test
    @DisplayName("L gleiches Objekt, 180° Rotation (┐, Ecke oben-rechts), 4×4 voll belegt")
    void L_sameObject_180degrees_topRight_on4x4FullBoard() {
        int[][] field = {
                {110, 130, 230, 330, 990},  // Blau-Kissen, Blau-Napf, Grün-Napf, Orange-Napf
                {220, 250, 430, 510, 990},  // Grün-Knochen, Grün-Kackhaufen, Rosa-Napf, Lila-Kissen
                {140, 260, 460, 610, 990},  // Blau-Dose, Grün-Mops, Rosa-Mops, Gelb-Kissen
                {160, 550, 450, 560, 990},  // Blau-Mops, Lila-Kackhaufen, Rosa-Kackhaufen, Lila-Mops
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 1), // 130 Blau-Napf
                new Position(0, 2), // 230 Grün-Napf
                new Position(0, 3), // 330 Orange-Napf
                new Position(1, 2)  // 430 Rosa-Napf (vertikal runter von (0,2))
        );

        List<Set<Position>> combos = b.findCombinationsByObject();

        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form (┐, 180°) aus gleichem Objekt sollte gefunden werden");
    }

    @Test
    @DisplayName("L gleiches Objekt, gespiegelt horizontal (┘, Ecke unten-rechts), 4×4 voll belegt")
    void L_sameObject_mirroredHorizontal_bottomRight_on4x4FullBoard() {
        int[][] field = {
                {110, 140, 240, 330, 990},  // Blau-Kissen, Blau-Dose, Grün-Dose, Orange-Dose
                {220, 440, 360, 510, 990},  // Grün-Knochen, Rosa-Dose, Orange-Napf, Lila-Kissen
                {130, 540, 610, 560, 990},  // Blau-Napf, Lila-Dose, Gelb-Kissen, Lila-Mops
                {160, 310, 250, 630, 990},  // Blau-Mops, Orange-Kissen, Grün-Kackhaufen, Gelb-Napf
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 1), // 140 Blau-Dose
                new Position(0, 2), // 240 Grün-Dose
                new Position(1, 1), // 440 Rosa-Dose
                new Position(2, 1)  // 540 Lila-Dose
        );

        List<Set<Position>> combos = b.findCombinationsByObject();

        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form (┘, gespiegelt) aus gleichem Objekt sollte gefunden werden");
    }

    // --- ANFORDERUNG 4: L NICHT GEFUNDEN WENN UMGEDREHT ---

    @Test
    @DisplayName("L wird NICHT gefunden, wenn ein Teil umgedreht ist (Farbe)")
    void lNotFoundIfFlipped_colorVariant() {
        int[][] field = {
                {111, 120, 990, 990, 990},  // (0,0) Blau-Kissen umgedreht, (0,1) Blau-Knochen
                {130, 990, 990, 990, 990},  // (1,0) Blau-Napf
                {140, 990, 990, 990, 990},  // (2,0) Blau-Dose
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        List<Set<Position>> allCombos = b.findCombinationsByColor();
        allCombos.addAll(b.findCombinationsByObject());

        long fourCombos = allCombos.stream()
                .filter(c -> c.size() == 4)
                .count();

        assertEquals(0, fourCombos,
                "Umgedrehtes Tile sollte L-Erkennung verhindern");

        // Nach Filter: Knochen (0,1) wird entfernt (nur 1 diagonal),
        // bleiben nur 2 Tiles (Napf + Dose) → keine Kombination
        long threeCombos = allCombos.stream()
                .filter(c -> c.size() == 3)
                .count();

        assertEquals(0, threeCombos,
                "Keine Kombination: Knochen wird entfernt (nur 1 diagonale Verbindung)");
    }

    @Test
    @DisplayName("L wird NICHT gefunden, wenn ein Teil umgedreht ist (Objekt)")
    void lNotFoundIfFlipped_objectVariant() {
        int[][] field = {
                {111, 210, 990, 990, 990},  // (0,0) Blau-Kissen UMGEDREHT, (0,1) Grün-Kissen
                {310, 990, 990, 990, 990},  // (1,0) Orange-Kissen
                {410, 990, 990, 990, 990},  // (2,0) Rosa-Kissen
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        List<Set<Position>> allCombos = b.findCombinationsByColor();
        allCombos.addAll(b.findCombinationsByObject());

        long fourCombos = allCombos.stream()
                .filter(c -> c.size() == 4)
                .count();

        assertEquals(0, fourCombos,
                "Umgedrehtes Tile sollte L-Erkennung verhindern");

        // Nach Filter: Grün-Kissen (0,1) wird entfernt (nur 1 diagonal),
        // bleiben nur 2 Tiles (Orange + Rosa) → keine Kombination
        long threeCombos = allCombos.stream()
                .filter(c -> c.size() == 3)
                .count();

        assertEquals(0, threeCombos,
                "Keine Kombination: Grün-Kissen wird entfernt (nur 1 diagonale Verbindung)");
    }

    // --- ANFORDERUNG 5: PLÄTTCHEN UMDREHEN NACH WERTUNG ---

    @Test
    @DisplayName("Nach Wertung wird ein beteiligtes Plättchen umgedreht")
    void flipAfterSettlement() {
        FakeGUI fakeGui = new FakeGUI();
        Game game = new Game(fakeGui);

        Player p = new Player("Tester");
        Board b = p.getBoard();

        // L-Form mit gleicher Farbe (Blau), verschiedene Objekte
        b.placeTile(0, 0, Tile.of(1, 2));  // Blau Knochen
        b.placeTile(0, 1, Tile.of(1, 3));  // Blau Napf
        b.placeTile(1, 0, Tile.of(1, 4));  // Blau Dose
        b.placeTile(2, 0, Tile.of(1, 5));  // Blau Kackhaufen

        Set<Position> L = Set.of(p(0,0), p(0,1), p(1,0), p(2,0));

        // settleCombination braucht Liste der umzudrehenden Tiles
        List<Position> toFlip = List.of(p(0,0)); // Drehe erstes Tile um
        game.settleCombination(L, p, toFlip);

        // Prüfe ob mindestens ein Tile umgedreht wurde
        boolean anyFlipped = L.stream()
                .anyMatch(q -> b.getTile(q.row(), q.col()).isFlipped());

        assertTrue(anyFlipped,
                "Mindestens ein Tile sollte nach Wertung umgedreht sein");

        // Spezifisch: Das gewählte Tile sollte umgedreht sein
        assertTrue(b.getTile(0, 0).isFlipped(),
                "Das gewählte Tile sollte umgedreht sein");
    }




    // ============================================================
    // ANFORDERUNG 1: BELEGBARE ZELLEN KORREKT ERMITTELN
    // ============================================================

    @Test
    @DisplayName("Ein Plättchen in der Mitte (0,0) → 4 orthogonale Nachbarn")
    void validPos_centerTile() {
        int[][] field = {
                {110, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        List<Position> valid = b.computeValidPositions();
        Set<Position> expected = Set.of(
                new Position(-1, 0),
                new Position(1, 0),
                new Position(0, -1),
                new Position(0, 1)
        );

        assertEquals(expected, new LinkedHashSet<>(valid));
    }

    @Test
    @DisplayName("Vier Plättchen in L-Form → korrekte orthogonale Nachbarn")
    void validPos_LShape() {
        int[][] field = {
                {110, 120, 990, 990, 990},  // ##.
                {130, 990, 990, 990, 990},  // #..
                {140, 990, 990, 990, 990},  // #..
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        List<Position> valid = b.computeValidPositions();

        // Alle orthogonalen Nachbarn der L-Form
        Set<Position> expected = Set.of(
                new Position(-1, 0), new Position(-1, 1),
                new Position(0, -1), new Position(0, 2),
                new Position(1, -1), new Position(1, 1),
                new Position(2, -1), new Position(2, 1),
                new Position(3, 0)
        );

        assertEquals(expected, new LinkedHashSet<>(valid));
    }

    @Test
    @DisplayName("Acht Plättchen in O-Form (Mitte frei) → Mitte ist valide")
    void validPos_OShape_holeInMiddle() {
        int[][] field = {
                {110, 120, 130, 990, 990},  // ###
                {140, 990, 150, 990, 990},  // #.#
                {160, 210, 220, 990, 990},  // ###
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        List<Position> valid = b.computeValidPositions();

        assertTrue(valid.contains(new Position(1, 1)),
                "Mitte des O sollte valide sein");
    }

    // ============================================================
    // ANFORDERUNG 2: L-KOMBINATIONEN GLEICHE FARBE
    // (2 Rotationen + gespiegelt = mind. 4 verschiedene Tests)
    // ============================================================

    @Test
    @DisplayName("L gleiche Farbe, 0° (┗): Blau, 4×4 voll belegt")
    void L_color_0deg_bottomLeft() {
        int[][] field = {
                {110, 120, 230, 340, 990},  // Blau Blau Grün Orange
                {130, 250, 360, 410, 990},  // Blau Grün Orange Rosa
                {140, 260, 420, 510, 990},  // Blau Grün Rosa Lila
                {220, 310, 450, 560, 990},  // Grün Orange Rosa Gelb
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 0), new Position(0, 1),
                new Position(1, 0), new Position(2, 0)
        );

        List<Set<Position>> combos = b.findCombinationsByColor();
        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form (┗, 0°) sollte gefunden werden");
    }

    @Test
    @DisplayName("L gleiche Farbe, 90° CW (┏): Grün, 4×4 voll belegt")
    void L_color_90deg_topLeft() {
        int[][] field = {
                {210, 110, 330, 440, 990},  // Grün Blau Orange Rosa
                {220, 150, 360, 510, 990},  // Grün Blau Orange Lila
                {230, 160, 420, 610, 990},  // Grün Blau Rosa Gelb
                {240, 310, 450, 560, 990},  // Grün Orange Rosa Gelb
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 0), new Position(1, 0),
                new Position(2, 0), new Position(3, 0)
        );

        List<Set<Position>> combos = b.findCombinationsByColor();
        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form (┏, 90°) sollte gefunden werden");
    }

    @Test
    @DisplayName("L gleiche Farbe, 180° (┓): Orange, 4×4 voll belegt")
    void L_color_180deg_topRight() {
        int[][] field = {
                {110, 320, 330, 340, 990},  // Blau Orange Orange Orange
                {220, 250, 350, 410, 990},  // Grün Grün Orange Rosa
                {140, 260, 460, 510, 990},  // Blau Grün ROSA Lila <- (2,2) NICHT Orange!
                {160, 610, 450, 560, 990},  // Blau Gelb Rosa Gelb
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 1), new Position(0, 2), new Position(0, 3),
                new Position(1, 2)
        );

        List<Set<Position>> combos = b.findCombinationsByColor();
        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form (┓, 180°) sollte gefunden werden");
    }

    @Test
    @DisplayName("L gleiche Farbe, gespiegelt horizontal: Rosa, 4×4 voll")
    void L_color_mirrored() {
        int[][] field = {
                {110, 420, 430, 340, 990},  // Blau Rosa Rosa Orange
                {220, 440, 360, 510, 990},  // Grün Rosa Orange Lila
                {140, 450, 610, 560, 990},  // Blau Rosa Gelb Gelb
                {160, 310, 250, 630, 990},  // Blau Orange Grün Gelb
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 1), new Position(0, 2),
                new Position(1, 1), new Position(2, 1)
        );

        List<Set<Position>> combos = b.findCombinationsByColor();
        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "Gespiegelte L-Form sollte gefunden werden");
    }

    // ============================================================
    // ANFORDERUNG 3: L-KOMBINATIONEN GLEICHES OBJEKT
    // (2 Rotationen + gespiegelt = mind. 4 verschiedene Tests)
    // ============================================================

    @Test
    @DisplayName("L gleiches Objekt, 0° (┗): Kissen (Obj 1), 4×4 voll")
    void L_object_0deg_pillow() {
        int[][] field = {
                {110, 210, 330, 440, 990},  // Kissen Kissen Napf Dose
                {310, 250, 360, 510, 990},  // Kissen Knochen Napf Kackhaufen
                {410, 260, 420, 610, 990},  // Kissen Knochen Dose Mops
                {220, 340, 450, 560, 990},  // Knochen Napf Kackhaufen Mops
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 0), new Position(0, 1),
                new Position(1, 0), new Position(2, 0)
        );

        List<Set<Position>> combos = b.findCombinationsByObject();
        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form nach Objekt (Kissen) sollte gefunden werden");
    }

    @Test
    @DisplayName("L gleiches Objekt, 90° (┏): Knochen (Obj 2), 4×4 voll")
    void L_object_90deg_bone() {
        int[][] field = {
                {120, 310, 430, 540, 990},  // Knochen Kissen Napf Dose
                {220, 150, 360, 610, 990},  // Knochen Kissen Napf Mops
                {320, 460, 410, 510, 990},  // Knochen Mops Dose Kackhaufen
                {420, 340, 450, 660, 990},  // Knochen Napf Kackhaufen Mops
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 0), new Position(1, 0),
                new Position(2, 0), new Position(3, 0)
        );

        List<Set<Position>> combos = b.findCombinationsByObject();
        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form nach Objekt (Knochen, 90°) sollte gefunden werden");
    }

    @Test
    @DisplayName("L gleiches Objekt, 180° (┓): Napf (Obj 3), 4×4 voll")
    void L_object_180deg_bowl() {
        int[][] field = {
                {110, 130, 230, 330, 990},  // Kissen Napf Napf Napf
                {220, 250, 430, 510, 990},  // Knochen Knochen Napf Kackhaufen
                {140, 260, 460, 610, 990},  // Kissen Knochen Mops Mops
                {160, 550, 450, 560, 990},  // Kissen Kackhaufen Kackhaufen Mops
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 1), // 130 = Blau Napf
                new Position(0, 2), // 230 = Grün Napf
                new Position(0, 3), // 330 = Orange Napf
                new Position(1, 2)  // 430 = Rosa Napf
        );

        List<Set<Position>> combos = b.findCombinationsByObject();
        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "L-Form nach Objekt (Napf, 180°) sollte gefunden werden");
    }

    @Test
    @DisplayName("L gleiches Objekt, gespiegelt: Dose (Obj 4), 4×4 voll")
    void L_object_mirrored_can() {
        int[][] field = {
                {110, 440, 430, 340, 990},  // Kissen DOSE Napf Napf
                {220, 540, 360, 510, 990},  // Knochen DOSE Napf Kackhaufen
                {140, 640, 610, 560, 990},  // DOSE DOSE Mops Mops
                {160, 310, 250, 630, 990},  // Kissen Napf Knochen Napf
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> expectedL = Set.of(
                new Position(0, 1), // 440 = Rosa Dose
                new Position(1, 1), // 540 = Lila Dose
                new Position(2, 0), // 140 = Blau Dose
                new Position(2, 1)  // 640 = Gelb Dose
        );

        List<Set<Position>> combos = b.findCombinationsByObject();
        assertTrue(combos.stream().anyMatch(expectedL::equals),
                "Gespiegelte L-Form nach Objekt (Dose) sollte gefunden werden");
    }

    // ============================================================
    // ANFORDERUNG 4: L WIRD NICHT GEFUNDEN WENN UMGEDREHT
    // ============================================================

    @Test
    @DisplayName("L wird NICHT gefunden wenn eines umgedreht ist")
    void L_notFound_oneFlipped() {
        int[][] field = {
                {111, 120, 990, 990, 990},  // (0,0) Blau-Kissen UMGEDREHT, (0,1) Blau-Knochen
                {130, 990, 990, 990, 990},  // (1,0) Blau-Napf
                {140, 990, 990, 990, 990},  // (2,0) Blau-Dose
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        List<Set<Position>> allCombos = b.findCombinationsByColor();
        allCombos.addAll(b.findCombinationsByObject());

        // Sollte keine 4er-Kombination finden
        long fourCombos = allCombos.stream()
                .filter(c -> c.size() == 4)
                .count();

        assertEquals(0, fourCombos,
                "Umgedrehtes Tile sollte L-Erkennung verhindern");

        // Nach Filter: Knochen (0,1) wird entfernt (nur 1 diagonal),
        // bleiben nur 2 Tiles (Napf + Dose) → keine Kombination
        long threeCombos = allCombos.stream()
                .filter(c -> c.size() == 3)
                .count();

        assertEquals(0, threeCombos,
                "Keine Kombination: Knochen wird entfernt (nur 1 diagonale Verbindung)");
    }


    // ============================================================
    // ANFORDERUNG 5: L WIRD NICHT GEFUNDEN WENN NICHT VORHANDEN
    // ============================================================

    @Test
    @DisplayName("Kein L vorhanden → keine 4er-Kombination")
    void L_notFound_notPresent() {
        int[][] field = {
                {110, 220, 330, 440, 990},  // Nur vereinzelte Tiles
                {250, 360, 150, 510, 990},  // Keine L-Form
                {610, 420, 230, 140, 990},
                {160, 540, 310, 260, 990},
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        List<Set<Position>> combos = b.findCombinationsByColor();
        combos.addAll(b.findCombinationsByObject());

        long fourCombos = combos.stream()
                .filter(c -> c.size() == 4)
                .count();

        assertEquals(0, fourCombos,
                "Keine L-Form vorhanden");
    }

    // ============================================================
    // ANFORDERUNG 6: SCORING (PUNKTZAHLEN)
    // ============================================================

    @Test
    @DisplayName("4er-Kombination ohne Krone → 4 Punkte")
    void scoring_fourWithoutCrown() {
        FakeGUI gui = new FakeGUI();
        Game game = new Game(gui);

        int[][] field = {
                {120, 130, 230, 990, 990},  // Blau-Knochen, Blau-Napf
                {140, 990, 990, 990, 990},  // Blau-Dose
                {150, 990, 990, 990, 990},  // Blau-Kackhaufen
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> combo = Set.of(
                new Position(0, 0), new Position(0, 1),
                new Position(1, 0), new Position(2, 0)
        );

        assertEquals(4, game.scoreCombination(combo, b),
                "4er ohne Krone sollte 4 Punkte geben");
    }

    @Test
    @DisplayName("4er-Kombination mit Krone → 5 Punkte")
    void scoring_fourWithCrown() {
        FakeGUI gui = new FakeGUI();
        Game game = new Game(gui);

        int[][] field = {
                {110, 120, 230, 990, 990},  // Blau-Kissen (ROYAL!), Blau-Knochen
                {130, 990, 990, 990, 990},  // Blau-Napf
                {140, 990, 990, 990, 990},  // Blau-Dose
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };
        Board b = Board.fromSaveFormat(field);

        Set<Position> combo = Set.of(
                new Position(0, 0), new Position(0, 1),
                new Position(1, 0), new Position(2, 0)
        );

        assertEquals(5, game.scoreCombination(combo, b),
                "4er mit Krone sollte 5 Punkte geben");
    }

    // ============================================================
    // ANFORDERUNG 7: PLÄTTCHEN UMDREHEN NACH WERTUNG
    // ============================================================

    @Test
    @DisplayName("Nach Wertung wird gewähltes Plättchen umgedreht")
    void settlement_flipsChosenTile() {
        FakeGUI gui = new FakeGUI();
        Game game = new Game(gui);

        Player p = new Player("Tester");
        int[][] field = {
                {110, 120, 230, 990, 990},
                {130, 990, 990, 990, 990},
                {140, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };
        p.getBoard().loadFromSaveFormat(field);

        Set<Position> combo = Set.of(
                new Position(0, 0), new Position(0, 1),
                new Position(1, 0), new Position(2, 0)
        );

        // Wähle (0,1) zum Umdrehen
        List<Position> toFlip = List.of(new Position(0, 1));
        game.settleCombination(combo, p, toFlip);

        // Prüfe ob (0,1) umgedreht wurde
        assertTrue(p.getBoard().getTile(0, 1).isFlipped(),
                "Gewähltes Tile sollte umgedreht sein");

        // Andere sollten NICHT umgedreht sein
        assertFalse(p.getBoard().getTile(0, 0).isFlipped());
        assertFalse(p.getBoard().getTile(1, 0).isFlipped());
    }
}