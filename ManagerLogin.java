import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ManagerLogin {

    private SessionManager sessionManager;
    private MenuPage menuPage;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public ManagerLogin() {
        this.sessionManager = SessionManager.getInstance();
    }

    public void start(Stage primaryStage) {
        this.menuPage = new MenuPage();
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.hide();
        });

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 900, 600);

        HBox mainPane = new HBox();

        VBox welcomePane = createWelcomePane(primaryStage);
        VBox loginForm = createManagerLoginForm(primaryStage);

        mainPane.getChildren().addAll(welcomePane, loginForm);

        welcomePane.setPrefWidth(scene.getWidth() * 0.4);
        loginForm.setPrefWidth(scene.getWidth() * 0.6);

        HBox.setHgrow(welcomePane, Priority.SOMETIMES);
        HBox.setHgrow(loginForm, Priority.SOMETIMES);

        root.setCenter(mainPane);
        root.setStyle("-fx-background-color: #000000;");
        scene.setFill(Color.TRANSPARENT);

        primaryStage.setTitle("Login Form");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();

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

    private VBox createWelcomePane(Stage primaryStage) {
        VBox welcomePane = new VBox(20);
        welcomePane.setAlignment(Pos.CENTER);
        welcomePane.setStyle("-fx-background-color: #7331FF;");

        ImageView profileIcon = new ImageView("file:icons/employee_profile_icon.png");
        profileIcon.setFitHeight(120);
        profileIcon.setFitWidth(120);

        Label welcomeLabel = new Label("Welcome");
        welcomeLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 30));
        welcomeLabel.setTextFill(Color.WHITE);

        Button backButton = ButtonStyle.expandPaneStyledButton("<");
        backButton.setOnAction(event -> primaryStage.close());
        backButton.setPrefWidth(100);
        backButton.setPrefHeight(30);

        Label registerLabel = new Label("               If you forgot data \n Contact admin to recover your data.");
        registerLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 20));
        registerLabel.setTextFill(Color.DARKGRAY);

        welcomePane.getChildren().addAll(profileIcon, welcomeLabel, registerLabel, backButton);

        VBox.setMargin(backButton, new Insets(50, 0, 0, 0));
        VBox.setMargin(profileIcon, new Insets(0, 0, 50, 0));
        VBox.setMargin(welcomeLabel, new Insets(0, 0, 10, 0));

        return welcomePane;
    }

    private VBox createManagerLoginForm(Stage primaryStage) {
        VBox managerFormContainer = new VBox(10);
        managerFormContainer.setPadding(new Insets(100, 20, 150, 20));
        managerFormContainer.setAlignment(Pos.CENTER);
        managerFormContainer.setStyle("-fx-background-color: #07080A;");

        Label signInLabel = new Label(" Manager Sign In ");
        signInLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 30));
        signInLabel.setTextFill(Color.WHITE);
        VBox.setMargin(signInLabel, new Insets(0, 0, 30, 0));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(20);
        gridPane.setAlignment(Pos.CENTER);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(10);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(80);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(10);

        gridPane.getColumnConstraints().addAll(column1, column2, column3);

        TextField accountNameField = new TextField();
        accountNameField.setPromptText("Manager Login");
        accountNameField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        accountNameField.setPrefHeight(40);

        Tooltip accountNameTooltip = new Tooltip("Enter your username");
        accountNameTooltip.setFont(Font.font("Gotham", FontWeight.NORMAL, 12));
        accountNameField.setTooltip(accountNameTooltip);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Manager Password");
        passwordField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        passwordField.setPrefHeight(40);

        Tooltip passwordTooltip = new Tooltip("Enter your password");
        passwordTooltip.setFont(Font.font("Gotham", FontWeight.NORMAL, 12));
        passwordField.setTooltip(passwordTooltip);

        TextField phoneNumberField = new TextField();
        phoneNumberField.setPromptText("Phone Number");
        phoneNumberField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        phoneNumberField.setPrefHeight(40);

        Tooltip phoneNumberTooltip = new Tooltip("Enter your phone number");
        phoneNumberTooltip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        phoneNumberField.setTooltip(phoneNumberTooltip);

        TextField emailField = new TextField();
        emailField.setPromptText("Manager Email");
        emailField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        emailField.setPrefHeight(40);

        Tooltip emailTooltip = new Tooltip("Enter your email");
        emailTooltip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        emailField.setTooltip(emailTooltip);

        gridPane.addRow(0, new Region(), accountNameField, new Region());
        gridPane.addRow(1, new Region(), passwordField, new Region());
        gridPane.addRow(2, new Region(), phoneNumberField, new Region());
        gridPane.addRow(3, new Region(), emailField, new Region());

        Button loginButton = ButtonStyle.createStyledButton("Sign in as Manager");
        loginButton.setOnAction(event -> {
            managerLogining(primaryStage, managerFormContainer, accountNameField, passwordField, phoneNumberField, emailField, loginButton);
        });
        loginButton.setPrefWidth(200);
        VBox.setMargin(loginButton, new Insets(20, 0, 20, 0));

        managerFormContainer.getChildren().addAll(signInLabel, gridPane, loginButton);
        return managerFormContainer;
    }

    private void showErrorMessage(String errorMessage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.showAndWait();
        });
    }

    private void showErrorMessageAndResetFields(List<String> errorMessages, VBox formContainer) {
        if (formContainer == null) {
            return;
        }

        StringBuilder errorMessage = new StringBuilder();
        for (String error : errorMessages) {
            errorMessage.append(error).append("\n");
        }

        showErrorMessage(errorMessage.toString());

        formContainer.setOpacity(1.0);
        formContainer.setTranslateY(0);
    }

    private void showSuccessMessage(Button button) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Operation successful");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        Optional<ButtonType> result = alert.showAndWait();
        result.ifPresent(buttonType -> {
            if (buttonType == okButton) {
                button.getStyleClass().clear();
                button.getStyleClass().add("registration-button-success");
                button.setText("");
                button.setDisable(true);
            }
        });
    }

    private void managerLogining(Stage primaryStage, VBox formContainer, TextField accountNameField, PasswordField passwordField, TextField phoneNumberField, TextField emailField, Button button) {
        String accountName = accountNameField.getText();
        String password = passwordField.getText();
        String phoneNumber = phoneNumberField.getText();
        String email = emailField.getText();
        String managerName = accountName;
        String ipAddress = SessionManager.getInstance().getIpAddress();

        if (accountName.isEmpty()) {
            showErrorMessageAndResetFields(Collections.singletonList("Please enter your username"), formContainer);
            return;
        }

        if (password.isEmpty()) {
            showErrorMessageAndResetFields(Collections.singletonList("Please enter your password"), formContainer);
            return;
        }

        if (phoneNumber.isEmpty()) {
            showErrorMessageAndResetFields(Collections.singletonList("Please enter your phone number"), formContainer);
            return;
        }

        if (email.isEmpty()) {
            showErrorMessageAndResetFields(Collections.singletonList("Please enter your email"), formContainer);
            return;
        }

        String reason = checkBlockedData(accountName, phoneNumber, email);
        if (reason != null) {
            alertService.showBlockedAccountAlert(reason);
            return;
        }

        if (isIpBlockedForManager(managerName, ipAddress)) {
            alertService.showIpBlockedAlert();
            return;
        }

        boolean loggedIn = checkManagerLogin(accountName, password, phoneNumber, email);
        if (loggedIn) {
            sessionManager.setManagerEnter(true);
            sessionManager.setCurrentManagerName(accountName);
            showSuccessMessage(button);
            logManagerSession(accountName);

            int managerId = SessionManager.getInstance().getEmployeeIdByName(managerName);

            String managerStatus = getManagerStatus(managerId);

            SessionManager.getInstance().logManagerActivity(managerId, "Login", managerStatus, "Manager logged in");

            showManagerDashboard(primaryStage);
        } else {
            List<String> errorMessages = new ArrayList<>();
            errorMessages.add("Incorrect login or password");
            showErrorMessageAndResetFields(errorMessages, formContainer);
        }
    }

    private boolean isIpBlockedForManager(String managerName, String ipAddress) {
        SessionManager sessionManager = SessionManager.getInstance();
        int managerId = sessionManager.getEmployeeIdByName(managerName);
        return isIpBlocked(managerId, ipAddress);
    }

    private boolean isIpBlocked(int managerId, String ipAddress) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT * FROM blocked_employee_sessions_ips WHERE manager_id = ? AND ip_address = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, managerId);
                pstmt.setString(2, ipAddress);

                ResultSet resultSet = pstmt.executeQuery();
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String checkBlockedData(String accountName, String phoneNumber, String email) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT reason FROM blocked_data WHERE phone_number = ? OR email = ? OR name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, phoneNumber);
                pstmt.setString(2, email);
                pstmt.setString(3, accountName);

                ResultSet resultSet = pstmt.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("reason");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getManagerStatus(int managerId) {
        String status = "Unknown";
        String sql = "SELECT employee_status FROM managers WHERE id_managers = ?";

        try (Connection conn = FirstConnectionToDataBase.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, managerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                status = rs.getString("employee_status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return status;
    }

    private void logManagerSession(String managerName) {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        PreparedStatement updateStatement = null;
        ResultSet resultSet = null;

        try {
            connection = establishDBConnection();

            String getManagerIdQuery = "SELECT id_managers FROM managers WHERE manager_name = ?";
            PreparedStatement getManagerIdStmt = connection.prepareStatement(getManagerIdQuery);
            getManagerIdStmt.setString(1, managerName);
            ResultSet managerIdResult = getManagerIdStmt.executeQuery();
            int managerId = -1;
            if (managerIdResult.next()) {
                managerId = managerIdResult.getInt("id_managers");
            }

            String checkSql = "SELECT session_id FROM employee_sessions WHERE manager_name = ? AND end_time IS NULL ORDER BY start_time DESC LIMIT 1";
            updateStatement = connection.prepareStatement(checkSql);
            updateStatement.setString(1, managerName);
            resultSet = updateStatement.executeQuery();

            if (resultSet.next()) {
                int sessionId = resultSet.getInt("session_id");
                String updateSql = "UPDATE employee_sessions SET end_time = NOW(), duration = TIMEDIFF(NOW(), start_time) WHERE session_id = ?";
                updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setInt(1, sessionId);
                updateStatement.executeUpdate();
            }

            String insertSql = "INSERT INTO employee_sessions (manager_id, manager_name, device_type, country, ip_address, start_time) VALUES (?, ?, ?, ?, ?, NOW())";
            insertStatement = connection.prepareStatement(insertSql);
            insertStatement.setInt(1, managerId);
            insertStatement.setString(2, managerName);
            insertStatement.setString(3, SessionManager.getInstance().getDeviceType());
            insertStatement.setString(4, SessionManager.getInstance().getCountryByIp(SessionManager.getInstance().getIpAddress()));
            insertStatement.setString(5, SessionManager.getInstance().getIpAddress());
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to log session: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (insertStatement != null) insertStatement.close();
                if (updateStatement != null) updateStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showManagerDashboard(Stage primaryStage) {
        try {
            if (sessionManager.isManagerEnter() && sessionManager.getCurrentManagerName() != null && !sessionManager.getCurrentManagerName().isEmpty()) {
                showManagerAccountDetails();
                primaryStage.close();
            } else {
                showLoginRequiredAlert();
            }
        } catch (Exception e) {
            alertService.showErrorAlert("An error occurred while showing manager dashboard.");
        }
    }

    private void showManagerAccountDetails() {
        ManagerAccountDetailsPage accountDetailsPage = new ManagerAccountDetailsPage();
        accountDetailsPage.init(sessionManager.getCurrentManagerName(), sessionManager.isManagerEnter());
        Stage accountDetailsStage = new Stage();
        accountDetailsPage.start(accountDetailsStage);
    }

    private void showLoginRequiredAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Login Required");
        alert.setContentText("Please log in to access your account.");

        ButtonType loginButton = new ButtonType("Login");
        alert.getButtonTypes().setAll(loginButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == loginButton) {
            alert.close();
        }
    }

    public boolean checkManagerLogin(String accountName, String password, String phoneNumber, String email) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT * FROM managers WHERE manager_name = ? AND password = ? AND manager_phone_number = ? AND manager_email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, accountName);
                pstmt.setString(2, password);
                pstmt.setString(3, phoneNumber);
                pstmt.setString(4, email);

                ResultSet resultSet = pstmt.executeQuery();
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
