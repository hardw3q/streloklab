module com.pixels.steloklab {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.pixels.steloklab to javafx.fxml;
    opens com.pixels.steloklab.controller to javafx.fxml;
    exports com.pixels.steloklab;
}