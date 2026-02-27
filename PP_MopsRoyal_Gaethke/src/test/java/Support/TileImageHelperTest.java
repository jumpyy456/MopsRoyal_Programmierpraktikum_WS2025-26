package Support;

import gui.TileImageHelper;
import logic.Tile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für TileImageHelper.
 * <p>
 * Testet die korrekte Generierung von Bilddateinamen für Tiles.
 * </p>
 */
class TileImageHelperTest {

    @Test
    @DisplayName("getImageName() bildet korrekt auf Dateinamen ab")
    void getImageName_mapping() {
        assertEquals("blau_kissen.png", TileImageHelper.getImageName(Tile.of(1, 1)));
        assertEquals("gruen_mops.png", TileImageHelper.getImageName(Tile.of(2, 6)));
        assertEquals("orange_napf.png", TileImageHelper.getImageName(Tile.of(3, 3)));
        assertEquals("rosa_dose.png", TileImageHelper.getImageName(Tile.of(4, 4)));
        assertEquals("lila_knochen.png", TileImageHelper.getImageName(Tile.of(5, 2)));
        assertEquals("gelb_kackhaufen.png", TileImageHelper.getImageName(Tile.of(6, 5)));
        assertEquals("blau_knochen.png", TileImageHelper.getImageName(Tile.of(1, 2)));
    }

    @Test
    @DisplayName("getImagePath() gibt vollständigen Pfad zurück")
    void getImagePath_fullPath() {
        Tile tile = Tile.of(1, 1);

        String path = TileImageHelper.getImagePath(tile);

        assertTrue(path.startsWith("/gui/images/"));
        assertTrue(path.endsWith(".png"));
        assertEquals("/gui/images/blau_kissen.png", path);
    }

    @Test
    @DisplayName("getImagePath() gibt Rückseite für umgedrehtes Tile")
    void getImagePath_flippedTile() {
        Tile tile = Tile.of(1, 2);  // Blau Knochen
        tile.flip();

        String path = TileImageHelper.getImagePath(tile);

        assertEquals("/gui/images/rueckseite.png", path);
    }

    @Test
    @DisplayName("getBacksideName() gibt korrekten Dateinamen")
    void getBacksideName_correct() {
        assertEquals("rueckseite.png", TileImageHelper.getBacksideName());
    }

    @Test
    @DisplayName("getImageName() wirft Exception bei null Tile")
    void getImageName_nullTile() {
        assertThrows(IllegalArgumentException.class, () -> TileImageHelper.getImageName(null));
    }

    @Test
    @DisplayName("getImageName() für alle 36 Kombinationen")
    void getImageName_allCombinations() {
        for (int color = 1; color <= 6; color++) {
            for (int object = 1; object <= 6; object++) {
                Tile tile = Tile.of(color, object);

                String imageName = TileImageHelper.getImageName(tile);

                assertNotNull(imageName);
                assertTrue(imageName.endsWith(".png"));
                assertTrue(imageName.contains("_"));
            }
        }
    }

    @Test
    @DisplayName("Verschiedene Tiles haben verschiedene ImageNames")
    void getImageName_uniqueness() {
        Tile tile1 = Tile.of(1, 2);  // Blau Knochen
        Tile tile2 = Tile.of(1, 3);  // Blau Napf
        Tile tile3 = Tile.of(2, 2);  // Grün Knochen

        String name1 = TileImageHelper.getImageName(tile1);
        String name2 = TileImageHelper.getImageName(tile2);
        String name3 = TileImageHelper.getImageName(tile3);

        assertNotEquals(name1, name2);
        assertNotEquals(name1, name3);
        assertNotEquals(name2, name3);
    }

    @Test
    @DisplayName("ImageName ist konsistent über mehrere Aufrufe")
    void getImageName_consistency() {
        Tile tile = Tile.of(3, 4);

        String name1 = TileImageHelper.getImageName(tile);
        String name2 = TileImageHelper.getImageName(tile);
        String name3 = TileImageHelper.getImageName(tile);

        assertEquals(name1, name2);
        assertEquals(name2, name3);
    }

    @Test
    @DisplayName("Umgedrehtes Tile ändert nicht getImageName(), nur getImagePath()")
    void getImageName_flippedDoesNotChangeImageName() {
        Tile tile = Tile.of(1, 2);

        String nameBeforeFlip = TileImageHelper.getImageName(tile);
        tile.flip();
        String nameAfterFlip = TileImageHelper.getImageName(tile);

        assertEquals(nameBeforeFlip, nameAfterFlip);

        String pathAfterFlip = TileImageHelper.getImagePath(tile);
        assertTrue(pathAfterFlip.contains("rueckseite"));
    }

    @Test
    @DisplayName("Royal Tiles haben normale ImageNames")
    void getImageName_royalTiles() {
        Tile[] royals = {
                Tile.of(1, 1),  // blau_kissen
                Tile.of(6, 5),  // gelb_kackhaufen
                Tile.of(2, 6),  // gruen_mops
                Tile.of(5, 2),  // lila_knochen
                Tile.of(3, 3),  // orange_napf
                Tile.of(4, 4)   // rosa_dose
        };

        for (Tile royal : royals) {
            assertTrue(royal.hasCrown());

            String imageName = TileImageHelper.getImageName(royal);
            assertNotNull(imageName);
            assertFalse(imageName.contains("royal"));
            assertFalse(imageName.contains("krone"));
        }
    }
}