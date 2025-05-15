package taskmanagement.controller;

import taskmanagement.model.Task;
import taskmanagement.model.TaskPhase;
import taskmanagement.model.User;
import taskmanagement.JsonUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarController {

    @FXML
    private Label monthLabel;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Button prevMonthButton;

    @FXML
    private Button nextMonthButton;

    private YearMonth currentYearMonth;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    @FXML
    public void initialize() {
        User loggedInUser = JsonUtil.getLoggedInUser();
        if (loggedInUser == null) {
            showError("No logged-in user found.");
            return;
        }
        System.out.println("CalendarController.initialize: Initializing for user: " + loggedInUser.getUsername());

        // Initialize current month
        currentYearMonth = YearMonth.now();
        updateCalendar();

        // Set up navigation buttons
        prevMonthButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });
        nextMonthButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });
    }

    private void updateCalendar() {
        // Update month label
        monthLabel.setText(currentYearMonth.format(MONTH_FORMATTER));

        // Clear grid
        calendarGrid.getChildren().clear();
        calendarGrid.getRowConstraints().clear();
        calendarGrid.getColumnConstraints().clear();

        // Set up columns (Sun-Sat)
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            calendarGrid.getColumnConstraints().add(col);
        }

        // Add day-of-week headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-font-weight: bold;");
            calendarGrid.add(dayLabel, i, 0);
            GridPane.setHalignment(dayLabel, javafx.geometry.HPos.CENTER);
        }

        // Get tasks and phases
        User loggedInUser = JsonUtil.getLoggedInUser();
        List<Task> tasks;
        List<TaskPhase> phases;
        if (loggedInUser.getRole() == User.Role.EMPLOYEE) {
            tasks = JsonUtil.getTasksByEmployeeId(loggedInUser.getEmployeeId());
            // Get phases linked to employee's tasks
            Set<String> phaseIds = tasks.stream()
                    .map(Task::getPhase)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            phases = JsonUtil.getAllTaskPhases().stream()
                    .filter(phase -> phaseIds.contains(phase.getId()))
                    .toList();
        } else if (loggedInUser.getRole() == User.Role.LEADER) {
            tasks = JsonUtil.getAllTasks();
            phases = JsonUtil.getAllTaskPhases();
        } else {
            showError("Access denied for role: " + loggedInUser.getRole());
            return;
        }

        // Set up calendar days
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday=0
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int row = 1;
        int col = firstDayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox dayBox = new VBox(5);
            dayBox.setAlignment(Pos.TOP_CENTER);
            dayBox.setPadding(new Insets(5));
            dayBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");

            // Add day number
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            dayBox.getChildren().add(dayLabel);

            // Add tasks for this day
            for (Task task : tasks) {
                try {
                    LocalDate startDate = LocalDate.parse(task.getStartDate(), DATE_FORMATTER);
                    LocalDate endDate = LocalDate.parse(task.getEndDate(), DATE_FORMATTER);
                    if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                        String displayText = loggedInUser.getRole() == User.Role.LEADER
                                ? task.getTitle() + " (" + JsonUtil.getEmployeeName(task.getAssignedEmployeeId()) + ")"
                                : task.getTitle();
                        Label taskLabel = new Label(displayText);
                        taskLabel.setWrapText(true);
                        taskLabel.setMaxWidth(100);
                        taskLabel.setTextFill(Color.WHITE);
                        taskLabel.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
                        taskLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
                        taskLabel.setPadding(new Insets(2));
                        // Tooltip
                        Tooltip.install(taskLabel, new Tooltip(task.getTitle()));
                        // Click for details
                        taskLabel.setOnMouseClicked(e -> showTaskDetails(task));
                        dayBox.getChildren().add(taskLabel);
                    }
                } catch (Exception e) {
                    System.out.println("CalendarController: Error parsing task dates for task " + task.getId() + ": " + e.getMessage());
                }
            }

            // Add phases for this day
            for (TaskPhase phase : phases) {
                // Check if any task in this phase spans this day
                boolean phaseActive = tasks.stream()
                        .filter(t -> phase.getId().equals(t.getPhase()))
                        .anyMatch(t -> {
                            try {
                                LocalDate startDate = LocalDate.parse(t.getStartDate(), DATE_FORMATTER);
                                LocalDate endDate = LocalDate.parse(t.getEndDate(), DATE_FORMATTER);
                                return !date.isBefore(startDate) && !date.isAfter(endDate);
                            } catch (Exception e) {
                                return false;
                            }
                        });
                if (phaseActive) {
                    Label phaseLabel = new Label(phase.getName());
                    phaseLabel.setWrapText(true);
                    phaseLabel.setMaxWidth(100);
                    phaseLabel.setTextFill(Color.WHITE);
                    phaseLabel.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                    phaseLabel.setFont(Font.font("System", FontPosture.ITALIC, 10));
                    phaseLabel.setPadding(new Insets(2));
                    // Tooltip
                    Tooltip.install(phaseLabel, new Tooltip(phase.getName()));
                    // Click for details
                    phaseLabel.setOnMouseClicked(e -> showPhaseDetails(phase, tasks));
                    dayBox.getChildren().add(phaseLabel);
                }
            }

            // Add day to grid
            calendarGrid.add(dayBox, col, row);
            col++;
            if (col == 7) {
                col = 0;
                row++;
                RowConstraints rowConst = new RowConstraints();
                rowConst.setMinHeight(100);
                calendarGrid.getRowConstraints().add(rowConst);
            }
        }
    }

    private void showTaskDetails(Task task) {
        TaskPhase phase = JsonUtil.getTaskPhaseById(task.getPhase());
        String phaseName = phase != null ? phase.getName() : "None";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Task Details");
        alert.setHeaderText(task.getTitle());
        alert.setContentText(
                "Description: " + (task.getDescription() != null ? task.getDescription() : "N/A") + "\n" +
                        "Priority: " + task.getPriority() + "\n" +
                        "Estimated Hours: " + task.getEstimatedHours() + "\n" +
                        "Start Date: " + task.getStartDate() + "\n" +
                        "End Date: " + task.getEndDate() + "\n" +
                        "Phase: " + phaseName
        );
        alert.showAndWait();
    }

    private void showPhaseDetails(TaskPhase phase, List<Task> allTasks) {
        StringBuilder taskList = new StringBuilder();
        List<Task> phaseTasks = allTasks.stream()
                .filter(t -> phase.getId().equals(t.getPhase()))
                .toList();
        if (phaseTasks.isEmpty()) {
            taskList.append("No tasks assigned to this phase.");
        } else {
            taskList.append("Tasks:\n");
            for (Task task : phaseTasks) {
                taskList.append("- ").append(task.getTitle());
                if (JsonUtil.getLoggedInUser().getRole() == User.Role.LEADER) {
                    taskList.append(" (").append(JsonUtil.getEmployeeName(task.getAssignedEmployeeId())).append(")");
                }
                taskList.append("\n");
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Phase Details");
        alert.setHeaderText(phase.getName());
        alert.setContentText(taskList.toString());
        alert.showAndWait();
    }

    private void showError(String message) {
        System.out.println("CalendarController: Error: " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}