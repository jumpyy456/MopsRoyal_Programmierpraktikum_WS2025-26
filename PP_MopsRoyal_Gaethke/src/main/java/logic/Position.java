package logic;

/**
 * Unveränderliche Position auf dem Spielfeld (0-basiert).
 *
 * @param row Zeilenindex (0..n-1)
 * @param col Spaltenindex (0..m-1)
 */
public record Position(int row, int col) {}

