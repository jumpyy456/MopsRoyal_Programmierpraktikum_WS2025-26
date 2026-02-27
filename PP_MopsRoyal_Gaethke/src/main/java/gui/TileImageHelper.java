package gui;

import logic.Tile;

/**
 * Hilfsmethoden zur Generierung von Bilddateinamen für Tiles.
 * <p>
 * Diese Klasse gehört zur GUI-Schicht, da Dateinamen Teil des
 * Erscheinungsbilds sind und nicht zur Spiellogik gehören.
 * </p>
 */
public class TileImageHelper {

    /** Farbzuordnung für die Bildnamen. Index 0 bleibt leer. */
    private static final String[] COLOR_NAMES = {
            "", "blau", "gruen", "orange", "rosa", "lila", "gelb"
    };

    /** Objektzuordnung für die Bildnamen. Index 0 bleibt leer. */
    private static final String[] OBJECT_NAMES = {
            "", "kissen", "knochen", "napf", "dose", "kackhaufen", "mops"
    };

    /**
     * Generiert den Bilddateinamen für ein Tile.
     *
     * @param tile das Tile (nicht null)
     * @return Dateiname, z.B. "blau_kissen.png"
     */
    public static String getImageName(Tile tile) {
        if (tile == null) {
            throw new IllegalArgumentException("tile darf nicht null sein");
        }

        int color = tile.getColor();
        int object = tile.getObject();

        if (color < 1 || color > 6 || object < 1 || object > 6) {
            throw new IllegalArgumentException(
                    "Ungültige Tile-Codes: color=" + color + ", object=" + object
            );
        }

        return COLOR_NAMES[color] + "_" + OBJECT_NAMES[object] + ".png";
    }

    /**
     * Gibt den Dateinamen für die Rückseite eines umgedrehten Tiles.
     *
     * @return Dateiname der Rückseite
     */
    public static String getBacksideName() {
        return "rueckseite.png";
    }

    /**
     * Gibt den vollständigen Pfad zu einem Tile-Bild zurück.
     *
     * @param tile das Tile (nicht null)
     * @return vollständiger Pfad, z.B. "/gui/images/blau_kissen.png"
     */
    public static String getImagePath(Tile tile) {
        String filename = tile.isFlipped()
                ? getBacksideName()
                : getImageName(tile);
        return "/gui/images/" + filename;
    }
}