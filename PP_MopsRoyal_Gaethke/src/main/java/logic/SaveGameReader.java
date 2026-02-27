package logic;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static logic.SaveGameWriter.validateNextCardCode;

/**
 * Liest einen Spielstand aus einer JSON-Datei.
 * <p>
 * Nutzt GSON für sicheres JSON-Parsing.
 * </p>
 */
public final class SaveGameReader {

    // --- Konstanten ---
    /** Save/Load: leeres Board-Feld */
    private static final int EMPTY_CELL_CODE = 990;

    private SaveGameReader() {}

    /**
     * Liest einen Spielstand aus einer Datei.
     *
     * @param file JSON-Datei mit Spielstand
     * @return geparster Spielstand
     * @throws IOException bei Lesefehlern
     * @throws IllegalArgumentException bei ungültigen Daten
     */
    public static GameSaveData read(File file) throws IOException {
        String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);

        Gson gson = new Gson();
        GameSaveData data;

        try {
            data = gson.fromJson(json, GameSaveData.class);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Ungültiges JSON-Format: " + e.getMessage(), e);
        }

        if (data == null) {
            throw new IllegalArgumentException("JSON-Datei ist leer oder ungültig");
        }

        // Validierung
        validateGameSaveData(data);

        return data;
    }

    /**
     * Validiert die geladenen Daten.
     */
    private static void validateGameSaveData(GameSaveData data) {
        // Spieler vorhanden?
        if (data.players == null || data.players.length == 0) {
            throw new IllegalArgumentException("Keine Spieler in Spielstandsdatei");
        }

        // Anzahl Spieler (2-4)
        if (data.players.length < 2 || data.players.length > 4) {
            throw new IllegalArgumentException(
                    "Ungültige Spieleranzahl: " + data.players.length + " (erwartet: 2-4)"
            );
        }

        // turn (0-basiert: Spieler 0 bis n-1) (0-basiert: 0 = erster Spieler, 1 = zweiter Spieler, etc.)
        if (data.turn < 0 || data.turn >= data.players.length) {
            throw new IllegalArgumentException(
                    "Ungültiger turn: " + data.turn + " (erwartet: 0-" + (data.players.length - 1) + ")"
            );
        }

        // nextCard
        validateNextCardCode(data.nextCard, "nextCard");

        // Spieler validieren
        for (int i = 0; i < data.players.length; i++) {
            PlayerSaveData p = data.players[i];

            if (p == null) {
                throw new IllegalArgumentException("Spieler " + i + " ist null");
            }

            // nr
            if (p.nr != i) {
                throw new IllegalArgumentException(
                        "Spieler " + i + " hat falsche nr: " + p.nr
                );
            }

            // name
            if (p.name == null || p.name.trim().isEmpty()) {
                throw new IllegalArgumentException("Spieler " + i + " hat leeren Namen");
            }

            // points
            if (p.points < 0) {
                throw new IllegalArgumentException(
                        "Spieler " + i + " hat negative Punkte: " + p.points
                );
            }

            // initial (Startkarte)
            validateTileCode(p.initial, "initial von Spieler " + i);

            // cards (Board muss 5x5 sein)
            if (p.cards == null) {
                throw new IllegalArgumentException("Spieler " + i + " hat keine cards");
            }

            if (p.cards.length != 5) {
                throw new IllegalArgumentException(
                        "Spieler " + i + " hat " + p.cards.length + " Zeilen (erwartet: 5)"
                );
            }

            for (int r = 0; r < 5; r++) {
                if (p.cards[r] == null) {
                    throw new IllegalArgumentException(
                            "Spieler " + i + " Zeile " + r + " ist null"
                    );
                }

                if (p.cards[r].length != 5) {
                    throw new IllegalArgumentException(
                            "Spieler " + i + " Zeile " + r + " hat " + p.cards[r].length +
                                    " Spalten (erwartet: 5)"
                    );
                }

                // Jede Zelle validieren
                for (int c = 0; c < 5; c++) {
                    int code = p.cards[r][c];
                    if (code == EMPTY_CELL_CODE) continue;  // Leer ist OK

                    int color = code / 100;
                    int object = (code % 100) / 10;
                    int flipped = code % 10;

                    if (color < 1 || color > 6 || object < 1 || object > 6 || flipped > 1) {
                        throw new IllegalArgumentException(
                                "Spieler " + i + " Position (" + r + "," + c + "): " +
                                        "Ungültiger Code " + code
                        );
                    }
                }
            }
        }
    }

    /**
     * Validiert einen Tile-Code (3-stellig oder 990 für leer).
     */
    private static void validateTileCode(int code, String fieldName) {
        if (code == EMPTY_CELL_CODE) return;  // Leer ist OK

        int color = code / 100;
        int object = (code % 100) / 10;

        if (color < 1 || color > 6 || object < 1 || object > 6) {
            throw new IllegalArgumentException(
                    "Ungültiger " + fieldName + " Code: " + code
            );
        }
    }

    /**
     * Daten-Container für geladenen Spielstand.
     * <p>
     * GSON nutzt diese Klasse zum Deserialisieren.
     * Feldnamen müssen exakt mit JSON-Keys übereinstimmen!
     * </p>
     */
    public static class GameSaveData {
        public PlayerSaveData[] players;
        public int turn;
        public int nextCard;
    }

    /**
     * Daten-Container für einen Spieler.
     */
    public static class PlayerSaveData {
        public int nr;
        public String name;
        public int points;
        public int initial;
        public int[][] cards;
    }
}