import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuPage extends Application {
    private Stage primaryStage;
    private BorderPane root;
    String text4 = "Description";
    String text8 = "Instructions";
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public void init() {
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.root = new BorderPane();

        primaryStage.setTitle("Menu");

        init();

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(10));
        menuBox.setStyle("-fx-background-color: #000000;");

        int buttonMinWidth = 260;

        String managerName = SessionManager.getInstance().getCurrentManagerName();
        String employeeStatus = getEmployeeStatus(managerName);

        Button homeButton = new Button("1. Home page (Ctrl + 1)");
        setButtonProperties(homeButton, buttonMinWidth);
        homeButton.setOnAction(e -> showHomePage(primaryStage));

        Button reviewsButton = new Button("2. Reviews (Ctrl + 2)");
        setButtonProperties(reviewsButton, buttonMinWidth);
        reviewsButton.setOnAction(e -> showReviewsPage(primaryStage));

        Button servicesButton = new Button("3. Services (Ctrl + 3)");
        setButtonProperties(servicesButton, buttonMinWidth);
        if ("super_admin".equals(employeeStatus)) {
            servicesButton.setText("3. Orders And Negotiations (Ctrl + 3)");
            servicesButton.setOnAction(e -> showOrdersOrNegotiationsPage(primaryStage));
        } else {
            servicesButton.setOnAction(e -> showServicesPage(primaryStage));
        }

        Button descriptionButton = new Button(text4);
        setButtonProperties(descriptionButton, buttonMinWidth);

        if ("super_admin".equals(employeeStatus)) {
            descriptionButton.setText("4. Create Or Edit Accounts (Ctrl + 4)");
            descriptionButton.setOnAction(e -> showCreateEmployeeAccountPage(primaryStage));
        } else if ("admin".equals(employeeStatus)) {
            descriptionButton.setText("4. Logs List (Ctrl + 4)");
            descriptionButton.setOnAction(e -> showLogsHistoryWindow(primaryStage));
        } else if ("main_manager".equals(employeeStatus)) {
            descriptionButton.setText("4. Orders And Negotiations (Ctrl + 4)");
            descriptionButton.setOnAction(e -> showOrdersOrNegotiationsPage(primaryStage));
        } else {
            descriptionButton.setText("4. Description (Ctrl + 4)");
            descriptionButton.setOnAction(e -> showDescriptionPage(primaryStage));
        }

        Button accountButton = new Button("5. Personal account (Ctrl + 5)");
        setButtonProperties(accountButton, buttonMinWidth);
        accountButton.setOnAction(e -> showRegistrationWindow(primaryStage));

        Button explanationButton = new Button("6. Contracts list (Ctrl + 6)");
        setButtonProperties(explanationButton, buttonMinWidth);
        explanationButton.setOnAction(e -> showContractsPage(primaryStage));

        Button managersButton = new Button("7. Managers (Ctrl + 7)");
        setButtonProperties(managersButton, buttonMinWidth);
        managersButton.setOnAction(e -> showManagersListPage(primaryStage));

        Button clientsButton = new Button(text8);
        setButtonProperties(clientsButton, buttonMinWidth);
        if ("accountant".equals(employeeStatus)) {
            clientsButton.setText("8. Bookkeeping (Ctrl + 8)");
            clientsButton.setOnAction(e -> showBookkeepingPage(primaryStage));
        } else if ("super_admin".equals(employeeStatus) || "manager".equals(employeeStatus) || "main_manager".equals(employeeStatus)) {
            clientsButton.setText("8. Clients List (Ctrl + 8)");
            clientsButton.setOnAction(e -> showClientsPage(primaryStage));
        } else {
            clientsButton.setText("8. User Instructions (Ctrl + 8)");
            clientsButton.setOnAction(e -> showInstructionsPage(primaryStage));
        }

        Button statsButton = new Button("9. Market statistics (Ctrl + 9)");
        setButtonProperties(statsButton, buttonMinWidth);
        statsButton.setOnAction(e -> showStatisticsPage(primaryStage));

        Button catalogButton = new Button("10. Catalog (Ctrl + 0)");
        setButtonProperties(catalogButton, buttonMinWidth);
        catalogButton.setOnAction(e -> showCatalogPage(primaryStage));

        Button instructionsButton = new Button();
        instructionsButton.setText(SessionManager.getInstance().isManagerEnter() ? "11. Employer Instructions (Ctrl + -)" : "11. Other Our Apps (Ctrl + -)");
        setButtonProperties(instructionsButton, buttonMinWidth);
        instructionsButton.setOnAction(e -> {
            if (SessionManager.getInstance().isManagerEnter()) {
                showEmployeerInstructionsPage(primaryStage);
            } else {
                showOtherAppsPage(primaryStage);
            }
        });

        Button logsHistoryButton = new Button("Logs History Window (Ctrl + L)");
        setButtonProperties(logsHistoryButton, buttonMinWidth);
        logsHistoryButton.setOnAction(e -> showLogsHistoryWindow(primaryStage));

        if ("super_admin".equals(employeeStatus) || "admin".equals(employeeStatus)) {
            menuBox.getChildren().add(logsHistoryButton);
        }

        menuBox.getChildren().addAll(
                homeButton, reviewsButton, servicesButton, descriptionButton, accountButton,
                explanationButton, managersButton, clientsButton, statsButton, catalogButton,
                instructionsButton
        );

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(menuBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(600);
        scrollPane.setStyle("-fx-background: black;");

        Scene menuScene = new Scene(scrollPane, 450, 600);
        primaryStage.setScene(menuScene);
        primaryStage.show();

        addHotkeys(menuScene);

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

    public void addHotkeys(Scene scene) {
        scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.M, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                showMenu();
            } else if (new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                showSupportWindow();
            } else if (new KeyCodeCombination(KeyCode.T, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                showPrivacyPolicyWindow();
            } else if (new KeyCodeCombination(KeyCode.P, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                showRegistrationWindow(primaryStage);
            } else if (event.getCode() == KeyCode.F5) {
                System.out.println("scene updated");
                HotKeysHandler.updatePage(primaryStage, scene, true);
            } else if (new KeyCodeCombination(KeyCode.TAB, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                switchToHotKeysClientInfoPage();
            } else if (new KeyCodeCombination(KeyCode.TAB, KeyCodeCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN).match(event)) {
                if ("super_admin".equals(getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    lastButtonChoose(primaryStage);
                } else {
                    switchToHotKeysEmployeeInfoPage();
                }
            } else if (new KeyCodeCombination(KeyCode.I, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                showInstructionsPage(primaryStage);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                HotKeysHandler.showConfirmationDialog(primaryStage);
            } else if (event.isControlDown() && event.getText().equals("1")) {
                showWindowByNumber(1);
            } else if (event.isControlDown() && event.getText().equals("2")) {
                showWindowByNumber(2);
            } else if (event.isControlDown() && event.getText().equals("3")) {
                showWindowByNumber(3);
            } else if (event.isControlDown() && event.getText().equals("4")) {
                showWindowByNumber(4);
            } else if (event.isControlDown() && event.getText().equals("5")) {
                showWindowByNumber(5);
            } else if (event.isControlDown() && event.getText().equals("6")) {
                showWindowByNumber(6);
            } else if (event.isControlDown() && event.getText().equals("7")) {
                showWindowByNumber(7);
            } else if (event.isControlDown() && event.getText().equals("8")) {
                showWindowByNumber(8);
            } else if (event.isControlDown() && event.getText().equals("9")) {
                showWindowByNumber(9);
            } else if (event.isControlDown() && event.getText().equals("0")) {
                showWindowByNumber(0);
            } else if (event.isControlDown() && event.getText().equals("-")) {
                showWindowByNumber(-1);
            } else if (new KeyCodeCombination(KeyCode.F, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                HotKeysHandler.showSearchDialog(primaryStage);
            }
        });
    }

    private void showWindowByNumber(int windowNumber) {
        switch (windowNumber) {
            case 1:
                showHomePage(primaryStage);
                break;
            case 2:
                showReviewsPage(primaryStage);
                break;
            case 3:
                if ("super_admin".equals(getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    showOrdersOrNegotiationsPage(primaryStage);
                } else {
                    showServicesPage(primaryStage);
                }
                break;
            case 4:
                if ("super_admin".equals(getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    showCreateEmployeeAccountPage(primaryStage);
                } if ("main_manager".equals(getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    showOrdersOrNegotiationsPage(primaryStage);
                } else {
                    showDescriptionPage(primaryStage);
                }
                break;
            case 5:
                showRegistrationWindow(primaryStage);
                break;
            case 6:
                showContractsPage(primaryStage);
                break;
            case 7:
                showManagersListPage(primaryStage);
                break;
            case 8:
                if ("accountant".equals(getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    showBookkeepingPage(primaryStage);
                } else if ("super_admin".equals(getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName())) || "manager".equals(getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    showClientsPage(primaryStage);
                } else if ("main_manager".equals(getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    showClientsPage(primaryStage);
                } else if ("manager".equals(getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    showClientsPage(primaryStage);
                } else {
                    showInstructionsPage(primaryStage);
                }
                break;
            case 9:
                showStatisticsPage(primaryStage);
                break;
            case 0:
                showCatalogPage(primaryStage);
                break;
            case -1:
                if (SessionManager.getInstance().isManagerEnter()) {
                    showEmployeerInstructionsPage(primaryStage);
                } else {
                    showOtherAppsPage(primaryStage);
                }
                break;
            default:
                break;
        }
    }

    public void switchToHotKeysClientInfoPage() {
        HotKeysClientInfoPage clientHotKeysInfoPage = new HotKeysClientInfoPage();
        Stage clientHotKeysInfoStage = new Stage();
        clientHotKeysInfoPage.start(clientHotKeysInfoStage);
    }

    public void switchToHotKeysEmployeeInfoPage() {
        HotKeysEmployeeInfoPage employeeHotKeysInfoPage = new HotKeysEmployeeInfoPage();
        Stage employeeHotKeysInfoStage = new Stage();
        employeeHotKeysInfoPage.start(employeeHotKeysInfoStage);
    }

    public void showRegistrationMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Required");
        alert.setHeaderText(null);
        alert.setContentText("You need to register or log in before accessing this page.");
        alert.showAndWait();
    }

    public String getEmployeeStatus(String managerName) {
        try (Connection connection = establishDBConnection()) {
            String query = "SELECT employee_status FROM managers WHERE manager_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, managerName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("employee_status");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void showMenu() {
        Stage menuStage = new Stage();
        MenuPage menuPage = new MenuPage();
        menuPage.start(menuStage);
    }

    public void showSupportWindow() {
        SupportWindow supportWindow = new SupportWindow();
        Stage supportStage = new Stage();
        supportWindow.start(supportStage);
        supportStage.show();
    }

    public void showPrivacyPolicyWindow() {
        PrivacyPolicyWindow privacyPolicyWindow = new PrivacyPolicyWindow();
        Stage privacyStage = new Stage();
        privacyPolicyWindow.start(privacyStage);
        privacyStage.show();
    }

    public void showHomePage(Stage currentStage) {
        try {
            currentStage.close();
            MainServiceApp mainServiceApp = new MainServiceApp();
            Stage mainPageStage = new Stage();
            mainServiceApp.start(mainPageStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showReviewsPage(Stage currentStage) {
        currentStage.close();
        ReviewsPage reviewsPage = new ReviewsPage();
        Stage reviewsStage = new Stage();
        reviewsPage.start(reviewsStage);
    }

    public void showServicesPage(Stage currentStage) {
        currentStage.close();
        ServicesPage servicesPage = new ServicesPage();
        Stage servicesStage = new Stage();
        servicesPage.start(servicesStage);
    }

    public void showDescriptionPage(Stage currentStage) {
        currentStage.close();
        DescriptionPage descriptionPage = new DescriptionPage();
        Stage descriptionStage = new Stage();
        descriptionPage.start(descriptionStage);
    }

    public void showCreateEmployeeAccountPage(Stage currentStage) {
        currentStage.close();
        AccountManagementPage accountManagementPage = new AccountManagementPage();
        Stage createEmployeeStage = new Stage();
        accountManagementPage.start(createEmployeeStage);
    }

    public void showRegistrationWindow(Stage currentStage) {
        currentStage.close();
        RegistrationWindow registrationWindow = new RegistrationWindow(root);
        Stage registrationStage = new Stage();
        registrationWindow.start(registrationStage);
    }

    public void showContractsPage(Stage currentStage) {
        if (SessionManager.getInstance().isManagerEnter() || SessionManager.getInstance().isClientEnter()) {
            currentStage.close();
            ContractsWindow contractsWindow = new ContractsWindow();
            Stage contractsStage = new Stage();
            contractsWindow.start(contractsStage);
        } else {
            showLoginMessage();
        }
    }

    public void showManagersListPage(Stage currentStage) {
        if (SessionManager.getInstance().isManagerEnter() && SessionManager.getInstance().getCurrentManagerName() != null) {
            currentStage.close();
            ManagersListPage managersListPage = new ManagersListPage();
            Stage managersListStage = new Stage();
            managersListPage.start(managersListStage);
        } else if (SessionManager.getInstance().isClientEnter() && SessionManager.getInstance().getCurrentClientName() != null) {
            currentStage.close();
            ManagersListPage managersListPage = new ManagersListPage();
            Stage managersListStage = new Stage();
            managersListPage.start(managersListStage);
        } else {
            showRegistrationMessage();
        }
    }

    public void showBookkeepingPage(Stage currentStage) {
        currentStage.close();
        BookkeepingPage bookkeepingPage = new BookkeepingPage();
        Stage bookkeepingStage = new Stage();
        bookkeepingPage.start(bookkeepingStage);
    }

    public void showInstructionsPage(Stage currentStage) {
        currentStage.close();
        UserInstructionsPage instructionsPage = new UserInstructionsPage();
        Stage instructionsStage = new Stage();
        instructionsPage.start(instructionsStage);
    }

    public void showClientsPage(Stage currentStage) {
        currentStage.close();
        ClientsPage clientsPage = new ClientsPage();
        Stage clientsStage = new Stage();
        clientsPage.start(clientsStage);
    }

    public void showOrdersOrNegotiationsPage(Stage currentStage) {
        currentStage.close();
        OrdersOrNegotiationsPage ordersOrNegotiations = new OrdersOrNegotiationsPage();
        Stage ordersOrNegotiationsStage = new Stage();
        ordersOrNegotiations.start(ordersOrNegotiationsStage);
    }

    public void showLogsHistoryWindow(Stage currentStage) {
        currentStage.close();
        LogsHistoryWindow logsHistoryWindow = new LogsHistoryWindow();
        try {
            Stage logsHistoryStage = new Stage();
            logsHistoryWindow.start(logsHistoryStage);
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error opening logs history window: " + e.getMessage());
        }
    }

    public void showStatisticsPage(Stage currentStage) {
        currentStage.close();
        StatisticsPage statisticsPage = new StatisticsPage();
        Stage statisticsStage = new Stage();
        statisticsPage.start(statisticsStage);
    }

    public void showCatalogPage(Stage currentStage) {
        currentStage.close();
        CatalogPage catalogPage = new CatalogPage();
        Stage catalogStage = new Stage();
        catalogPage.start(catalogStage);
    }

    public void showEmployeerInstructionsPage(Stage currentStage) {
        currentStage.close();
        EmployeerInstructionsPage employeerInstructionsPage = new EmployeerInstructionsPage();
        Stage employeerInstructionsStage = new Stage();
        employeerInstructionsPage.start(employeerInstructionsStage);
    }

    public void showOtherAppsPage(Stage currentStage) {
        currentStage.close();
        OtherAppsPage otherAppsPage = new OtherAppsPage();
        Stage otherAppsStage = new Stage();
        otherAppsPage.start(otherAppsStage);
    }

    public void showHotkeysInfoPage(Stage currentStage) {
        currentStage.close();
        Stage hotkeysInfoStage = new Stage();
        HotKeysClientInfoPage hotkeysInfoPage = new HotKeysClientInfoPage();
        hotkeysInfoPage.start(hotkeysInfoStage);
    }

    public void lastButtonChoose(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Choose Window");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to open the Services window, the Hotkeys window, or the Description window?");

        ButtonType servicesButton = new ButtonType("Services Page");
        ButtonType hotkeysButton = new ButtonType("Hotkeys Info");
        ButtonType descriptionButton = new ButtonType("Description Page");

        alert.getButtonTypes().setAll(servicesButton, hotkeysButton, descriptionButton);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == servicesButton) {
                showServicesPage(primaryStage);
            } else if (buttonType == hotkeysButton) {
                showHotkeysInfoPage(primaryStage);
            } else if (buttonType == descriptionButton) {
                showDescriptionPage(primaryStage);
            }
        });
    }

    public void showLoginMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Authorization Required");
        alert.setHeaderText(null);
        alert.setContentText("You need to log in before accessing this page.");
        alert.showAndWait();
    }

    private void setButtonProperties(Button button, double minWidth) {
        button.setMinWidth(minWidth);
        button.setTextFill(javafx.scene.paint.Color.WHITE);
        button.setFont(javafx.scene.text.Font.font("Open Sans", FontWeight.NORMAL, 14));
        button.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 1px; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        button.setOpacity(1.0);

        FadeTransition colorIn = new FadeTransition(Duration.millis(300), button);
        colorIn.setToValue(1.0);

        FadeTransition colorOut = new FadeTransition(Duration.millis(300), button);
        colorOut.setToValue(0.7);

        button.setOnMouseEntered(e -> {
            colorIn.play();
            button.setStyle("-fx-background-color: #7331FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        });

        button.setOnMouseExited(e -> {
            colorOut.play();
            button.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: white; -fx-border-radius: 15px;");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
