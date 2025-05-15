module com.example.taskmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.logging;

    opens taskmanagement to javafx.fxml, com.fasterxml.jackson.databind;
    opens taskmanagement.controller to javafx.fxml, com.fasterxml.jackson.databind;
    opens taskmanagement.model to javafx.fxml, com.fasterxml.jackson.databind;

    exports taskmanagement;

}