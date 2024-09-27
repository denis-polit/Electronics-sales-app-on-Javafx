import javafx.scene.control.TextInputDialog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ClientDataEdit {

    private String clientName;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private final BookkeepingPage bookkeepingPage = new BookkeepingPage();
    private ClientAccountDetailsPage clientAccountDetailsPage;

    public ClientDataEdit(String clientName) {
        this.clientName = clientName;
        clientAccountDetailsPage = new ClientAccountDetailsPage(clientName);
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            System.out.println("Attempting to establish DB connection...");
            Connection conn = connectionToDataBase.getConnection();
            if (conn != null) {
                System.out.println("Database connection established successfully.");
            } else {
                System.out.println("Failed to establish database connection.");
            }
            return conn;
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    public void openEditUserNameWindow() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Edit User Name");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new user name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::updateUserName);
    }

    public void openEditUserPhoneNumberWindow() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Edit Phone Number");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new phone number:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::updatePhoneNumber);
    }

    public void openEditUserPasswordWindow() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Edit Password");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new password:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::updatePassword);
    }

    public void openEditUserEmailWindow() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Edit Email");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::updateEmail);
    }

    private void updateUserName(String newName) {
        try {
            Connection connection = establishDBConnection();

            String getUserSql = "SELECT user_name FROM users WHERE user_name = ?";
            PreparedStatement getUserStatement = connection.prepareStatement(getUserSql);
            getUserStatement.setString(1, clientName);
            ResultSet resultSet = getUserStatement.executeQuery();

            if (resultSet.next()) {
                String oldName = resultSet.getString("user_name");

                String checkSql = "SELECT user_name FROM users WHERE user_name = ?";
                PreparedStatement checkStatement = connection.prepareStatement(checkSql);
                checkStatement.setString(1, newName);
                ResultSet checkResultSet = checkStatement.executeQuery();

                if (!checkResultSet.next()) {
                    String updateSql = "UPDATE users SET user_name = ? WHERE user_name = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                    updateStatement.setString(1, newName);
                    updateStatement.setString(2, clientName);

                    int rowsUpdated = updateStatement.executeUpdate();

                    if (rowsUpdated > 0) {
                        int userId = SessionManager.getInstance().getClientIdByName(clientName);
                        SessionManager.getInstance().logActivity(userId, "Update", "UserName",
                                "Updated user name from: " + oldName + " to: " + newName);

                        clientName = newName;
                        clientAccountDetailsPage.fetchAndDisplayUserDetails();
                    } else {
                        alertService.showErrorAlert("Failed to update user name.");
                    }
                } else {
                    alertService.showErrorAlert("This user name is already taken. Please choose a different one.");
                }
            } else {
                alertService.showErrorAlert("User not found.");
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while updating user name.");
        }
    }

    private void updatePhoneNumber(String newPhoneNumber) {
        try {
            Connection connection = establishDBConnection();

            String getPhoneSql = "SELECT user_phone_number FROM users WHERE user_name = ?";
            PreparedStatement getPhoneStatement = connection.prepareStatement(getPhoneSql);
            getPhoneStatement.setString(1, clientName);
            ResultSet resultSet = getPhoneStatement.executeQuery();

            if (resultSet.next()) {
                String oldPhoneNumber = resultSet.getString("user_phone_number");

                String updateSql = "UPDATE users SET user_phone_number = ? WHERE user_name = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setString(1, newPhoneNumber);
                updateStatement.setString(2, clientName);

                int rowsUpdated = updateStatement.executeUpdate();

                if (rowsUpdated > 0) {
                    int userId = SessionManager.getInstance().getClientIdByName(clientName);
                    SessionManager.getInstance().logActivity(userId, "Update", "UserPhoneNumber",
                            "Updated phone number from: " + oldPhoneNumber + " to: " + newPhoneNumber);

                    clientAccountDetailsPage.fetchAndDisplayUserDetails();
                } else {
                    alertService.showErrorAlert("Failed to update phone number.");
                }
            } else {
                alertService.showErrorAlert("User not found.");
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while updating phone number.");
        }
    }

    private void updatePassword(String newPassword) {
        try {
            Connection connection = establishDBConnection();

            String getPasswordSql = "SELECT password FROM users WHERE user_name = ?";
            PreparedStatement getPasswordStatement = connection.prepareStatement(getPasswordSql);
            getPasswordStatement.setString(1, clientName);
            ResultSet resultSet = getPasswordStatement.executeQuery();

            if (resultSet.next()) {
                String oldPassword = resultSet.getString("password");

                String updateSql = "UPDATE users SET password = ? WHERE user_name = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setString(1, newPassword);
                updateStatement.setString(2, clientName);

                int rowsUpdated = updateStatement.executeUpdate();

                if (rowsUpdated > 0) {
                    int userId = SessionManager.getInstance().getClientIdByName(clientName);
                    SessionManager.getInstance().logActivity(userId, "Update", "Password",
                            "Updated password from: " + oldPassword + " to: " + newPassword);

                    clientAccountDetailsPage.fetchAndDisplayUserDetails();
                } else {
                    alertService.showErrorAlert("Failed to update password.");
                }
            } else {
                alertService.showErrorAlert("User not found.");
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while updating password.");
        }
    }

    private void updateEmail(String newEmail) {
        try {
            Connection connection = establishDBConnection();

            String getEmailSql = "SELECT user_email FROM users WHERE user_name = ?";
            PreparedStatement getEmailStatement = connection.prepareStatement(getEmailSql);
            getEmailStatement.setString(1, clientName);
            ResultSet resultSet = getEmailStatement.executeQuery();

            if (resultSet.next()) {
                String oldEmail = resultSet.getString("user_email");

                String updateSql = "UPDATE users SET user_email = ? WHERE user_name = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setString(1, newEmail);
                updateStatement.setString(2, clientName);

                int rowsUpdated = updateStatement.executeUpdate();

                if (rowsUpdated > 0) {
                    int userId = SessionManager.getInstance().getClientIdByName(clientName);
                    SessionManager.getInstance().logActivity(userId, "Update", "UserEmail",
                            "Updated email from: " + oldEmail + " to: " + newEmail);

                    clientAccountDetailsPage.fetchAndDisplayUserDetails();
                } else {
                    alertService.showErrorAlert("Failed to update email.");
                }
            } else {
                alertService.showErrorAlert("User not found.");
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while updating email.");
        }
    }
}
