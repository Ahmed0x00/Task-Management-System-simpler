package taskmanagement.controller;

import taskmanagement.model.LeaveType;
import taskmanagement.model.Request;
import taskmanagement.model.User;
import taskmanagement.JsonUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ViewRequestsController {

    @FXML
    private TableView<Request> requestTable;

    @FXML
    private TableColumn<Request, String> employeeNameColumn;

    @FXML
    private TableColumn<Request, String> typeColumn;

    @FXML
    private TableColumn<Request, String> startDateColumn;

    @FXML
    private TableColumn<Request, String> endDateColumn;

    @FXML
    private TableColumn<Request, String> statusColumn;

    @FXML
    private TableColumn<Request, String> reasonColumn;

    @FXML
    private TableColumn<Request, Void> actionColumn;

    @FXML
    private VBox submissionForm;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ComboBox<LeaveType> leaveTypeComboBox;

    @FXML
    private HBox leaveTypeBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField reasonField;

    @FXML
    private Button submitButton;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        User loggedInUser = JsonUtil.getLoggedInUser();
        System.out.println("ViewRequestsController.initialize: Initializing for user: " +
                (loggedInUser != null ? loggedInUser.getUsername() : "null"));

        if (loggedInUser == null) {
            showError("No logged-in user found.");
            return;
        }

        // Initialize table columns
        employeeNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(JsonUtil.getEmployeeName(cellData.getValue().getEmployeeId())));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDisplayType()));
        startDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStartDate()));
        endDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEndDate()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));
        reasonColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReason()));

        // Action column for leaders (approve/deny buttons)
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button approveButton = new Button("Approve");
            private final Button denyButton = new Button("Deny");

            {
                approveButton.setOnAction(event -> {
                    Request request = getTableView().getItems().get(getIndex());
                    request.setStatus(Request.Status.APPROVED);
                    if (JsonUtil.updateRequest(request)) {
                        new Alert(Alert.AlertType.INFORMATION, "Request approved.").showAndWait();
                        loadRequests();
                    } else {
                        showError("Failed to approve request.");
                    }
                });
                denyButton.setOnAction(event -> {
                    Request request = getTableView().getItems().get(getIndex());
                    request.setStatus(Request.Status.DENIED);
                    if (JsonUtil.updateRequest(request)) {
                        new Alert(Alert.AlertType.INFORMATION, "Request denied.").showAndWait();
                        loadRequests();
                    } else {
                        showError("Failed to deny request.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Request request = getTableRow().getItem();
                    if (loggedInUser.getRole() == User.Role.LEADER && request.getStatus() == Request.Status.PENDING) {
                        HBox buttons = new HBox(5, approveButton, denyButton);
                        setGraphic(buttons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        // Initialize submission form
        typeComboBox.getItems().addAll("MISSION", "PERMISSION", "LEAVE");
        leaveTypeComboBox.setItems(FXCollections.observableArrayList(JsonUtil.getAllLeaveTypes()));
        leaveTypeComboBox.setCellFactory(lv -> new ListCell<LeaveType>() {
            @Override
            protected void updateItem(LeaveType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitle());
            }
        });
        leaveTypeComboBox.setButtonCell(new ListCell<LeaveType>() {
            @Override
            protected void updateItem(LeaveType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitle());
            }
        });
        leaveTypeBox.setVisible(false);

        // Show/hide leaveTypeComboBox based on type selection
        typeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            leaveTypeBox.setVisible("LEAVE".equals(newValue));
        });

        // Load requests based on role
        if (loggedInUser.getRole() == User.Role.LEADER) {
            submissionForm.setVisible(false);
            loadAllRequests();
        } else if (loggedInUser.getRole() == User.Role.EMPLOYEE) {
            submissionForm.setVisible(true);
            submitButton.setDisable(false);
            loadEmployeeRequests(loggedInUser.getEmployeeId());
        } else {
            showError("Access denied for role: " + loggedInUser.getRole());
            requestTable.setDisable(true);
            submissionForm.setVisible(false);
        }
    }

    private void loadAllRequests() {
        requestTable.getItems().clear();
        requestTable.getItems().addAll(FXCollections.observableArrayList(JsonUtil.getAllRequests()));
    }

    private void loadEmployeeRequests(String employeeId) {
        requestTable.getItems().clear();
        requestTable.getItems().addAll(FXCollections.observableArrayList(JsonUtil.getRequestsByEmployee(employeeId)));
    }

    private void loadRequests() {
        User loggedInUser = JsonUtil.getLoggedInUser();
        if (loggedInUser.getRole() == User.Role.LEADER) {
            loadAllRequests();
        } else {
            loadEmployeeRequests(loggedInUser.getEmployeeId());
        }
    }

    @FXML
    private void handleSubmitRequest() {
        User loggedInUser = JsonUtil.getLoggedInUser();
        if (loggedInUser.getRole() != User.Role.EMPLOYEE) {
            showError("Only employees can submit requests.");
            return;
        }

        String type = typeComboBox.getValue();
        LeaveType leaveType = leaveTypeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String reason = reasonField.getText().trim();

        if (type == null || startDate == null || endDate == null || reason.isEmpty()) {
            showError("Type, start date, end date, and reason are required.");
            return;
        }
        if ("LEAVE".equals(type) && leaveType == null) {
            showError("Leave type is required for LEAVE requests.");
            return;
        }

        Request request = new Request(
                0, // ID will be set by RequestUtil
                loggedInUser.getEmployeeId(),
                type,
                leaveType != null ? leaveType.getId() : null,
                startDate.format(DATE_FORMATTER),
                endDate.format(DATE_FORMATTER),
                Request.Status.PENDING,
                reason
        );

        if (JsonUtil.addRequest(request)) {
            new Alert(Alert.AlertType.INFORMATION, "Request submitted successfully.").showAndWait();
            loadEmployeeRequests(loggedInUser.getEmployeeId());
            clearForm();
        } else {
            showError("Failed to submit request.");
        }
    }

    private void clearForm() {
        typeComboBox.setValue(null);
        leaveTypeComboBox.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        reasonField.clear();
        leaveTypeBox.setVisible(false);
    }

    private void showError(String message) {
        System.out.println("ViewRequestsController: Error: " + message);
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}