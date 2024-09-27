import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class EditClientContract {

    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService;

    public EditClientContract() {
        alertService = new AlertServiceImpl();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            return connectionToDataBase.getConnection();
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    public void handleEditContract(int contractId) {
        try {
            Stage editStage = new Stage();
            VBox editLayout = new VBox(10);
            editLayout.setPadding(new Insets(15, 10, 15, 10));
            editLayout.setStyle("-fx-background-color: black; -fx-text-fill: white;");
            editLayout.setAlignment(Pos.CENTER);

            Label selectFieldLabel = new Label("Select field to edit:");
            selectFieldLabel.setTextFill(Color.WHITE);
            selectFieldLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

            ComboBox<String> fieldComboBox = new ComboBox<>();
            fieldComboBox.getItems().addAll("Deadline", "Delivery Method", "Payment Method");
            fieldComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

            TextField newValueField = new TextField();
            newValueField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
            newValueField.setPromptText("New value");
            Contract selectedContract = getContractById(contractId);

            Button confirmButton = ButtonStyle.expandPaneStyledButton("Confirm");
            confirmButton.setOnAction(e -> {
                String selectedField = fieldComboBox.getValue();
                if (selectedField != null) {
                    try {
                        if (selectedField.equals("Deadline")) {
                            showDeadlineInputDialog(editStage, contractId);
                        } else if (selectedField.equals("Delivery Method")) {
                            showDeliveryMethodSelectionWindow(editStage, selectedContract);
                        } else if (selectedField.equals("Payment Method")) {
                            showPaymentMethodSelectionWindow(editStage, selectedContract);
                        } else {
                            editContractField(contractId, selectedField, newValueField.getText());
                        }
                        editStage.close();
                        alertService.showSuccessAlert("Contract field successfully updated.");
                    } catch (Exception ex) {
                        alertService.showErrorAlert("Error editing contract: " + ex.getMessage());
                    }
                } else {
                    alertService.showErrorAlert("Please select a field to edit.");
                }
            });

            editLayout.getChildren().addAll(selectFieldLabel, fieldComboBox, confirmButton);
            editLayout.setSpacing(15);

            Scene editScene = new Scene(editLayout, 400, 400);
            editScene.setFill(Color.BLACK);
            editStage.setScene(editScene);
            editStage.setTitle("Edit Contract");
            editStage.show();
        } catch (Exception ex) {
            alertService.showErrorAlert("Error establishing database connection: " + ex.getMessage());
        }
    }

    public void showDeadlineInputDialog(Stage editStage, int contractId) {
        Stage deadlineInputDialog = new Stage();
        deadlineInputDialog.initModality(Modality.APPLICATION_MODAL);
        deadlineInputDialog.initOwner(editStage);
        deadlineInputDialog.setTitle("Set Deadline");

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setStyle("-fx-background-color: black;");

        DatePicker datePicker = new DatePicker();
        Button confirmButton = ButtonStyle.expandPaneStyledButton("Confirm");

        datePicker.setStyle("-fx-background-color: black; -fx-text-fill: black; -fx-border-color: white; -fx-background-radius: 15px;");
        confirmButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-background-radius: 15px;");

        confirmButton.setOnAction(event -> {
            LocalDate selectedDate = datePicker.getValue();
            LocalDate currentDate = LocalDate.now();
            if (selectedDate == null) {
                alertService.showErrorAlert("Please select a date.");
            } else if (selectedDate.isBefore(currentDate) || selectedDate.isEqual(currentDate)) {
                alertService.showErrorAlert("Please select a date in the future.");
            } else {
                String formattedDate = selectedDate.toString();
                if (!formattedDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    alertService.showErrorAlert("Invalid date format.");
                } else {
                    try {
                        Connection connection = establishDBConnection();
                        String updateSql = "UPDATE contracts SET deadline = ? WHERE id_contracts = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                            updateStatement.setString(1, formattedDate);
                            updateStatement.setInt(2, contractId);
                            updateStatement.executeUpdate();
                        }
                        connection.close();
                        logContractEdit(contractId, "Deadline", formattedDate);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        alertService.showErrorAlert("Failed to update deadline.");
                    }
                    deadlineInputDialog.close();
                }
            }
        });

        vBox.getChildren().addAll(datePicker, confirmButton);

        Scene scene = new Scene(vBox, 300, 200);
        scene.setFill(Color.BLACK);
        deadlineInputDialog.setScene(scene);
        deadlineInputDialog.showAndWait();
    }

    private void showDeliveryMethodSelectionWindow(Stage parentStage, Contract selectedContract) {
        ComboBox<String> deliveryMethodComboBox = new ComboBox<>();
        deliveryMethodComboBox.getItems().addAll("Courier delivery", "Mail delivery", "Self pick-up");
        deliveryMethodComboBox.setPromptText("Select Delivery Method");
        deliveryMethodComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        Button confirmButton = ButtonStyle.expandPaneStyledButton("Confirm");
        confirmButton.setOnAction(e -> {
            String selectedDeliveryMethod = deliveryMethodComboBox.getValue();
            if (selectedDeliveryMethod != null) {
                try {
                    editContractField(selectedContract.getId(), "Delivery Method", selectedDeliveryMethod);
                    logContractEdit(selectedContract.getId(), "Delivery Method", selectedDeliveryMethod);
                    alertService.showSuccessAlert("Delivery Method successfully updated.");
                    parentStage.close();
                } catch (Exception ex) {
                    alertService.showErrorAlert("Error updating Delivery Method: " + ex.getMessage());
                }
            } else {
                alertService.showErrorAlert("Please select a Delivery Method.");
            }
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(deliveryMethodComboBox, confirmButton);
        layout.setSpacing(15);

        Scene scene = new Scene(layout, 400, 200);
        scene.setFill(Color.BLACK);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Select Delivery Method");
        stage.initOwner(parentStage);
        stage.show();
    }

    private void showPaymentMethodSelectionWindow(Stage parentStage, Contract selectedContract) {
        ComboBox<String> paymentMethodComboBox = new ComboBox<>();
        paymentMethodComboBox.getItems().addAll("Cash", "Credit card", "Bank transfer");
        paymentMethodComboBox.setPromptText("Select Payment Method");
        paymentMethodComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        Button confirmButton = ButtonStyle.expandPaneStyledButton("Confirm");
        confirmButton.setOnAction(e -> {
            String selectedPaymentMethod = paymentMethodComboBox.getValue();
            if (selectedPaymentMethod != null) {
                try {
                    editContractField(selectedContract.getId(), "Payment Method", selectedPaymentMethod);
                    logContractEdit(selectedContract.getId(), "Payment Method", selectedPaymentMethod);
                    alertService.showSuccessAlert("Payment Method successfully updated.");
                    parentStage.close();
                } catch (Exception ex) {
                    alertService.showErrorAlert("Error updating Payment Method: " + ex.getMessage());
                }
            } else {
                alertService.showErrorAlert("Please select a Payment Method.");
            }
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(paymentMethodComboBox, confirmButton);
        layout.setSpacing(15);

        Scene scene = new Scene(layout, 400, 200);
        scene.setFill(Color.BLACK);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Select Payment Method");
        stage.initOwner(parentStage);
        stage.show();
    }

    private void logContractEdit(int contractId, String field, String newValue) {
        try {
            SessionManager sessionManager = SessionManager.getInstance();
            String currentClientName = sessionManager.getCurrentClientName();
            int userId = sessionManager.getClientIdByName(currentClientName);
            String userIp = sessionManager.getIpAddress();
            String deviceType = sessionManager.getDeviceType();
            String actionType = "Edit";
            String objectType = "Contract";
            String details = "Contract ID: " + contractId + ", Field: " + field + ", New Value: " + newValue;

            Connection connection = establishDBConnection();
            String logSql = "INSERT INTO activity_log (user_id, action_type, object_type, details, timestamp, user_ip, user_device_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement logStatement = connection.prepareStatement(logSql)) {
                logStatement.setInt(1, userId);
                logStatement.setString(2, actionType);
                logStatement.setString(3, objectType);
                logStatement.setString(4, details);
                logStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                logStatement.setString(6, userIp);
                logStatement.setString(7, deviceType);
                logStatement.executeUpdate();
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to log activity: " + e.getMessage());
        }
    }

    private void editContractField(int contractId, String selectedField, String newValue) {
        try {
            Connection connection = establishDBConnection();

            String updateSql = "";
            switch (selectedField) {
                case "Additional Products":
                    updateSql = "UPDATE contracts SET additional_products = ? WHERE id_contracts = ?";
                    break;
                case "Delivery Method":
                    updateSql = "UPDATE contracts SET delivery_method = ? WHERE id_contracts = ?";
                    break;
                case "Product Count":
                    updateSql = "UPDATE contracts SET product_count = ? WHERE id_contracts = ?";
                    break;
                case "Payment Method":
                    updateSql = "UPDATE contracts SET pay_method = ? WHERE id_contracts = ?";
                    break;
                default:
                    alertService.showErrorAlert("Invalid field selected for editing.");
                    return;
            }

            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setString(1, newValue);
                updateStatement.setInt(2, contractId);
                updateStatement.executeUpdate();
            }

            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Contract getContractById(int contractId) throws SQLException {
        Connection connection = establishDBConnection();
        String query = "SELECT * FROM contracts WHERE id_contracts = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, contractId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Contract(
                            resultSet.getInt("id_contracts"),
                            resultSet.getString("client_id"),
                            resultSet.getString("manager_id"),
                            resultSet.getInt("product_count"),
                            resultSet.getString("status"),
                            resultSet.getString("delivery_method"),
                            resultSet.getString("pay_method"),
                            null,
                            resultSet.getString("additional_products"),
                            resultSet.getString("deadline"),
                            resultSet.getDouble("total_amount")
                    );
                }
            }
        }
        return null;
    }
}
