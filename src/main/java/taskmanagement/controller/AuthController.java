package taskmanagement.controller;

import taskmanagement.model.User;
import taskmanagement.model.User.Role;
import taskmanagement.JsonUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        User user = JsonUtil.findByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            JsonUtil.setLoggedInUser(user);

            try {
                Role role = user.getRole();
                String view = switch (role) {
                    case ADMIN -> "/AdminDashboard.fxml";
                    case LEADER, EMPLOYEE -> "/EmployeeDashboard.fxml";
                };

                java.net.URL resourceUrl = getClass().getResource(view);
                if (resourceUrl == null) {
                    throw new IOException("Resource not found: " + view);
                }
                Parent root = FXMLLoader.load(resourceUrl);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("UI error. Check logs.");
            }

        } else {
            errorLabel.setText("Invalid username or password.");
        }
    }
}