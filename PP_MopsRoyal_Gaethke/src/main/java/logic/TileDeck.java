package logic;

import java.util.*;

/**
 * Verwaltet den Ziehstapel eines Spielers.
 * Jeder Spieler hat seinen eigenen Vorrat von 36 Tiles (6 Farben × 6 Objekte).
 * Regeln:
 * - Startplättchen darf KEINE Krone haben
 * - Jedes Tile kann nur einmal gezogen werden
 * - Ziehen erfolgt zufällig
 */
public class TileDeck {

    /** Verbleibende Tiles im Vorrat (bereits gemischt). */
    private final List<Tile> remaining;

    /** Merkt, welche Startplättchen (Farbe+Objekt) bereits vergeben wurden. */
    private final Set<Integer> assignedStartCodes = new HashSet<>();

    /** Zufallsgenerator für die Tile-Auswahl. */
    private final Random rnd;

    /**
     * Erstellt einen neuen, gemischten Vorrat für einen Spieler.
     * <p>
     * Alle 36 möglichen Kombinationen werden erzeugt und sofort gemischt.
     * </p>
     *
     * @param rnd Zufallsgenerator (nicht {@code null})
     * @throws NullPointerException wenn {@code rnd} null ist
     */
    public TileDeck(Random rnd) {
        this.rnd = rnd;
        this.remaining = new ArrayList<>(36);

        // Alle 36 möglichen Tiles (6 Farben × 6 Objekte)
        for (int color = 1; color <= 6; color++) {
            for (int obj = 1; obj <= 6; obj++) {
                remaining.add(Tile.of(color, obj));
            }
        }

        // Mischen
        Collections.shuffle(remaining, rnd);
    }

    /**
     * Zieht ein zufälliges Startplättchen (ohne Krone).
     * <p>
     * <b>WICHTIG:</b> Das Startplättchen wird NICHT aus dem Vorrat entfernt!
     * Es bleibt im Deck und kann später im Spiel normal gezogen werden.
     * Dies ist wichtig für die Spielregel "Spieler setzt aus wenn eigene Startkarte gezogen wird".
     * </p>
     * <p>
     * <b>Warum?</b> Jede Karte existiert nur einmal im Spiel. Wenn ein Spieler seine
     * Startkarte zieht, setzt er aus und der nächste Spieler legt sie. Die Startkarte
     * wird also erst aus dem Deck entfernt, wenn sie tatsächlich gelegt wird
     * (über {@link #draw()}).
     * </p>
     *
     * @return ein Startplättchen (ohne Krone, bleibt im Vorrat)
     * @throws IllegalStateException wenn Vorrat leer ist
     */
    public Tile drawStartTile() {
        if (remaining.isEmpty()) {
            throw new IllegalStateException("Vorrat ist leer!");
        }

        while (true) {
            // Zufälligen Index wählen
            int idx = rnd.nextInt(remaining.size());
            Tile tile = remaining.get(idx);

            if (tile.hasCrown()) {
                continue; // Startplättchen ohne Krone
            }

            // Eindeutigkeit nur nach Farbe+Objekt (flipped ist hier sowieso false)
            int code = tile.getColor() * 10 + tile.getObject();
            if (!assignedStartCodes.add(code)) {
                continue; // schon vergeben -> neues ziehen
            }

            // Kopie zurückgeben, damit jeder Spieler sein eigenes Tile-Objekt hat
            return tile.copy();
        }
    }


    /**
     * Zieht das nächste reguläre Tile aus dem Vorrat.
     * <p>
     * Das Tile wird vom Anfang der (bereits gemischten) Liste entnommen.
     * </p>
     *
     * @return nächstes Tile (nicht {@code null})
     * @throws IllegalStateException wenn der Vorrat leer ist
     */
    public Tile draw() {
        if (remaining.isEmpty()) {
            throw new IllegalStateException("Vorrat ist leer!");
        }

        // Oberstes Tile ziehen (bereits gemischt)
        return remaining.removeFirst();
    }

    /**
     * Gibt die Anzahl verbleibender Tiles im Vorrat zurück.
     *
     * @return Anzahl verbleibender Tiles (0..36)
     */
    public int size() {
        return remaining.size();
    }

    /**
     * Prüft, ob der Vorrat leer ist.
     *
     * @return {@code true}, wenn keine Tiles mehr vorhanden sind
     */
    public boolean isEmpty() {
        return remaining.isEmpty();
    }

    /**
     * Entfernt bereits gespielte Tiles aus dem verbleibenden Vorrat.
     * <p>
     * Dies wird beim Laden eines Spielstands aufgerufen, um sicherzustellen,
     * dass Tiles die bereits auf Boards liegen nicht erneut gezogen werden können.
     * </p>
     * <p>
     * Der Mechanismus ist analog zum normalen Spielflow:
     * - Im normalen Spiel: {@link #draw()} entfernt Tiles aus {@code remaining}
     * - Beim Load: Diese Methode entfernt alle bereits gelegten Tiles
     * </p>
     *
     * @param playedTiles Liste der bereits gespielten Tiles, die entfernt werden sollen
     *                    (nicht {@code null}, darf leer sein)
     * @throws NullPointerException wenn {@code playedTiles} null ist
     */
    public void markTilesAsPlayed(List<Tile> playedTiles) {
        java.util.Objects.requireNonNull(playedTiles, "playedTiles darf nicht null sein");

        for (Tile tile : playedTiles) {
            // Entferne Tile mit gleicher Farbe und gleichem Objekt
            // (Ein Tile ist eindeutig durch Farbe + Objekt identifiziert,
            //  der Flipped-Status spielt keine Rolle beim Vergleich)
            remaining.removeIf(t -> t.getColor() == tile.getColor()
                    && t.getObject() == tile.getObject());
        }
    }
}