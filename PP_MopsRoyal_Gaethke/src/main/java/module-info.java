module PP.MopsRoyal.Gaethke {
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires com.google.gson;

    exports gui;

    opens gui to javafx.fxml;
    opens logic to com.google.gson;
}