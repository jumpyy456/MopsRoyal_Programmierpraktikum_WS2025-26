package unit;

import logic.Tile;
import logic.TileColorCode;
import logic.TileObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

/**
 * Tests für die Tile-Klasse.
 * <p>
 * Testet:
 * <ul>
 *   <li>Royal-Status (Krone)</li>
 *   <li>Enum-basierte Tile-Erstellung</li>
 *   <li>Start-Pool (keine royalen Tiles)</li>
 *   <li>Zufalls-Startplättchen</li>
 *   <li>Flip-Funktionalität</li>
 *   <li>Kompatibilität mit int-Codes</li>
 * </ul>
 * </p>
 */
class TileTest {

    // --- Royal-Status (Krone) mit int-Codes ---

    @Test
    @DisplayName("isRoyal(int, int) ist TRUE für alle 6 royal-Kombinationen")
    void isRoyal_int_true_for_all_royals() {
        assertTrue(Tile.isRoyal(1, 1), "blau_kissen");
        assertTrue(Tile.isRoyal(6, 5), "gelb_kackhaufen");
        assertTrue(Tile.isRoyal(2, 6), "gruen_mops");
        assertTrue(Tile.isRoyal(5, 2), "lila_knochen");
        assertTrue(Tile.isRoyal(3, 3), "orange_napf");
        assertTrue(Tile.isRoyal(4, 4), "rosa_dose");
    }

    @Test
    @DisplayName("isRoyal(int, int) ist FALSE für nicht-royale Kombinationen")
    void isRoyal_int_false_for_non_royals() {
        assertFalse(Tile.isRoyal(1, 2), "blau_knochen");
        assertFalse(Tile.isRoyal(2, 3), "gruen_napf");
        assertFalse(Tile.isRoyal(3, 1), "orange_kissen");
        assertFalse(Tile.isRoyal(4, 6), "rosa_mops");
        assertFalse(Tile.isRoyal(5, 5), "lila_kackhaufen");
        assertFalse(Tile.isRoyal(6, 2), "gelb_knochen");
    }

    // --- Royal-Status (Krone) mit Enums ---

    @Test
    @DisplayName("isRoyal(Enum, Enum) ist TRUE für alle 6 royal-Kombinationen")
    void isRoyal_enum_true_for_all_royals() {
        assertTrue(Tile.isRoyal(TileColorCode.BLUE, TileObject.PILLOW), "blau_kissen");
        assertTrue(Tile.isRoyal(TileColorCode.YELLOW, TileObject.POOP), "gelb_kackhaufen");
        assertTrue(Tile.isRoyal(TileColorCode.GREEN, TileObject.PUG), "gruen_mops");
        assertTrue(Tile.isRoyal(TileColorCode.PURPLE, TileObject.BONE), "lila_knochen");
        assertTrue(Tile.isRoyal(TileColorCode.ORANGE, TileObject.BOWL), "orange_napf");
        assertTrue(Tile.isRoyal(TileColorCode.PINK, TileObject.CAN), "rosa_dose");
    }

    @Test
    @DisplayName("isRoyal(Enum, Enum) ist FALSE für nicht-royale Kombinationen")
    void isRoyal_enum_false_for_non_royals() {
        assertFalse(Tile.isRoyal(TileColorCode.BLUE, TileObject.BONE), "blau_knochen");
        assertFalse(Tile.isRoyal(TileColorCode.GREEN, TileObject.BOWL), "gruen_napf");
        assertFalse(Tile.isRoyal(TileColorCode.ORANGE, TileObject.PILLOW), "orange_kissen");
        assertFalse(Tile.isRoyal(TileColorCode.PINK, TileObject.PUG), "rosa_mops");
        assertFalse(Tile.isRoyal(TileColorCode.PURPLE, TileObject.POOP), "lila_kackhaufen");
        assertFalse(Tile.isRoyal(TileColorCode.YELLOW, TileObject.BONE), "gelb_knochen");
    }

    // --- Tile.of() Factory-Methoden ---

    @Test
    @DisplayName("Tile.of(int, int) setzt hasCrown() korrekt")
    void tileOf_int_sets_crown_correctly() {
        Tile royal = Tile.of(1, 1); // Blau Kissen (royal)
        assertTrue(royal.hasCrown(), "Royal tile sollte Krone haben");

        Tile normal = Tile.of(1, 2); // Blau Knochen (nicht royal)
        assertFalse(normal.hasCrown(), "Normales tile sollte keine Krone haben");
    }

    @Test
    @DisplayName("Tile.of(Enum, Enum) setzt hasCrown() korrekt")
    void tileOf_enum_sets_crown_correctly() {
        Tile royal = Tile.of(TileColorCode.BLUE, TileObject.PILLOW);
        assertTrue(royal.hasCrown(), "Royal tile sollte Krone haben");

        Tile normal = Tile.of(TileColorCode.BLUE, TileObject.BONE);
        assertFalse(normal.hasCrown(), "Normales tile sollte keine Krone haben");
    }

    @Test
    @DisplayName("Tile.of(int, int) und Tile.of(Enum, Enum) erzeugen äquivalente Tiles")
    void tileOf_int_and_enum_equivalent() {
        Tile fromInt = Tile.of(3, 4); // Orange Dose
        Tile fromEnum = Tile.of(TileColorCode.ORANGE, TileObject.CAN);

        assertEquals(fromInt.getColor(), fromEnum.getColor(), "Farbcode sollte gleich sein");
        assertEquals(fromInt.getObject(), fromEnum.getObject(), "Objektcode sollte gleich sein");
        assertEquals(fromInt.hasCrown(), fromEnum.hasCrown(), "Crown-Status sollte gleich sein");
    }

    // --- Start-Pool ---

    @Test
    @DisplayName("START_POOL hat korrekte Größe (36 - 6 royal = 30)")
    @SuppressWarnings("unchecked")
    void startPool_size() throws Exception {
        Field f = Tile.class.getDeclaredField("START_POOL");
        f.setAccessible(true);
        List<Tile> pool = (List<Tile>) f.get(null);

        assertEquals(30, pool.size(), "START_POOL sollte 30 Tiles haben (36 - 6 royale)");
    }

    @Test
    @DisplayName("START_POOL enthält keine royalen Tiles")
    @SuppressWarnings("unchecked")
    void startPool_no_royals() throws Exception {
        Field f = Tile.class.getDeclaredField("START_POOL");
        f.setAccessible(true);
        List<Tile> pool = (List<Tile>) f.get(null);

        for (Tile t : pool) {
            assertFalse(Tile.isRoyal(t.getColor(), t.getObject()),
                    "START_POOL sollte kein royales Tile enthalten: Farbe=" + t.getColor() + ", Objekt=" + t.getObject());
        }
    }

    @Test
    @DisplayName("START_POOL enthält keine umgedrehten Tiles")
    @SuppressWarnings("unchecked")
    void startPool_not_flipped() throws Exception {
        Field f = Tile.class.getDeclaredField("START_POOL");
        f.setAccessible(true);
        List<Tile> pool = (List<Tile>) f.get(null);

        for (Tile t : pool) {
            assertFalse(t.isFlipped(), "Start-Tiles sollten nicht umgedreht sein");
        }
    }

    // --- Zufalls-Startplättchen ---

    @Test
    @DisplayName("randomStartTile() ist deterministisch bei gleichem Seed")
    void randomStartTile_deterministic_with_seed() {
        Random r1 = new Random(12345);
        Random r2 = new Random(12345);

        for (int i = 0; i < 10; i++) {
            Tile a = Tile.randomStartTile(r1);
            Tile b = Tile.randomStartTile(r2);

            assertEquals(a.getColor(), b.getColor(),
                    "Gleicher Seed → gleiche Farbe");
            assertEquals(a.getObject(), b.getObject(),
                    "Gleicher Seed → gleiches Objekt");
        }
    }

    @RepeatedTest(5)
    @DisplayName("randomStartTile() liefert niemals royale Tiles")
    void randomStartTile_never_royal() {
        Random rnd = new Random();

        for (int i = 0; i < 200; i++) {
            Tile t = Tile.randomStartTile(rnd);

            assertFalse(Tile.isRoyal(t.getColor(), t.getObject()),
                    "randomStartTile sollte niemals royal liefern: Farbe=" + t.getColor() + ", Objekt=" + t.getObject());
            assertFalse(t.hasCrown(),
                    "Start-Tile sollte keine Krone haben");
        }
    }

    @Test
    @DisplayName("randomStartTile() liefert verschiedene Tiles")
    void randomStartTile_variety() {
        Random rnd = new Random();
        Tile first = Tile.randomStartTile(rnd);

        boolean foundDifferent = false;
        for (int i = 0; i < 50; i++) {
            Tile next = Tile.randomStartTile(rnd);
            if (next.getColor() != first.getColor() ||
                    next.getObject() != first.getObject()) {
                foundDifferent = true;
                break;
            }
        }

        assertTrue(foundDifferent,
                "randomStartTile sollte verschiedene Tiles liefern");
    }

    // --- Flip-Funktionalität ---

    @Test
    @DisplayName("flip() ändert isFlipped() Status")
    void flip_toggles_status() {
        Tile tile = Tile.of(1, 2);

        assertFalse(tile.isFlipped(), "Initial nicht umgedreht");

        tile.flip();
        assertTrue(tile.isFlipped(), "Nach flip() umgedreht");

        tile.flip();
        assertFalse(tile.isFlipped(), "Nach erneutem flip() wieder normal");
    }

    @Test
    @DisplayName("flip() ändert nicht Farbe oder Objekt")
    void flip_preserves_color_and_object() {
        Tile tile = Tile.of(3, 4); // Orange Dose

        int colorBefore = tile.getColor();
        int objectBefore = tile.getObject();

        tile.flip();

        assertEquals(colorBefore, tile.getColor(), "Farbe unverändert");
        assertEquals(objectBefore, tile.getObject(), "Objekt unverändert");
    }

    @Test
    @DisplayName("hasCrown() bleibt nach flip() erhalten")
    void crown_survives_flip() {
        Tile royal = Tile.of(1, 1); // Royal mit Krone

        assertTrue(royal.hasCrown(), "Hat initial Krone");

        royal.flip();
        assertTrue(royal.hasCrown(), "Hat nach flip() immer noch Krone");
    }

    // --- Getter ---

    @Test
    @DisplayName("getColor() liefert korrekte Farbe (int)")
    void getColor_correct() {
        Tile tile = Tile.of(5, 3); // Lila Napf
        assertEquals(5, tile.getColor());
    }

    @Test
    @DisplayName("getObject() liefert korrektes Objekt (int)")
    void getObject_correct() {
        Tile tile = Tile.of(2, 6); // Grün Mops
        assertEquals(6, tile.getObject());
    }

    @Test
    @DisplayName("getColorEnum() liefert korrekte Farbe (Enum)")
    void getColorEnum_correct() {
        Tile tile = Tile.of(5, 3);
        assertEquals(TileColorCode.PURPLE, tile.getColorEnum());
    }

    @Test
    @DisplayName("getObjectEnum() liefert korrektes Objekt (Enum)")
    void getObjectEnum_correct() {
        Tile tile = Tile.of(2, 6);
        assertEquals(TileObject.PUG, tile.getObjectEnum());
    }

    // --- Konstruktor-Tests ---

    @Test
    @DisplayName("Direkter Konstruktor mit Enums setzt alle Werte korrekt")
    void constructor_enum_sets_all_values() {
        Tile tile = new Tile(TileColorCode.PINK, TileObject.BONE, true, true);

        assertEquals(4, tile.getColor(), "Farbe korrekt");
        assertEquals(2, tile.getObject(), "Objekt korrekt");
        assertTrue(tile.isFlipped(), "Flipped korrekt");
        assertTrue(tile.hasCrown(), "Crown korrekt");
    }

    @Test
    @DisplayName("Tile.of() erstellt nicht-umgedrehtes Tile")
    void tileOf_not_flipped() {
        Tile tile = Tile.of(3, 3);
        assertFalse(tile.isFlipped(), "Tile.of() sollte nicht umgedrehte Tiles erstellen");
    }

    // --- Konstanten ---

    @Test
    @DisplayName("COLOR_COUNT ist 6")
    void colorCount_is_six() {
        assertEquals(6, Tile.COLOR_COUNT, "Es sollte 6 Farben geben");
    }

    @Test
    @DisplayName("OBJECT_COUNT ist 6")
    void objectCount_is_six() {
        assertEquals(6, Tile.OBJECT_COUNT, "Es sollte 6 Objekte geben");
    }

    // --- Konvertierungsmethoden ---

    @Test
    @DisplayName("fromColorCode() konvertiert int zu Enum korrekt")
    void fromColorCode_converts_correctly() {
        assertEquals(TileColorCode.BLUE, Tile.fromColorCode(1));
        assertEquals(TileColorCode.GREEN, Tile.fromColorCode(2));
        assertEquals(TileColorCode.ORANGE, Tile.fromColorCode(3));
        assertEquals(TileColorCode.PINK, Tile.fromColorCode(4));
        assertEquals(TileColorCode.PURPLE, Tile.fromColorCode(5));
        assertEquals(TileColorCode.YELLOW, Tile.fromColorCode(6));
    }

    @Test
    @DisplayName("fromObjectCode() konvertiert int zu Enum korrekt")
    void fromObjectCode_converts_correctly() {
        assertEquals(TileObject.PILLOW, Tile.fromObjectCode(1));
        assertEquals(TileObject.BONE, Tile.fromObjectCode(2));
        assertEquals(TileObject.BOWL, Tile.fromObjectCode(3));
        assertEquals(TileObject.CAN, Tile.fromObjectCode(4));
        assertEquals(TileObject.POOP, Tile.fromObjectCode(5));
        assertEquals(TileObject.PUG, Tile.fromObjectCode(6));
    }

    @Test
    @DisplayName("fromColorCode() wirft Exception bei ungültigem Code")
    void fromColorCode_throws_for_invalid() {
        assertThrows(IllegalArgumentException.class, () -> Tile.fromColorCode(0));
        assertThrows(IllegalArgumentException.class, () -> Tile.fromColorCode(7));
        assertThrows(IllegalArgumentException.class, () -> Tile.fromColorCode(-1));
    }

    @Test
    @DisplayName("fromObjectCode() wirft Exception bei ungültigem Code")
    void fromObjectCode_throws_for_invalid() {
        assertThrows(IllegalArgumentException.class, () -> Tile.fromObjectCode(0));
        assertThrows(IllegalArgumentException.class, () -> Tile.fromObjectCode(7));
        assertThrows(IllegalArgumentException.class, () -> Tile.fromObjectCode(-1));
    }

    // --- Copy ---

    @Test
    @DisplayName("copy() erstellt unabhängige Kopie")
    void copy_creates_independent_copy() {
        Tile original = Tile.of(2, 4);
        Tile copy = original.copy();

        assertEquals(original.getColor(), copy.getColor());
        assertEquals(original.getObject(), copy.getObject());
        assertEquals(original.isFlipped(), copy.isFlipped());
        assertEquals(original.hasCrown(), copy.hasCrown());

        // Änderungen am Original sollten Kopie nicht beeinflussen
        original.flip();
        assertFalse(copy.isFlipped(), "Kopie sollte unverändert bleiben");
    }

    // --- Equals/HashCode ---

    @Test
    @DisplayName("equals() funktioniert korrekt")
    void equals_works_correctly() {
        Tile t1 = Tile.of(1, 2);
        Tile t2 = Tile.of(1, 2);
        Tile t3 = Tile.of(1, 3);

        assertEquals(t1, t2, "Gleiche Tiles sollten gleich sein");
        assertNotEquals(t1, t3, "Verschiedene Tiles sollten ungleich sein");
    }
}