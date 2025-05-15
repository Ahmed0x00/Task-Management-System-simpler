package taskmanagement.controller;

import taskmanagement.ViewLoaderUtil;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class AdminController {

    @FXML
    private StackPane mainContent;

    @FXML
    public void initialize() {
        // No need to load AdminDashboard.fxml; it's already loaded by AuthController
        System.out.println("AdminController initialized");
    }

    public void loadManageUsersView() {
        ViewLoaderUtil.loadView(mainContent, "/ManageUsers.fxml");
    }

    public void loadManageEmployeesView() {
        ViewLoaderUtil.loadView(mainContent, "/ManageEmployees.fxml");
    }

    public void loadManageEmployeeTypesView() {
        ViewLoaderUtil.loadView(mainContent, "/ManageEmployeeTypes.fxml");
    }

    public void loadManageProjectsView() {
        ViewLoaderUtil.loadView(mainContent, "/ManageProjects.fxml");
    }

    public void loadManageCustomersView() {
        ViewLoaderUtil.loadView(mainContent, "/ManageCustomers.fxml");
    }

    public void loadManageTasksView() {
        ViewLoaderUtil.loadView(mainContent, "/ManageTasks.fxml");
    }

    public void loadManageTasksPhasesView() {
        ViewLoaderUtil.loadView(mainContent, "/ManageTaskPhases.fxml");
    }
}