package taskmanagement.controller;

import taskmanagement.model.Employee;
import taskmanagement.model.EmployeeType;
import taskmanagement.model.User;
import taskmanagement.JsonUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.util.StringConverter;

import java.util.List;
import java.util.UUID;

public class ManageEmployeesController {

    @FXML
    private GridPane employeeGrid;

    private static final int COLUMNS = 4;

    @FXML
    public void initialize() {
        loadEmployees();
    }

    private void loadEmployees() {
        employeeGrid.getChildren().clear();
        List<Employee> employees = JsonUtil.getAllEmployees();

        int col = 0;
        int row = 0;

        for (Employee employee : employees) {
            VBox card = createEmployeeCard(employee);
            employeeGrid.add(card, col, row);
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createEmployeeCard(Employee employee) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-background-color: #f2f2f2;");
        card.setPrefWidth(200);

        Label nameLabel = new Label("Name: " + employee.getName());
        Label emailLabel = new Label("Email: " + employee.getEmail());
        Label phoneLabel = new Label("Phone: " + employee.getPhone());
        EmployeeType type = JsonUtil.getEmployeeTypeById(employee.getEmployeeTypeId());
        Label typeLabel = new Label("Type: " + (type != null ? type.getTitle() : "None"));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> showEmployeeDialog(employee, true));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            JsonUtil.deleteEmployeeById(employee.getId());
            loadEmployees();
        });

        card.getChildren().addAll(nameLabel, emailLabel, phoneLabel, typeLabel, editBtn, deleteBtn);
        return card;
    }

    @FXML
    public void onAddEmployee() {
        // Show user creation dialog first
        Dialog<User> userDialog = new Dialog<>();
        userDialog.initModality(Modality.APPLICATION_MODAL);
        userDialog.setTitle("Create Employee User");

        // User form layout
        VBox userContent = new VBox(10);
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField roleField = new TextField("EMPLOYEE");
        roleField.setEditable(false);

        userContent.getChildren().addAll(
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                new Label("Role:"), roleField
        );

        userDialog.getDialogPane().setContent(userContent);
        userDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        userDialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Username and Password are required.");
                    alert.showAndWait();
                    return null;
                }

                // Check for duplicate username
                if (JsonUtil.getAllUsers().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username))) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Username already exists.");
                    alert.showAndWait();
                    return null;
                }

                String employeeId = UUID.randomUUID().toString();
                User user = new User(
                        UUID.randomUUID().toString(),
                        username,
                        password,
                        User.Role.EMPLOYEE,
                        employeeId
                );
                return user;
            }
            return null;
        });

        userDialog.showAndWait().ifPresent(user -> {
            // Save user to users.json
            JsonUtil.addUser(user);
            // Show employee creation dialog with pre-filled name
            showEmployeeDialog(new Employee(user.getEmployeeId(), user.getUsername(), "", "", 0), false);
        });
    }

    private void showEmployeeDialog(Employee employee, boolean editing) {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editing ? "Edit Employee" : "Add Employee");

        // Form layout
        VBox content = new VBox(10);
        TextField nameField = new TextField(employee.getName());
        if (!editing) {
            nameField.setEditable(false); // Name is username from user creation, non-editable
        }
        TextField emailField = new TextField(employee.getEmail());
        TextField phoneField = new TextField(employee.getPhone());

        // ComboBox for EmployeeType
        ComboBox<EmployeeType> typeCombo = new ComboBox<>();
        typeCombo.getItems().add(null); // Allow "None" option
        typeCombo.getItems().addAll(JsonUtil.getAllEmployeeTypes());
        typeCombo.setConverter(new StringConverter<EmployeeType>() {
            @Override
            public String toString(EmployeeType type) {
                return type != null ? type.getTitle() : "None";
            }

            @Override
            public EmployeeType fromString(String string) {
                return null; // Not needed for selection
            }
        });
        // Set initial value
        typeCombo.setValue(employee.getEmployeeTypeId() != 0 ? JsonUtil.getEmployeeTypeById(employee.getEmployeeTypeId()) : null);

        content.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Email:"), emailField,
                new Label("Phone:"), phoneField,
                new Label("Employee Type:"), typeCombo
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                EmployeeType selectedType = typeCombo.getValue();

                if (name.isEmpty() || email.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Name and Email are required.");
                    alert.showAndWait();
                    return null;
                }

                employee.setName(name);
                employee.setEmail(email);
                employee.setPhone(phone);
                employee.setEmployeeTypeId(selectedType != null ? selectedType.getId() : 0);
                return employee;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (editing) {
                JsonUtil.updateEmployee(result);
            } else {
                JsonUtil.addEmployee(result);
            }
            loadEmployees();
        });
    }
}