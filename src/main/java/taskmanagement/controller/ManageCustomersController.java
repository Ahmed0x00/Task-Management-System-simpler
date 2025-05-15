package taskmanagement.controller;

import taskmanagement.model.Customer;
import taskmanagement.JsonUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import java.util.List;
import java.util.UUID;

public class ManageCustomersController {

    @FXML
    private GridPane customerGrid;

    private static final int COLUMNS = 4;

    @FXML
    public void initialize() {
        loadCustomers();
    }

    private void loadCustomers() {
        customerGrid.getChildren().clear();
        List<Customer> customers = JsonUtil.getAllCustomers();

        int col = 0;
        int row = 0;

        for (Customer customer : customers) {
            VBox card = createCustomerCard(customer);
            customerGrid.add(card, col, row);
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createCustomerCard(Customer customer) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-background-color: #f2f2f2;");
        card.setPrefWidth(200);

        Label nameLabel = new Label("Name: " + customer.getName());
        Label phoneLabel = new Label("Phone: " + (customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "None"));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> showCustomerDialog(customer, true));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            JsonUtil.deleteCustomerById(customer.getId());
            loadCustomers();
        });

        card.getChildren().addAll(nameLabel, phoneLabel, editBtn, deleteBtn);
        return card;
    }

    @FXML
    public void onAddCustomer() {
        showCustomerDialog(new Customer(UUID.randomUUID().toString(), "", null), false);
    }

    private void showCustomerDialog(Customer customer, boolean editing) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editing ? "Edit Customer" : "Add Customer");

        // Form layout
        VBox content = new VBox(10);
        TextField nameField = new TextField(customer.getName());
        TextField phoneField = new TextField(customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "");

        content.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Phone Number:"), phoneField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String name = nameField.getText().trim();
                String phoneNumber = phoneField.getText().trim();

                if (name.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Name is required.");
                    alert.showAndWait();
                    return null;
                }

                customer.setName(name);
                customer.setPhoneNumber(phoneNumber.isEmpty() ? null : phoneNumber);
                return customer;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (editing) {
                JsonUtil.updateCustomer(result);
            } else {
                JsonUtil.addCustomer(result);
            }
            loadCustomers();
        });
    }
}