import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class EditEmployeeAccount {

    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private CreateEmployeeAccounts createEmployeeAccounts = new CreateEmployeeAccounts();

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void editEmployeeDialog() {
        String managerName = showInputDialog("Enter Manager Name");
        if (managerName != null && !managerName.isEmpty()) {
            String[] fields = {"password", "email", "phoneNumber"};
            ChoiceDialog<String> fieldDialog = new ChoiceDialog<>(fields[0], fields);
            fieldDialog.setTitle("Select Field");
            fieldDialog.setHeaderText(null);
            fieldDialog.setContentText("Select Field to Edit:");

            Optional<String> fieldResult = fieldDialog.showAndWait();
            if (fieldResult.isPresent()) {
                String newValue = showInputDialog("Enter New Value for " + fieldResult.get());
                if (newValue != null && !newValue.isEmpty()) {
                    try (Connection connection = establishDBConnection()) {
                        if (fieldResult.get().equals("email") && !createEmployeeAccounts.isEmailUnique(newValue, connection)) {
                            alertService.showErrorAlert("Email is already in use.");
                            return;
                        }

                        if (fieldResult.get().equals("phoneNumber") && !createEmployeeAccounts.isPhoneNumberUnique(newValue, connection)) {
                            alertService.showErrorAlert("Phone number is already in use.");
                            return;
                        }

                        editEmployeeAccount(newValue, fieldResult.get(), managerName);
                    } catch (SQLException ex) {
                        alertService.showErrorAlert("Error editing manager account: " + ex.getMessage());
                    }
                }
            }
        }
    }

    private String showInputDialog(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input Dialog");
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void editEmployeeAccount(String newValue, String fieldToEdit, String managerName) {
        try (Connection connection = establishDBConnection()) {
            String sql = "UPDATE managers SET " + fieldToEdit + " = ? WHERE manager_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newValue);
                statement.setString(2, managerName);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    alertService.showSuccessAlert("Manager account updated successfully.");
                } else {
                    alertService.showErrorAlert("No manager account found with the given name.");
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error while editing manager account: " + e.getMessage());
        }
    }
}
