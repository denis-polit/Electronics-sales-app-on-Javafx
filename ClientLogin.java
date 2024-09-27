import javafx.application.Platform;
import javafx.geometry.HPos;
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

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientLogin {

    private MenuPage menuPage;
    private FirstConnectionToDataBase connectionToDataBase;
    private HttpServletRequest request;
    private AlertServiceImpl alertService = new AlertServiceImpl();

    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.hide();
        });

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 900, 600);

        HBox mainPane = new HBox();

        VBox welcomePane = createWelcomePane(primaryStage);
        VBox loginForm = createClientLoginForm(primaryStage);

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
        return connectionToDataBase.getConnection();
    }

    private VBox createWelcomePane(Stage primaryStage) {
        VBox welcomePane = new VBox(20);
        welcomePane.setAlignment(Pos.CENTER);
        welcomePane.setStyle("-fx-background-color: #7331FF;");

        ImageView profileIcon = new ImageView("file:icons/profile_icon.png");
        profileIcon.setFitHeight(120);
        profileIcon.setFitWidth(120);

        Label welcomeLabel = new Label("Welcome");
        welcomeLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 30));
        welcomeLabel.setTextFill(Color.WHITE);

        Button backButton = ButtonStyle.expandPaneStyledButton("<");
        backButton.setOnAction(event -> primaryStage.close());
        backButton.setPrefWidth(100);
        backButton.setPrefHeight(30);

        Label registerLabel = new Label("If you don't have an account yet -\n                     Register");
        registerLabel.setOnMouseClicked(event -> showClientRegistrationWindow(primaryStage));
        registerLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 20));
        registerLabel.setTextFill(Color.DARKGRAY);

        welcomePane.getChildren().addAll(profileIcon, welcomeLabel, registerLabel, backButton);

        VBox.setMargin(backButton, new Insets(50, 0, 0, 0));
        VBox.setMargin(profileIcon, new Insets(0, 0, 50, 0));
        VBox.setMargin(welcomeLabel, new Insets(0, 0, 10, 0));

        return welcomePane;
    }

    private VBox createClientLoginForm(Stage primaryStage) {
        VBox clientFormContainer = new VBox(10);
        clientFormContainer.setPadding(new Insets(100, 20, 150, 20));
        clientFormContainer.setAlignment(Pos.CENTER);
        clientFormContainer.setStyle("-fx-background-color: #07080A;");

        Label signInLabel = new Label(" Sign In ");
        signInLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 30));
        signInLabel.setTextFill(Color.WHITE);
        VBox.setMargin(signInLabel, new Insets(0, 0, 0, 0));

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
        accountNameField.setPromptText("User Login");
        accountNameField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        accountNameField.setPrefHeight(40);

        Tooltip accountNameTooltip = new Tooltip("Enter your username");
        accountNameTooltip.setFont(Font.font("Gotham", FontWeight.NORMAL, 12));
        accountNameField.setTooltip(accountNameTooltip);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("User Password");
        passwordField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        passwordField.setPrefHeight(40);

        Tooltip passwordTooltip = new Tooltip("Enter your password");
        passwordTooltip.setFont(Font.font("Gotham", FontWeight.NORMAL, 12));
        passwordField.setTooltip(passwordTooltip);

        GridPane.setHgrow(accountNameField, Priority.ALWAYS);
        GridPane.setHgrow(passwordField, Priority.ALWAYS);
        GridPane.setHalignment(accountNameField, HPos.CENTER);
        GridPane.setHalignment(passwordField, HPos.CENTER);

        gridPane.addRow(0, new Region(), accountNameField, new Region());
        gridPane.addRow(1, new Region(), passwordField, new Region());

        VBox.setVgrow(gridPane, Priority.ALWAYS);

        Button loginButton = ButtonStyle.createStyledButton("Sign in as Client");
        loginButton.setOnAction(event -> clientLogining(clientFormContainer, accountNameField, passwordField, loginButton, primaryStage));
        loginButton.setPrefWidth(200);
        VBox.setMargin(loginButton, new Insets(20, 0, 20, 0));

        Separator separator = new Separator();

        Label forgotPasswordLabel = new Label("Forgot Password?");
        forgotPasswordLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 16));
        forgotPasswordLabel.setTextFill(Color.DARKGRAY);
        forgotPasswordLabel.setOnMouseClicked(event -> {
            primaryStage.close();
            ForgotClientData forgotClientData = new ForgotClientData();
            Stage forgotPasswordStage = new Stage();
            try {
                forgotClientData.start(forgotPasswordStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        clientFormContainer.getChildren().addAll(signInLabel, gridPane, loginButton, separator, forgotPasswordLabel);
        return clientFormContainer;
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

    public boolean checkClientLogin(String accountName, String password) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT id FROM users WHERE user_name = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, accountName);
                pstmt.setString(2, password);

                ResultSet resultSet = pstmt.executeQuery();
                if (resultSet.next()) {
                    int userId = resultSet.getInt("id");
                    if (isIpBlocked(userId, SessionManager.getInstance().getIpAddress())) {
                        alertService.showIpBlockedAlert();
                        return false;
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isIpBlocked(int userId, String ipAddress) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT * FROM blocked_sessions_ips WHERE user_id = ? AND ip_address = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);
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

    private void clientLogining(VBox formContainer, TextField accountNameField, PasswordField passwordField, Button button, Stage primaryStage) {
        String accountName = accountNameField.getText();
        String password = passwordField.getText();

        if (accountName.isEmpty() || password.isEmpty()) {
            List<String> errorMessages = new ArrayList<>();
            if (accountName.isEmpty()) {
                errorMessages.add("Username is required");
            }
            if (password.isEmpty()) {
                errorMessages.add("Password is required");
            }
            showErrorMessageAndResetFields(errorMessages, formContainer);
        } else {
            String reason = checkBlockedData(accountName, "", "");
            if (reason != null) {
                alertService.showBlockedAccountAlert(reason);
                return;
            }

            boolean loggedIn = checkClientLogin(accountName, password);
            if (loggedIn) {
                SessionManager.getInstance().setClientEnter(true);
                SessionManager.getInstance().setCurrentClientName(accountName);
                showSuccessMessage(button);
                logSession(accountName);

                int userId = SessionManager.getInstance().getClientIdByName(accountName);
                SessionManager.getInstance().logActivity(userId, "Login", "User", "User logged in");

                showClientDashboard(primaryStage);
            } else {
                List<String> errorMessages = new ArrayList<>();
                errorMessages.add("Incorrect login or password");
                showErrorMessageAndResetFields(errorMessages, formContainer);
            }
        }
    }

    private void logSession(String clientName) {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        PreparedStatement updateStatement = null;
        ResultSet resultSet = null;

        try {
            connection = establishDBConnection();

            String getUserIdQuery = "SELECT id FROM users WHERE user_name = ?";
            PreparedStatement getUserIdStmt = connection.prepareStatement(getUserIdQuery);
            getUserIdStmt.setString(1, clientName);
            ResultSet userIdResult = getUserIdStmt.executeQuery();
            int userId = -1;
            if (userIdResult.next()) {
                userId = userIdResult.getInt("id");
            }

            String checkSql = "SELECT session_id FROM sessions WHERE user_name = ? AND end_time IS NULL ORDER BY start_time DESC LIMIT 1";
            updateStatement = connection.prepareStatement(checkSql);
            updateStatement.setString(1, clientName);
            resultSet = updateStatement.executeQuery();

            if (resultSet.next()) {
                int sessionId = resultSet.getInt("session_id");
                String updateSql = "UPDATE sessions SET end_time = NOW(), duration = TIMEDIFF(NOW(), start_time) WHERE session_id = ?";
                updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setInt(1, sessionId);
                updateStatement.executeUpdate();
            }

            String insertSql = "INSERT INTO sessions (user_id, user_name, device_type, country, ip_address, start_time) VALUES (?, ?, ?, ?, ?, NOW())";
            insertStatement = connection.prepareStatement(insertSql);
            insertStatement.setInt(1, userId);
            insertStatement.setString(2, clientName);
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

    private void showClientDashboard(Stage primaryStage) {
        try {
            if (SessionManager.getInstance().isClientEnter()) {
                String currentClientName = SessionManager.getInstance().getCurrentClientName();
                if (currentClientName != null && !currentClientName.isEmpty()) {
                    ClientAccountDetailsPage accountDetailsPage = new ClientAccountDetailsPage(currentClientName);
                    Stage accountDetailsStage = new Stage();
                    accountDetailsPage.start(accountDetailsStage);
                    primaryStage.close();
                } else {
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
            } else {
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
        } catch (Exception e) {
            System.err.println("An error occurred while showing client dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showClientRegistrationWindow(Stage currentStage) {
        try {
            ClientRegistration clientRegistration = new ClientRegistration();
            Stage clientRegistrationStage = new Stage();
            clientRegistration.start(clientRegistrationStage);
            currentStage.close();
        } catch (Exception e) {
            System.err.println("Error occurred while opening Client Registration window: " + e.getMessage());
            e.printStackTrace();
        }
    }
}