package gameflow;

import Support.FakeGUI;
import logic.Game;
import logic.GameEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Umfassende Tests für Aussetzen-Mechanismen in Mops Royal.
 */
class StartTileAndFullBoardTest {

    @Test
    @DisplayName("Spieler zieht eigene Startkarte → muss aussetzen")
    void playerDrawsOwnStartTile_mustSkip() {
        FakeGUI gui = new FakeGUI();

        int[][] board1 = createBoardWithStartTile(120);  // Blau-Knochen
        int[][] board2 = createBoardWithStartTile(230);  // Grün-Napf

        List<Game.PlayerData> playerData = List.of(
                new Game.PlayerData("Alice", 0, board1, 120),
                new Game.PlayerData("Bob", 0, board2, 230)
        );

        // Alice ist am Zug, nächstes Tile ist 12 (ihre Startkarte!)
        Game game = new Game(gui, playerData, 0, 12);

        game.onBoardClick(0, 1);

        assertTrue(gui.wasCalled("showGameEvent"));
        assertEquals(GameEvent.PLAYER_SKIP, gui.lastGameEvent);
        assertEquals("Alice", gui.lastGameEventPlayer.getName());
    }

    @Test
    @DisplayName("Startkarte wird NICHT aus dem Spiel entfernt nach Aussetzen")
    void startTileRemainsInGame_afterPlayerSkips() {
        FakeGUI gui = new FakeGUI();

        int[][] board1 = createBoardWithNTiles(5, 120);
        int[][] board2 = createBoardWithNTiles(5, 230);

        List<Game.PlayerData> playerData = List.of(
                new Game.PlayerData("Alice", 0, board1, 120),
                new Game.PlayerData("Bob", 0, board2, 230)
        );

        Game game = new Game(gui, playerData, 0, 12);

        game.onBoardClick(1, 0);

        int nextCard = game.getNextCard();
        assertEquals(12, nextCard,
                "Die Startkarte sollte noch verfügbar sein für den nächsten Spieler");

        assertEquals("Bob", gui.lastActivePlayer.getName());
    }

    @Test
    @DisplayName("Integration: Startkarte bleibt im Deck nach startNewGame()")
    void integrationTest_startTileRemainsInDeckAfterGameStart() {
        FakeGUI gui = new FakeGUI();
        Game game = new Game(gui);

        List<String> names = List.of("Alice", "Bob");
        boolean[] takesPart = {true, true};
        game.startNewGame(names, takesPart);

        int firstTile = game.getNextCard();
        assertTrue(firstTile >= 11 && firstTile <= 66);
    }

    // ========================================
    // TESTS: VOLLES BOARD (25 TILES)
    // ========================================

    @Test
    @DisplayName("Spieler mit vollem Board (25 Tiles) muss aussetzen")
    void playerWithFullBoard_mustSkipTurn() {
        FakeGUI gui = new FakeGUI();

        int[][] board1 = createFullBoard(120);  // 25 Tiles
        int[][] board2 = createFullBoard(230);  // 25 Tiles
        int[][] board3 = createBoardWithNTiles(23, 340);  // 24 Tiles

        List<Game.PlayerData> playerData = List.of(
                new Game.PlayerData("Alice", 10, board1, 120),
                new Game.PlayerData("Bob", 12, board2, 230),
                new Game.PlayerData("Charlie", 8, board3, 340)
        );

        Game game = new Game(gui, playerData, 1, 45);

        game.onBoardClick(0, 3);

        assertTrue(gui.wasCalled("showGameEvent"));
        assertEquals(GameEvent.PLAYER_MUST_SKIP_FULL_BOARD, gui.lastGameEvent);
        assertEquals("Bob", gui.lastGameEventPlayer.getName());
    }

    @Test
    @DisplayName("Spieler mit vollem Board: Klick wechselt zum nächsten Spieler")
    void playerWithFullBoard_clickSwitchesToNextPlayer() {
        FakeGUI gui = new FakeGUI();

        int[][] fullBoard = createFullBoard(120);
        int[][] normalBoard = createBoardWithNTiles(20, 230);

        List<Game.PlayerData> playerData = List.of(
                new Game.PlayerData("Alice", 10, fullBoard, 120),
                new Game.PlayerData("Bob", 8, normalBoard, 230)
        );

        Game game = new Game(gui, playerData, 0, 34);

        gui.reset();
        game.onBoardClick(0, 3);

        assertTrue(gui.wasCalled("showActivePlayer"));
        assertEquals("Bob", gui.lastActivePlayer.getName());
    }

    @Test
    @DisplayName("Bei Spielende: Alle Spieler haben 25 Tiles")
    void allPlayersHave25Tiles_gameEnds() {
        FakeGUI gui = new FakeGUI();

        int[][] fullBoard1 = createFullBoard(120);
        int[][] fullBoard2 = createFullBoard(230);
        int[][] fullBoard3 = createFullBoard(340);

        List<Game.PlayerData> playerData = List.of(
                new Game.PlayerData("Alice", 15, fullBoard1, 120),
                new Game.PlayerData("Bob", 18, fullBoard2, 230),
                new Game.PlayerData("Charlie", 12, fullBoard3, 340)
        );

        Game game = new Game(gui, playerData, 2, 34);

        assertDoesNotThrow(() -> game.onBoardClick(0, 0));
    }

    @Test
    @DisplayName("Spieler mit vollem Board zieht Startkarte → Setzt aus OHNE Nachholrunde")
    void playerWithFullBoard_drawsStartTile_skipsWithoutCatchup() {
        FakeGUI gui = new FakeGUI();

        int[][] fullBoard = createFullBoard(120);
        int[][] normalBoard = createBoardWithNTiles(20, 230);

        List<Game.PlayerData> playerData = List.of(
                new Game.PlayerData("Alice", 10, fullBoard, 120),
                new Game.PlayerData("Bob", 8, normalBoard, 230)
        );

        // Alice ist am Zug, Tile ist 12 (ihre Startkarte!)
        Game game = new Game(gui, playerData, 0, 12);

        game.onBoardClick(0, 3);

        assertTrue(gui.wasCalled("showGameEvent"));
        assertEquals("Bob", gui.lastActivePlayer.getName());
        assertEquals(GameEvent.PLAYER_MUST_SKIP_FULL_BOARD, gui.lastGameEvent);
    }

    // ========================================
    // HILFSMETHODEN
    // ========================================

    /**
     * Erstellt ein Board mit nur einem Startplättchen.
     */
    private int[][] createBoardWithStartTile(int startTileCode) {
        int[][] board = new int[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                board[r][c] = 990;
            }
        }
        board[0][0] = startTileCode;
        return board;
    }

    /**
     * Erstellt ein Board mit n Tiles (+ Startplättchen).
     * Verwendet verschiedene Tiles (keine Duplikate!).
     */
    private int[][] createBoardWithNTiles(int n, int startTileCode) {
        int[][] board = new int[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                board[r][c] = 990;
            }
        }

        board[0][0] = startTileCode;

        // Verschiedene Tiles für jede Position
        int[][] tiles = {
                {120, 130, 140, 150, 160},  // Blau 2-6
                {210, 220, 240, 250, 260},  // Grün 1,2,4,5,6
                {310, 320, 340, 350, 360},  // Orange 1,2,4,5,6
                {410, 420, 430, 450, 460},  // Rosa 1,2,3,5,6
                {510, 530, 540, 550, 560}   // Lila 1,3,4,5,6
        };

        int placed = 0;
        for (int r = 0; r < 5 && placed < n; r++) {
            for (int c = 0; c < 5 && placed < n; c++) {
                if (r == 0 && c == 0) continue;
                board[r][c] = tiles[r][c];
                placed++;
            }
        }

        return board;
    }

    /**
     * Erstellt ein volles 5x5 Board mit 25 verschiedenen Tiles.
     */
    private int[][] createFullBoard(int startTileCode) {
        // 25 verschiedene Tiles (keine Duplikate!)
        return new int[][] {
                {startTileCode, 130, 140, 150, 160},  // Row 0
                {210, 220, 240, 250, 260},            // Row 1
                {310, 320, 340, 350, 360},            // Row 2
                {410, 420, 430, 450, 460},            // Row 3
                {510, 530, 540, 550, 560}             // Row 4
        };
    }
}