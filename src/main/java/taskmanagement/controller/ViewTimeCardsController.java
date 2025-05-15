package taskmanagement.controller;

import taskmanagement.model.TimeCard;
import taskmanagement.model.User;
import taskmanagement.JsonUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ViewTimeCardsController {

    @FXML
    private TableView<TimeCard> timeCardTable;

    @FXML
    private TableColumn<TimeCard, String> employeeNameColumn;

    @FXML
    private TableColumn<TimeCard, String> dateColumn;

    @FXML
    private TableColumn<TimeCard, String> clockInColumn;

    @FXML
    private TableColumn<TimeCard, String> clockOutColumn;

    @FXML
    private TableColumn<TimeCard, Double> hoursWorkedColumn;

    @FXML
    private VBox submissionForm;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField clockInField;

    @FXML
    private TextField clockOutField;

    @FXML
    private TextField hoursWorkedField;

    @FXML
    private Button submitButton;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FULL_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        User loggedInUser = JsonUtil.getLoggedInUser();
        System.out.println("ViewTimecardsController.initialize: Initializing for user: " +
                (loggedInUser != null ? loggedInUser.getUsername() : "null"));

        if (loggedInUser == null) {
            showError("No logged-in user found.");
            return;
        }

        // Initialize table columns
        employeeNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(JsonUtil.getEmployeeName(cellData.getValue().getEmployeeId())));
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        clockInColumn.setCellValueFactory(cellData -> {
            String fullTimestamp = cellData.getValue().getClockInTime();
            if (fullTimestamp != null) {
                try {
                    LocalTime time = LocalTime.parse(fullTimestamp, FULL_TIMESTAMP_FORMATTER);
                    return new SimpleStringProperty(time.format(TIME_FORMATTER));
                } catch (DateTimeParseException e) {
                    System.out.println("ViewTimecardsController: Invalid clockInTime format: " + fullTimestamp);
                }
            }
            return new SimpleStringProperty("");
        });
        clockOutColumn.setCellValueFactory(cellData -> {
            String fullTimestamp = cellData.getValue().getClockOutTime();
            if (fullTimestamp != null) {
                try {
                    LocalTime time = LocalTime.parse(fullTimestamp, FULL_TIMESTAMP_FORMATTER);
                    return new SimpleStringProperty(time.format(TIME_FORMATTER));
                } catch (DateTimeParseException e) {
                    System.out.println("ViewTimecardsController: Invalid clockOutTime format: " + fullTimestamp);
                }
            }
            return new SimpleStringProperty("");
        });
        hoursWorkedColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getHoursWorked()));

        // Load time cards based on role
        if (loggedInUser.getRole() == User.Role.LEADER) {
            submissionForm.setVisible(false);
            loadAllTimeCards();
        } else if (loggedInUser.getRole() == User.Role.EMPLOYEE) {
            submissionForm.setVisible(true);
            submitButton.setDisable(false);
            loadEmployeeTimeCards(loggedInUser.getEmployeeId());
        } else {
            showError("Access denied for role: " + loggedInUser.getRole());
            timeCardTable.setDisable(true);
            submissionForm.setVisible(false);
        }
    }

    private void loadAllTimeCards() {
        timeCardTable.getItems().clear();
        timeCardTable.getItems().addAll(FXCollections.observableArrayList(JsonUtil.getAllTimeCards()));
    }

    private void loadEmployeeTimeCards(String employeeId) {
        timeCardTable.getItems().clear();
        timeCardTable.getItems().addAll(FXCollections.observableArrayList(JsonUtil.getTimeCardsByEmployee(employeeId)));
    }

    @FXML
    private void handleSubmitTimeCard() {
        User loggedInUser = JsonUtil.getLoggedInUser();
        if (loggedInUser.getRole() != User.Role.EMPLOYEE) {
            showError("Only employees can submit time cards.");
            return;
        }

        LocalDate date = datePicker.getValue();
        String clockInInput = clockInField.getText().trim();
        String clockOutInput = clockOutField.getText().trim();
        String hoursWorkedText = hoursWorkedField.getText().trim();
        Double hoursWorked = hoursWorkedText.isEmpty() ? null : Double.parseDouble(hoursWorkedText);

        if (date == null || clockInInput.isEmpty() || clockOutInput.isEmpty()) {
            showError("Date, clock-in time, and clock-out time are required.");
            return;
        }

        try {
            // Parse time inputs as HH:mm
            LocalTime.parse(clockInInput, TIME_FORMATTER);
            LocalTime.parse(clockOutInput, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            showError("Invalid time format. Use HH:mm (e.g., 09:43).");
            return;
        }

        // Combine date and time into full timestamp
        String clockInTime = date.format(DATE_FORMATTER) + " " + clockInInput + ":00";
        String clockOutTime = date.format(DATE_FORMATTER) + " " + clockOutInput + ":00";

        TimeCard timeCard = new TimeCard(
                0, // ID will be set by TimeCardUtil
                loggedInUser.getEmployeeId(),
                clockInTime,
                clockOutTime,
                date.format(DATE_FORMATTER),
                hoursWorked
        );

        if (JsonUtil.addTimeCard(timeCard)) {
            new Alert(Alert.AlertType.INFORMATION, "Time card submitted successfully.").showAndWait();
            loadEmployeeTimeCards(loggedInUser.getEmployeeId());
            clearForm();
        } else {
            showError("Failed to submit time card.");
        }
    }

    private void clearForm() {
        datePicker.setValue(null);
        clockInField.clear();
        clockOutField.clear();
        hoursWorkedField.clear();
    }

    private void showError(String message) {
        System.out.println("ViewTimecardsController: Error: " + message);
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}