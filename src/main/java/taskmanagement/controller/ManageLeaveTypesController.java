package taskmanagement.controller;

import taskmanagement.model.LeaveType;
import taskmanagement.JsonUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;

import java.util.List;
import java.util.UUID;

public class ManageLeaveTypesController {

    @FXML
    private GridPane leaveTypeGrid;

    @FXML
    private Button addLeaveTypeButton;

    private static final int COLUMNS = 4;

    @FXML
    public void initialize() {
        loadLeaveTypes();
    }

    private void loadLeaveTypes() {
        System.out.println("ManageLeaveTypesController.loadLeaveTypes: Loading leave types");
        leaveTypeGrid.getChildren().clear();
        List<LeaveType> leaveTypes = JsonUtil.getAllLeaveTypes();
        System.out.println("ManageLeaveTypesController.loadLeaveTypes: Loaded " + leaveTypes.size() + " leave types");

        int col = 0;
        int row = 0;

        for (LeaveType leaveType : leaveTypes) {
            VBox card = createLeaveTypeCard(leaveType);
            leaveTypeGrid.add(card, col, row);
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createLeaveTypeCard(LeaveType leaveType) {
        System.out.println("ManageLeaveTypesController.createLeaveTypeCard: Creating card for leave type: " + leaveType.getTitle());
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-background-color: #f2f2f2;");
        card.setPrefWidth(200);

        Label titleLabel = new Label("Title: " + leaveType.getTitle());
        Label descLabel = new Label("Description: " + (leaveType.getDescription() != null ? leaveType.getDescription() : "None"));

        VBox buttons = new VBox(5);
        // Edit and Delete buttons available to all users
        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> showLeaveTypeDialog(leaveType, true));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            System.out.println("ManageLeaveTypesController.deleteBtn: Deleting leave type: " + leaveType.getTitle());
            JsonUtil.deleteLeaveTypeById(leaveType.getId());
            loadLeaveTypes();
        });

        buttons.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(titleLabel, descLabel, buttons);
        return card;
    }

    @FXML
    public void onAddLeaveType() {
        System.out.println("ManageLeaveTypesController.onAddLeaveType: Opening add leave type dialog");
        showLeaveTypeDialog(new LeaveType(UUID.randomUUID().toString(), "", ""), false);
    }

    private void showLeaveTypeDialog(LeaveType leaveType, boolean editing) {
        System.out.println("ManageLeaveTypesController.showLeaveTypeDialog: Opening dialog for leave type: " + leaveType.getTitle() + ", editing: " + editing);
        Dialog<LeaveType> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editing ? "Edit Leave Type" : "Add Leave Type");

        // Form layout
        VBox content = new VBox(10);
        TextField titleField = new TextField(leaveType.getTitle());
        TextArea descField = new TextArea(leaveType.getDescription() != null ? leaveType.getDescription() : "");
        descField.setPrefRowCount(3);

        content.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Description:"), descField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String title = titleField.getText().trim();
                String description = descField.getText().trim();

                if (title.isEmpty()) {
                    System.out.println("ManageLeaveTypesController.showLeaveTypeDialog: Validation failed - Title is empty");
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Title is required.");
                    alert.showAndWait();
                    return null;
                }

                leaveType.setTitle(title);
                leaveType.setDescription(description.isEmpty() ? null : description);
                System.out.println("ManageLeaveTypesController.showLeaveTypeDialog: Leave type updated in dialog: " + leaveType.getTitle());
                return leaveType;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            System.out.println("ManageLeaveTypesController.showLeaveTypeDialog: Dialog result - Leave Type: " + result.getTitle());
            if (editing) {
                JsonUtil.updateLeaveType(result);
                System.out.println("ManageLeaveTypesController.showLeaveTypeDialog: Leave type updated: " + result.getTitle());
            } else {
                JsonUtil.addLeaveType(result);
                System.out.println("ManageLeaveTypesController.showLeaveTypeDialog: Leave type added: " + result.getTitle());
            }
            loadLeaveTypes();
        });
    }
}