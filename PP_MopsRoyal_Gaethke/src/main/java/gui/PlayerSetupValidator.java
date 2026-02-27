package gui;


import java.util.*;

/**
 * Fachlicher Validator für die Spielereingabe (Anzahl, Namen, Farbzuordnung).
 */
public final class PlayerSetupValidator {
    /**
     * Prüft, dass 2 bis 4 Spieler teilnehmen.
     *
     * @param takesPart vier Flags (Index 0..3 → Spieler 1..4)
     * @throws IllegalArgumentException wenn die Anzahl < 2 oder > 4 ist
     */
    public static void validateCount(boolean[] takesPart) {
        int n = 0;
        for (boolean b : takesPart) {
            if (b) n++;
        }
        if (n < 2 || n > 4) {
            throw new IllegalArgumentException("Es müssen zwischen 2 und 4 Spieler teilnehmen (aktuell: " + n + ")");
        }
    }


    /**
     * Prüft, dass alle teilnehmenden Namen nicht leer und eindeutig sind.
     * Eindeutigkeit ist CASE-INSENSITIVE
     * @param names     Liste der vier Namensfelder
     * @param takesPart Teilnahme-Flags
     * @throws IllegalArgumentException bei leerem/duplizierten Namen
     */
    public static void validateNames(List<String> names, boolean[] takesPart) {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < names.size(); i++) {
            // nur aktive Spieler prüfen
            if (!takesPart[i]) continue;
            String raw = names.get(i);
            if (raw == null || raw.trim().isEmpty()) {
                throw new IllegalArgumentException("Spieler " + (i + 1) + ": Name darf nicht leer sein.");
            }
            String key = raw.trim().toLowerCase(Locale.ROOT); // case-insensitive
            if (!seen.add(key)) {
                throw new IllegalArgumentException("Namen müssen eindeutig sein: '" + raw + "'.");
            }
        }
    }
}
