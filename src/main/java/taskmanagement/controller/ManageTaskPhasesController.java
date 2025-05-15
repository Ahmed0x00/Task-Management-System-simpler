package taskmanagement.controller;

import taskmanagement.model.TaskPhase;
import taskmanagement.JsonUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import java.util.List;
import java.util.UUID;

public class ManageTaskPhasesController {

    @FXML
    private GridPane taskPhaseGrid;

    private static final int COLUMNS = 4;

    @FXML
    public void initialize() {
        loadTaskPhases();
    }

    private void loadTaskPhases() {
        taskPhaseGrid.getChildren().clear();
        List<TaskPhase> taskPhases = JsonUtil.getAllTaskPhases();

        int col = 0;
        int row = 0;

        for (TaskPhase taskPhase : taskPhases) {
            VBox card = createTaskPhaseCard(taskPhase);
            taskPhaseGrid.add(card, col, row);
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createTaskPhaseCard(TaskPhase taskPhase) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-background-color: #f2f2f2;");
        card.setPrefWidth(200);

        Label nameLabel = new Label("Name: " + taskPhase.getName());

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> showTaskPhaseDialog(taskPhase, true));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            JsonUtil.deleteTaskPhaseById(taskPhase.getId());
            loadTaskPhases();
        });

        card.getChildren().addAll(nameLabel, editBtn, deleteBtn);
        return card;
    }

    @FXML
    public void onAddTaskPhase() {
        showTaskPhaseDialog(new TaskPhase(UUID.randomUUID().toString(), ""), false);
    }

    private void showTaskPhaseDialog(TaskPhase taskPhase, boolean editing) {
        Dialog<TaskPhase> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editing ? "Edit Task Phase" : "Add Task Phase");

        // Form layout
        VBox content = new VBox(10);
        TextField nameField = new TextField(taskPhase.getName());

        content.getChildren().addAll(
                new Label("Name:"), nameField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String name = nameField.getText().trim();

                if (name.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Name is required.");
                    alert.showAndWait();
                    return null;
                }

                taskPhase.setName(name);
                return taskPhase;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (editing) {
                JsonUtil.updateTaskPhase(result);
            } else {
                JsonUtil.addTaskPhase(result);
            }
            loadTaskPhases();
        });
    }
}