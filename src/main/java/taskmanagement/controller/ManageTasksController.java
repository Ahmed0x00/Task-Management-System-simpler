package taskmanagement.controller;

import taskmanagement.model.Task;
import taskmanagement.model.Task.Priority;
import taskmanagement.model.Employee;
import taskmanagement.model.Project;
import taskmanagement.model.TaskLog;
import taskmanagement.model.TaskPhase;
import taskmanagement.JsonUtil;
import taskmanagement.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ManageTasksController {

    @FXML
    private GridPane taskGrid;

    @FXML
    private Button addTaskButton;

    private static final int COLUMNS = 4;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private User loggedInUser;

    @FXML
    public void initialize() {
        loggedInUser = JsonUtil.getLoggedInUser();
        if (loggedInUser == null || loggedInUser.getRole() == User.Role.EMPLOYEE) {
            addTaskButton.setVisible(false);
            addTaskButton.setManaged(false);
        }
        loadTasks();
    }

    private void loadTasks() {
        taskGrid.getChildren().clear();
        List<Task> tasks;
        if (loggedInUser.getRole() == User.Role.EMPLOYEE) {
            String employeeId = loggedInUser.getEmployeeId();
            if (employeeId == null) {
                return; // No tasks if no employeeId
            }
            tasks = JsonUtil.getTasksByEmployeeId(employeeId);
        } else {
            tasks = JsonUtil.getAllTasks();
        }

        int col = 0;
        int row = 0;

        for (Task task : tasks) {
            VBox card = createTaskCard(task);
            taskGrid.add(card, col, row);
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createTaskCard(Task task) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-background-color: #f2f2f2;");
        card.setPrefWidth(200);

        Label codeLabel = new Label("Code: " + (task.getCode() != null ? task.getCode() : "None"));
        Label titleLabel = new Label("Title: " + task.getTitle());
        Label descLabel = new Label("Description: " + (task.getDescription() != null ? task.getDescription() : "None"));

        // Fetch assigned employee name
        String assignedName;
        if (task.getAssignedEmployeeId() != null) {
            Employee employee = JsonUtil.getEmployeeById(task.getAssignedEmployeeId());
            assignedName = (employee != null && employee.getName() != null) ? employee.getName() : "Unknown";
        } else {
            assignedName = "None";
        }
        Label employeeLabel = new Label("Assigned: " + assignedName);

        // Fetch project title
        String projectTitle = "None";
        if (task.getProjectId() != null) {
            Project project = JsonUtil.getProjectById(task.getProjectId());
            projectTitle = (project != null && project.getName() != null) ? project.getName() : "Unknown";
        }
        Label projectLabel = new Label("Project: " + projectTitle);

        // Fetch phase name
        String phaseName = "None";
        if (task.getPhase() != null) {
            TaskPhase phase = JsonUtil.getTaskPhaseById(task.getPhase());
            phaseName = (phase != null && phase.getName() != null) ? phase.getName() : "Unknown";
        }
        Label phaseLabel = new Label("Phase: " + phaseName);

        Label priorityLabel = new Label("Priority: " + task.getPriority());

        // Fetch creator username
        String creatorUsername = "None";
        if (task.getCreatorUserId() != null) {
            User creator = JsonUtil.getUserById(task.getCreatorUserId());
            creatorUsername = (creator != null && creator.getUsername() != null) ? creator.getUsername() : "Unknown";
        }
        Label creatorLabel = new Label("Creator: " + creatorUsername);

        Label startDateLabel = new Label("Start: " + (task.getStartDate() != null ? task.getStartDate() : "None"));
        Label endDateLabel = new Label("End: " + (task.getEndDate() != null ? task.getEndDate() : "None"));
        Label hoursLabel = new Label("Hours: " + (task.getEstimatedHours() != null ? task.getEstimatedHours() : "None"));

        // Add buttons only if the task is not in the "Completed" phase
        VBox buttons = new VBox(5);
        if (!"Completed".equalsIgnoreCase(phaseName)) {
            if (loggedInUser.getRole() == User.Role.EMPLOYEE && task.getAssignedEmployeeId() != null && task.getAssignedEmployeeId().equals(loggedInUser.getEmployeeId())) {
                TaskPhase phase = task.getPhase() != null ? JsonUtil.getTaskPhaseById(task.getPhase()) : null;
                boolean isUnderWork = phase != null && phase.getName().equalsIgnoreCase("Under Work");
                Button actionButton = new Button(isUnderWork ? "Finish" : "Start");
                actionButton.setOnAction(e -> {
                    if (isUnderWork) {
                        // Finish task: set phase to COMPLETED and update task log
                        TaskPhase completedPhase = JsonUtil.findTaskPhaseByName("Completed");
                        if (completedPhase != null) {
                            task.setPhase(completedPhase.getId());
                            JsonUtil.updateTask(task);
                            // Update task log
                            TaskLog log = JsonUtil.getByTaskIdAndEmployeeId(task.getId(), loggedInUser.getEmployeeId());
                            if (log != null) {
                                LocalDateTime endTime = LocalDateTime.now();
                                log.setEndedAt(endTime.format(DATETIME_FORMATTER));
                                long seconds = java.time.Duration.between(
                                        LocalDateTime.parse(log.getStartedAt(), DATETIME_FORMATTER),
                                        endTime
                                ).getSeconds();
                                long hours = seconds / 3600;
                                long minutes = (seconds % 3600) / 60;
                                log.setTimeSpent(hours + " hours, " + minutes + " minutes");
                                JsonUtil.updateTaskLog(log);
                                System.out.println("TaskLog updated: " + log);
                            } else {
                                System.out.println("No TaskLog found for task: " + task.getId());
                            }
                        } else {
                            System.out.println("Completed phase not found!");
                        }
                    } else {
                        // Start task: set phase to UNDER_WORK and add task log
                        TaskPhase underWorkPhase = JsonUtil.findTaskPhaseByName("Under Work");
                        if (underWorkPhase != null) {
                            task.setPhase(underWorkPhase.getId());
                            JsonUtil.updateTask(task);
                            // Create task log
                            TaskLog log = new TaskLog(
                                UUID.randomUUID().toString(), // Unique ID for the log
                                task.getTitle(), // Task name
                                assignedName, // Employee name
                                LocalDateTime.now().format(DATETIME_FORMATTER), // Started at
                                null, // Ended at (not yet finished)
                                null // Time spent (not yet finished)
                            );
                            JsonUtil.addTaskLog(log);
                            System.out.println("TaskLog created: " + log);
                        } else {
                            System.out.println("Under Work phase not found!");
                        }
                    }
                    loadTasks();
                });
                buttons.getChildren().add(actionButton);
            } else if (loggedInUser.getRole() != User.Role.EMPLOYEE) {
                Button editBtn = new Button("Edit");
                editBtn.setOnAction(e -> showTaskDialog(task, true));

                Button deleteBtn = new Button("Delete");
                deleteBtn.setOnAction(e -> {
                    JsonUtil.deleteTaskById(task.getId());
                    loadTasks();
                });

                buttons.getChildren().addAll(editBtn, deleteBtn);
            }
        }

        card.getChildren().addAll(codeLabel, titleLabel, descLabel, employeeLabel, projectLabel,
                phaseLabel, priorityLabel, creatorLabel, startDateLabel, endDateLabel,
                hoursLabel);

        if (!buttons.getChildren().isEmpty()) {
            card.getChildren().add(buttons);
        }

        return card;
    }

    @FXML
    public void onAddTask() {
        TaskPhase defaultPhase = JsonUtil.findTaskPhaseByName("Pending");
        String defaultPhaseId = defaultPhase != null ? defaultPhase.getId() : null;
        showTaskDialog(new Task(UUID.randomUUID().toString(), "", "", null, null, null,
                defaultPhaseId, Priority.MEDIUM, loggedInUser.getId(), null, null, null), false);
    }

    private void showTaskDialog(Task task, boolean editing) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editing ? "Edit Task" : "Add Task");

        // Form layout
        VBox content = new VBox(10);
        TextField codeField = new TextField(task.getCode());
        TextField titleField = new TextField(task.getTitle());
        TextArea descField = new TextArea(task.getDescription() != null ? task.getDescription() : "");
        descField.setPrefRowCount(3);

        // Employee ComboBox (from employees.json)
        ComboBox<Employee> employeeCombo = new ComboBox<>();
        employeeCombo.getItems().add(null); // Allow unassigned
        List<Employee> employees = JsonUtil.getAllEmployees();
        employeeCombo.getItems().addAll(employees);
        employeeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Employee emp, boolean empty) {
                super.updateItem(emp, empty);
                setText(empty || emp == null ? "None" : emp.getName());
            }
        });
        employeeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Employee emp, boolean empty) {
                super.updateItem(emp, empty);
                setText(empty || emp == null ? "None" : emp.getName());
            }
        });
        // Set current employee
        if (task.getAssignedEmployeeId() != null) {
            employees.stream()
                    .filter(emp -> task.getAssignedEmployeeId().equals(emp.getId()))
                    .findFirst()
                    .ifPresent(employeeCombo::setValue);
        }

        // Project ComboBox
        ComboBox<Project> projectCombo = new ComboBox<>();
        projectCombo.getItems().add(null); // Allow no project
        List<Project> projects = JsonUtil.getAllProjects();
        projectCombo.getItems().addAll(projects);
        projectCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Project proj, boolean empty) {
                super.updateItem(proj, empty);
                setText(empty || proj == null ? "None" : proj.getName());
            }
        });
        projectCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Project proj, boolean empty) {
                super.updateItem(proj, empty);
                setText(empty || proj == null ? "None" : proj.getName());
            }
        });
        // Set current project
        if (task.getProjectId() != null) {
            projects.stream()
                    .filter(proj -> task.getProjectId().equals(proj.getId()))
                    .findFirst()
                    .ifPresent(projectCombo::setValue);
        }

        // Phase ComboBox (from task_phases.json)
        ComboBox<TaskPhase> phaseCombo = new ComboBox<>();
        List<TaskPhase> phases = JsonUtil.getAllTaskPhases();
        phaseCombo.getItems().addAll(phases);
        phaseCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TaskPhase phase, boolean empty) {
                super.updateItem(phase, empty);
                setText(empty || phase == null ? "None" : phase.getName());
            }
        });
        phaseCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TaskPhase phase, boolean empty) {
                super.updateItem(phase, empty);
                setText(empty || phase == null ? "None" : phase.getName());
            }
        });
        // Set current phase
        if (task.getPhase() != null) {
            phases.stream()
                    .filter(phase -> task.getPhase().equals(phase.getId()))
                    .findFirst()
                    .ifPresent(phaseCombo::setValue);
        }

        // Priority ComboBox
        ComboBox<Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(Priority.values());
        priorityCombo.setValue(task.getPriority());

        // Creator (non-editable, set to logged-in user)
        TextField creatorField = new TextField(task.getCreatorUserId());
        creatorField.setEditable(false);

        // Dates
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        if (task.getStartDate() != null) {
            try {
                startDatePicker.setValue(LocalDate.parse(task.getStartDate(), DATE_FORMATTER));
            } catch (java.time.format.DateTimeParseException ignored) {}
        }
        if (task.getEndDate() != null) {
            try {
                endDatePicker.setValue(LocalDate.parse(task.getEndDate(), DATE_FORMATTER));
            } catch (java.time.format.DateTimeParseException ignored) {}
        }

        // Estimated Hours
        TextField hoursField = new TextField(task.getEstimatedHours() != null ? task.getEstimatedHours().toString() : "");

        content.getChildren().addAll(
                new Label("Code:"), codeField,
                new Label("Title:"), titleField,
                new Label("Description:"), descField,
                new Label("Assigned Employee:"), employeeCombo,
                new Label("Project:"), projectCombo,
                new Label("Phase:"), phaseCombo,
                new Label("Priority:"), priorityCombo,
                new Label("Creator User ID:"), creatorField,
                new Label("Start Date:"), startDatePicker,
                new Label("End Date:"), endDatePicker,
                new Label("Estimated Hours:"), hoursField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String code = codeField.getText().trim();
                String title = titleField.getText().trim();
                String description = descField.getText().trim();
                String employeeId = employeeCombo.getValue() != null ? employeeCombo.getValue().getId() : null;
                String projectId = projectCombo.getValue() != null ? projectCombo.getValue().getId() : null;
                String phaseId = phaseCombo.getValue() != null ? phaseCombo.getValue().getId() : null;
                Priority priority = priorityCombo.getValue();
                String creatorId = creatorField.getText();
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                String hoursText = hoursField.getText().trim();
                Double estimatedHours = null;

                if (title.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Title is required.");
                    alert.showAndWait();
                    return null;
                }

                if (creatorId.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Creator User ID is required.");
                    alert.showAndWait();
                    return null;
                }

                if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "End Date cannot be before Start Date.");
                    alert.showAndWait();
                    return null;
                }

                if (!hoursText.isEmpty()) {
                    try {
                        estimatedHours = Double.parseDouble(hoursText);
                        if (estimatedHours < 0) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Estimated Hours cannot be negative.");
                            alert.showAndWait();
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Estimated Hours must be a valid number.");
                        alert.showAndWait();
                        return null;
                    }
                }

                task.setCode(code.isEmpty() ? null : code);
                task.setTitle(title);
                task.setDescription(description.isEmpty() ? null : description);
                task.setAssignedEmployeeId(employeeId);
                task.setProjectId(projectId);
                task.setPhase(phaseId);
                task.setPriority(priority);
                task.setCreatorUserId(creatorId);
                task.setStartDate(startDate != null ? startDate.format(DATE_FORMATTER) : null);
                task.setEndDate(endDate != null ? endDate.format(DATE_FORMATTER) : null);
                task.setEstimatedHours(estimatedHours);
                return task;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (editing) {
                JsonUtil.updateTask(result);
            } else {
                JsonUtil.addTask(result);
            }
            loadTasks();
        });
    }
}