package logic;

/**
 * Aufzählung aller möglichen Informationstypen für die Benutzeranzeige.
 * <p>
 * Die GUI übersetzt diese Informationstypen in lokalisierte, nutzerfreundliche
 * Meldungen. So bleibt die Logik sprachunabhängig.
 * </p>
 *
 * @see GUIConnector#showInfo(InfoType)
 */
public enum InfoType {

    // --- Datei-Operationen ---
    /** Spielstand wurde erfolgreich geladen. */
    GAME_LOADED,

    /** Spielstand wurde erfolgreich gespeichert. */
    GAME_SAVED
}
