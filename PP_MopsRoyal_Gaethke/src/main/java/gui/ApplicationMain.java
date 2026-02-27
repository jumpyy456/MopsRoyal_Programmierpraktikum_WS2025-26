package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.Game;

import java.io.IOException;

/**
 * Startet die Mops Royal Anwendung.
 *
 * @author mjo
 */
public class ApplicationMain extends Application {

    /**
     * Erstellt und zeigt das Hauptfenster.
     * <p>
     * Reihenfolge der Initialisierung:
     * <ol>
     *   <li>FXML laden → UserInterfaceController wird erstellt + initialize() aufgerufen</li>
     *   <li>UserInterfaceController.initialize() erstellt JavaFXGUI und Game</li>
     *   <li>Fenster-Handler registrieren</li>
     *   <li>Fenster anzeigen</li>
     * </ol>
     * </p>
     * <p>
     * WICHTIG: Die Erstellung von JavaFXGUI und Game erfolgt in
     * UserInterfaceController.initialize(), nicht hier. Dies ermöglicht
     * eine saubere Trennung von GUI und Logik.
     * </p>
     *
     * @param stage das Hauptfenster
     * @throws IOException wenn FXML nicht geladen werden kann
     */
    @Override
    public void start(Stage stage) throws Exception {
        // 1. FXML laden (triggert UserInterfaceController.initialize())
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/UserInterface.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1450, 710);

        // 2. Controller holen
        UserInterfaceController ui = fxmlLoader.getController();

        // 3. Fenster-Handler
        stage.setOnCloseRequest(event -> {
            event.consume();
            ui.handleWindowClose();
        });

        // 4. Fenster nicht resizable
        stage.setResizable(false);

        // 5. Fenster anzeigen
        stage.setTitle("Mops Royal");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Main-Methode zum Starten der Anwendung.
     *
     * @param args Kommandozeilenargumente (nicht verwendet)
     */
    public static void main(String... args) {
        launch(args);
    }
}