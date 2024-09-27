import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class EditClientAccount {

    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void editUserDialog() {
        String clientName = showInputDialog("Enter User Name");
        if (clientName != null && !clientName.isEmpty()) {
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
                        if (fieldResult.get().equals("email") && !isEmailUniqueForUsers(newValue, connection)) {
                            alertService.showErrorAlert("Email is already in use.");
                            return;
                        }

                        if (fieldResult.get().equals("phoneNumber") && !isPhoneNumberUniqueForUsers(newValue, connection)) {
                            alertService.showErrorAlert("Phone number is already in use.");
                            return;
                        }

                        editClientAccount(newValue, fieldResult.get(), clientName);
                    } catch (SQLException ex) {
                        alertService.showErrorAlert("Error editing client account: " + ex.getMessage());
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

    private boolean isEmailUniqueForUsers(String email, Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM users WHERE user_email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count") == 0;
                }
            }
        }
        return false;
    }

    private boolean isPhoneNumberUniqueForUsers(String phoneNumber, Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM users WHERE user_phone_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phoneNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count") == 0;
                }
            }
        }
        return false;
    }

    private void editClientAccount(String newValue, String fieldToEdit, String clientName) {
        try (Connection connection = establishDBConnection()) {
            String sql = "UPDATE users SET " + fieldToEdit + " = ? WHERE user_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newValue);
                statement.setString(2, clientName);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    alertService.showSuccessAlert("Client account updated successfully.");
                } else {
                    alertService.showErrorAlert("No client account found with the given name.");
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error while editing client account: " + e.getMessage());
        }
    }
}
