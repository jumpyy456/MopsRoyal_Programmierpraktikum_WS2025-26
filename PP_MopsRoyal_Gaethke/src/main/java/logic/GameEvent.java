package logic;

/**
 * Ereignisse während des Spielablaufs, die dem Benutzer angezeigt werden.
 * <p>
 * Diese Events werden über {@link GUIConnector#showGameEvent(GameEvent, Player)}
 * als Dialog angezeigt und informieren über besondere Spielsituationen.
 * </p>
 */
public enum GameEvent {
    /** Spieler muss aussetzen, weil er seine eigene Startkarte gezogen hat. */
    PLAYER_SKIP,

    /** Eine Nachholrunde für ausgesetzte Spieler beginnt. */
    CATCH_UP_ROUND_START,

    /** Spieler muss aussetzen, weil sein Board bereits voll ist (25 Plättchen). */
    PLAYER_MUST_SKIP_FULL_BOARD
}