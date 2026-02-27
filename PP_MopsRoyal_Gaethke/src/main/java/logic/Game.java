package logic;

import java.util.*;

/**
 * Zentrale Spielsteuerung für Mops Royal.
 * <p>
 * Diese Klasse koordiniert den gesamten Spielablauf und bietet
 * drei verschiedene Konstruktoren für unterschiedliche Verwendungszwecke.
 * </p>
 */
public class Game {

    // --- Konstanten ---
    /** Save/Load: leeres Board-Feld */
    private static final int EMPTY_CELL_CODE = 990;

    /** Save/Load: kein nextTile (Deck leer) */
    private static final int NO_NEXT_TILE_CODE = 99;

    /** Maximale Anzahl platzierbarer Tiles pro Spieler (5×5 - 1 Startplättchen). */
    private static final int MAX_TILES_PLACED = 24;

    /**
     * Feste Zuordnung von Spielerindex → Spielerfarbe.
     * <p>
     * Diese Zuordnung definiert zugleich die Zugreihenfolge nach Index:
     * 0=BLUE, 1=GREEN, 2=RED, 3=YELLOW.
     * </p>
     */
    private static final PlayerColor[] PLAYER_COLORS = {
            PlayerColor.BLUE, PlayerColor.GREEN, PlayerColor.RED, PlayerColor.YELLOW
    };

    // --- Felder ---
    /** Array aller aktiven Spieler in Sitzreihenfolge. */
    private Player[] players;

    /** Index des aktuell aktiven Spielers (0-basiert). */
    private int currentIndex = 0;

    /** Verbindung zur GUI für Updates und Fehleranzeigen. */
    private final GUIConnector gui;

    /** Zufallsgenerator für Tile-Decks. */
    private final Random rnd = new Random();

    /** Zentrales Deck für alle Spieler */
    private TileDeck centralDeck;

    /** Das aktuelle Tile, das ALLE Spieler in dieser Runde legen müssen. */
    private Tile currentTile;

    /** Flag: Verhindert Klicks während der Pause nach einem Zug. */
    private boolean waitingForNextPlayer = false;

    /** Flag: Spiel ist beendet (alle Spieler haben 25 Plättchen gelegt). */
    private boolean gameEnded = false;

    /** Flag-Array: Welcher Spieler muss nachholen */
    private boolean[] playerNeedsCatchUp;

    // --- Konstruktoren ---
    /**
     * Konstruktor 1: Erstellt ein neues Spiel (aus UserInterfaceController).
     * <p>
     * Dieser Konstruktor wird vom UI-Controller aufgerufen, um ein frisches Spiel zu starten.
     * Nach der Erstellung muss {@link #startNewGame(List, boolean[])} aufgerufen werden.
     * </p>
     *
     * @param gui GUI-Connector für Anzeige-Updates (nicht {@code null})
     * @throws NullPointerException wenn {@code gui} null ist
     */
    public Game(GUIConnector gui) {
        java.util.Objects.requireNonNull(gui, "gui darf nicht null sein");
        this.gui = gui;
        this.players = new Player[0];
    }

    /**
     * Konstruktor 2: Erstellt ein Spiel mit einem Board (für Tests).
     * <p>
     * Dieser Konstruktor wird in Tests verwendet, um eine spezifische Spielsituation
     * mit einem vorgegebenen Board zu erstellen.
     *
     * @param gui GUI-Connector (nicht {@code null})
     * @param boardData 5×5 int-Array mit Tile-Codes (Format: Farbe*100 + Objekt*10 + Flipped)
     * @throws NullPointerException wenn {@code gui} oder {@code boardData} null ist
     * @throws IllegalArgumentException wenn boardData ungültiges Format hat
     */
    public Game(GUIConnector gui, int[][] boardData) {
        java.util.Objects.requireNonNull(gui, "gui darf nicht null sein");
        java.util.Objects.requireNonNull(boardData, "boardData darf nicht null sein");

        this.gui = gui;

        // Erstelle einen Test-Spieler mit dem gegebenen Board
        Player testPlayer = new Player("TestSpieler");
        testPlayer.getBoard().loadFromSaveFormat(boardData);

        this.players = new Player[]{testPlayer};
        this.currentIndex = 0;
        this.centralDeck = new TileDeck(rnd);
        this.currentTile = centralDeck.draw();
    }

    /**
     * Konstruktor 3: Erstellt ein Spiel mitten im Spielgeschehen (für Laden).
     * <p>
     * Dieser Konstruktor rekonstruiert einen vollständigen Spielstand aus gespeicherten Daten.
     * Alle Spieler, ihre Boards, Punkte und der aktuelle Spielzustand werden wiederhergestellt.
     * Farben werden aus der Spielerreihenfolge abgeleitet
     * </p>
     *
     * @param gui GUI-Connector (nicht {@code null})
     * @param playerData Liste mit Spieler-Daten (mindestens 2, maximal 4 Spieler)
     * @param currentPlayerIndex Index des aktuell aktiven Spielers (0-basiert)
     * @param nextTileCode Code des nächsten Tiles (Format: Farbe*10 + Objekt)
     * @throws NullPointerException wenn {@code gui} oder {@code playerData} null ist
     * @throws IllegalArgumentException bei ungültigen Daten
     */
    public Game(GUIConnector gui, List<PlayerData> playerData, int currentPlayerIndex, int nextTileCode) {
        java.util.Objects.requireNonNull(gui, "gui darf nicht null sein");
        java.util.Objects.requireNonNull(playerData, "playerData darf nicht null sein");

        if (playerData.size() < 2 || playerData.size() > 4) {
            throw new IllegalArgumentException("2-4 Spieler erforderlich, nicht " + playerData.size());
        }

        this.gui = gui;
        this.currentIndex = currentPlayerIndex;
        this.playerNeedsCatchUp = new boolean[playerData.size()];

        // Spieler aus Daten rekonstruieren
        this.players = new Player[playerData.size()];
        for (int i = 0; i < playerData.size(); i++) {
            PlayerData pd = playerData.get(i);
            Player p = new Player(pd.name);
            p.setScore(pd.score);
            p.getBoard().loadFromSaveFormat(pd.boardData);

            // Nach Normalisierung zur Bounding-Box liegt es nicht zwingend bei (0,0).
            // Rekonstruiere aus dem explizit gespeicherten 'initial'-Code.
            int initialCode = pd.initial;
            if (initialCode != EMPTY_CELL_CODE) {
                int color = initialCode / 100;
                int object = (initialCode % 100) / 10;
                Tile startTile = Tile.of(color, object);
                p.setStartTile(startTile);
            }

            // tilesPlaced aus Board-Größe ableiten
            int tileCount = p.getBoard().getTileCount();
            if (tileCount > 0) {
                p.setTilesPlaced(tileCount - 1); // -1 wegen Startplättchen
            }

            players[i] = p;
        }

        // Deck erstellen und bereits gespielte Tiles entfernen
        this.centralDeck = new TileDeck(rnd);
        List<Tile> playedTiles = collectPlayedTiles(playerData);
        centralDeck.markTilesAsPlayed(playedTiles);

        // Nächstes Tile aus Code rekonstruieren
        if (nextTileCode == NO_NEXT_TILE_CODE) {
            this.currentTile = null;
        } else {
            int color = nextTileCode / 10;
            int object = nextTileCode % 10;
            this.currentTile = Tile.of(color, object);

            // nextCard wurde bereits gezogen, also auch aus Deck entfernen
            centralDeck.markTilesAsPlayed(java.util.List.of(this.currentTile));
        }

        updateGUI();
    }

    /**
     * Startet ein neues Spiel.
     *
     * @param playerData die Spielerdatei
     * @return playedTiles Liste aller gespielten Tiles
     */
    private List<Tile> collectPlayedTiles(List<PlayerData> playerData) {
        List<Tile> playedTiles = new ArrayList<>();

        for (PlayerData pd : playerData) {
            int[][] boardData = pd.boardData;
            for (int[] row : boardData) {
                for (int code : row) {
                    if (code == EMPTY_CELL_CODE) continue;

                    int color = code / 100;
                    int object = (code % 100) / 10;
                    playedTiles.add(Tile.of(color, object));
                }
            }

            if (pd.initial != EMPTY_CELL_CODE) {
                int color = pd.initial / 100;
                int object = (pd.initial % 100) / 10;
                playedTiles.add(Tile.of(color, object));
            }
        }

        return playedTiles;
    }


    // --- Hilfsklasse für Konstruktor 3 ---

    /**
     * Daten-Container für einen Spieler (zum Laden/Speichern).
     */
    public record PlayerData(String name, int score, int[][] boardData, int initial) {
    }


    // --- Spielablauf ---

    /**
     * Startet ein neues Spiel.
     *
     * @param names Liste aller Spielernamen (nicht {@code null})
     * @param takesPart Teilnahme-Flags für jeden Spieler
     */
    public void startNewGame(List<String> names, boolean[] takesPart) {
        java.util.Objects.requireNonNull(names, "names");
        java.util.Objects.requireNonNull(takesPart, "takesPart");

        if (names.size() != takesPart.length) {
            throw new IllegalArgumentException("names und takesPart müssen gleich lang sein");
        }

        // Spielzustände zurücksetzen
        gameEnded = false;
        waitingForNextPlayer = false;
        currentIndex = 0;

        // Teilnehmende Spieler zählen
        int participantCount = 0;
        for (boolean takes : takesPart) {
            if (takes) participantCount++;
        }

        players = new Player[participantCount];
        playerNeedsCatchUp = new boolean[participantCount];
        int idx = 0;

        for (int i = 0; i < takesPart.length; i++) {
            if (!takesPart[i]) continue;

            String name = names.get(i).trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Leerer Spielername an Index " + i);
            }

            Player player = new Player(name);
            players[idx++] = player;
        }

        // Deck erstellen
        centralDeck = new TileDeck(rnd);

        // Startplättchen für jeden Spieler
        for (Player p : players) {
            Tile startTile = centralDeck.drawStartTile();
            p.placeStartTile(startTile);
        }

        // Erstes Tile ziehen
        drawNextCentralTile();

        // GUI aktualisieren
        if (gui != null) {
            updateGUI();
            gui.enableSaveButton();
        }
    }

    /**
     * Wechselt zum nächsten Spieler.
     */
    private void nextPlayer() {
        currentIndex = (currentIndex + 1) % players.length;
    }

    /**
     * Zieht das nächste Plättchen aus dem zentralen Deck.
     */
    private void drawNextCentralTile() {
        if (centralDeck.isEmpty()) {
            currentTile = null;
            return;
        }

        currentTile = centralDeck.draw();
    }

    /**
     * Prüft ob zwei Tiles identisch sind (gleiche Farbe und gleiches Objekt).
     *
     * @param tile1 erstes Tile (kann null sein)
     * @param tile2 zweites Tile (kann null sein)
     * @return true wenn beide Tiles identisch sind
     */
    private boolean isSameAsStartTile(Tile tile1, Tile tile2) {
        if (tile1 == null || tile2 == null) {
            return false;
        }
        return tile1.getColor() == tile2.getColor()
                && tile1.getObject() == tile2.getObject();
    }

    /**
     * Aktualisiert die gesamte GUI mit dem aktuellen Spielzustand.
     * <p>
     * Wird nach jeder Zustandsänderung aufgerufen (z.B. nach Plättchen-Platzierung,
     * Spielerwechsel, Wertung).
     * </p>
     */
    private void updateGUI() {
        if (gui == null || players.length == 0) return;

        Player current = players[currentIndex];

        gui.showActivePlayer(current);
        gui.renderBoard(current.getBoard().snapshotTiles(),
                current.getBoard().getSnapshotOrigin());
        gui.highlightPositions(current.getBoard().computeValidPositions());
        gui.showNextTile(currentTile);
        gui.updatePlayerBoards(Arrays.asList(players));

        Map<Player, Integer> scores = new LinkedHashMap<>();
        for (Player p : players) {
            scores.put(p, p.getScore());
        }
        gui.showScores(scores);
    }

    /**
     * Behandelt einen Klick auf das Spielfeld (Plättchen-Platzierung).
     * <p>
     * Diese Methode koordiniert den gesamten Spielablauf nach einem Klick
     * </p>
     *
     * @param row Zeile der geklickten Position (Board-Koordinaten)
     * @param col Spalte der geklickten Position (Board-Koordinaten)
     */
    public void onBoardClick(int row, int col) {
        if (players.length == 0 || currentTile == null || waitingForNextPlayer) {
            return;
        }

        Player current = players[currentIndex];

        // CHECK 1: Board voll?
        if (current.getTilesPlaced() >= MAX_TILES_PLACED) {
            handleFullBoardSkip(current);
            return;
        }

        // CHECK 2: Startkarte gezogen?
        if (isSameAsStartTile(currentTile, current.getStartTile())) {
            handleStartTileSkip(current);
            return;
        }

        // Normaler Spielzug
        List<Position> validPos = current.getBoard().computeValidPositions();
        Position clicked = new Position(row, col);

        if (!validPos.contains(clicked)) {
            if (gui != null) {
                gui.showUserError(ErrorType.INVALID_POSITION);
            }
            return;
        }

        try {
            current.getBoard().placeTile(row, col, currentTile);
            current.incrementTilesPlaced();
            updateGUIAfterMove(current);

            List<Combination> combinations = findAllPossibleCombinations(current);

            if (!combinations.isEmpty()) {
                handleScoringPhase(combinations);
            } else {
                handleNoScoringTransition();
            }

        } catch (Exception e) {
            if (gui != null) {
                gui.showUserError(ErrorType.TILE_PLACEMENT_FAILED, e.getMessage());
            }
        }
    }

    /**
     * Behandelt den Fall wenn ein Spieler wegen vollem Board übersprungen wird.
     * @param current
     */
    private void handleFullBoardSkip(Player current) {
        long playersWithFullBoard = Arrays.stream(players)
                .filter(p -> p.getTilesPlaced() >= MAX_TILES_PLACED)
                .count();

        if (playersWithFullBoard < players.length && gui != null) {
            gui.showGameEvent(GameEvent.PLAYER_MUST_SKIP_FULL_BOARD, current);
        }

        advanceToNextPlayer();

        if (!checkGameEnd()) {
            checkIfCurrentPlayerMustSkip();
        }
    }

    /**
     * Behandelt den Fall wenn ein Spieler seine Startkarte zieht.
     * @param current
     */
    private void handleStartTileSkip(Player current) {
        if (gui != null) {
            gui.showGameEvent(GameEvent.PLAYER_SKIP, current);
        }

        playerNeedsCatchUp[currentIndex] = true;
        advanceToNextPlayer();

        if (!checkGameEnd()) {
            checkIfCurrentPlayerMustSkip();
        }
    }

    /**
     * Liefert die Farbe für einen Spielerindex.
     *
     * @param playerIndex Index im Spieler-Array (0-basiert)
     * @return die zugeordnete Spielerfarbe
     * @throws ArrayIndexOutOfBoundsException wenn der Index außerhalb 0..3 liegt
     */
    public PlayerColor getPlayerColor(int playerIndex) {
        return PLAYER_COLORS[playerIndex];
    }

    /**
     * Liefert die Farbe des aktuell aktiven Spielers.
     *
     * @return Spielerfarbe des Spielers an {@code currentIndex}
     */
    public PlayerColor getCurrentPlayerColor() {
        return getPlayerColor(currentIndex);
    }

    /**
     * Wechselt zum nächsten Spieler und zieht ggf. neues Tile.
     */
    private void advanceToNextPlayer() {
        int previousIndex = currentIndex;
        nextPlayer();

        if (previousIndex == players.length - 1) {
            drawNextCentralTile();
        }

        updateGUI();
    }

    /**
     * Startet die Wertungsphase.
     * @param combinations
     */
    private void handleScoringPhase(List<Combination> combinations) {
        waitingForNextPlayer = true;

        if (gui != null) {
            gui.showScoringView(players[currentIndex], combinations, this::onScoringComplete);
        }
    }

    /**
     * Callback nach Abschluss der Wertung.
     */
    private void onScoringComplete() {
        advanceToNextPlayer();
        waitingForNextPlayer = false;

        if (!checkGameEnd()) {
            checkIfCurrentPlayerMustSkip();
        }
    }

    /**
     * Behandelt den Übergang zum nächsten Spieler ohne Wertung.
     */
    private void handleNoScoringTransition() {
        waitingForNextPlayer = true;

        if (gui != null) {
            gui.scheduleNextPlayer(() -> {
                advanceToNextPlayer();
                waitingForNextPlayer = false;

                if (!checkGameEnd()) {
                    checkIfCurrentPlayerMustSkip();
                }
            });
        }
    }

    /**
     * Prüft ob der aktuelle Spieler automatisch übersprungen werden muss.
     */
    private void checkIfCurrentPlayerMustSkip() {
        if (currentTile == null || players.length == 0) {
            return;
        }

        int maxSkips = players.length;
        int skipped = 0;

        while (skipped < maxSkips) {
            Player current = players[currentIndex];

            // check 1: Board voll?
            if (current.getTilesPlaced() >= MAX_TILES_PLACED) {
                advanceToNextPlayerSilent();
                skipped++;
                if (checkGameEnd()) return;
                continue;
            }

            // check 2: Startkarte gezogen?
            if (isSameAsStartTile(currentTile, current.getStartTile())) {
                if (gui != null) {
                    gui.showGameEvent(GameEvent.PLAYER_SKIP, current);
                }
                playerNeedsCatchUp[currentIndex] = true;
                advanceToNextPlayerSilent();
                skipped++;
                if (checkGameEnd()) return;
                continue;
            }

            break;
        }

        updateGUI();
    }

    /**
     * Wechselt zum nächsten Spieler ohne GUI-Update.
     */
    private void advanceToNextPlayerSilent() {
        int previousIndex = currentIndex;
        nextPlayer();

        if (previousIndex == players.length - 1) {
            drawNextCentralTile();
        }
    }

    /**
     * Aktualisiert die GUI nach einer Plättchen-Platzierung.
     * <p>
     * Rendert das Board des Spielers, entfernt Highlights (da Zug beendet),
     * aktualisiert alle Mini-Boards und zeigt aktuelle Punktestände.
     * </p>
     * @param justMoved der Spieler, der gerade ein Plättchen platziert hat
     */
    private void updateGUIAfterMove(Player justMoved) {
        if (gui == null) return;

        gui.renderBoard(justMoved.getBoard().snapshotTiles(),
                justMoved.getBoard().getSnapshotOrigin());
        gui.highlightPositions(java.util.Collections.emptyList());
        gui.updatePlayerBoards(Arrays.asList(players));

        java.util.Map<Player, Integer> scores = new java.util.LinkedHashMap<>();
        for (Player p : players) {
            scores.put(p, p.getScore());
        }
        gui.showScores(scores);
    }

    // --- Scoring ---

    /**
     * Berechnet die Punktzahl einer Kombination.
     * <p>
     * Basis-Punkte nach Größe:
     * <ul>
     *   <li>3 Plättchen → 2 Punkte</li>
     *   <li>4 Plättchen → 4 Punkte</li>
     *   <li>5 Plättchen → 7 Punkte</li>
     * </ul>
     * Bonus: +1 Punkt wenn mindestens ein Plättchen eine Krone hat.
     * </p>
     *
     * @param combo die zu bewertende Kombination (Größe 3-5)
     * @param board das Board mit den Plättchen
     * @return berechnete Punktzahl (2-8 Punkte)
     * @throws IllegalArgumentException wenn Kombinationsgröße ungültig (nicht 3-5)
     */
    public int scoreCombination(Set<Position> combo, Board board) {
        int size = combo.size();
        int basePoints = switch (size) {
            case 3 -> 2;
            case 4 -> 4;
            case 5 -> 7;
            default -> throw new IllegalArgumentException("Ungültige Kombinationsgröße: " + size);
        };

        boolean hasCrown = false;
        for (Position pos : combo) {
            Tile tile = board.getTile(pos);
            if (tile != null && tile.hasCrown()) {
                hasCrown = true;
                break;
            }
        }

        return basePoints + (hasCrown ? 1 : 0);
    }

    /**
     * Führt die Wertung einer Kombination durch.
     * <p>
     * Die umzudrehenden Plättchen müssen Teil der Kombination sein und
     * mindestens 2 orthogonale Nachbarn innerhalb der Kombination haben
     * </p>
     *
     * @param combo die gewertete Kombination
     * @param player der Spieler, der die Wertung durchführt
     * @param tilesToFlip Positionen der umzudrehenden Plättchen (1-2 Elemente)
     * @throws IllegalArgumentException wenn tilesToFlip leer, >2 Elemente hat,
     *         oder Positionen enthält die nicht in combo sind
     */
    public void settleCombination(Set<Position> combo, Player player, List<Position> tilesToFlip) {
        Objects.requireNonNull(combo, "combo");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(tilesToFlip, "tilesToFlip");

        if (tilesToFlip.isEmpty() || tilesToFlip.size() > 2) {
            throw new IllegalArgumentException(
                    "Es müssen 1-2 Tiles umgedreht werden, nicht " + tilesToFlip.size()
            );
        }

        int points = scoreCombination(combo, player.getBoard());
        player.addScore(points);

        for (Position pos : tilesToFlip) {
            if (!combo.contains(pos)) {
                throw new IllegalArgumentException(
                        "Position " + pos + " ist nicht Teil der Kombination"
                );
            }
            player.getBoard().flipTile(pos);
        }

        if (gui != null) {
            updateGUI();
        }
    }

    /**
     * Findet alle wertbaren Kombinationen eines Spielers.
     * <p>
     * Durchsucht das Board nach Kombinationen gleicher Farbe und gleichen Objekts.
     * Eine Kombination ist wertbar, wenn sie mindestens ein umdrehbares Plättchen
     * enthält (Plättchen mit ≥2 Nachbarn in der Kombination).
     * </p>
     * <p>
     * Für jede wertbare Kombination wird ein Combination-Objekt erstellt,
     * das die Positionen, umdrehbare Tiles, den Typ und die Punktzahl enthält.
     * </p>
     *
     * @param player der Spieler
     * @return Liste aller wertbaren Kombinationen
     */
    public List<Combination> findAllPossibleCombinations(Player player) {
        List<Combination> result = new ArrayList<>();
        Board board = player.getBoard();

        List<Set<Position>> colorCombos = board.findCombinationsByColor();
        for (Set<Position> combo : colorCombos) {
            List<Position> flippable = board.getFlippableTilesInCombination(combo);
            if (!flippable.isEmpty()) {
                result.add(new Combination(combo, flippable));
            }
        }

        List<Set<Position>> objectCombos = board.findCombinationsByObject();
        for (Set<Position> combo : objectCombos) {
            List<Position> flippable = board.getFlippableTilesInCombination(combo);
            if (!flippable.isEmpty()) {
                result.add(new Combination(combo, flippable));
            }
        }

        return result;
    }

    /**
     * Verarbeitet eine Wertungs-Auswahl aus der GUI.
     * <p>
     * Wird von der Wertungsansicht aufgerufen,
     * nachdem der Benutzer eine Kombination und ein umzudrehendes Plättchen ausgewählt hat.
     * </p>
     *
     * @param combination die ausgewählte Kombination
     * @param selectedPosition die Position des umzudrehenden Plättchens
     */
    public void scoreCombinationFromGUI(Combination combination, Position selectedPosition) {
        if (players.length == 0) return;

        Player current = players[currentIndex];

        if (!combination.flippablePositions().contains(selectedPosition)) {
            if (gui != null) {
                gui.showUserError(ErrorType.INVALID_SELECTION);
            }
            return;
        }

        settleCombination(combination.positions(), current,
                Collections.singletonList(selectedPosition));

        if (gui != null) {
            gui.hideScoringView();
        }
    }

    /**
     * Überspringt die Wertung ohne Punkte zu vergeben.
     * <p>
     * Wird vom Wertungsansicht aufgerufen, wenn der Benutzer
     * die Wertung abbricht.
     * Schließt die Wertungsansicht ohne Änderungen
     * am Spielzustand (keine Punkte, keine umgedrehten Plättchen).
     * </p>
     */
    public void skipScoring() {
        if (gui != null) {
            gui.hideScoringView();
        }
    }


    // --- Spielende ---

    /**
     * Prüft ob das Spiel zu Ende ist.
     * <p>
     * Das Spiel endet wenn alle Spieler 25 Plättchen gelegt haben.
     * </p>
     *
     * @return true wenn Spiel beendet wurde
     */
    private boolean checkGameEnd() {
        if (gameEnded) {
            return true;
        }

        long playersWithFullBoard = java.util.Arrays.stream(players)
                .filter(p -> p.getTilesPlaced() >= MAX_TILES_PLACED)
                .count();

        if (playersWithFullBoard == players.length) {
            gameEnded = true;
            determineWinner();
            return true;
        }

        return false;
    }

    /**
     * Prüft nach dem Laden, ob wertbare Kombinationen vorhanden sind.
     */
    public void checkForCombinationsAfterLoad() {
        if (players.length == 0 || gui == null) {
            return;
        }

        // alle Spieler voll sind? -> Spielende
        long playersWithFullBoard = java.util.Arrays.stream(players)
                .filter(p -> p.getTilesPlaced() >= MAX_TILES_PLACED)
                .count();

        if (playersWithFullBoard == players.length) {
            gameEnded = true;
            determineWinner();
            return;  // Wertungsansicht verhindern
        }

        Player current = players[currentIndex];

        // Wertungsansicht zeigen wenn Board voll (zb bei Spiel laden relevant)
        if (current.getTilesPlaced() < MAX_TILES_PLACED) {
            return;
        }

        // Board voll -> Prüfe Kombinationen
        List<Combination> combinations = findAllPossibleCombinations(current);

        if (!combinations.isEmpty()) {
            waitingForNextPlayer = true;
            gui.showScoringView(current, combinations, this::onScoringComplete);
        }
    }

    /**
     * Ermittelt den Sieger oder Gleichstand und zeigt das Ergebnis an.
     * <p>
     * Bei Punktgleichstand wird der Tie-Breaker angewendet:
     * Der Spieler mit weniger umgedrehten Plättchen gewinnt.
     * Bei gleichem Score und gleicher Anzahl umgedrehter Plättchen ist es Unentschieden.
     * </p>
     */
    private void determineWinner() {
        if (players.length == 0) {
            return;
        }

        int maxScore = Arrays.stream(players)
                .mapToInt(Player::getScore)
                .max()
                .orElse(0);

        List<Player> playersWithMaxScore = Arrays.stream(players)
                .filter(p -> p.getScore() == maxScore)
                .toList();

        // Tie-Breaker
        List<Player> winners;
        if (playersWithMaxScore.size() > 1) {
            // Minimale Anzahl umgedrehter Plättchen
            int minFlipped = playersWithMaxScore.stream()
                    .mapToInt(p -> p.getBoard().countFlippedTiles())
                    .min()
                    .orElse(0);

            // Spieler mit maxScore und minFlipped sind Gewinner
            winners = playersWithMaxScore.stream()
                    .filter(p -> p.getBoard().countFlippedTiles() == minFlipped)
                    .toList();
        } else {
            winners = playersWithMaxScore;
        }

        if (gui != null) {
            gui.showWinner(winners, maxScore);
        }
    }

    // --- Speichern ---

    /**
     * Gibt den Code des nächsten Plättchens für die Spielstand-Speicherung zurück.
     *
     * @return Plättchen-Code (11-66) oder 99 wenn Deck leer
     */
    public int getNextCard() {
        if (currentTile == null) return NO_NEXT_TILE_CODE;
        return currentTile.getColor() * 10 + currentTile.getObject();
    }

    /**
     * Gibt den aktuellen Spieler-Index zurück.
     *
     * @return Spieler-Index (0-3)
     */
    public int getTurnMeta() {
        return currentIndex;
    }

    /**
     * Prüft ob das Spiel initialisiert ist.
     * <p>
     * Ein Spiel gilt als initialisiert wenn Spieler vorhanden sind
     * und Startplättchen gesetzt wurden.
     * </p>
     *
     * @return true wenn Spiel spielbereit ist
     */
    public boolean isGameInitialized() {
        return players != null && players.length > 0;
    }

    /**
     * Prüft, ob das Spiel vollständig konfiguriert ist.
     *
     * @return true wenn mindestens 2 Spieler mit Startplättchen vorhanden sind
     */
    public boolean isGameConfigured() {
        if (players == null || players.length < 2) {
            return false;
        }

        for (Player player : players) {
            Board board = player.getBoard();
            if (board != null && board.getTileCount() > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Speichert den aktuellen Spielstand in eine JSON-Datei.
     *
     * @param file Ziel-Datei für den Spielstand
     * @throws java.io.IOException wenn Schreibfehler auftritt
     */
    public void saveGameToFile(java.io.File file) throws java.io.IOException {
        int turn0Based = getTurnMeta();
        int nextTileCode = getNextCard();
        SaveGameWriter.write(file, Arrays.asList(players), turn0Based, nextTileCode);
    }

    /**
     * Prüft ob das Spiel in der Wertungsphase ist.
     * <p>
     * Wird verwendet um zu verhindern, dass während der Wertung gespeichert wird.
     * </p>
     *
     * @return true wenn gerade eine Wertung stattfindet
     */
    public boolean isInScoringPhase() {
        return waitingForNextPlayer;
    }

    /**
     * Gibt das Spieler-Array zurück
     * <p>
     * Wird für die Ergebnisanzeige benötigt.
     * </p>
     *
     * @return Array aller Spieler
     */
    public Player[] getPlayers() {
        return java.util.Arrays.copyOf(players, players.length);
    }

    /**
     * Gibt den Index des aktuell aktiven Spielers zurück.
     * <p>
     * Wird für die Hervorhebung des aktiven Spielers benötigt.
     * </p>
     *
     * @return currentIndex (0-basiert, 0-3)
     */
    public int getCurrentPlayerIndex() {
        return currentIndex;
    }
}