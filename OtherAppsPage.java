import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class OtherAppsPage extends Application {
    private BorderPane root;
    private SessionManager sessionManager = SessionManager.getInstance();
    private Stage primaryStage;
    private FirstConnectionToDataBase connectionToDataBase;
    private Connection connection;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.root = new BorderPane();
        MenuPage menuPage = new MenuPage();

        VBox layout = new VBox();
        layout.setStyle("-fx-background-color: black");
        layout.setPadding(new Insets(10));
        layout.setSpacing(10);

        HeaderComponent headerComponent = new HeaderComponent();
        Node header = headerComponent.createHeader();
        layout.getChildren().add(header);

        VBox otherAppsContainer = new VBox();
        otherAppsContainer.setPadding(new Insets(10));
        otherAppsContainer.setSpacing(10);
        otherAppsContainer.setStyle("-fx-background-color: black");

        Label otherAppsLabel = createBoldLabel("Other Apps:", "-fx-font-size: 18px;");

        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
            connection = connectionToDataBase.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Connection is null or closed.");
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
            return;
        }

        if (otherAppsAvailable()) {
            Button subscribeButton = new Button("Subscribe to Newsletter");
            subscribeButton.setOnAction(e -> subscribeToNewsletter());
            otherAppsContainer.getChildren().addAll(otherAppsLabel, subscribeButton);
        } else {
            Label noAppsLabel = createBoldLabel("Are there other apps: No", "-fx-font-size: 14px;");
            otherAppsContainer.getChildren().addAll(otherAppsLabel, noAppsLabel);
        }

        layout.getChildren().add(otherAppsContainer);

        Scene scene = new Scene(layout, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Other Apps");
        primaryStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();
    }

    private Connection establishDBConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (connectionToDataBase != null) {
                connection = connectionToDataBase.getConnection();
                if (connection == null || connection.isClosed()) {
                    throw new SQLException("Connection is null or closed.");
                }
            } else {
                throw new SQLException("Database connection is not initialized.");
            }
        }
        return connection;
    }

    private boolean otherAppsAvailable() {
        try (Connection connection = establishDBConnection()) {
            String query = "SELECT COUNT(*) AS count FROM apps";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt("count");
                        return count > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void subscribeToNewsletter() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Subscribe to Newsletter");
        alert.setHeaderText(null);
        alert.setContentText("You have successfully subscribed to our newsletter!");
        alert.showAndWait();
    }

    private Label createBoldLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style + "; -fx-text-fill: white;");
        label.setFont(Font.font("Gotham", FontWeight.BOLD, 16));
        return label;
    }

    private void addNewApp() {
        if (SessionManager.getInstance().isManagerEnter()) {
            String managerName = SessionManager.getInstance().getCurrentManagerName();
            String employeeStatus = getEmployeeStatus(managerName);
            if ("super_admin".equals(employeeStatus)) {
                openAddAppForm();
            } else {
                alertService.showErrorAlert("You don't have permission to add apps. Please contact support.");
            }
        } else {
            alertService.showErrorAlert("You need to login as a manager to add apps.");
        }
    }

    private void openAddAppForm() {
        Stage addAppStage = new Stage();
        addAppStage.setTitle("Add a New App");

        TextField appNameField = new TextField();
        TextArea appDescriptionArea = new TextArea();
        DatePicker appDateAddedPicker = new DatePicker();
        TextField appUrlField = new TextField();

        Label nameLabel = new Label("App Name:");
        Label descriptionLabel = new Label("Description:");
        Label dateAddedLabel = new Label("Date Added:");
        Label urlLabel = new Label("App URL:");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String appName = appNameField.getText();
            String appDescription = appDescriptionArea.getText();
            LocalDate appDateAdded = appDateAddedPicker.getValue();
            String appUrl = appUrlField.getText();

            saveAppToDatabase(appName, appDescription, appDateAdded, appUrl);
            addAppStage.close();
            refreshPage();
        });

        VBox formLayout = new VBox(10);
        formLayout.getChildren().addAll(nameLabel, appNameField, descriptionLabel, appDescriptionArea, dateAddedLabel, appDateAddedPicker, urlLabel, appUrlField, saveButton);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setPadding(new Insets(20));

        Scene scene = new Scene(formLayout, 400, 350);
        addAppStage.setScene(scene);
        addAppStage.show();
    }

    private void saveAppToDatabase(String appName, String appDescription, LocalDate appDateAdded, String appUrl) {
        try (Connection connection = establishDBConnection()) {
            String query = "INSERT INTO apps (app_name, app_description, app_date_added, app_url) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, appName);
                statement.setString(2, appDescription);
                statement.setDate(3, Date.valueOf(appDateAdded));
                statement.setString(4, appUrl);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private void refreshPage() {
        try {
            start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class HeaderComponent {

        private VBox createHeader() {
            VBox header = new VBox(10);
            header.setPadding(new Insets(10));
            header.setStyle("-fx-background-color: black");

            Image logoImage = new Image("file:icons/LOGO_our.jpg");
            ImageView logoImageView = new ImageView(logoImage);
            logoImageView.setFitWidth(50);
            logoImageView.setFitHeight(50);

            Circle logoCircle = new Circle(25);
            logoCircle.setFill(new ImagePattern(logoImage));
            logoCircle.setCursor(Cursor.HAND);

            Region leftRegion = new Region();
            HBox.setHgrow(leftRegion, Priority.ALWAYS);

            Button menuButton = ButtonStyle.createStyledButton("     Menu     ");
            menuButton.setOnAction(e -> showMenu());

            Button supportButton = ButtonStyle.createStyledButton(" Support  ");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = ButtonStyle.createStyledButton("  Privacy Policy  ");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button accountButton = ButtonStyle.createStyledButton("  Personal Account  ");
            accountButton.setOnAction(e -> showRegistrationWindow());

            Button addAppsButton = ButtonStyle.createStyledButton("  Add Apps  ");
            addAppsButton.setOnAction(e -> addNewApp());

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, accountButton, addAppsButton, leftRegion);
            topContent.setAlignment(Pos.CENTER);
            VBox.setVgrow(topContent, Priority.ALWAYS);

            HBox.setMargin(logoCircle, new Insets(0, 150, 0, 0));

            header.getChildren().addAll(topContent);

            return header;
        }

        private void showMenu() {
            primaryStage.close();
            Stage menuStage = new Stage();
            MenuPage menuPage = new MenuPage();
            menuPage.start(menuStage);
        }

        private void showSupportWindow() {
            SupportWindow supportWindow = new SupportWindow();
            Stage supportStage = new Stage();
            supportWindow.start(supportStage);
            supportStage.show();
        }

        private void showPrivacyPolicyWindow() {
            PrivacyPolicyWindow privacyPolicyWindow = new PrivacyPolicyWindow();
            Stage privacyStage = new Stage();
            privacyPolicyWindow.start(privacyStage);
            privacyStage.show();
        }

        private void showRegistrationWindow() {
            RegistrationWindow registrationWindow = new RegistrationWindow(root);
            Stage registrationStage = new Stage();
            registrationWindow.start(registrationStage);
        }
    }
}
