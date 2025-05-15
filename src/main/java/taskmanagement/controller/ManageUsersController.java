package taskmanagement.controller;

import taskmanagement.model.Employee;
import taskmanagement.model.User;
import taskmanagement.JsonUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;

import java.util.List;
import java.util.UUID;

public class ManageUsersController {

    @FXML
    private GridPane userGrid;

    private static final int COLUMNS = 4;

    @FXML
    public void initialize() {
        loadUsers();
    }

    private void loadUsers() {
        userGrid.getChildren().clear();
        List<User> users = JsonUtil.getAllUsers();

        int col = 0;
        int row = 0;

        for (User user : users) {
            VBox card = createUserCard(user);
            userGrid.add(card, col, row);
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-background-color: #f2f2f2;");
        card.setPrefWidth(200);

        Label usernameLabel = new Label("Username: " + user.getUsername());
        Label roleLabel = new Label("Role: " + user.getRole());
        Label employeeLabel = new Label("Employee: " + (user.getEmployeeId() != null ? user.getEmployeeId() : "None"));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> showUserDialog(user, true));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            JsonUtil.deleteUserById(user.getId());
            loadUsers();
        });

        card.getChildren().addAll(usernameLabel, roleLabel, employeeLabel, editBtn, deleteBtn);
        return card;
    }

    @FXML
    public void onAddUser() {
        showUserDialog(new User(UUID.randomUUID().toString(), "", "", User.Role.EMPLOYEE, null), false);
    }

    private void showUserDialog(User user, boolean editing) {
        Dialog<User> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editing ? "Edit User" : "Add User");

        // Form layout
        VBox content = new VBox(10);
        TextField usernameField = new TextField(user.getUsername());
        PasswordField passwordField = new PasswordField();
        if (editing) passwordField.setPromptText("Leave blank to keep current");

        ComboBox<User.Role> roleCombo = new ComboBox<>();
        roleCombo.getItems().setAll(User.Role.values());
        roleCombo.setValue(user.getRole());

        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.getItems().add(null); // Allow no employee
        List<Employee> employees = JsonUtil.getAllEmployees();
        for (Employee emp : employees) {
            employeeCombo.getItems().add(emp.getId());
        }
        employeeCombo.setValue(user.getEmployeeId());

        content.getChildren().addAll(
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                new Label("Role:"), roleCombo
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Username is required.");
                    alert.showAndWait();
                    return null;
                }
                user.setUsername(username);
                if (!passwordField.getText().isEmpty()) {
                    user.setPassword(passwordField.getText());
                }
                user.setRole(roleCombo.getValue());
                user.setEmployeeId(employeeCombo.getValue());
                return user;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (editing) {
                JsonUtil.updateUser(result);
            } else {
                JsonUtil.addUser(result);
            }
            loadUsers();
        });
    }
}