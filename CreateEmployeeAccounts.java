import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;

public class CreateEmployeeAccounts {

    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    CreateEmployeeAccounts() {

    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        BorderPane root = new BorderPane();
        MenuPage menuPage = new MenuPage();

        root.setStyle("-fx-background-color: black;");

        VBox centerContainer = new VBox(10);
        centerContainer.setPadding(new Insets(10));

        createInputFields(centerContainer);

        root.setCenter(centerContainer);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Account Details");
        primaryStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();

        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private void createInputFields(VBox container) {
        TextField managerNameField = createTextFieldWithPrompt("Manager Name");
        setFieldStyleAndTooltip(managerNameField, "Manager Name");

        TextField passwordField = createTextFieldWithPrompt("Password");
        setFieldStyleAndTooltip(passwordField, "Password");

        TextField phoneNumberField = createTextFieldWithPrompt("Phone Number");
        setFieldStyleAndTooltip(phoneNumberField, "Phone Number");

        TextField emailField = createTextFieldWithPrompt("Email");
        setFieldStyleAndTooltip(emailField, "Email");

        ComboBox<String> employeeStatusComboBox = new ComboBox<>();
        employeeStatusComboBox.setPromptText("Employee Status");
        employeeStatusComboBox.getItems().addAll("Manager", "Main Manager", "Accountant", "Admin", "Super Admin");
        employeeStatusComboBox.setEditable(true);
        employeeStatusComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        String comboBoxStyle = "-fx-control-inner-background: black; -fx-font-size: 14px;";
        employeeStatusComboBox.getEditor().setStyle(comboBoxStyle);
        Tooltip employeeStatusTooltip = new Tooltip("Select or type employee status");
        employeeStatusComboBox.setTooltip(employeeStatusTooltip);
        employeeStatusComboBox.getEditor().setStyle("-fx-text-fill: white;");

        TextField salaryKeyField = createTextFieldWithPrompt("Salary Key");
        setFieldStyleAndTooltip(salaryKeyField, "Salary Key");

        Button createManagerButton = ButtonStyle.createStyledButton("Create Manager Account");
        createManagerButton.setOnAction(e -> {
            if (managerNameField.getText().isEmpty()) {
                alertService.showErrorAlert("Manager Name field is empty! Please fill it.");
                return;
            }
            if (passwordField.getText().isEmpty()) {
                alertService.showErrorAlert("Password field is empty! Please fill it.");
                return;
            }
            if (phoneNumberField.getText().isEmpty()) {
                alertService.showErrorAlert("Phone Number field is empty! Please fill it.");
                return;
            }
            if (emailField.getText().isEmpty()) {
                alertService.showErrorAlert("Email field is empty! Please fill it.");
                return;
            }
            if (employeeStatusComboBox.getSelectionModel().isEmpty()) {
                alertService.showErrorAlert("Employee Status field is empty! Please select or type a status.");
                return;
            }
            if (salaryKeyField.getText().isEmpty()) {
                alertService.showErrorAlert("Salary Key field is empty! Please fill it.");
                return;
            }

            createManagerAccount(
                    managerNameField.getText(),
                    passwordField.getText(),
                    phoneNumberField.getText(),
                    emailField.getText(),
                    employeeStatusComboBox.getSelectionModel().getSelectedItem(),
                    salaryKeyField.getText()
            );
        });

        container.getChildren().addAll(
                createBoldLabel("Create Manager Account", "-fx-text-fill: white;"),
                managerNameField,
                passwordField,
                phoneNumberField,
                emailField,
                employeeStatusComboBox,
                salaryKeyField,
                createManagerButton
        );
    }

    private void setFieldStyleAndTooltip(TextField field, String promptText) {
        field.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        field.setPromptText(promptText);
        field.setPrefWidth(200);
        Tooltip tooltip = new Tooltip(promptText);
        field.setTooltip(tooltip);
    }

    private TextField createTextFieldWithPrompt(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        return textField;
    }

    private Label createBoldLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        label.setFont(Font.font("Gotham", FontWeight.NORMAL, 24));
        return label;
    }

    private void createManagerAccount(String managerName, String password, String phoneNumber, String email, String employeeStatus, String bonusKey) {
        try (Connection connection = establishDBConnection()) {
            if (isEmailUnique(email, connection)) {
                if (isPhoneNumberUnique(phoneNumber, connection)) {
                    if (isManagerNameUnique(managerName, connection)) {
                        String sql = "INSERT INTO managers (manager_name, password, manager_phone_number, manager_email, employee_status) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                            statement.setString(1, managerName);
                            statement.setString(2, password);
                            statement.setString(3, phoneNumber);
                            statement.setString(4, email);
                            statement.setString(5, employeeStatus);

                            int rowsInserted = statement.executeUpdate();
                            if (rowsInserted > 0) {
                                ResultSet generatedKeys = statement.getGeneratedKeys();
                                if (generatedKeys.next()) {
                                    int managerId = generatedKeys.getInt(1);
                                    updateBonusKey(managerId, bonusKey);
                                    saveManagerNameToBookkeeping(managerName);
                                }
                                alertService.showSuccessAlert("A new manager account was created successfully.");
                            }
                        }
                    } else {
                        alertService.showErrorAlert("This name is already taken. Please choose another one.");
                    }
                } else {
                    alertService.showErrorAlert("Phone number is already registered. Please use another one.");
                }
            } else {
                alertService.showErrorAlert("Email is already registered. Please use another one.");
            }
        } catch (SQLException ex) {
            alertService.showErrorAlert("Error while creating manager account: " + ex.getMessage());
        }
    }

    public boolean isEmailUnique(String email, Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM managers WHERE manager_email = ?";
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

    public boolean isPhoneNumberUnique(String phoneNumber, Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM managers WHERE manager_phone_number = ?";
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

    private boolean isManagerNameUnique(String managerName, Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM managers WHERE manager_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, managerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count") == 0;
                }
            }
        }
        return false;
    }

    private void saveManagerNameToBookkeeping(String managerName) {
        try (Connection connection = establishDBConnection()) {
            String sql = "INSERT INTO bookkeeping (employee_name) VALUES (?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, managerName);
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    alertService.showSuccessAlert("Name saved to bookkeeping successfully.");
                } else {
                    alertService.showErrorAlert("Failed to save manager name to bookkeeping.");
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error saving manager name to bookkeeping: " + e.getMessage());
        }
    }

    private void updateBonusKey(int managerId, String bonusKey) {
        try (Connection connection = establishDBConnection()) {
            String sql = "UPDATE bookkeeping SET bonus_key = ? WHERE manager_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, bonusKey);
                statement.setInt(2, managerId);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    alertService.showSuccessAlert("Bonus key updated successfully for manager ID: " + managerId);
                } else {
                    alertService.showErrorAlert("No matching record found in bookkeeping table for manager ID: " + managerId);
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error updating bonus key: " + e.getMessage());
        }
    }
}
