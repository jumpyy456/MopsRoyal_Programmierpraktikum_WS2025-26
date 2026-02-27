package logic;

import java.util.*;

/**
 * Finder für gültige Kombinationen auf einem Spielfeld.
 * <p>
 * Diese Klasse ist verantwortlich für das Erkennen von Kombinationen
 * (3-5 zusammenhängende Plättchen gleicher Farbe oder gleichen Objekts)
 * auf einem Board.
 * </p>
 * <p>
 * <b>Hinweis:</b> Diese Klasse wurde aus {@link Board} extrahiert.
 * Die Logik ist 1:1 identisch, nur als statische Methoden mit Board-Parameter.
 * </p>
 */
public final class CombinationFinder {

    // Privater Konstruktor verhindert Instanziierung
    private CombinationFinder() {}

    // ===========================================
    // Öffentliche API
    // ===========================================

    /**
     * Findet alle gültigen Kombinationen (3, 4 oder 5 Felder) mit <b>gleicher Farbe</b>.
     * <p>
     * Eine gültige Kombination besteht aus Tiles, die:
     * <ul>
     *   <li>die gleiche Farbe haben,</li>
     *   <li>zusammenhängend sind (orthogonal oder diagonal),</li>
     *   <li><b>nicht</b> umgedreht ({@link Tile#isFlipped()}) sind,</li>
     *   <li>genau 3, 4 oder 5 Tiles umfassen.</li>
     * </ul>
     * </p>
     *
     * @param board das zu durchsuchende Board (nicht {@code null})
     * @return Liste von Mengen mit je 3-5 Positionen, die eine gültige Kombination bilden
     */
    public static List<Set<Position>> findCombinationsByColor(Board board) {
        return findCombinations(board, true);
    }

    /**
     * Findet alle gültigen Kombinationen (3, 4 oder 5 Felder) mit <b>gleichem Objekt</b>.
     * <p>
     * Eine gültige Kombination besteht aus Tiles, die:
     * <ul>
     *   <li>das gleiche Objekt haben,</li>
     *   <li>zusammenhängend sind (orthogonal oder diagonal),</li>
     *   <li><b>nicht</b> umgedreht ({@link Tile#isFlipped()}) sind,</li>
     *   <li>genau 3, 4 oder 5 Tiles umfassen.</li>
     * </ul>
     * </p>
     *
     * @param board das zu durchsuchende Board (nicht {@code null})
     * @return Liste von Mengen mit je 3-5 Positionen, die eine gültige Kombination bilden
     */
    public static List<Set<Position>> findCombinationsByObject(Board board) {
        return findCombinations(board, false);
    }

    /**
     * Ermittelt alle umdrehbaren Plättchen in einer Kombination.
     * <p>
     * Die Regel für umdrehbare Plättchen:
     * <ul>
     *   <li><b>Ungerade Anzahl (3, 5) mit orthogonalen Verbindungen:</b> 1 Plättchen mit den meisten orthogonalen Nachbarn</li>
     *   <li><b>Ungerade Anzahl (3, 5) rein diagonal:</b> 1 Plättchen mit den meisten diagonalen Nachbarn</li>
     *   <li><b>Gerade Anzahl (4) mit orthogonalen Verbindungen:</b> Alle Plättchen mit mindestens 2 orthogonalen Nachbarn</li>
     *   <li><b>Gerade Anzahl (4) rein diagonal:</b> Die 2 Plättchen mit den meisten diagonalen Nachbarn</li>
     * </ul>
     * </p>
     * <p>
     * Kombinationen mit 2 oder weniger Plättchen haben keine umdrehbaren Tiles
     * (leere Liste wird zurückgegeben).
     * </p>
     *
     * @param combo die zu prüfende Kombination (nicht {@code null})
     * @return Liste der Positionen umdrehbarer Plättchen (kann leer sein)
     */
    public static List<Position> getFlippableTilesInCombination(Set<Position> combo) {
        Objects.requireNonNull(combo, "combo");

        if (combo.size() <= 2) {
            return Collections.emptyList();
        }

        // Prüfe ob rein diagonal (gilt für ungerade und gerade Kombinationen)
        boolean hasOrthogonalWithin = false;
        for (Position pos : combo) {
            if (countOrthogonalNeighbors(pos, combo) > 0) {
                hasOrthogonalWithin = true;
                break;
            }
        }

        boolean isOddSize = combo.size() % 2 == 1;

        if (hasOrthogonalWithin) {
            // Gemischte Kombi (orthogonal + diagonal)
            if (isOddSize) {
                // Ungerade: 1 Tile mit max orthogonalen Nachbarn
                return findTopNTilesWithMaxOrthogonalNeighbors(combo, 1);
            } else {
                // Gerade: Alle Tiles mit ≥2 orthogonalen Nachbarn
                return findTilesWithAtLeastTwoOrthogonalNeighbors(combo);
            }
        } else {
            // Rein diagonal (ungerade oder gerade)
            if (isOddSize) {
                // Ungerade diagonal → NUR 1 Tile mit den meisten diagonalen Nachbarn
                return findTopNTilesWithMaxDiagonalNeighbors(combo, 1);
            } else {
                // Gerade diagonal → Die 2 Tiles mit den meisten diagonalen Nachbarn
                return findTopNTilesWithMaxDiagonalNeighbors(combo, 2);
            }
        }
    }

    // ===========================================
    // Kombinations-Suche (intern)
    // ===========================================

    /**
     * Findet alle gültigen Kombinationen.
     * <p>
     * Für jede zusammenhängende Gruppe werden ALLE möglichen zusammenhängenden
     * Subgraphen der Größe 3-5 als separate Kombinationen zurückgegeben.
     * Dies ermöglicht dem Spieler, zwischen verschiedenen Wertungsmöglichkeiten zu wählen.
     * </p>
     * <p>
     * <b>WICHTIG:</b> Gruppen > 5 werden NICHT übersprungen! Stattdessen werden
     * alle möglichen 3er/4er/5er-Subsets innerhalb großer Gruppen gefunden.
     * </p>
     *
     * @param board das Board
     * @param byColor true = nach Farbe gruppieren, false = nach Objekt gruppieren
     * @return Liste von Kombinationen (jede ist ein Set von Positionen)
     */
    private static List<Set<Position>> findCombinations(Board board, boolean byColor) {
        List<Set<Position>> allCombinations = new ArrayList<>();
        Set<Position> visited = new HashSet<>();

        // Für jedes nicht-umgedrehte Tile - iteriere über alle belegten Positionen
        // WICHTIG: Wir nutzen hier die gleiche Iteration wie im Original (über tiles.entrySet())
        // Da wir keinen direkten Zugriff auf die interne Map haben, iterieren wir über das Snapshot
        // und konvertieren die Array-Indizes zurück zu Board-Positionen.

        Tile[][] snapshot = board.snapshotTiles();
        Position origin = board.getSnapshotOrigin();

        if (origin == null) {
            return allCombinations; // Leeres Board
        }

        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = snapshot[r][c];
                if (tile == null) {
                    continue;
                }

                // Konvertiere Array-Index zurück zu Board-Position
                Position start = new Position(origin.row() + r, origin.col() + c);

                if (visited.contains(start) || tile.isFlipped()) {
                    continue; // Bereits besucht oder umgedreht
                }

                // Finde zusammenhängende Gruppe mit gleicher Farbe/Objekt
                Set<Position> group = findConnectedGroup(board, start, byColor);

                // Überspringe nur Gruppen < 3 (zu klein für Kombination)
                if (group.size() < 3) {
                    visited.addAll(group);
                    continue;
                }

                // Finde alle zusammenhängenden Subkombinationen (3-5 Tiles)
                // egal wie groß die Gruppe ist - auch bei > 5 Tiles
                allCombinations.addAll(findAllValidSubcombinations(group));

                // Markiere alle Tiles der Gruppe als besucht
                visited.addAll(group);
            }
        }

        return allCombinations;
    }

    /**
     * Findet alle zusammenhängenden Teilmengen einer Gruppe.
     * <p>
     * Für eine Gruppe der Größe n werden alle zusammenhängenden Subgraphen
     * der Größen 3 bis min(n, 5) gefunden.
     * </p>
     *
     * @param group die Ausgangsgruppe
     * @return Liste aller gültigen Subkombinationen
     */
    private static List<Set<Position>> findAllValidSubcombinations(Set<Position> group) {
        Set<Set<Position>> uniqueCombinations = new HashSet<>();

        // Teste alle möglichen Subset-Größen (3 bis 5)
        for (int targetSize = 3; targetSize <= Math.min(group.size(), 5); targetSize++) {
            uniqueCombinations.addAll(findConnectedSubsetsOfSize(group, targetSize));
        }

        return new ArrayList<>(uniqueCombinations);
    }

    /**
     * Findet alle gültigen zusammenhängenden Teilmengen einer bestimmten Größe.
     *
     * @param group die Ausgangsgruppe
     * @param targetSize gewünschte Größe der Teilmengen
     * @return Liste aller gültigen zusammenhängenden Teilmengen der Zielgröße
     */
    private static List<Set<Position>> findConnectedSubsetsOfSize(Set<Position> group, int targetSize) {
        List<Set<Position>> result = new ArrayList<>();
        List<Position> positions = new ArrayList<>(group);

        // Generiere alle k-elementigen Teilmengen und prüfe ob gültig
        generateValidSubsets(positions, targetSize, 0, new ArrayList<>(), result);

        return result;
    }

    /**
     * Rekursiver Helfer zum Generieren aller gültigen k-elementigen Teilmengen.
     *
     * @param positions alle verfügbaren Positionen
     * @param targetSize Zielgröße
     * @param start Startindex für Rekursion
     * @param current aktuelle Teilmenge (im Aufbau)
     * @param result Ergebnisliste
     */
    private static void generateValidSubsets(List<Position> positions, int targetSize, int start,
                                             List<Position> current, List<Set<Position>> result) {
        if (current.size() == targetSize) {
            Set<Position> subset = new LinkedHashSet<>(current);
            // Prüfe: zusammenhängend UND gültig (Kompaktheit)
            if (isConnected(subset) && isValidCombination(subset)) {
                result.add(subset);
            }
            return;
        }

        for (int i = start; i < positions.size(); i++) {
            current.add(positions.get(i));
            generateValidSubsets(positions, targetSize, i + 1, current, result);
            current.removeLast();
        }
    }

    /**
     * Prüft, ob eine Menge von Positionen zusammenhängend ist.
     * <p>
     * Verwendet BFS um zu prüfen, ob alle Positionen über orthogonale
     * oder diagonale Nachbarschaft erreichbar sind.
     * </p>
     *
     * @param positions zu prüfende Positionen
     * @return {@code true} wenn alle Positionen zusammenhängend sind
     */
    private static boolean isConnected(Set<Position> positions) {
        if (positions.isEmpty()) return true;
        if (positions.size() == 1) return true;

        // BFS von einem beliebigen Startpunkt
        Position start = positions.iterator().next();
        Set<Position> visited = new HashSet<>();
        Queue<Position> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            for (int k = 0; k < Direction.ALL_COUNT; k++) {
                Position neighbor = new Position(
                        current.row() + Direction.ALL_DR[k],
                        current.col() + Direction.ALL_DC[k]
                );

                if (positions.contains(neighbor) && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited.size() == positions.size();
    }

    /**
     * Prüft ob eine Kombination gültig ist.
     * <p>
     * <b>Regeln:</b> Eine Kombination ist gültig wenn:
     * <ul>
     *   <li>Sie eine lineare diagonale Kette ist ODER</li>
     *   <li>Bei 4 Tiles: Mindestens eine Dimension der Bounding Box ≥ 3 (verhindert 2×2 Quadrate)</li>
     *   <li>Bei 5 Tiles: Bounding Box nicht größer als 4×4, bei 3×3 muss zentrales Tile vorhanden sein</li>
     *   <li>Ihre Kompaktheit ≥ 0.5 ist UND nach Filterung noch >= 3 Tiles hat UND bei 3 Tiles eine L-Form ist</li>
     * </ul>
     * </p>
     * <p>
     * <b>Filterung:</b> Tiles in gemischten Formen mit 0 orthogonalen und nur 1 diagonalen
     * Nachbarn werden iterativ entfernt, bis keine solchen Tiles mehr vorhanden sind.
     * </p>
     * <p>
     * <b>L-Form (3 Tiles):</b> Genau ein Tile (das "Eck") muss 2 orthogonale Nachbarn haben.
     * </p>
     *
     * @param combo die zu prüfende Kombination
     * @return {@code true} wenn die Kombination gültig ist
     */
    private static boolean isValidCombination(Set<Position> combo) {
        if (combo.size() < 3 || combo.size() > 5) {
            return false;
        }

        // Prüfe ob rein diagonal
        boolean isOnlyDiagonal = true;
        for (Position pos : combo) {
            if (countOrthogonalNeighbors(pos, combo) > 0) {
                isOnlyDiagonal = false;
                break;
            }
        }

        if (isOnlyDiagonal) {
            return isLinearDiagonalChain(combo);
        }

        // Ab hier: nur noch gemischte Formen

        // Bei gemischten Formen muss jedes Tile mindestens 1 orthogonalen Nachbarn haben
        // (verhindert verstreute Formen mit nur diagonal verbundenen "Ausreißern")
        for (Position pos : combo) {
            if (countOrthogonalNeighbors(pos, combo) == 0) {
                return false; // Tile nur diagonal verbunden → ungültig
            }
        }

        // Berechne Bounding Box
        int minRow = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;

        for (Position pos : combo) {
            minRow = Math.min(minRow, pos.row());
            maxRow = Math.max(maxRow, pos.row());
            minCol = Math.min(minCol, pos.col());
            maxCol = Math.max(maxCol, pos.col());
        }

        int height = maxRow - minRow + 1;
        int width = maxCol - minCol + 1;

        // Geometrische Regeln
        if (combo.size() == 4) {
            if (height < 3 && width < 3) {
                return false;
            }
        } else if (combo.size() == 5) {
            boolean isStraightLine = (height == 1 && width == 5) || (height == 5 && width == 1);

            if (!isStraightLine) {
                if ((height == 4 && width >= 3) || (width == 4 && height >= 3)) {
                    return false;
                }
                if (height > 4 || width > 4) {
                    return false;
                }
            }
        }

        // Kompaktheit prüfen
        double compactness = calculateCompactness(combo);
        if (compactness < 0.5) {
            return false;
        }

        // Filter schwach verbundener Tiles
        Set<Position> filtered = filterWeaklyConnectedTilesInCombo(combo);
        if (filtered.size() < 3 || filtered.size() != combo.size()) {
            return false;
        }

        // L-Form Prüfung für 3er
        return combo.size() != 3 || !(compactness < 1.0) || isLShape(combo);
    }

    /**
     * Prüft ob eine 3er-Kombination eine L-Form ist.
     * <p>
     * Eine L-Form hat:
     * <ul>
     *   <li>Genau ein "Eck"-Tile mit 2 orthogonalen Nachbarn</li>
     *   <li>Die 3 Tiles sind NICHT alle in einer Linie (Row oder Column)</li>
     * </ul>
     * </p>
     *
     * @param combo die 3er-Kombination
     * @return {@code true} wenn es eine L-Form ist
     */
    private static boolean isLShape(Set<Position> combo) {
        if (combo.size() != 3) {
            return false;
        }

        // Prüfe: Genau 1 Tile hat 2 orthogonale Nachbarn
        int tilesWithTwoOrthNeighbors = 0;

        for (Position pos : combo) {
            int orthCount = countOrthogonalNeighbors(pos, combo);
            if (orthCount == 2) {
                tilesWithTwoOrthNeighbors++;
            }
        }

        if (tilesWithTwoOrthNeighbors != 1) {
            return false; // Kein eindeutiges Eck
        }

        // Zusätzliche Prüfung: Tiles dürfen nicht alle in einer Linie sein
        List<Position> posList = new ArrayList<>(combo);

        // Alle in derselben Row?
        boolean sameRow = posList.stream()
                .map(Position::row)
                .distinct()
                .count() == 1;

        // Alle in derselben Column?
        boolean sameCol = posList.stream()
                .map(Position::col)
                .distinct()
                .count() == 1;

        // L-Form: nicht in einer Linie
        return !sameRow && !sameCol;
    }

    /**
     * Filtert schwach verbundene Tiles aus einer Kombination.
     * <p>
     * Ein Tile gilt als schwach verbunden wenn es 0 orthogonale und nur 1 diagonalen
     * Nachbarn hat. Diese werden iterativ entfernt bis keine mehr vorhanden sind.
     * </p>
     *
     * @param combo die Kombination
     * @return gefilterte Kombination
     */
    private static Set<Position> filterWeaklyConnectedTilesInCombo(Set<Position> combo) {
        Set<Position> filtered = new LinkedHashSet<>(combo);
        boolean changed = true;

        while (changed) {
            changed = false;
            Set<Position> toRemove = new HashSet<>();

            for (Position pos : filtered) {
                int orthCount = countOrthogonalNeighbors(pos, filtered);
                int diagCount = countDiagonalNeighbors(pos, filtered);

                if (orthCount == 0 && diagCount == 1) {
                    toRemove.add(pos);
                    changed = true;
                }
            }

            filtered.removeAll(toRemove);
        }

        return filtered;
    }

    /**
     * Prüft ob eine rein diagonale Kombination eine lineare Kette ist.
     * <p>
     * Eine lineare diagonale Kette bedeutet:
     * <ul>
     *   <li>Die Tiles liegen auf einer geraden diagonalen Linie</li>
     *   <li>Alle Schritte gehen in dieselbe diagonale Richtung (z.B. immer +1,+1)</li>
     * </ul>
     * </p>
     *
     * @param combo die zu prüfende Kombination (muss rein diagonal sein)
     * @return {@code true} wenn es eine lineare Kette ist
     */
    private static boolean isLinearDiagonalChain(Set<Position> combo) {
        if (combo.size() < 2) {
            return true;
        }

        // Sortiere Tiles nach Position (z.B. nach Row, dann Col)
        List<Position> sorted = new ArrayList<>(combo);
        sorted.sort(Comparator.comparingInt(Position::row).thenComparingInt(Position::col));

        // Prüfe: Alle Schritte müssen dieselbe diagonale Richtung haben
        int firstDr = sorted.get(1).row() - sorted.get(0).row();
        int firstDc = sorted.get(1).col() - sorted.get(0).col();

        for (int i = 1; i < sorted.size() - 1; i++) {
            int dr = sorted.get(i + 1).row() - sorted.get(i).row();
            int dc = sorted.get(i + 1).col() - sorted.get(i).col();

            // Prüfe: Richtung ist konsistent
            if (dr != firstDr || dc != firstDc) {
                return false; // Richtungswechsel → nicht linear
            }

            // Prüfe: Schrittgröße ist 1 (diagonal)
            if (Math.abs(dr) != 1 || Math.abs(dc) != 1) {
                return false; // Kein diagonaler Schritt
            }
        }

        // Prüfe auch den ersten Schritt
        return Math.abs(firstDr) == 1 && Math.abs(firstDc) == 1;
    }

    /**
     * Berechnet die Kompaktheit einer Kombination.
     * <p>
     * Kompaktheit = Anzahl_Tiles / Bounding_Box_Fläche
     * </p>
     * <p>
     * Die Bounding Box ist das kleinste Rechteck, das alle Tiles der Kombination umschließt.
     * Eine hohe Kompaktheit bedeutet, dass die Tiles dicht beieinander liegen.
     * </p>
     *
     * @param combo die Kombination
     * @return Kompaktheit (0.0 bis 1.0)
     */
    private static double calculateCompactness(Set<Position> combo) {
        if (combo.isEmpty()) {
            return 0.0;
        }

        // Finde Bounding Box
        int minRow = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;

        for (Position pos : combo) {
            minRow = Math.min(minRow, pos.row());
            maxRow = Math.max(maxRow, pos.row());
            minCol = Math.min(minCol, pos.col());
            maxCol = Math.max(maxCol, pos.col());
        }

        int height = maxRow - minRow + 1;
        int width = maxCol - minCol + 1;
        int area = height * width;

        return (double) combo.size() / area;
    }

    // ===========================================
    // Nachbar-Zählung
    // ===========================================

    /**
     * Zählt orthogonale Nachbarn einer Position innerhalb einer Gruppe.
     *
     * @param pos die Position
     * @param group die Gruppe
     * @return Anzahl orthogonaler Nachbarn (0-4)
     */
    static int countOrthogonalNeighbors(Position pos, Set<Position> group) {
        int count = 0;

        for (int k = 0; k < Direction.ORTHOGONAL_COUNT; k++) {
            Position neighbor = new Position(
                    pos.row() + Direction.ORTHOGONAL_DR[k],
                    pos.col() + Direction.ORTHOGONAL_DC[k]
            );
            if (group.contains(neighbor)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Zählt diagonale Nachbarn einer Position innerhalb einer Gruppe.
     *
     * @param pos die Position
     * @param group die Gruppe
     * @return Anzahl diagonaler Nachbarn (0-4)
     */
    static int countDiagonalNeighbors(Position pos, Set<Position> group) {
        int count = 0;

        for (int k = 0; k < Direction.DIAGONAL_COUNT; k++) {
            Position neighbor = new Position(
                    pos.row() + Direction.DIAGONAL_DR[k],
                    pos.col() + Direction.DIAGONAL_DC[k]
            );
            if (group.contains(neighbor)) {
                count++;
            }
        }

        return count;
    }

    // ===========================================
    // Gruppen-Suche (DFS)
    // ===========================================

    /**
     * Findet eine zusammenhängende Gruppe von Tiles mit gleicher Farbe/Objekt.
     * <p>
     * Verwendet Tiefensuche (DFS) um alle verbundenen Tiles zu finden (orthogonal und diagonal).
     * </p>
     *
     * @param board das Board
     * @param start Startposition
     * @param byColor true = gleiche Farbe, false = gleiches Objekt
     * @return Set aller Positionen in der zusammenhängenden Gruppe
     */
    private static Set<Position> findConnectedGroup(Board board, Position start, boolean byColor) {
        Set<Position> group = new LinkedHashSet<>();
        Set<Position> visited = new HashSet<>();

        Tile startTile = board.getTile(start);
        if (startTile == null || startTile.isFlipped()) {
            return group; // Kein Tile oder umgedreht
        }

        // DFS (Tiefensuche)
        dfsConnected(board, start, startTile, byColor, group, visited);

        return group;
    }

    /**
     * Tiefensuche (DFS) für zusammenhängende Tiles.
     *
     * @param board das Board
     * @param current aktuelle Position
     * @param referenceTile Referenz-Tile (für Vergleich Farbe/Objekt)
     * @param byColor true = Farbe vergleichen, false = Objekt vergleichen
     * @param group Ergebnis-Set (wird gefüllt)
     * @param visited bereits besuchte Positionen
     */
    private static void dfsConnected(Board board, Position current, Tile referenceTile, boolean byColor,
                                     Set<Position> group, Set<Position> visited) {
        if (visited.contains(current)) {
            return; // Bereits besucht
        }

        Tile tile = board.getTile(current);
        if (tile == null || tile.isFlipped()) {
            return; // Kein Tile oder umgedreht
        }

        // Prüfe ob Tile zur Gruppe gehört
        boolean matches = byColor
                ? tile.getColor() == referenceTile.getColor()
                : tile.getObject() == referenceTile.getObject();

        if (!matches) {
            return; // Farbe/Objekt passt nicht
        }

        // Tile zur Gruppe hinzufügen
        visited.add(current);
        group.add(current);

        // Rekursiv alle 8 Nachbarn besuchen (orthogonal + diagonal)
        for (int k = 0; k < Direction.ALL_COUNT; k++) {
            Position neighbor = new Position(
                    current.row() + Direction.ALL_DR[k],
                    current.col() + Direction.ALL_DC[k]
            );
            dfsConnected(board, neighbor, referenceTile, byColor, group, visited);
        }
    }

    // ===========================================
    // Umdrehbare Tiles finden
    // ===========================================

    /**
     * Findet die top N Tiles mit den meisten orthogonalen Nachbarn in einer Kombination.
     * <p>
     * Diese Methode wird für gemischte Kombinationen (orthogonal + diagonal) verwendet.
     * Bei ungeraden Kombinationen (3er, 5er) gibt es laut Spielregeln immer genau ein Tile
     * mit der maximalen Anzahl Nachbarn. Dann wird typischerweise {@code maxCount = 1} gesetzt.
     * </p>
     *
     * @param combo die Kombination (nicht {@code null})
     * @param maxCount maximale Anzahl zu findender Tiles (typisch 1 für ungerade Kombinationen)
     * @return Liste mit maximal maxCount Tiles (die mit den meisten orthogonalen Nachbarn)
     */
    private static List<Position> findTopNTilesWithMaxOrthogonalNeighbors(Set<Position> combo, int maxCount) {
        // Spezialfall: 5er-Linie - hier haben 3 Tiles je 2 Nachbarn,
        // aber nur das geometrische Zentrum soll umdrehbar sein
        if (combo.size() == 5 && maxCount == 1 && isLinearChain(combo, false)) {
            return List.of(findGeometricCenter(combo));
        }

        List<Position> result = new ArrayList<>();
        int maxNeighbors = 0;

        // Finde Maximum
        for (Position pos : combo) {
            int count = countOrthogonalNeighbors(pos, combo);
            if (count > maxNeighbors) {
                maxNeighbors = count;
            }
        }

        // Sammle die ersten maxCount Tiles mit Maximum
        for (Position pos : combo) {
            int count = countOrthogonalNeighbors(pos, combo);
            if (count == maxNeighbors) {
                result.add(pos);
                if (result.size() == maxCount) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Findet die top N Tiles mit den meisten diagonalen Nachbarn in einer Kombination.
     * <p>
     * Diese Methode wird für rein diagonale Kombinationen verwendet:
     * </p>
     * <ul>
     *   <li>Ungerade Kombinationen (3er, 5er): maxCount = 1 – genau ein Tile mit Maximum</li>
     *   <li>Gerade Kombinationen (4er): maxCount = 2 – die beiden mittleren Tiles einer linearen Kette</li>
     * </ul>
     *
     * @param combo die Kombination (nicht {@code null})
     * @param maxCount maximale Anzahl zu findender Tiles (1-2)
     * @return Liste mit maximal maxCount Tiles (die mit den meisten diagonalen Nachbarn)
     */
    private static List<Position> findTopNTilesWithMaxDiagonalNeighbors(Set<Position> combo, int maxCount) {
        // Spezialfall: 5er diagonale Linie - hier haben 3 Tiles je 2 Nachbarn,
        // aber nur das geometrische Zentrum soll umdrehbar sein
        if (combo.size() == 5 && maxCount == 1 && isLinearChain(combo, true)) {
            return List.of(findGeometricCenter(combo));
        }

        List<Position> result = new ArrayList<>();
        int maxNeighbors = 0;

        // Finde Maximum
        for (Position pos : combo) {
            int count = countDiagonalNeighbors(pos, combo);
            if (count > maxNeighbors) {
                maxNeighbors = count;
            }
        }

        // Sammle die ersten maxCount Tiles mit Maximum
        for (Position pos : combo) {
            int count = countDiagonalNeighbors(pos, combo);
            if (count == maxNeighbors) {
                result.add(pos);
                if (result.size() == maxCount) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Findet Tiles mit mindestens 2 orthogonalen Nachbarn in einer Kombination.
     *
     * @param combo die Kombination
     * @return Liste der Tiles mit ≥2 orthogonalen Nachbarn
     */
    private static List<Position> findTilesWithAtLeastTwoOrthogonalNeighbors(Set<Position> combo) {
        List<Position> result = new ArrayList<>();

        for (Position pos : combo) {
            int count = countOrthogonalNeighbors(pos, combo);
            if (count >= 2) {
                result.add(pos);
            }
        }

        return result;
    }

    /**
     * Prüft, ob eine Kombination eine lineare Kette ist (gerade Linie).
     * <p>
     * Eine lineare Kette hat genau 2 Endpunkte (Tiles mit nur 1 Nachbarn)
     * und alle anderen Tiles haben genau 2 Nachbarn.
     * </p>
     *
     * @param combo die zu prüfende Kombination
     * @param diagonal true für diagonale Nachbarschaft, false für orthogonale
     * @return true wenn die Kombination eine gerade Linie ist
     */
    private static boolean isLinearChain(Set<Position> combo, boolean diagonal) {
        int endpoints = 0;  // Tiles mit genau 1 Nachbarn
        int middle = 0;     // Tiles mit genau 2 Nachbarn

        for (Position pos : combo) {
            int neighbors = diagonal
                    ? countDiagonalNeighbors(pos, combo)
                    : countOrthogonalNeighbors(pos, combo);

            if (neighbors == 1) {
                endpoints++;
            } else if (neighbors == 2) {
                middle++;
            } else {
                // Mehr als 2 Nachbarn = keine lineare Kette
                return false;
            }
        }

        // Lineare Kette: genau 2 Endpunkte, Rest sind Mitteltiles
        return endpoints == 2 && middle == combo.size() - 2;
    }

    /**
     * Findet das geometrische Zentrum einer Kombination.
     * <p>
     * Das Zentrum ist die Position mit dem minimalen Abstand zu allen anderen.
     * Bei einer 5er-Linie ist das immer das mittlere (3.) Tile.
     * </p>
     *
     * @param combo die Kombination (nicht leer)
     * @return die zentrale Position
     */
    private static Position findGeometricCenter(Set<Position> combo) {
        Position center = null;
        int minTotalDistance = Integer.MAX_VALUE;

        for (Position candidate : combo) {
            int totalDistance = 0;
            for (Position other : combo) {
                // Manhattan-Distanz (funktioniert für orthogonal und diagonal)
                totalDistance += Math.abs(candidate.row() - other.row())
                        + Math.abs(candidate.col() - other.col());
            }

            if (totalDistance < minTotalDistance) {
                minTotalDistance = totalDistance;
                center = candidate;
            }
        }

        return center;
    }
}