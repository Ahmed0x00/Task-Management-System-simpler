package taskmanagement.controller;

import taskmanagement.model.Project;
import taskmanagement.JsonUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

public class ManageProjectsController {

    @FXML
    private GridPane projectGrid;

    private static final int COLUMNS = 4;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        loadProjects();
    }

    private void loadProjects() {
        projectGrid.getChildren().clear();
        List<Project> projects = JsonUtil.getAllProjects();

        int col = 0;
        int row = 0;

        for (Project project : projects) {
            VBox card = createProjectCard(project);
            projectGrid.add(card, col, row);
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createProjectCard(Project project) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-background-color: #f2f2f2;");
        card.setPrefWidth(200);

        Label nameLabel = new Label("Name: " + project.getName());
        Label descLabel = new Label("Description: " + (project.getDescription() != null ? project.getDescription() : "None"));
        Label startDateLabel = new Label("Start: " + (project.getStartDate() != null ? project.getStartDate() : "None"));
        Label endDateLabel = new Label("End: " + (project.getEndDate() != null ? project.getEndDate() : "None"));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> showProjectDialog(project, true));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            JsonUtil.deleteProjectById(project.getId());
            loadProjects();
        });

        card.getChildren().addAll(nameLabel, descLabel, startDateLabel, endDateLabel, editBtn, deleteBtn);
        return card;
    }

    @FXML
    public void onAddProject() {
        showProjectDialog(new Project(UUID.randomUUID().toString(), "", "", null, null), false);
    }

    private void showProjectDialog(Project project, boolean editing) {
        Dialog<Project> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editing ? "Edit Project" : "Add Project");

        // Form layout
        VBox content = new VBox(10);
        TextField nameField = new TextField(project.getName());
        TextArea descField = new TextArea(project.getDescription() != null ? project.getDescription() : "");
        descField.setPrefRowCount(3);
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        // Populate DatePickers if dates exist
        if (project.getStartDate() != null) {
            try {
                startDatePicker.setValue(LocalDate.parse(project.getStartDate(), DATE_FORMATTER));
            } catch (DateTimeParseException ignored) {}
        }
        if (project.getEndDate() != null) {
            try {
                endDatePicker.setValue(LocalDate.parse(project.getEndDate(), DATE_FORMATTER));
            } catch (DateTimeParseException ignored) {}
        }

        content.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Description:"), descField,
                new Label("Start Date:"), startDatePicker,
                new Label("End Date:"), endDatePicker
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String name = nameField.getText().trim();
                String description = descField.getText().trim();
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();

                if (name.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Name is required.");
                    alert.showAndWait();
                    return null;
                }

                if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "End Date cannot be before Start Date.");
                    alert.showAndWait();
                    return null;
                }

                project.setName(name);
                project.setDescription(description.isEmpty() ? null : description);
                project.setStartDate(startDate != null ? startDate.format(DATE_FORMATTER) : null);
                project.setEndDate(endDate != null ? endDate.format(DATE_FORMATTER) : null);
                return project;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (editing) {
                JsonUtil.updateProject(result);
            } else {
                JsonUtil.addProject(result);
            }
            loadProjects();
        });
    }
}