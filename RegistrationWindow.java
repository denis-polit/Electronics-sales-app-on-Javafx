import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class RegistrationWindow extends Application {
    private Stage primaryStage;
    private Stage clientLoginStage;
    private Stage clientRegistrationStage;
    private Stage managerLoginStage;
    private MenuPage menuPage;
    private SessionManager sessionManager = SessionManager.getInstance();
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public RegistrationWindow(BorderPane root) {
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        boolean clientEnter = sessionManager.isClientEnter();
        boolean managerEnter = sessionManager.isManagerEnter();
        String currentClientName = sessionManager.getCurrentClientName();
        String currentManagerName = sessionManager.getCurrentManagerName();

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: black;");

        String profileImagePath;
        if (managerEnter && currentManagerName != null) {
            profileImagePath = "file:icons/employee_profile_icon.png";
        } else if (clientEnter && currentClientName != null) {
            profileImagePath = "file:icons/profile_icon.png";
        } else {
            profileImagePath = "file:icons/not_auth_profile_icon.png";
        }

        ImageView profileIcon = new ImageView(profileImagePath);
        profileIcon.setFitHeight(140);
        profileIcon.setFitWidth(140);
        VBox.setMargin(profileIcon, new Insets(30, 0, 60, 0));

        Button loginButton = ButtonStyle.expandPaneStyledButton("Login as Client");
        Button clientRegisterButton = ButtonStyle.expandPaneStyledButton("Register as Client");
        Button managerLoginButton = ButtonStyle.expandPaneStyledButton("Login as Manager");
        Button myAccountButton = ButtonStyle.expandPaneStyledButton("My Account");
        Button logoutButton = ButtonStyle.expandPaneStyledButton("Logout from account");

        setButtonMinWidth(loginButton);
        setButtonMinWidth(clientRegisterButton);
        setButtonMinWidth(managerLoginButton);
        setButtonMinWidth(logoutButton);
        setButtonMinWidth(myAccountButton);

        loginButton.setOnAction(event -> showClientLogin());
        clientRegisterButton.setOnAction(event -> showClientRegistrationWindow());
        managerLoginButton.setOnAction(event -> showManagerLoginWindow());
        logoutButton.setOnAction(event -> logout());
        myAccountButton.setOnAction(event -> showUserDashboard());

        vbox.getChildren().addAll(profileIcon, loginButton, clientRegisterButton, managerLoginButton, myAccountButton, logoutButton);

        Scene scene = new Scene(vbox, 450, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Account Options");
        primaryStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();
    }

    private void setButtonMinWidth(Button button) {
        int buttonMinWidth = 260;
        button.setMinWidth(buttonMinWidth);
    }

    private void showClientLogin() {
        if (sessionManager.isClientEnter() || sessionManager.isManagerEnter()) {
            alertService.showInfoAlert("Already Logged In", null, "You are already logged in. Please log out from the current account before logging in again.");
        } else {
            if (clientLoginStage == null) {
                clientLoginStage = new Stage();
                ClientLogin clientLogin = new ClientLogin();
                clientLogin.start(clientLoginStage);
                primaryStage.close();
            } else {
                clientLoginStage.show();
            }
        }
    }

    private void showClientRegistrationWindow() {
        if (sessionManager.isClientEnter() || sessionManager.isManagerEnter()) {
            alertService.showInfoAlert("Already Logged In", null, "You are already logged in. Please log out from the current account before registering a new client.");
        } else {
            try {
                if (clientRegistrationStage == null) {
                    clientRegistrationStage = new Stage();
                    ClientRegistration clientRegistration = new ClientRegistration();
                    clientRegistration.start(clientRegistrationStage);
                    primaryStage.close();
                } else {
                    clientRegistrationStage.show();
                }
            } catch (Exception e) {
                System.err.println("Error occurred while opening Client Registration window: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showManagerLoginWindow() {
        if (sessionManager.isClientEnter() || sessionManager.isManagerEnter()) {
            alertService.showInfoAlert("Already Logged In", null, "You are already logged in. Please log out from the current account before logging in as a manager.");
        } else {
            try {
                if (managerLoginStage == null) {
                    managerLoginStage = new Stage();
                    ManagerLogin managerLogin = new ManagerLogin();
                    managerLogin.start(managerLoginStage);
                    primaryStage.close();
                } else {
                    managerLoginStage.show();
                }
            } catch (Exception e) {
                System.err.println("Error occurred while opening Manager Login window: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showUserDashboard() {
        if (sessionManager.isClientEnter() || sessionManager.isManagerEnter()) {
            if (sessionManager.isClientEnter()) {
                System.out.println("Opening client dashboard");
                showClientDashboard();
                primaryStage.close();
            } else if (sessionManager.isManagerEnter()) {
                System.out.println("Opening manager dashboard");
                showManagerDashboard();
                primaryStage.close();
            }
        } else {
            alertService.showInfoAlert("Access Denied", null, "Please log in or register to access your account.");
        }
    }

    private void showClientDashboard() {
        try {
            if (sessionManager.isClientEnter() && sessionManager.getCurrentClientName() != null
                    && !sessionManager.getCurrentClientName().isEmpty()) {
                ClientAccountDetailsPage accountDetailsPage = new ClientAccountDetailsPage(sessionManager.getCurrentClientName());
                Stage accountDetailsStage = new Stage();
                accountDetailsPage.start(accountDetailsStage);
            } else {
                showLoginAlert("client");
            }
        } catch (Exception e) {
            handleDashboardError("client", e);
        }
    }

    private void showManagerDashboard() {
        try {
            if (sessionManager.isManagerEnter() && sessionManager.getCurrentManagerName() != null
                    && !sessionManager.getCurrentManagerName().isEmpty()) {
                ManagerAccountDetailsPage accountDetailsPage = new ManagerAccountDetailsPage();
                accountDetailsPage.init(sessionManager.getCurrentManagerName(), sessionManager.isManagerEnter());
                Stage accountDetailsStage = new Stage();
                accountDetailsPage.start(accountDetailsStage);
            } else {
                showLoginAlert("manager");
            }
        } catch (Exception e) {
            handleDashboardError("manager", e);
        }
    }

    private void showLoginAlert(String userType) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Login Required");
        alert.setContentText("Please log in as " + userType + " to access your account.");

        ButtonType loginButton = new ButtonType("Login");
        alert.getButtonTypes().setAll(loginButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == loginButton) {
            alert.close();
        }
    }

    private void handleDashboardError(String userType, Exception e) {
        System.err.println("An error occurred while showing " + userType + " dashboard: " + e.getMessage());
        e.printStackTrace();
    }

    private void logout() {
        SessionManager sessionManager = SessionManager.getInstance();

        if (!sessionManager.isClientEnter() && !sessionManager.isManagerEnter()) {
            alertService.showInfoAlert("Already Logged Out", null, "You are already logged out.");
        } else {
            String actionType = "Logout";
            String objectType = "Session";
            String details = "User logged out";

            if (sessionManager.isClientEnter()) {
                int clientId = sessionManager.getClientIdByName(sessionManager.getCurrentClientName());
                sessionManager.logActivity(clientId, actionType, objectType, details);
            }

            if (sessionManager.isManagerEnter()) {
                int managerId = sessionManager.getEmployeeIdByName(sessionManager.getCurrentManagerName());
                sessionManager.logManagerActivity(managerId, actionType, objectType, details);
            }

            sessionManager.setClientEnter(false);
            sessionManager.setManagerEnter(false);
            sessionManager.setCurrentClientName(null);
            sessionManager.setCurrentManagerName(null);
            alertService.showInfoAlert("Logout Successful", null, "You are now logged out.");
        }
    }
}
