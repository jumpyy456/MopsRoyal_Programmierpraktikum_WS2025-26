package gameflow;

import logic.*;
import Support.FakeGUI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

/**
 * Tests für die Game-Klasse.
 * <p>
 * Testet:
 * <ul>
 *   <li>Spielinitialisierung</li>
 *   <li>Spielerwechsel</li>
 *   <li>Tile-Ziehen</li>
 *   <li>Spielablauf</li>
 *   <li>Konstruktoren</li>
 * </ul>
 * </p>
 */
class GameTest {

    private FakeGUI fakeGui;
    private Game game;

    @BeforeEach
    void setUp() {
        fakeGui = new FakeGUI();
        game = new Game(fakeGui);
    }

    // --- Spielinitialisierung ---

    @Test
    @DisplayName("startNewGame() mit 2 Spielern")
    void startNewGame_twoPlayers() {
        List<String> names = List.of("Alice", "Bob");
        boolean[] takesPart = {true, true};

        assertDoesNotThrow(() -> game.startNewGame(names, takesPart));

        assertTrue(fakeGui.wasCalled("showActivePlayer"));
        assertTrue(fakeGui.wasCalled("showNextTile"));
    }

    @Test
    @DisplayName("startNewGame() mit 4 Spielern")
    void startNewGame_fourPlayers() {
        List<String> names = List.of("Alice", "Bob", "Charlie", "Diana");
        boolean[] takesPart = {true, true, true, true};

        assertDoesNotThrow(() -> game.startNewGame(names, takesPart));
    }

    @Test
    @DisplayName("startNewGame() mit nur teilnehmenden Spielern")
    void startNewGame_somePlayersSkipped() {
        List<String> names = List.of("Alice", "Bob", "Charlie", "Diana");
        boolean[] takesPart = {true, false, true, false};

        assertDoesNotThrow(() -> game.startNewGame(names, takesPart));
    }

    @Test
    @DisplayName("startNewGame() wirft Exception bei leerem Namen")
    void startNewGame_emptyName() {
        List<String> names = List.of("Alice", "");
        boolean[] takesPart = {true, true};

        assertThrows(IllegalArgumentException.class, () -> game.startNewGame(names, takesPart));
    }

    @Test
    @DisplayName("startNewGame() wirft Exception bei ungleicher Länge")
    void startNewGame_unequalLength() {
        List<String> names = List.of("Alice", "Bob");
        boolean[] takesPart = {true, true, true};

        assertThrows(IllegalArgumentException.class, () -> game.startNewGame(names, takesPart));
    }

    @Test
    @DisplayName("startNewGame() wirft Exception bei null")
    void startNewGame_nullParameters() {
        assertThrows(NullPointerException.class, () -> {
            game.startNewGame(null, new boolean[]{true});
        });

        assertThrows(NullPointerException.class, () -> {
            game.startNewGame(List.of("Alice"), null);
        });
    }

    // --- Startplättchen ---

    @Test
    @DisplayName("Jeder Spieler erhält ein Startplättchen")
    void eachPlayerGetsStartTile() {
        List<String> names = List.of("Alice", "Bob");
        boolean[] takesPart = {true, true};

        game.startNewGame(names, takesPart);

        assertNotNull(fakeGui.lastNextTile);
    }

    // --- Konstruktor-Tests ---

    @Test
    @DisplayName("Konstruktor 1: Neues Spiel mit GUIConnector")
    void constructor1_newGame() {
        FakeGUI gui = new FakeGUI();
        assertDoesNotThrow(() -> new Game(gui));
    }

    @Test
    @DisplayName("Konstruktor 1: Wirft Exception bei null GUI")
    void constructor1_nullGui() {
        assertThrows(NullPointerException.class, () -> new Game(null));
    }

    @Test
    @DisplayName("Konstruktor 2: Mit Board-Array für Tests")
    void constructor2_withBoardArray() {
        FakeGUI gui = new FakeGUI();

        // Verschiedene Tiles (keine Duplikate!)
        int[][] boardData = {
                {110, 120, 990, 990, 990},  // Blau-Kissen, Blau-Knochen
                {130, 990, 990, 990, 990},  // Blau-Napf
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };

        assertDoesNotThrow(() -> new Game(gui, boardData));
    }

    @Test
    @DisplayName("Konstruktor 2: Wirft Exception bei null Board")
    void constructor2_nullBoard() {
        FakeGUI gui = new FakeGUI();
        assertThrows(NullPointerException.class, () -> new Game(gui, null));
    }

    @Test
    @DisplayName("Konstruktor 3: Laden eines Spielstands")
    void constructor3_loadGameState() {
        FakeGUI gui = new FakeGUI();

        // Boards mit verschiedenen Tiles
        int[][] board1 = {
                {120, 990, 990, 990, 990},  // Blau-Knochen (Start)
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };
        int[][] board2 = {
                {230, 990, 990, 990, 990},  // Grün-Napf (Start)
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };

        List<Game.PlayerData> playerData = List.of(
                new Game.PlayerData("Alice", 5, board1, 120),
                new Game.PlayerData("Bob", 3, board2, 230)
        );

        assertDoesNotThrow(() -> new Game(gui, playerData, 0, 34));  // Orange-Dose als nächstes
    }

    @Test
    @DisplayName("Konstruktor 3: Wirft Exception bei zu wenig Spielern")
    void constructor3_tooFewPlayers() {
        FakeGUI gui = new FakeGUI();

        int[][] board = {
                {120, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };

        List<Game.PlayerData> playerData = List.of(
                new Game.PlayerData("Alice", 0, board, 120)
        );

        assertThrows(IllegalArgumentException.class, () ->
                new Game(gui, playerData, 0, 110));
    }

    @Test
    @DisplayName("Konstruktor 3: Wirft Exception bei zu vielen Spielern")
    void constructor3_tooManyPlayers() {
        FakeGUI gui = new FakeGUI();

        int[][] board = {
                {120, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };

        List<Game.PlayerData> playerData = List.of(
                new Game.PlayerData("Alice", 0, board, 120),
                new Game.PlayerData("Bob", 0, board, 230),
                new Game.PlayerData("Charlie", 0, board, 340),
                new Game.PlayerData("Diana", 0, board, 410),
                new Game.PlayerData("Eve", 0, board, 520)  // 5 Spieler!
        );

        assertThrows(IllegalArgumentException.class, () ->
                new Game(gui, playerData, 0, 110));
    }

    // --- Speichern/Laden ---

    @Test
    @DisplayName("getNextCard() gibt korrekten Code zurück")
    void getNextCard_correctCode() {
        List<String> names = List.of("Alice", "Bob");
        boolean[] takesPart = {true, true};

        game.startNewGame(names, takesPart);

        int code = game.getNextCard();
        assertTrue(code >= 11 && code <= 66 || code == 99);
    }

    @Test
    @DisplayName("getTurnMeta() gibt aktuellen Spieler zurück")
    void getTurnMeta_currentPlayer() {
        List<String> names = List.of("Alice", "Bob");
        boolean[] takesPart = {true, true};

        game.startNewGame(names, takesPart);

        int turn = game.getTurnMeta();
        assertTrue(turn >= 0 && turn <= 1);
    }

    // --- Scoring ---

    @Test
    @DisplayName("scoreCombination() gibt korrekte Punkte für 3er")
    void scoreCombination_three() {
        Board b = new Board();
        Set<Position> combo = Set.of(
                new Position(0, 0),
                new Position(0, 1),
                new Position(0, 2)
        );

        // Verschiedene Tiles mit gleicher Farbe
        b.placeTile(new Position(0, 0), Tile.of(1, 2));  // Blau Knochen
        b.placeTile(new Position(0, 1), Tile.of(1, 3));  // Blau Napf
        b.placeTile(new Position(0, 2), Tile.of(1, 4));  // Blau Dose

        assertEquals(2, game.scoreCombination(combo, b),
                "3er-Kombination sollte 2 Punkte geben");
    }

    @Test
    @DisplayName("scoreCombination() gibt korrekte Punkte für 4er")
    void scoreCombination_four() {
        Board b = new Board();
        Set<Position> combo = Set.of(
                new Position(0, 0),
                new Position(0, 1),
                new Position(0, 2),
                new Position(1, 0)
        );

        // Verschiedene Tiles mit gleicher Farbe
        b.placeTile(new Position(0, 0), Tile.of(2, 1));  // Grün Kissen
        b.placeTile(new Position(0, 1), Tile.of(2, 2));  // Grün Knochen
        b.placeTile(new Position(0, 2), Tile.of(2, 3));  // Grün Napf
        b.placeTile(new Position(1, 0), Tile.of(2, 4));  // Grün Dose

        assertEquals(4, game.scoreCombination(combo, b),
                "4er-Kombination sollte 4 Punkte geben");
    }

    @Test
    @DisplayName("scoreCombination() gibt korrekte Punkte für 5er")
    void scoreCombination_five() {
        Board b = new Board();
        Set<Position> combo = Set.of(
                new Position(0, 0),
                new Position(0, 1),
                new Position(0, 2),
                new Position(1, 0),
                new Position(2, 0)
        );

        // Verschiedene Tiles mit gleicher Farbe (Orange, vermeidet 3,3 royal)
        b.placeTile(new Position(0, 0), Tile.of(3, 1));  // Orange Kissen
        b.placeTile(new Position(0, 1), Tile.of(3, 2));  // Orange Knochen
        b.placeTile(new Position(0, 2), Tile.of(3, 4));  // Orange Dose
        b.placeTile(new Position(1, 0), Tile.of(3, 5));  // Orange Kackhaufen
        b.placeTile(new Position(2, 0), Tile.of(3, 6));  // Orange Mops

        assertEquals(7, game.scoreCombination(combo, b),
                "5er-Kombination sollte 7 Punkte geben");
    }

    @Test
    @DisplayName("scoreCombination() gibt Bonuspunkt für Krone")
    void scoreCombination_withCrown() {
        Board b = new Board();
        Set<Position> combo = Set.of(
                new Position(0, 0),
                new Position(0, 1),
                new Position(0, 2)
        );

        // Blau-Kissen (1,1) ist royal!
        b.placeTile(new Position(0, 0), Tile.of(1, 1));  // Blau Kissen (ROYAL)
        b.placeTile(new Position(0, 1), Tile.of(1, 2));  // Blau Knochen
        b.placeTile(new Position(0, 2), Tile.of(1, 3));  // Blau Napf

        assertEquals(3, game.scoreCombination(combo, b),
                "3er-Kombination mit Krone sollte 3 Punkte geben (2 + 1 Bonus)");
    }

    @Test
    @DisplayName("scoreCombinationFromGUI(): Bei L-Form wird genau das ausgewählte Tile geflippt (2 Auswahlmöglichkeiten)")
    void scoreCombinationFromGUI_LSelection_flipsOnlySelected() {
        FakeGUI gui = new FakeGUI();

        // 4er-L-Form (gleiche Farbe, verschiedene Objekte), keine Royals -> keine Krone
        int[][] boardData = {
                {120, 130, 140, 990, 990},
                {150, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990},
                {990, 990, 990, 990, 990}
        };

        Game g = new Game(gui, boardData);
        Player player = g.getPlayers()[0];
        Board b = player.getBoard();

        Set<Position> combo = Set.of(
                new Position(0, 0),
                new Position(0, 1),
                new Position(0, 2),
                new Position(1, 0)
        );

        List<Position> flippable = b.getFlippableTilesInCombination(combo);
        assertEquals(2, flippable.size(),
                "Bei einer 4er-L-Form sollten genau zwei Tiles umdrehbar sein");

        Position selected = flippable.get(0);
        Position other = flippable.get(1);

        assertFalse(b.getTile(selected).isFlipped(), "Ausgewähltes Tile muss vorher unflipped sein");
        assertFalse(b.getTile(other).isFlipped(), "Das andere Kandidaten-Tile muss vorher unflipped sein");

        int scoreBefore = player.getScore();

        Combination c = new Combination(combo, flippable);
        g.scoreCombinationFromGUI(c, selected);

        // Genau das ausgewählte Tile wird geflippt
        assertTrue(b.getTile(selected).isFlipped(), "Ausgewähltes Tile muss geflippt werden");
        assertFalse(b.getTile(other).isFlipped(), "Nicht ausgewähltes Tile darf nicht geflippt werden");

        // Punkte: 4er-Kombination -> 4 Punkte (keine Krone in dieser Kombi)
        assertEquals(scoreBefore + 4, player.getScore(), "4er-Kombination sollte 4 Punkte geben");
    }



}