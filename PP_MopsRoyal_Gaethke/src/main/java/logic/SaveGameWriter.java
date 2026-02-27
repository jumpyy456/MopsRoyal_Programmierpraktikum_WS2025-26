package logic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Schreibt den Spielstand in eine JSON-Datei gemäß Aufgabenstellung.
 * <p>
 * Erstellt ein JSON-Dokument mit allen Spieler-Boards, Punkteständen,
 * dem aktuellen Spieler und dem nächsten Plättchen im vorgegebenen Format.
 * </p>
 */
public final class SaveGameWriter {

    // --- Konstanten ---
    /** Save/Load: leeres Board-Feld */
    private static final int EMPTY_CELL_CODE = 990;

    /** Save/Load: kein nextTile (Deck leer) */
    private static final int NO_NEXT_TILE_CODE = 99;

    private SaveGameWriter() {}

    /**
     * Schreibt den Spielstand in eine Datei.
     * <p>
     * Erstellt eine JSON-Datei mit UTF-8 Encoding, die alle relevanten
     * Spielinformationen enthält und von {@link SaveGameReader} wieder
     * eingelesen werden kann.
     * </p>
     *
     * @param target Ziel-Datei für den Spielstand (nicht {@code null})
     * @param players Liste aller Spieler (nicht {@code null}, 2-4 Spieler)
     * @param turn aktueller Spieler (0-basiert, d.h. 0-3)
     * @param nextCard Code des nächsten Plättchens (2-stellig, 11-66) oder 99 wenn leer
     * @throws IOException wenn Schreibfehler auftritt
     */
    public static void write(File target, List<Player> players, int turn, int nextCard)
            throws IOException {
        String json = buildJson(players, turn, nextCard);
        Files.writeString(target.toPath(), json);
    }

    /**
     * Erstellt den JSON-String aus Spielerdaten.
     * <p>
     * Baut das JSON-Dokument gemäß Aufgabenstellung auf:
     * <ul>
     *   <li>players-Array mit allen Spieler-Boards (5×5 int-Matrix)</li>
     *   <li>turn: aktueller Spieler (0-3)</li>
     *   <li>nextCard: nächstes Plättchen (11-66 oder 99)</li>
     * </ul>
     * </p>
     *
     * @param players Liste aller Spieler
     * @param turn aktueller Spieler (0-basiert)
     * @param nextCard nächstes Plättchen
     * @return JSON-String
     */
    private static String buildJson(List<Player> players, int turn, int nextCard) {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("{\n");
        sb.append("  \"players\": [\n");

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);

            int[][] board = p.getBoard().toSaveFormat();  //  Gibt int[][] zurück

            // Nach Normalisierung zur Bounding-Box liegt es nicht zwingend bei Array[0][0].
            int initialCode = EMPTY_CELL_CODE;  // Default: leer
            Tile startTile = p.getStartTile();
            if (startTile != null) {
                initialCode = startTile.getColor() * 100 + startTile.getObject() * 10;
            }

            sb.append("    {\n");
            sb.append("      \"nr\": ").append(i).append(",\n");
            sb.append("      \"name\": \"").append(escapeJson(p.getName())).append("\",\n");
            sb.append("      \"initial\": ").append(initialCode).append(",\n");
            sb.append("      \"points\": ").append(p.getScore()).append(",\n");
            sb.append("      \"cards\": ").append(buildCardsMatrixJson(board)).append('\n');
            sb.append("    }");
            if (i < players.size() - 1) sb.append(',');
            sb.append('\n');
        }

        sb.append("  ],\n");
        sb.append("  \"turn\": ").append(turn).append(",\n");
        sb.append("  \"nextCard\": ").append(nextCard).append('\n');
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Konvertiert ein 5×5 Board in JSON-Array-Format.
     * <p>
     * Erstellt ein verschachteltes Array: {@code [[110,120,...],[130,140,...],...]}.
     * Jede Zelle enthält entweder einen 3-stelligen Tile-Code oder 990 (leer).
     * </p>
     *
     * @param board 5×5 int-Array mit Tile-Codes
     * @return JSON-String der Matrix
     */
    private static String buildCardsMatrixJson(int[][] board) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("[\n");

        for (int r = 0; r < 5; r++) {
            sb.append("      [");
            for (int c = 0; c < 5; c++) {
                sb.append(board[r][c]);
                if (c < 4) sb.append(',');
            }
            sb.append(']');
            if (r < 4) sb.append(',');
            sb.append('\n');
        }

        sb.append("      ]");
        return sb.toString();
    }

    /**
     * Escaped Sonderzeichen für gültiges JSON.
     * <p>
     * Konvertiert Zeichen wie {@code "}, {@code \}, Zeilenumbrüche etc.
     * in ihre JSON-Escape-Sequenzen ({@code \"}, {@code \\}, {@code \n}).
     * </p>
     *
     * @param s zu escapender String (kann {@code null} sein)
     * @return escaped String (leer wenn {@code null})
     */
    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b");  break;
                case '\f': out.append("\\f");  break;
                case '\n': out.append("\\n");  break;
                case '\r': out.append("\\r");  break;
                case '\t': out.append("\\t");  break;
                default:
                    if (ch < 0x20) {
                        out.append(String.format("\\u%04x", (int) ch));
                    } else {
                        out.append(ch);
                    }
            }
        }
        return out.toString();
    }

    /**
     * Validiert einen nextCard-Code (2-stellig).
     * <p>
     * Format: Farbe*10 + Objekt (z.B. 33 = Orange Napf)
     * <br>Leer: 99
     * </p>
     *
     * @param code zu prüfender Code
     * @param fieldName Name des Feldes (für Fehlermeldung)
     * @throws IllegalArgumentException wenn Code ungültig
     */
    static void validateNextCardCode(int code, String fieldName) {
        if (code == NO_NEXT_TILE_CODE) return;  // Leer ist OK (kein Tile mehr)

        int color = code / 10;   // 1. Stelle = Farbe
        int object = code % 10;  // 2. Stelle = Objekt

        if (color < 1 || color > 6 || object < 1 || object > 6) {
            throw new IllegalArgumentException(
                    "Ungültiger " + fieldName + " Code: " + code +
                            " (erwartet: 2-stellig, Farbe 1-6, Objekt 1-6)"
            );
        }
    }
}