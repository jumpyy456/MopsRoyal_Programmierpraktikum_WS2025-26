package gui;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller für den Spieler-Setup-Dialog (zugehörige FXML-Datei).
 * <p>
 * Aufgabe dieses Controllers ist ausschließlich das Einsammeln der Nutzereingaben
 * (Teilnahme-Flags und Namen) sowie das Setzen sinnvoller Defaults. Jegliche
 * fachliche Validierung (z. B. Mindestanzahl Spieler, eindeutige/ nicht-leere Namen)
 * findet außerhalb statt.
 * </p>
 */
public class PlayerSetupDialogController {

    // --- Konstanten ---

    /** Maximale Anzahl der Spieler im Spiel. */
    private static final int MAX_PLAYERS = 4;

    /** Präfix für Default-Spielernamen (z.B. "Spieler 1"). */
    private static final String DEFAULT_PLAYER_NAME_PREFIX = "Spieler ";

    // --- FXML-gebundene Steuerelemente ---

    /** CheckBoxen für die Teilnahme der Spieler 1-4. */
    @FXML private CheckBox cb1, cb2, cb3, cb4;

    /** TextFields für die Namen der Spieler 1-4. */
    @FXML private TextField tf1, tf2, tf3, tf4;

    /**
     * Setzt initial sinnvolle Defaults für den Dialog.
     * <p>
     * Voreinstellung:
     * <ul>
     *   <li>Spieler 1 und 2 sind immer aktiv und können nicht deaktiviert werden</li>
     *   <li>Spieler 3 ist optional</li>
     *   <li>Spieler 4 ist nur aktivierbar, wenn Spieler 3 mitspielt</li>
     *   <li>Textfelder sind nur für teilnehmende Spieler aktiviert</li>
     * </ul>
     * </p>
     * <p>
     * Wird automatisch vom FXMLLoader nach dem Laden der FXML aufgerufen.
     * </p>
     */
    @FXML
    private void initialize() {
        // Spieler 1 und 2 immer als mitspielend festgelegt
        cb1.setSelected(true);
        cb1.setDisable(true); // Kann nicht abgewählt werden

        cb2.setSelected(true);
        cb2.setDisable(true);

        // Spieler 4 nur wenn Spieler 3 mitspielt
        cb4.setDisable(true); // Initial deaktiviert

        // Spieler 3 CheckBox überwacht
        cb3.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Wenn Spieler 3 abgewählt wird, auch Spieler 4 abwählen
            if (!newValue) {
                cb4.setSelected(false);
            }
            // Spieler 4 CheckBox aktivieren/deaktivieren
            cb4.setDisable(!newValue);

            // TextFeld-Status aktualisieren
            updateTextFieldStates();
        });

        // Listener für alle CheckBoxen: TextField-Status aktualisieren
        cb1.selectedProperty().addListener((obs, old, val) -> updateTextFieldStates());
        cb2.selectedProperty().addListener((obs, old, val) -> updateTextFieldStates());
        cb4.selectedProperty().addListener((obs, old, val) -> updateTextFieldStates());

        // Initial TextFields aktualisieren
        updateTextFieldStates();

        // Default-Namen als Platzhalter setzen
        setDefaultPlaceholders();
    }

    /**
     * Aktualisiert den Enabled/Disabled-Status aller Textfelder.
     * <p>
     * Ein TextField ist nur aktiviert, wenn die zugehörige CheckBox ausgewählt ist.
     * </p>
     */
    private void updateTextFieldStates() {
        // TextField nur aktiviert wenn CheckBox ausgewählt
        tf1.setDisable(!cb1.isSelected());
        tf2.setDisable(!cb2.isSelected());
        tf3.setDisable(!cb3.isSelected());
        tf4.setDisable(!cb4.isSelected());
    }

    /**
     * Setzt Default-Platzhalter für alle Namensfelder.
     * <p>
     * Diese werden angezeigt, wenn der Benutzer nichts eingibt.
     * </p>
     */
    private void setDefaultPlaceholders() {
        // Default-Spielernamen als Platzhalter
        tf1.setPromptText(DEFAULT_PLAYER_NAME_PREFIX + "1");
        tf2.setPromptText(DEFAULT_PLAYER_NAME_PREFIX + "2");
        tf3.setPromptText(DEFAULT_PLAYER_NAME_PREFIX + "3");
        tf4.setPromptText(DEFAULT_PLAYER_NAME_PREFIX + "4");
    }

    /**
     * Liefert die Teilnahme-Flags in fester Reihenfolge (Spieler 1..4).
     *
     * @return Array der Länge {@value MAX_PLAYERS} mit den Checkbox-Zuständen:
     *         Index 0 → Spieler 1, Index 1 → Spieler 2, Index 2 → Spieler 3, Index 3 → Spieler 4
     */
    public boolean[] getTakesPart() {
        return new boolean[]{cb1.isSelected(), cb2.isSelected(), cb3.isSelected(), cb4.isSelected()};
    }

    /**
     * Liefert die in die Namensfelder eingetragenen Texte in fester Reihenfolge (Spieler 1..4).
     * <p>
     * Wenn ein Feld leer ist, wird der Default-Name verwendet.
     * </p>
     *
     * @return Namen der Spieler (1..{@value MAX_PLAYERS}); leere Felder werden durch "Spieler N" ersetzt
     */
    public List<String> getNames() {
        List<String> names = new ArrayList<>(MAX_PLAYERS);

        // Default-Namen verwenden wenn leer
        names.add(getNameOrDefault(tf1, 1));
        names.add(getNameOrDefault(tf2, 2));
        names.add(getNameOrDefault(tf3, 3));
        names.add(getNameOrDefault(tf4, 4));

        return names;
    }

    /**
     * Hilfsmethode: Gibt den eingegebenen Namen zurück oder einen Default-Namen.
     *
     * @param textField das TextField mit dem Namen
     * @param playerNumber Spielernummer (1-{@value MAX_PLAYERS})
     * @return eingegebener Name oder "Spieler N"
     */
    private String getNameOrDefault(TextField textField, int playerNumber) {
        String text = textField.getText().trim();
        return text.isEmpty() ? DEFAULT_PLAYER_NAME_PREFIX + playerNumber : text;
    }
}