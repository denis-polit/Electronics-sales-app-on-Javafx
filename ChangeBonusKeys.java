import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChangeBonusKeys {

    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    private TextField createTextFieldWithPrompt(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        return textField;
    }

    public void showChangeKeysWindow() {
        Stage changeKeysStage = new Stage();
        VBox changeKeysForm = new VBox(10);
        changeKeysForm.setPadding(new Insets(10));
        changeKeysForm.setAlignment(Pos.CENTER);

        ComboBox<String> employeeNameComboBox = new ComboBox<>();
        employeeNameComboBox.setPromptText("Select Employee Name");
        populateEmployeeNames(employeeNameComboBox);

        TextField newKeyField = createTextFieldWithPrompt("New Key");

        Button submitButton = ButtonStyle.createStyledButton("Submit");
        submitButton.setOnAction(event -> {
            String selectedEmployee = employeeNameComboBox.getValue();
            String newKey = newKeyField.getText();
            if (selectedEmployee != null && !selectedEmployee.isEmpty() && newKey != null && !newKey.isEmpty()) {
                updateEmployeeBonusKey(selectedEmployee, newKey);
                changeKeysStage.close();
            } else {
                alertService.showErrorAlert("Please select an employee and enter a new key.");
            }
        });

        changeKeysForm.getChildren().addAll(employeeNameComboBox, newKeyField, submitButton);

        Scene changeKeysScene = new Scene(changeKeysForm, 300, 200);
        changeKeysStage.setScene(changeKeysScene);
        changeKeysStage.setTitle("Change Keys");
        changeKeysStage.show();
    }

    private void populateEmployeeNames(ComboBox<String> comboBox) {
        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT employee_name FROM bookkeeping";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    comboBox.getItems().add(resultSet.getString("employee_name"));
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error fetching employee names: " + e.getMessage());
        }
    }

    private void updateEmployeeBonusKey(String employeeName, String newKey) {
        try (Connection connection = establishDBConnection()) {
            String sql = "UPDATE bookkeeping SET bonus_key = ? WHERE employee_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newKey);
                statement.setString(2, employeeName);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    alertService.showSuccessAlert("Key updated successfully for employee: " + employeeName);
                } else {
                    alertService.showErrorAlert("No employee found with the given name.");
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error updating key: " + e.getMessage());
        }
    }
}
