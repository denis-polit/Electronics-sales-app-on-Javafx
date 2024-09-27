import javafx.application.Application;
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
import java.util.Optional;

public class ForgotClientData extends Application {

    private MenuPage menuPage;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Forgot Data");

        VBox welcomePane = createWelcomePane(primaryStage);
        VBox forgotDataForm = createForgotDataForm(primaryStage);

        HBox mainPane = new HBox();
        mainPane.getChildren().addAll(welcomePane, forgotDataForm);

        welcomePane.setPrefWidth(900 * 0.4);
        forgotDataForm.setPrefWidth(900 * 0.6);

        HBox.setHgrow(welcomePane, Priority.ALWAYS);
        HBox.setHgrow(forgotDataForm, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setCenter(mainPane);
        root.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
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

        ImageView profileIcon = new ImageView("file:icons/remember_icon.png");
        profileIcon.setFitHeight(120);
        profileIcon.setFitWidth(120);

        Label welcomeLabel = new Label("Remember");
        welcomeLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 30));
        welcomeLabel.setTextFill(Color.WHITE);

        Button backButton = ButtonStyle.expandPaneStyledButton("<");
        backButton.setOnAction(event -> {
            primaryStage.close();
            showPreviousStage();
        });
        backButton.setStyle("-fx-background-color: #7331FF; -fx-border-color: white; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 15px;");
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

    private VBox createForgotDataForm(Stage primaryStage) {
        VBox forgotDataContainer = new VBox(20);
        forgotDataContainer.setPadding(new Insets(100, 20, 150, 20));
        forgotDataContainer.setAlignment(Pos.CENTER);
        forgotDataContainer.setStyle("-fx-background-color: #07080A;");

        Label forgotDataLabel = new Label(" Forgot Data ");
        forgotDataLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 30));
        forgotDataLabel.setTextFill(Color.WHITE);
        VBox.setMargin(forgotDataLabel, new Insets(0, 0, 30, 0));

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
        Tooltip accountNameTooltip = new Tooltip("Enter your username");
        accountNameTooltip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        accountNameField.setTooltip(accountNameTooltip);

        TextField phoneNumberField = new TextField();
        phoneNumberField.setPromptText("User Phone Number");
        phoneNumberField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        Tooltip phoneNumberTooltip = new Tooltip("Enter your phone number");
        phoneNumberTooltip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        phoneNumberField.setTooltip(phoneNumberTooltip);

        TextField emailField = new TextField();
        emailField.setPromptText("User Email");
        emailField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        Tooltip emailTooltip = new Tooltip("Enter your email");
        emailTooltip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        emailField.setTooltip(emailTooltip);

        gridPane.addRow(0, new Region(), accountNameField, new Region());
        gridPane.addRow(1, new Region(), phoneNumberField, new Region());
        gridPane.addRow(2, new Region(), emailField, new Region());

        Button submitButton = ButtonStyle.expandPaneStyledButton("Submit");
        submitButton.setOnAction(event -> clientLogining(accountNameField, phoneNumberField, emailField, submitButton));

        forgotDataContainer.getChildren().addAll(forgotDataLabel, gridPane, submitButton);
        return forgotDataContainer;
    }

    private boolean checkClientForgotData(String accountName, String phoneNumber, String email) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT * FROM users WHERE user_name = ? AND user_phone_number = ? AND user_email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, accountName);
                pstmt.setString(2, phoneNumber);
                pstmt.setString(3, email);

                ResultSet resultSet = pstmt.executeQuery();
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to check login details. Please try again.");
        }
        return false;
    }

    private void clientLogining(TextField accountNameField, TextField phoneNumberField, TextField emailField, Button button) {
        String accountName = accountNameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String email = emailField.getText();

        if (accountName.isEmpty()) {
            alertService.showErrorAlert("Please enter your username.");
            return;
        }

        if (phoneNumber.isEmpty()) {
            alertService.showErrorAlert("Please enter your phone number.");
            return;
        }

        if (email.isEmpty()) {
            alertService.showErrorAlert("Please enter your email.");
            return;
        }

        boolean loggedIn = checkClientForgotData(accountName, phoneNumber, email);
        if (loggedIn) {
            SessionManager sessionManager = SessionManager.getInstance();
            sessionManager.setClientEnter(true);
            sessionManager.setCurrentClientName(accountName);

            int clientId = sessionManager.getClientIdByName(accountName);
            String details = String.format("Phone: %s, Email: %s", phoneNumber, email);
            sessionManager.logActivity(clientId, "Password Recovery", "Account", details);

            alertService.showErrorAlert("Login successful.");
            showClientDashboard();
        } else {
            alertService.showErrorAlert("Incorrect data");
        }
    }

    private void showClientDashboard() {
        try {
            if (SessionManager.getInstance().isClientEnter()) {
                String currentClientName = SessionManager.getInstance().getCurrentClientName();
                if (currentClientName != null && !currentClientName.isEmpty()) {
                    ClientAccountDetailsPage accountDetailsPage = new ClientAccountDetailsPage(currentClientName);
                    Stage accountDetailsStage = new Stage();
                    accountDetailsPage.start(accountDetailsStage);
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

    private void showPreviousStage() {
        Stage loginStage = new Stage();
        ClientLogin clientLogin = new ClientLogin();
        clientLogin.start(loginStage);
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
