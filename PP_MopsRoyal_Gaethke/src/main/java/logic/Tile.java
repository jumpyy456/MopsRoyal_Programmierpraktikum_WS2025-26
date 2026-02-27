package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Repräsentiert ein einzelnes Spielplättchen ("Tile") im Spiel Mops Royal.
 * <p>
 * Ein Plättchen wird durch eine Farbe ({@link TileColorCode}) und ein Objekt
 * ({@link TileObject}) beschrieben. Manche Kombinationen tragen eine Krone
 * und gelten als „royal". Ein Plättchen kann außerdem umgedreht werden
 * (zeigt dann die Rückseite).
 * </p>
 */
public class Tile {

    // --- Konstanten ---
    /** Anzahl der verschiedenen Farben (für Schleifen). */
    public static final int COLOR_COUNT = TileColorCode.values().length;

    /** Anzahl der verschiedenen Objekte (für Schleifen). */
    public static final int OBJECT_COUNT = TileObject.values().length;

    // --- Felder ---
    /** Farbe des Plättchens. */
    private final TileColorCode color;

    /** Objekt/Symbol des Plättchens. */
    private final TileObject tileObject;

    /** {@code true} wenn Plättchen umgedreht wurde (nach Wertung). */
    private boolean flipped;

    /** {@code true} wenn Plättchen eine Krone hat (Royal-Status). */
    private final boolean crown;

    // --- Konstruktoren ---

    /**
     * Konstruktor mit Enum-Parametern.
     *
     * @param color   Farbe des Plättchens (nicht {@code null})
     * @param object  Objekt des Plättchens (nicht {@code null})
     * @param flipped true, wenn das Plättchen umgedreht ist
     * @param crown   true, wenn dieses Plättchen eine Krone zeigt
     */
    public Tile(TileColorCode color, TileObject object, boolean flipped, boolean crown) {
        this.color = color;
        this.tileObject = object;
        this.flipped = flipped;
        this.crown = crown;
    }

    // --- Factory-Methoden ---

    /**
     * Erzeugt ein neues Tile mit Enum-Parametern.
     * <p>
     * Der Crown-Status wird automatisch basierend auf {@link #isRoyal(TileColorCode, TileObject)} gesetzt.
     * </p>
     *
     * @param color  Farbe (nicht {@code null})
     * @param object Objekt (nicht {@code null})
     * @return neues Plättchen mit korrektem Krone-Status
     */
    public static Tile of(TileColorCode color, TileObject object) {
        return new Tile(color, object, false, isRoyal(color, object));
    }

    /**
     * Erzeugt ein neues Tile mit int-Parametern (für Kompatibilität).
     * <p>
     * Diese Methode konvertiert int-Codes (1-6) zu den entsprechenden Enums.
     * Wird für JSON-Laden und Tests benötigt.
     * </p>
     *
     * @param colorCode  Farbcode (1-6)
     * @param objectCode Objektcode (1-6)
     * @return neues Plättchen mit korrektem Krone-Status
     * @throws IllegalArgumentException wenn Codes außerhalb 1-6 liegen
     */
    public static Tile of(int colorCode, int objectCode) {
        TileColorCode color = fromColorCode(colorCode);
        TileObject object = fromObjectCode(objectCode);
        return of(color, object);
    }

    // --- Statische Hilfsmethoden ---

    /**
     * Prüft, ob eine Farb-Objekt-Kombination ein royales Motiv darstellt.
     * <p>
     * Royale Plättchen tragen eine Krone und dürfen nicht als Startplättchen verwendet werden.
     * </p>
     *
     * @param color  Farbe (nicht {@code null})
     * @param object Objekt (nicht {@code null})
     * @return {@code true}, wenn das Motiv royal ist
     */
    public static boolean isRoyal(TileColorCode color, TileObject object) {
        return (color == TileColorCode.BLUE && object == TileObject.PILLOW)     // blau_kissen
                || (color == TileColorCode.YELLOW && object == TileObject.POOP)     // gelb_kackhaufen
                || (color == TileColorCode.GREEN && object == TileObject.PUG)       // gruen_mops
                || (color == TileColorCode.PURPLE && object == TileObject.BONE)     // lila_knochen
                || (color == TileColorCode.ORANGE && object == TileObject.BOWL)     // orange_napf
                || (color == TileColorCode.PINK && object == TileObject.CAN);       // rosa_dose
    }

    /**
     * Prüft, ob eine Farb-Objekt-Kombination (als int-Codes) royal ist.
     * <p>
     * Convenience-Methode für Kompatibilität mit int-basiertem Code.
     * </p>
     *
     * @param colorCode  Farbcode (1-6)
     * @param objectCode Objektcode (1-6)
     * @return {@code true}, wenn das Motiv royal ist
     */
    public static boolean isRoyal(int colorCode, int objectCode) {
        return isRoyal(fromColorCode(colorCode), fromObjectCode(objectCode));
    }

    /**
     * Konvertiert einen int-Farbcode (1-6) zum entsprechenden Enum.
     *
     * @param code Farbcode (1-6)
     * @return entsprechendes {@link TileColorCode}
     * @throws IllegalArgumentException wenn Code ungültig
     */
    public static TileColorCode fromColorCode(int code) {
        for (TileColorCode c : TileColorCode.values()) {
            if (c.code() == code) {
                return c;
            }
        }
        throw new IllegalArgumentException("Ungültiger Farbcode: " + code);
    }

    /**
     * Konvertiert einen int-Objektcode (1-6) zum entsprechenden Enum.
     *
     * @param code Objektcode (1-6)
     * @return entsprechendes {@link TileObject}
     * @throws IllegalArgumentException wenn Code ungültig
     */
    public static TileObject fromObjectCode(int code) {
        for (TileObject o : TileObject.values()) {
            if (o.code() == code) {
                return o;
            }
        }
        throw new IllegalArgumentException("Ungültiger Objektcode: " + code);
    }

    // --- Startplättchen-Pool ---

    /**
     * Unveränderliche Liste aller erlaubten Startplättchen (nicht royal).
     */
    private static final List<Tile> START_POOL;

    static {
        List<Tile> list = new ArrayList<>();
        for (TileColorCode c : TileColorCode.values()) {
            for (TileObject o : TileObject.values()) {
                if (!isRoyal(c, o)) {
                    list.add(Tile.of(c, o));
                }
            }
        }
        START_POOL = Collections.unmodifiableList(list);
    }

    /**
     * Liefert ein zufälliges, gültiges Startplättchen.
     *
     * @param rnd Random-Instanz für die Zufallsauswahl
     * @return ein zufälliges, nicht-royales Tile
     */
    public static Tile randomStartTile(Random rnd) {
        return START_POOL.get(rnd.nextInt(START_POOL.size()));
    }

    // --- Getter ---

    /**
     * Gibt die Farbe als Enum zurück.
     *
     * @return Farbe des Plättchens
     */
    public TileColorCode getColorEnum() {
        return color;
    }

    /**
     * Gibt das Objekt als Enum zurück.
     *
     * @return Objekt des Plättchens
     */
    public TileObject getObjectEnum() {
        return tileObject;
    }

    /**
     * Gibt den Farbcode als int zurück (für Kompatibilität).
     *
     * @return Farbcode (1-6)
     */
    public int getColor() {
        return color.code();
    }

    /**
     * Gibt den Objektcode als int zurück (für Kompatibilität).
     *
     * @return Objektcode (1-6)
     */
    public int getObject() {
        return tileObject.code();
    }

    /**
     * Gibt zurück, ob dieses Plättchen eine Krone trägt.
     *
     * @return {@code true}, wenn das Plättchen royal ist
     */
    public boolean hasCrown() {
        return crown;
    }

    /**
     * Prüft, ob das Plättchen aktuell umgedreht ist.
     *
     * @return {@code true}, wenn Rückseite sichtbar ist
     */
    public boolean isFlipped() {
        return flipped;
    }

    // --- Mutator ---

    /**
     * Dreht das Plättchen um (Vorderseite ↔ Rückseite).
     */
    public void flip() {
        flipped = !flipped;
    }

    // --- Kopie ---

    /**
     * Erstellt eine unabhängige Kopie dieses Tiles.
     *
     * @return neue Tile-Instanz mit gleichen Eigenschaften
     */
    public Tile copy() {
        return new Tile(color, tileObject, flipped, crown);
    }

    // --- Object-Methoden ---

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tile other = (Tile) obj;
        return color == other.color
                && tileObject == other.tileObject
                && flipped == other.flipped
                && crown == other.crown;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(color, tileObject, flipped, crown);
    }

    @Override
    public String toString() {
        return "Tile{" + color + "-" + tileObject +
                (crown ? ", ROYAL" : "") +
                (flipped ? ", flipped" : "") + "}";
    }
}