package gui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Hilfsfenster zur Anzeige von statischen Hilfebildern.
 */
public final class HelpWindow {

    /**
     * Öffnet ein nicht-resizables Fenster in der exakten Bildgröße.
     * Das Bild füllt den sichtbaren Bereich vollständig aus.
     *
     * @param resourcePath Pfad zur Bildressource
     * @param title         Fenstertitel
     */
    public static void showAtImageSize(String resourcePath, String title) {
        URL url = HelpWindow.class.getResource(resourcePath);
        if (url == null) {
            System.err.println("Help image not found: " + resourcePath);
            return;
        }

        // Bild synchron laden (false = nicht background)
        Image image = new Image(url.toExternalForm(), false);

        // ImageView zeigt das Bild , füllt gesamten StackPane Bereich
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setFitWidth(image.getWidth());
        view.setFitHeight(image.getHeight());

        // StackPane ohne Padding, Scene exakt in Bildgröße
        StackPane root = new StackPane(view);

        Scene scene = new Scene(root, image.getWidth(), image.getHeight());

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setResizable(false);   // Fenstergröße fix
        stage.setScene(scene);
        stage.sizeToScene();         // exakt an Bildgröße anpassen
        stage.show();
    }
}
