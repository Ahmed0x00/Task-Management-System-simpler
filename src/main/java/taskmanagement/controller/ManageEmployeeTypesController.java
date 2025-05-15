package taskmanagement.controller;

import taskmanagement.model.EmployeeType;
import taskmanagement.JsonUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;

import java.util.List;

public class ManageEmployeeTypesController {

    @FXML
    private GridPane employeeTypeGrid;

    private static final int COLUMNS = 4;

    @FXML
    public void initialize() {
        loadEmployeeTypes();
    }

    private void loadEmployeeTypes() {
        employeeTypeGrid.getChildren().clear();
        List<EmployeeType> employeeTypes = JsonUtil.getAllEmployeeTypes();

        int col = 0;
        int row = 0;

        for (EmployeeType employeeType : employeeTypes) {
            VBox card = createEmployeeTypeCard(employeeType);
            employeeTypeGrid.add(card, col, row);
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createEmployeeTypeCard(EmployeeType employeeType) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-background-color: #f2f2f2;");
        card.setPrefWidth(200);

        Label titleLabel = new Label("Title: " + employeeType.getTitle());
        Label idLabel = new Label("ID: " + employeeType.getId());

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> showEmployeeTypeDialog(employeeType, true));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            JsonUtil.deleteEmployeeTypeById(employeeType.getId());
            loadEmployeeTypes();
        });

        card.getChildren().addAll(idLabel, titleLabel, editBtn, deleteBtn);
        return card;
    }

    @FXML
    public void onAddEmployeeType() {
        showEmployeeTypeDialog(new EmployeeType(0, ""), false);
    }

    private void showEmployeeTypeDialog(EmployeeType employeeType, boolean editing) {
        Dialog<EmployeeType> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editing ? "Edit Employee Type" : "Add Employee Type");

        // Form layout
        VBox content = new VBox(10);
        TextField titleField = new TextField(employeeType.getTitle());

        content.getChildren().addAll(
                new Label("Title:"), titleField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String title = titleField.getText().trim();
                if (title.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Title is required.");
                    alert.showAndWait();
                    return null;
                }

                employeeType.setTitle(title);
                return employeeType;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (editing) {
                JsonUtil.updateEmployeeType(result);
            } else {
                JsonUtil.addEmployeeType(result);
            }
            loadEmployeeTypes();
        });
    }
}