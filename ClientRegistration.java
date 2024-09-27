import javafx.application.Application;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientRegistration extends Application {

    private Stage primaryStage;
    private Stage clientLoginStage;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        MenuPage menuPage = new MenuPage();
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.hide();
        });

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 900, 600);

        HBox mainPane = new HBox();

        VBox welcomePane = createWelcomePane(primaryStage);
        VBox registrationForm = createClientRegistrationForm();

        mainPane.getChildren().addAll(welcomePane, registrationForm);

        welcomePane.setPrefWidth(scene.getWidth() * 0.4);
        registrationForm.setPrefWidth(scene.getWidth() * 0.6);

        HBox.setHgrow(welcomePane, Priority.SOMETIMES);
        HBox.setHgrow(registrationForm, Priority.SOMETIMES);

        root.setCenter(mainPane);
        root.setStyle("-fx-background-color: black;");
        scene.setFill(Color.TRANSPARENT);

        primaryStage.setTitle("Registration Form");
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

        Label registerLabel = new Label("If you don't have an account yet-\n                     Login");
        registerLabel.setOnMouseClicked(event -> showClientLogin());
        registerLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 20));
        registerLabel.setTextFill(Color.DARKGRAY);

        welcomePane.getChildren().addAll(profileIcon, welcomeLabel, registerLabel, backButton);

        VBox.setMargin(backButton, new Insets(50, 0, 0, 0));
        VBox.setMargin(profileIcon, new Insets(0, 0, 50, 0));
        VBox.setMargin(welcomeLabel, new Insets(0, 0, 10, 0));

        return welcomePane;
    }

    private VBox createClientRegistrationForm() {
        VBox clientFormContainer = new VBox(20);
        clientFormContainer.setPadding(new Insets(100, 20, 150, 20));
        clientFormContainer.setAlignment(Pos.CENTER);
        clientFormContainer.setStyle("-fx-background-color: #07080A;");

        Label signUpLabel = new Label(" Sign Up ");
        signUpLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 30));
        signUpLabel.setTextFill(Color.WHITE);
        VBox.setMargin(signUpLabel, new Insets(0, 0, 30, 0));

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
        accountNameTooltip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        accountNameField.setTooltip(accountNameTooltip);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("User Password");
        passwordField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        passwordField.setPrefHeight(40);

        Tooltip passwordTooltip = new Tooltip("Enter your password");
        passwordTooltip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        passwordField.setTooltip(passwordTooltip);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        confirmPasswordField.setPrefHeight(40);

        Tooltip confirmPasswordTooltip = new Tooltip("Enter your password again");
        confirmPasswordTooltip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        confirmPasswordField.setTooltip(confirmPasswordTooltip);

        TextField phoneNumberField = new TextField();
        phoneNumberField.setPromptText("User Phone Number");
        phoneNumberField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        phoneNumberField.setPrefHeight(40);

        Tooltip phoneNumberTooltip = new Tooltip("Enter your phone number");
        phoneNumberTooltip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        phoneNumberField.setTooltip(phoneNumberTooltip);

        TextField emailField = new TextField();
        emailField.setPromptText("User Email");
        emailField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        emailField.setPrefHeight(40);

        Tooltip emailTooltip = new Tooltip("Enter your email");
        emailTooltip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        emailField.setTooltip(emailTooltip);

        gridPane.addRow(0, new Region(), accountNameField, new Region());
        gridPane.addRow(1, new Region(), passwordField, new Region());
        gridPane.addRow(2, new Region(), confirmPasswordField, new Region());
        gridPane.addRow(3, new Region(), phoneNumberField, new Region());
        gridPane.addRow(4, new Region(), emailField, new Region());

        Button registerButton = ButtonStyle.expandPaneStyledButton("Register as Client");
        registerButton.setOnAction(event -> clientRegistration(clientFormContainer, accountNameField, passwordField, confirmPasswordField, phoneNumberField, emailField, registerButton));
        registerButton.setPrefWidth(200);
        VBox.setMargin(registerButton, new Insets(20, 0, 20, 0));

        clientFormContainer.getChildren().addAll(signUpLabel, gridPane, registerButton);
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

    private void clearFields(TextField accountNameField, PasswordField passwordField, PasswordField confirmPasswordField, TextField phoneNumberField, TextField emailField) {
        accountNameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        phoneNumberField.clear();
        emailField.clear();
    }

    private List<String> validateForm(boolean isRegistration, String user_name, String password, String confirmPassword, String user_phone_number, String user_email) {
        List<String> errorMessages = new ArrayList<>();

        if (user_name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || user_phone_number.isEmpty() || user_email.isEmpty()) {
            errorMessages.add("All fields are required");
        }

        if (isRegistration && !password.equals(confirmPassword)) {
            errorMessages.add("Passwords do not match");
        }

        if (!isValidPhoneNumber(user_phone_number)) {
            errorMessages.add("Invalid phone number format");
        }

        if (!isValidEmail(user_email)) {
            errorMessages.add("Invalid email format");
        }

        if (!isValidPassword(password)) {
            errorMessages.add("Invalid password format");
        }

        return errorMessages;
    }

    private boolean isValidPhoneNumber(String user_phone_number) {
        Pattern pattern = Pattern.compile("^\\+(?:[0-9] ?){6,14}[0-9]$");

        Matcher matcher = pattern.matcher(user_phone_number);
        return matcher.matches();
    }

    private boolean isValidEmail(String user_email) {
        Pattern pattern = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
        Matcher matcher = pattern.matcher(user_email);
        return matcher.matches();
    }

    private boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{6,}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private void clientRegistration(VBox formContainer, TextField accountNameField, PasswordField passwordField, PasswordField confirmPasswordField, TextField phoneNumberField, TextField emailField, Button button) {
        String user_name = accountNameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String user_phone_number = phoneNumberField.getText();
        String user_email = emailField.getText();

        if (user_name.isEmpty()) {
            showErrorMessageAndResetFields(Collections.singletonList("Please enter your username"), formContainer);
            return;
        }

        if (password.isEmpty()) {
            showErrorMessageAndResetFields(Collections.singletonList("Please enter your password"), formContainer);
            return;
        }

        if (confirmPassword.isEmpty()) {
            showErrorMessageAndResetFields(Collections.singletonList("Please confirm your password"), formContainer);
            return;
        }

        if (user_phone_number.isEmpty()) {
            showErrorMessageAndResetFields(Collections.singletonList("Please enter your phone number"), formContainer);
            return;
        }

        if (user_email.isEmpty()) {
            showErrorMessageAndResetFields(Collections.singletonList("Please enter your email"), formContainer);
            return;
        }

        List<String> errorMessages = validateForm(true, user_name, password, confirmPassword, user_phone_number, user_email);
        if (!errorMessages.isEmpty()) {
            showErrorMessageAndResetFields(errorMessages, formContainer);
            return;
        }

        if (isAccountBlocked(user_name)) {
            showErrorMessageAndResetFields(Collections.singletonList("Account name is blocked. Please choose another username."), formContainer);
            return;
        }

        if (isPhoneNumberBlocked(user_phone_number)) {
            showErrorMessageAndResetFields(Collections.singletonList("Phone number is blocked. Please use another phone number."), formContainer);
            return;
        }

        if (isEmailBlocked(user_email)) {
            showErrorMessageAndResetFields(Collections.singletonList("Email is blocked. Please use another email address."), formContainer);
            return;
        }

        if (isAccountNameTaken(user_name)) {
            showErrorMessageAndResetFields(Collections.singletonList("Account name is already taken"), formContainer);
            return;
        }

        if (isPhoneNumberTaken(user_phone_number)) {
            showErrorMessageAndResetFields(Collections.singletonList("Phone number is already registered"), formContainer);
            return;
        }

        if (isEmailTaken(user_email)) {
            showErrorMessageAndResetFields(Collections.singletonList("Email is already registered"), formContainer);
            return;
        }

        if (saveClientData(user_name, password, user_phone_number, user_email)) {
            showSuccessMessage(button);

            int userId = SessionManager.getInstance().getClientIdByName(user_name);
            SessionManager.getInstance().logActivity(userId, "Registration", "User", "Registered successfully with phone: " + user_phone_number + ", email: " + user_email);

            SessionManager.getInstance().setClientEnter(true);
            SessionManager.getInstance().setCurrentClientName(user_name);

            clearFields(accountNameField, passwordField, confirmPasswordField, phoneNumberField, emailField);

            showClientDashboard(primaryStage);
        } else {
            showErrorMessage("Failed to register user. Please try again later.");
        }
    }

    private void showClientDashboard(Stage primaryStage) {
        try {
            if (SessionManager.getInstance().isClientEnter()) {
                String currentClientName = SessionManager.getInstance().getCurrentClientName();
                if (currentClientName != null && !currentClientName.isEmpty()) {
                    Stage accountDetailsStage = new Stage();
                    ClientAccountDetailsPage accountDetailsPage = new ClientAccountDetailsPage(currentClientName);
                    accountDetailsPage.start(accountDetailsStage);

                    primaryStage.close();
                } else {
                    showLoginRequiredAlert();
                }
            } else {
                showLoginRequiredAlert();
            }
        } catch (Exception e) {
            System.err.println("An error occurred while showing client dashboard: " + e.getMessage());
            e.printStackTrace();
        }
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

    private boolean isAccountBlocked(String user_name) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT * FROM blocked_data WHERE name = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setString(1, user_name);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error checking account block status. Please try again later.");
        }
        return false;
    }

    private boolean isPhoneNumberBlocked(String user_phone_number) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT * FROM blocked_data WHERE phone_number = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setString(1, user_phone_number);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error checking phone number block status. Please try again later.");
        }
        return false;
    }

    private boolean isEmailBlocked(String user_email) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT * FROM blocked_data WHERE email = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setString(1, user_email);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error checking email block status. Please try again later.");
        }
        return false;
    }

    private boolean isPhoneNumberTaken(String user_phone_number) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT * FROM users WHERE user_phone_number = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setString(1, user_phone_number);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error checking phone number availability. Please try again later.");
        }
        return false;
    }

    private boolean isEmailTaken(String user_email) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT * FROM users WHERE user_email = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setString(1, user_email);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error checking email availability. Please try again later.");
        }
        return false;
    }

    private boolean isAccountNameTaken(String user_name) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT * FROM users WHERE user_name = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setString(1, user_name);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Error checking account name availability. Please try again later.");
        }
        return false;
    }

    private boolean saveClientData(String user_name, String password, String user_phone_number, String user_email) {
        try (Connection conn = establishDBConnection()) {
            String query = "INSERT INTO users (user_name, password, user_phone_number, user_email) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setString(1, user_name);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, user_phone_number);
                preparedStatement.setString(4, user_email);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Data inserted successfully.");
                    return true;
                } else {
                    System.out.println("Failed to insert data.");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void showClientLogin() {
        if (clientLoginStage == null) {
            clientLoginStage = new Stage();
            ClientLogin clientLogin = new ClientLogin();
            try {
                clientLogin.start(clientLoginStage);
                primaryStage.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            clientLoginStage.show();
            primaryStage.close();
        }
    }
}
