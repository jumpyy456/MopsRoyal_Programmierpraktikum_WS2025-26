package logic;

/**
 * Aufzählung aller möglichen Fehlertypen für die Benutzeranzeige.
 * <p>
 * Die GUI übersetzt diese Fehlertypen in lokalisierte, nutzerfreundliche
 * Fehlermeldungen. So bleibt die Logik sprachunabhängig.
 * </p>
 *
 * @see GUIConnector#showUserError(ErrorType)
 */
public enum ErrorType {

    // --- Spielzug-Fehler ---
    /** Spieler hat auf eine ungültige (nicht markierte) Position geklickt. */
    INVALID_POSITION,

    /** Ungültiges Plättchen zum Umdrehen ausgewählt. */
    INVALID_SELECTION,

    // --- Platzierungs-Fehler ---
    /** Allgemeiner Fehler beim Platzieren eines Plättchens. */
    TILE_PLACEMENT_FAILED,

    // --- Datei-Fehler ---
    /** Fehler beim Laden eines Spielstands. */
    LOAD_FAILED,

    /** Fehler beim Speichern eines Spielstands. */
    SAVE_FAILED,

    /** Ungültige/korrupte Spielstandsdatei. */
    INVALID_SAVE_FILE,

    // --- Spiel-Zustand-Fehler ---
    /** Kein Spiel gestartet (z.B. beim Speichern ohne aktives Spiel). */
    NO_GAME_STARTED,

    /** Aktion während Wertungsphase nicht erlaubt. */
    ACTION_DURING_SCORING,

    /** Dialog konnte nicht geladen werden. */
    DIALOG_LOAD_FAILED
}
