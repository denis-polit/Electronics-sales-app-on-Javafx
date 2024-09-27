import javafx.scene.control.TextInputDialog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class EmployeeEditOwnAccount {
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private String managerName;
    ManagerAccountDetailsPage managerAccountDetailsPage;

    public EmployeeEditOwnAccount(String managerName) {
        this.managerName = managerName;
        managerAccountDetailsPage = new ManagerAccountDetailsPage();
    }

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            return connectionToDataBase.getConnection();
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    public void openEditManagerNameWindow() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Edit Manager Name");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new manager name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::updateManagerName);
    }

    public void openEditManagerPhoneNumberWindow() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Edit Phone Number");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new phone number:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::updatePhoneNumber);
    }

    public void openEditManagerPasswordWindow() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Edit Password");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new password:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::updatePassword);
    }

    public void openEditManagerEmailWindow() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Edit Email");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::updateEmail);
    }

    private void updateManagerName(String newManagerName) {
        try (Connection connection = establishDBConnection()) {
            String getManagerSql = "SELECT manager_name, employee_status FROM managers WHERE manager_name = ?";
            try (PreparedStatement getManagerStatement = connection.prepareStatement(getManagerSql)) {
                getManagerStatement.setString(1, managerName);
                ResultSet resultSet = getManagerStatement.executeQuery();

                if (resultSet.next()) {
                    String oldManagerName = resultSet.getString("manager_name");
                    String employeeStatus = resultSet.getString("employee_status");

                    String checkSql = "SELECT manager_name FROM managers WHERE manager_name = ?";
                    try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                        checkStatement.setString(1, newManagerName);
                        ResultSet checkResultSet = checkStatement.executeQuery();

                        if (!checkResultSet.next()) {
                            String updateSql = "UPDATE managers SET manager_name = ? WHERE manager_name = ?";
                            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                                updateStatement.setString(1, newManagerName);
                                updateStatement.setString(2, managerName);

                                int rowsUpdated = updateStatement.executeUpdate();

                                if (rowsUpdated > 0) {
                                    int managerId = SessionManager.getInstance().getClientIdByName(managerName);
                                    String details = "Updated manager name from: " + oldManagerName + " to: " + newManagerName;
                                    SessionManager.getInstance().logManagerActivity(managerId, "Update", "ManagerName", details);
                                    managerName = newManagerName;
                                    managerAccountDetailsPage.fetchAndDisplayManagerDetails();
                                } else {
                                    alertService.showErrorAlert("Failed to update manager name.");
                                }
                            }
                        } else {
                            alertService.showErrorAlert("This manager name is already taken. Please choose a different one.");
                        }
                    }
                } else {
                    alertService.showErrorAlert("Manager not found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while updating manager name.");
        }
    }

    private void updatePhoneNumber(String newPhoneNumber) {
        try (Connection connection = establishDBConnection()) {
            String getPhoneSql = "SELECT manager_phone_number, employee_status FROM managers WHERE manager_name = ?";
            try (PreparedStatement getPhoneStatement = connection.prepareStatement(getPhoneSql)) {
                getPhoneStatement.setString(1, managerName);
                ResultSet resultSet = getPhoneStatement.executeQuery();

                if (resultSet.next()) {
                    String oldPhoneNumber = resultSet.getString("manager_phone_number");
                    String employeeStatus = resultSet.getString("employee_status");

                    String updateSql = "UPDATE managers SET manager_phone_number = ? WHERE manager_name = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                        updateStatement.setString(1, newPhoneNumber);
                        updateStatement.setString(2, managerName);

                        int rowsUpdated = updateStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            int managerId = SessionManager.getInstance().getClientIdByName(managerName);
                            String details = "Updated phone number from: " + oldPhoneNumber + " to: " + newPhoneNumber;
                            SessionManager.getInstance().logManagerActivity(managerId, "Update", "ManagerPhoneNumber", details);
                            managerAccountDetailsPage.fetchAndDisplayManagerDetails();
                        } else {
                            alertService.showErrorAlert("Failed to update phone number.");
                        }
                    }
                } else {
                    alertService.showErrorAlert("Manager not found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while updating phone number.");
        }
    }

    private void updatePassword(String newPassword) {
        try {
            Connection connection = establishDBConnection();

            String getPasswordSql = "SELECT manager_password FROM managers WHERE manager_name = ?";
            PreparedStatement getPasswordStatement = connection.prepareStatement(getPasswordSql);
            getPasswordStatement.setString(1, managerName);
            ResultSet resultSet = getPasswordStatement.executeQuery();

            if (resultSet.next()) {
                String oldPassword = resultSet.getString("manager_password");

                String updateSql = "UPDATE managers SET manager_password = ? WHERE manager_name = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setString(1, newPassword);
                updateStatement.setString(2, managerName);

                int rowsUpdated = updateStatement.executeUpdate();

                if (rowsUpdated > 0) {
                    int managerId = SessionManager.getInstance().getClientIdByName(managerName);
                    SessionManager.getInstance().logActivity(managerId, "Update", "Password",
                            "Updated password from: " + oldPassword + " to: " + newPassword);

                    managerAccountDetailsPage.fetchAndDisplayManagerDetails();
                } else {
                    alertService.showErrorAlert("Failed to update password.");
                }
            } else {
                alertService.showErrorAlert("Manager not found.");
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while updating password.");
        }
    }

    private void updateEmail(String newEmail) {
        try (Connection connection = establishDBConnection()) {
            String getEmailSql = "SELECT manager_email, employee_status FROM managers WHERE manager_name = ?";
            try (PreparedStatement getEmailStatement = connection.prepareStatement(getEmailSql)) {
                getEmailStatement.setString(1, managerName);
                ResultSet resultSet = getEmailStatement.executeQuery();

                if (resultSet.next()) {
                    String oldEmail = resultSet.getString("manager_email");
                    String employeeStatus = resultSet.getString("employee_status");

                    String checkEmailSql = "SELECT * FROM managers WHERE manager_email = ?";
                    try (PreparedStatement checkEmailStatement = connection.prepareStatement(checkEmailSql)) {
                        checkEmailStatement.setString(1, newEmail);
                        ResultSet emailResultSet = checkEmailStatement.executeQuery();

                        if (!emailResultSet.next()) {
                            String updateSql = "UPDATE managers SET manager_email = ? WHERE manager_name = ?";
                            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                                updateStatement.setString(1, newEmail);
                                updateStatement.setString(2, managerName);

                                int rowsUpdated = updateStatement.executeUpdate();

                                if (rowsUpdated > 0) {
                                    int managerId = SessionManager.getInstance().getClientIdByName(managerName);
                                    String details = "Updated email from: " + oldEmail + " to: " + newEmail;
                                    SessionManager.getInstance().logManagerActivity(managerId, "Update", "ManagerEmail", details);
                                    managerAccountDetailsPage.fetchAndDisplayManagerDetails();
                                } else {
                                    alertService.showErrorAlert("Failed to update email.");
                                }
                            }
                        } else {
                            alertService.showErrorAlert("Email already in use. Please enter a different email.");
                        }
                    }
                } else {
                    alertService.showErrorAlert("Manager not found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while updating email.");
        }
    }
}
