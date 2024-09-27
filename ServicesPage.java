import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ServicesPage extends Application {

    Stage primaryStage;
    private BorderPane root;
    private SessionManager sessionManager;
    private FirstConnectionToDataBase connectionToDataBase;
    private Connection connection;
    private Stage loadingStage;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.root = new BorderPane();
        MenuPage menuPage = new MenuPage();
        this.sessionManager = SessionManager.getInstance();

        VBox contentLayout = new VBox(20);
        contentLayout.setStyle("-fx-background-color: black");
        contentLayout.setAlignment(Pos.CENTER);

        VBox servicesLayout = new VBox();
        servicesLayout.setStyle("-fx-background-color: black");
        servicesLayout.setAlignment(Pos.CENTER);

        VBox contentContainer = new VBox(20);
        contentContainer.setPadding(new Insets(20));

        HeaderComponent headerComponent = new HeaderComponent();
        Node header = headerComponent.createHeader();
        contentLayout.getChildren().addAll(header, servicesLayout);

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

        loadingStage = LoadingIndicatorUtil.showLoadingIndicator(primaryStage);

        Task<Void> loadDataTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                System.out.println("Starting to add services from database");
                addServicesFromDatabase(contentContainer);
                System.out.println("Finished adding services from database");
                return null;
            }

            @Override
            protected void succeeded() {
                System.out.println("Successfully loaded data");
                Platform.runLater(() -> {
                    servicesLayout.getChildren().add(contentContainer);
                    LoadingIndicatorUtil.hideLoadingIndicator(loadingStage);
                });
            }

            @Override
            protected void failed() {
                System.out.println("Failed to load data from the database");
                Platform.runLater(() -> {
                    LoadingIndicatorUtil.hideLoadingIndicator(loadingStage);
                    alertService.showErrorAlert("Failed to load data from the database.");
                });
            }
        };

        new Thread(loadDataTask).start();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(contentLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Services Page");
        primaryStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();
    }

    private Connection establishDBConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (connectionToDataBase == null) {
                connectionToDataBase = FirstConnectionToDataBase.getInstance();
            }
            connection = connectionToDataBase.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Failed to establish a valid database connection.");
            }
        }
        return connection;
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
            alertService.showErrorAlert("Failed to fetch employee status: " + e.getMessage());
        }
        return null;
    }

    public class HeaderComponent {

        private VBox createHeader() {
            VBox header = new VBox(5);
            header.setPadding(new Insets(5));
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

            Button actionButton;
            if ("super_admin".equals(getEmployeeStatus(sessionManager.getCurrentManagerName()))) {
                actionButton = ButtonStyle.createStyledButton("Add Service");
                actionButton.setOnAction(event -> {
                    try {
                        new AddServiceWindow().start(new Stage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                actionButton = ButtonStyle.createStyledButton("Suggest");
                actionButton.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Functionality Not Available");
                    alert.setHeaderText(null);
                    alert.setContentText("This functionality is not available yet. Stay tuned for updates!");
                    alert.showAndWait();
                });
            }

            Button accountButton = ButtonStyle.createStyledButton("  Personal Account  ");
            accountButton.setOnAction(e -> showRegistrationWindow());

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(leftRegion, logoCircle, menuButton, supportButton, privacyButton,  actionButton, accountButton);
            topContent.setAlignment(Pos.CENTER_RIGHT);
            VBox.setVgrow(topContent, Priority.ALWAYS);

            HBox.setMargin(logoCircle, new Insets(0, 200, 0, 0));

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

    private List<Service> fetchServicesFromDatabase() {
        List<Service> services = new ArrayList<>();

        try (Connection connection = establishDBConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT services_id, services_title, services_description, services_icon FROM services")) {

            while (resultSet.next()) {
                int serviceId = resultSet.getInt("services_id");
                String title = resultSet.getString("services_title");
                String description = resultSet.getString("services_description");
                String iconPath = resultSet.getString("services_icon");

                System.out.println("Fetched service: " + title);

                services.add(new Service(serviceId, title, description, iconPath));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to fetch services from the database: " + e.getMessage());
        }

        return services;
    }

    private void addServicesFromDatabase(VBox container) {
        List<Service> services = fetchServicesFromDatabase();

        for (Service service : services) {
            addServiceSection(container, service.getTitle(), service.getDescription(), service.getIconPath(), service.getId());
        }
    }

    private void addServiceSection(VBox container, String title, String description, String iconPath, int serviceId) {
        System.out.println("Adding service: " + title);
        HBox serviceSection = new HBox(10);
        serviceSection.setStyle("-fx-background-color: black; -fx-padding: 20; -fx-border-color: #CCCCCC; -fx-border-radius: 15px;");
        serviceSection.setPrefWidth(600);

        VBox textContainer = new VBox();
        textContainer.setAlignment(Pos.CENTER_LEFT);
        textContainer.setSpacing(5);

        Text titleText = new Text(title);
        titleText.setFont(Font.font("Gotham", FontWeight.BOLD, 30));
        titleText.setFill(Color.LIGHTGRAY);
        VBox.setMargin(titleText, new Insets(0, 0, 20, 0));

        Text descriptionText = new Text(description);
        descriptionText.setFont(Font.font("Gotham", 16));
        descriptionText.setFill(Color.LIGHTGRAY);
        descriptionText.setWrappingWidth(600);

        textContainer.getChildren().addAll(titleText, descriptionText);

        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(0, 0, 20, 0));

        try {
            ImageView iconView = new ImageView(new Image(iconPath));
            iconView.setFitWidth(100);
            iconView.setFitHeight(100);
            imageContainer.getChildren().add(iconView);
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to load image for service: " + title + " - " + e.getMessage());
        }

        serviceSection.getChildren().addAll(imageContainer, textContainer);

        Button editButton = ButtonStyle.expandPaneStyledButton(" Edit ");
        Button deleteButton = ButtonStyle.expandPaneStyledButton("Delete");

        String employeeStatus = getEmployeeStatus(sessionManager.getCurrentManagerName());

        DeleteOrEditServicesItem deleteOrEditServicesItem = new DeleteOrEditServicesItem(serviceId);

        if ("super_admin".equals(employeeStatus)) {
            editButton.setOnAction(event -> deleteOrEditServicesItem.handleEditService(serviceId));
            deleteButton.setOnAction(event -> deleteOrEditServicesItem.handleDeleteService(serviceId));
            editButton.setVisible(true);
            deleteButton.setVisible(true);
        } else {
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        }

        VBox buttonsContainer = new VBox(10);
        buttonsContainer.getChildren().addAll(editButton, deleteButton);
        buttonsContainer.setAlignment(Pos.CENTER_LEFT);

        serviceSection.getChildren().add(buttonsContainer);

        VBox.setMargin(serviceSection, new Insets(0, 0, 20, 0));

        System.out.println("Service added to container: " + title);
        container.getChildren().add(serviceSection);
    }
}
