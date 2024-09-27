import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class EditEmployeeContracts {

    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private String managerName;

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            return connectionToDataBase.getConnection();
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    public void showEditContractsWindow(int contractId) {
        Stage editContractsStage = new Stage();
        editContractsStage.setTitle("Edit Contracts");

        VBox editContractsForm = new VBox(10);
        editContractsForm.setPadding(new Insets(10));
        editContractsForm.setAlignment(Pos.CENTER);
        editContractsForm.setStyle("-fx-background-color: black;");

        ChoiceBox<String> statusChoiceBox = new ChoiceBox<>();
        statusChoiceBox.getItems().addAll("Under consideration", "In processing", "Transferred to delivery service",
                "Successfully completed", "Unsuccessfully completed");
        statusChoiceBox.setValue("Under consideration");
        statusChoiceBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        statusChoiceBox.setPrefWidth(200);

        Label selectStatusLabel = new Label("Select the new status:");
        selectStatusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        Button submitButton = ButtonStyle.expandPaneStyledButton("Submit");
        submitButton.setOnAction(event -> {
            try {
                String newStatus = statusChoiceBox.getValue();
                if (newStatus != null) {
                    editContractStatus(contractId, newStatus);
                    editContractsStage.close();
                }
            } catch (NumberFormatException e) {
                alertService.showErrorAlert("Invalid Contract ID. Please enter a valid integer.");
            }
        });

        editContractsForm.getChildren().addAll(selectStatusLabel, statusChoiceBox, submitButton);

        Scene editContractsScene = new Scene(editContractsForm, 400, 400);
        editContractsScene.setFill(Color.BLACK);
        editContractsStage.setScene(editContractsScene);
        editContractsStage.show();
    }

    public String showEditStatusDialog() {
        List<String> statusOptions = Arrays.asList(
                "Pending", "In Progress", "Sent to mail", "Completed Successfully", "Completed Unsuccessfully");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(statusOptions.get(0), statusOptions);
        dialog.setTitle("Edit Contract Status");
        dialog.setHeaderText(null);
        dialog.setContentText("Choose the new status:");

        return dialog.showAndWait().orElse(null);
    }

    public void editContractStatus(int contractId, String newStatus) {
        try {
            Connection connection = establishDBConnection();

            String updateStatusSql = "UPDATE contracts SET status = ? WHERE id_contracts = ?";
            PreparedStatement updateStatusStatement = connection.prepareStatement(updateStatusSql);
            updateStatusStatement.setString(1, newStatus);
            updateStatusStatement.setInt(2, contractId);
            int rowsAffected = updateStatusStatement.executeUpdate();

            if (rowsAffected > 0) {
                alertService.showSuccessAlert("Status successfully updated!");

                if (newStatus.equals("Successfully completed")) {
                    updateBookkeepingData(connection, contractId);
                }
            } else {
                alertService.showErrorAlert("Failed to update contract status. Contract with ID " + contractId + " not found.");
            }

            updateStatusStatement.close();
            connection.close();
        } catch (SQLException e) {
            alertService.showErrorAlert("Error updating contract status: " + e.getMessage());
        }
    }

    public void updateBookkeepingData(Connection connection, int contractId) {
        try {
            String contractInfoSql = "SELECT total_amount FROM contracts WHERE id_contracts = ?";
            try (PreparedStatement contractInfoStatement = connection.prepareStatement(contractInfoSql)) {
                contractInfoStatement.setInt(1, contractId);
                try (ResultSet contractInfoResultSet = contractInfoStatement.executeQuery()) {
                    if (contractInfoResultSet.next()) {
                        double contractTotalAmount = contractInfoResultSet.getDouble("total_amount");
                        String updatePriceSql = "UPDATE bookkeeping SET successful_contracts_price = successful_contracts_price + ? WHERE employee_name = ?";
                        try (PreparedStatement updatePriceStatement = connection.prepareStatement(updatePriceSql)) {
                            updatePriceStatement.setDouble(1, contractTotalAmount);
                            updatePriceStatement.setString(2, managerName);
                            updatePriceStatement.executeUpdate();
                        }
                        String updateContractsSql = "UPDATE bookkeeping SET successful_contracts = CONCAT(successful_contracts, ?, ',') WHERE employee_name = ?";
                        try (PreparedStatement updateContractsStatement = connection.prepareStatement(updateContractsSql)) {
                            updateContractsStatement.setString(1, contractId + ",");
                            updateContractsStatement.setString(2, managerName);
                            updateContractsStatement.executeUpdate();
                        }
                    } else {
                        alertService.showErrorAlert("Contract details not found for ID: " + contractId);
                    }
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error updating bookkeeping data: " + e.getMessage());
        }
    }

    public boolean showDeleteConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this contract?");

        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    public void deleteContract(int contractId) {
        try {
            Connection connection = establishDBConnection();

            String deleteContractSql = "DELETE FROM contracts WHERE id_contracts = ?";
            PreparedStatement deleteContractStatement = connection.prepareStatement(deleteContractSql);
            deleteContractStatement.setInt(1, contractId);
            int rowsAffected = deleteContractStatement.executeUpdate();

            if (rowsAffected > 0) {
                alertService.showSuccessAlert("Contract successfully deleted!");
            } else {
                alertService.showErrorAlert("Failed to delete contract. Contract with ID " + contractId + " not found.");
            }

            deleteContractStatement.close();
            connection.close();
        } catch (SQLException e) {
            alertService.showErrorAlert("Error deleting contract: " + e.getMessage());
        }
    }
}
