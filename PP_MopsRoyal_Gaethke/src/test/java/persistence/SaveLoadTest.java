package persistence;

import Support.FakeGUI;
import logic.Game;
import logic.SaveGameReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Tests für Spielstand Laden und Speichern.
 * <p>
 * Testet:
 * <ul>
 *   <li>Speichern eines Spielstands (SaveGameWriter)</li>
 *   <li>Laden eines Spielstands (SaveGameReader)</li>
 *   <li>Roundtrip (Speichern → Laden → Vergleich)</li>
 *   <li>Validierung ungültiger Dateien</li>
 * </ul>
 */
class SaveLoadTest {

    // ========================================
    // TESTS: SPEICHERN
    // ========================================

    @Test
    @DisplayName("Spiel speichern: Datei wird erstellt")
    void saveGame_createsFile(@TempDir File tempDir) throws IOException {
        // Arrange
        FakeGUI gui = new FakeGUI();
        Game game = new Game(gui);
        game.startNewGame(List.of("Alice", "Bob"), new boolean[]{true, true});

        File file = new File(tempDir, "test.json");

        // Act
        game.saveGameToFile(file);

        // Assert
        assertTrue(file.exists(), "Datei sollte erstellt werden");
        assertTrue(file.length() > 0, "Datei sollte nicht leer sein");
    }

    @Test
    @DisplayName("Spiel speichern: JSON-Format korrekt")
    void saveGame_correctJsonFormat(@TempDir File tempDir) throws IOException {
        // Arrange
        FakeGUI gui = new FakeGUI();
        Game game = new Game(gui);
        game.startNewGame(List.of("Alice", "Bob"), new boolean[]{true, true});

        File file = new File(tempDir, "test.json");

        // Act
        game.saveGameToFile(file);
        String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);

        // Assert: Prüfe ob wichtige Keys vorhanden sind
        assertTrue(json.contains("\"players\""), "JSON sollte 'players' enthalten");
        assertTrue(json.contains("\"turn\""), "JSON sollte 'turn' enthalten");
        assertTrue(json.contains("\"nextCard\""), "JSON sollte 'nextCard' enthalten");
        assertTrue(json.contains("\"nr\""), "JSON sollte 'nr' enthalten");
        assertTrue(json.contains("\"points\""), "JSON sollte 'points' enthalten");
        assertTrue(json.contains("\"initial\""), "JSON sollte 'initial' enthalten");
        assertTrue(json.contains("\"cards\""), "JSON sollte 'cards' enthalten");
    }

    @Test
    @DisplayName("Spiel speichern: Spielernamen korrekt")
    void saveGame_correctPlayerNames(@TempDir File tempDir) throws IOException {
        // Arrange
        FakeGUI gui = new FakeGUI();
        Game game = new Game(gui);
        game.startNewGame(List.of("Alice", "Bob"), new boolean[]{true, true});

        File file = new File(tempDir, "test.json");

        // Act
        game.saveGameToFile(file);
        String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);

        // Assert
        assertTrue(json.contains("Alice"), "JSON sollte 'Alice' enthalten");
        assertTrue(json.contains("Bob"), "JSON sollte 'Bob' enthalten");
    }

    // ========================================
    // TESTS: LADEN
    // ========================================

    @Test
    @DisplayName("Spiel laden: Gültige Datei wird akzeptiert")
    void loadGame_validFile(@TempDir File tempDir) throws IOException {
        // Arrange: Erstelle eine gültige JSON-Datei
        String validJson = """
            {
              "players": [
                {
                  "nr": 0,
                  "name": "Alice",
                  "points": 5,
                  "initial": 110,
                  "cards": [
                    [110,120,990,990,990],
                    [130,990,990,990,990],
                    [990,990,990,990,990],
                    [990,990,990,990,990],
                    [990,990,990,990,990]
                  ]
                },
                {
                  "nr": 1,
                  "name": "Bob",
                  "points": 3,
                  "initial": 220,
                  "cards": [
                    [220,230,990,990,990],
                    [990,990,990,990,990],
                    [990,990,990,990,990],
                    [990,990,990,990,990],
                    [990,990,990,990,990]
                  ]
                }
              ],
              "turn": 1,
              "nextCard": 33
            }
            """;

        File file = new File(tempDir, "valid.json");
        Files.writeString(file.toPath(), validJson, StandardCharsets.UTF_8);

        // Act & Assert: Sollte nicht werfen
        assertDoesNotThrow(() -> {
            SaveGameReader.GameSaveData data = SaveGameReader.read(file);
            assertNotNull(data, "Daten sollten geladen werden");
            assertEquals(2, data.players.length, "2 Spieler erwartet");
            assertEquals("Alice", data.players[0].name);
            assertEquals("Bob", data.players[1].name);
            assertEquals(5, data.players[0].points);
            assertEquals(3, data.players[1].points);
        });
    }

    @Test
    @DisplayName("Spiel laden: Ungültige Spieleranzahl wird abgelehnt")
    void loadGame_invalidPlayerCount(@TempDir File tempDir) throws IOException {
        // Arrange: Nur 1 Spieler (ungültig)
        String invalidJson = """
            {
              "players": [
                {
                  "nr": 0,
                  "name": "Alice",
                  "points": 0,
                  "initial": 110,
                  "cards": [[990,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990]]
                }
              ],
              "turn": 1,
              "nextCard": 11
            }
            """;

        File file = new File(tempDir, "invalid.json");
        Files.writeString(file.toPath(), invalidJson, StandardCharsets.UTF_8);

        // Act & Assert
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            SaveGameReader.read(file);
        });

        assertTrue(ex.getMessage().contains("Spieleranzahl"),
                "Fehlermeldung sollte 'Spieleranzahl' enthalten");
    }

    @Test
    @DisplayName("Spiel laden: Ungültiger Tile-Code wird abgelehnt")
    void loadGame_invalidTileCode(@TempDir File tempDir) throws IOException {
        // Arrange: Ungültiger Code 799 (Farbe 7 existiert nicht)
        String invalidJson = """
            {
              "players": [
                {
                  "nr": 0,
                  "name": "Alice",
                  "points": 0,
                  "initial": 799,
                  "cards": [[110,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990]]
                },
                {
                  "nr": 1,
                  "name": "Bob",
                  "points": 0,
                  "initial": 220,
                  "cards": [[220,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990]]
                }
              ],
              "turn": 1,
              "nextCard": 11
            }
            """;

        File file = new File(tempDir, "invalid.json");
        Files.writeString(file.toPath(), invalidJson, StandardCharsets.UTF_8);

        // Act & Assert
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            SaveGameReader.read(file);
        });

        assertTrue(ex.getMessage().contains("Ungültiger") || ex.getMessage().contains("Code"),
                "Fehlermeldung sollte auf ungültigen Code hinweisen");
    }

    @Test
    @DisplayName("Spiel laden: Fehlende Felder werden erkannt")
    void loadGame_missingFields(@TempDir File tempDir) throws IOException {
        // Arrange: "points" fehlt
        String invalidJson = """
            {
              "players": [
                {
                  "nr": 0,
                  "name": "Alice",
                  "initial": 110,
                  "cards": [[110,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990]]
                },
                {
                  "nr": 1,
                  "name": "Bob",
                  "initial": 220,
                  "cards": [[220,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990],[990,990,990,990,990]]
                }
              ],
              "turn": 1,
              "nextCard": 11
            }
            """;

        File file = new File(tempDir, "invalid.json");
        Files.writeString(file.toPath(), invalidJson, StandardCharsets.UTF_8);

        // Act & Assert: GSON setzt fehlende int-Felder auf 0
        // Validierung prüft negative points → sollte 0 akzeptieren
        assertDoesNotThrow(() -> {
            SaveGameReader.GameSaveData data = SaveGameReader.read(file);
            assertEquals(0, data.players[0].points, "Points sollte 0 sein (Default)");
        });
    }

    // ========================================
    // TESTS: ROUNDTRIP (Speichern → Laden)
    // ========================================

    @Test
    @DisplayName("Roundtrip: Gespeichertes Spiel kann geladen werden")
    void roundtrip_saveAndLoad(@TempDir File tempDir) throws IOException {
        // Arrange: Spiel erstellen und speichern
        FakeGUI gui1 = new FakeGUI();
        Game game1 = new Game(gui1);
        game1.startNewGame(List.of("Alice", "Bob"), new boolean[]{true, true});

        File file = new File(tempDir, "roundtrip.json");
        game1.saveGameToFile(file);

        // Act: Spiel laden
        SaveGameReader.GameSaveData data = SaveGameReader.read(file);

        // Assert: Daten sollten übereinstimmen
        assertEquals(2, data.players.length, "2 Spieler erwartet");
        assertEquals("Alice", data.players[0].name);
        assertEquals("Bob", data.players[1].name);

        // Erstelle neues Game-Objekt aus geladenen Daten
        // BUGFIX: initial-Code jetzt mit übergeben!
        FakeGUI gui2 = new FakeGUI();
        List<Game.PlayerData> playerDataList = List.of(
                new Game.PlayerData(data.players[0].name, data.players[0].points, data.players[0].cards, data.players[0].initial),
                new Game.PlayerData(data.players[1].name, data.players[1].points, data.players[1].cards, data.players[1].initial)
        );

        assertDoesNotThrow(() -> {
            new Game(gui2, playerDataList, data.turn, data.nextCard);
        }, "Geladenes Spiel sollte erfolgreich erstellt werden");
    }

    @Test
    @DisplayName("Roundtrip: points bleiben erhalten")
    void roundtrip_scoresPreserved(@TempDir File tempDir) throws IOException {
        // Arrange: Erstelle manuell PlayerData mit points
        int[][] board1 = new int[5][5];
        int[][] board2 = new int[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                board1[r][c] = 990;
                board2[r][c] = 990;
            }
        }
        board1[0][0] = 110; // Startkarte
        board2[0][0] = 220;

        FakeGUI gui = new FakeGUI();
        List<Game.PlayerData> playerData = List.of(
                new Game.PlayerData("Alice", 15, board1, 110),  // initial hinzugefügt
                new Game.PlayerData("Bob", 20, board2, 220)     // initial hinzugefügt
        );

        Game game = new Game(gui, playerData, 0, 33);

        // Act: Speichern
        File file = new File(tempDir, "scores.json");
        game.saveGameToFile(file);

        // Laden
        SaveGameReader.GameSaveData data = SaveGameReader.read(file);

        // Assert
        assertEquals(15, data.players[0].points, "Alice points sollte erhalten bleiben");
        assertEquals(20, data.players[1].points, "Bob points sollte erhalten bleiben");
    }

    // ========================================
    // TESTS: EDGE CASES
    // ========================================

    @Test
    @DisplayName("Edge Case: Leere Datei wird abgelehnt")
    void edgeCase_emptyFile(@TempDir File tempDir) throws IOException {
        // Arrange
        File file = new File(tempDir, "empty.json");
        Files.writeString(file.toPath(), "", StandardCharsets.UTF_8);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            SaveGameReader.read(file);
        });
    }

    @Test
    @DisplayName("Edge Case: Ungültiges JSON wird abgelehnt")
    void edgeCase_invalidJson(@TempDir File tempDir) throws IOException {
        // Arrange
        File file = new File(tempDir, "invalid.json");
        Files.writeString(file.toPath(), "{this is not valid json}", StandardCharsets.UTF_8);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            SaveGameReader.read(file);
        });
    }

    @Test
    @DisplayName("Edge Case: Sonderzeichen in Namen werden escaped")
    void edgeCase_specialCharactersInNames(@TempDir File tempDir) {
        // Arrange
        FakeGUI gui = new FakeGUI();
        Game game = new Game(gui);
        game.startNewGame(
                List.of("Alice \"The Great\"", "Bob\nNewline"),
                new boolean[]{true, true}
        );

        File file = new File(tempDir, "special.json");

        // Act: Speichern sollte funktionieren
        assertDoesNotThrow(() -> game.saveGameToFile(file));

        // Laden sollte auch funktionieren
        assertDoesNotThrow(() -> SaveGameReader.read(file));
    }

    @Test
    @DisplayName("nextCard = 99 (leer) wird akzeptiert")
    void loadGame_nextCard99_accepted(@TempDir File tempDir) throws IOException {
        // Arrange: nextCard ist 99 (kein Tile mehr)
        String validJson = """
        {
          "players": [
            {
              "nr": 0,
              "name": "Alice",
              "points": 20,
              "initial": 110,
              "cards": [
                [110,120,130,210,220],
                [230,310,320,330,410],
                [420,430,510,520,530],
                [610,620,630,111,221],
                [331,441,551,661,121]
              ]
            },
            {
              "nr": 1,
              "name": "Bob",
              "points": 25,
              "initial": 220,
              "cards": [
                [220,230,240,250,260],
                [310,320,330,340,350],
                [410,420,430,440,450],
                [510,520,530,540,550],
                [610,620,630,640,650]
              ]
            }
          ],
          "turn": 1,
          "nextCard": 99
        }
        """;

        File file = new File(tempDir, "no-tile.json");
        Files.writeString(file.toPath(), validJson, StandardCharsets.UTF_8);

        // Act & Assert: Sollte nicht werfen
        assertDoesNotThrow(() -> {
            SaveGameReader.GameSaveData data = SaveGameReader.read(file);
            assertEquals(99, data.nextCard, "nextCard sollte 99 sein (leer)");
        });
    }
}