package logic;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Repräsentiert eine wertbare Kombination im Spiel.
 * <p>
 * Eine Kombination besteht aus:
 * <ul>
 *   <li>Einer Menge von Positionen (3-5 Tiles)</li>
 *   <li>Einer Liste von umdrehbaren Positionen (1-2 innere Tiles)</li>
 *   <li>Einem Typ (Farbe oder Objekt)</li>
 * </ul>
 * </p>
 *
 * @param positions          Alle Positionen der Kombination.
 * @param flippablePositions Positionen, die umgedreht werden dürfen (innere Tiles).
 */
public record Combination(Set<Position> positions, List<Position> flippablePositions) {

    /**
     * Erstellt eine neue Kombination.
     *
     * @param positions          alle Positionen der Kombination (nicht null)
     * @param flippablePositions umdrehbare Positionen (nicht null)
     */
    public Combination(Set<Position> positions, List<Position> flippablePositions) {
        this.positions = Collections.unmodifiableSet(positions);
        this.flippablePositions = Collections.unmodifiableList(flippablePositions);
    }

    /**
     * Gibt alle Positionen der Kombination zurück.
     *
     * @return unveränderliche Menge von Positionen
     */
    @Override
    public Set<Position> positions() {
        return positions;
    }

    /**
     * Gibt die umdrehbaren Positionen zurück.
     *
     * @return unveränderliche Liste von Positionen
     */
    @Override
    public List<Position> flippablePositions() {
        return flippablePositions;
    }

    /**
     * Gibt die Größe der Kombination zurück.
     *
     * @return Anzahl der Tiles (3-5)
     */
    public int getSize() {
        return positions.size();
    }
}