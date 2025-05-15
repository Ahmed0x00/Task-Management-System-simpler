package taskmanagement.controller;

import taskmanagement.ViewLoaderUtil;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class EmployeeController {

    @FXML
    private StackPane mainContent;

    @FXML
    public void initialize() {
        System.out.println("EmployeeController initialized");
    }

    public void loadManageTasksView() {
        ViewLoaderUtil.loadView(mainContent, "/ManageTasks.fxml");
    }

    public void loadManageTaskLogsView() {
        ViewLoaderUtil.loadView(mainContent, "/ManageTaskLogs.fxml");
    }

    public void loadManageLeaveTypesView() {
        ViewLoaderUtil.loadView(mainContent, "/ManageLeaveTypes.fxml");
    }

    public void loadViewRequests() {
        ViewLoaderUtil.loadView(mainContent, "/ViewRequests.fxml");
    }

    public void loadTimeCards() {
        ViewLoaderUtil.loadView(mainContent, "/ViewTimeCards.fxml");
    }

    public void loadCalendar() {
        ViewLoaderUtil.loadView(mainContent, "/Calendar.fxml");
    }
}