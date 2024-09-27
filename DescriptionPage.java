import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

public class DescriptionPage extends Application {

    Stage primaryStage;
    private BorderPane root;
    private SessionManager sessionManager;
    private FirstConnectionToDataBase connectionToDataBase;
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

        VBox descriptionLayout = new VBox();
        descriptionLayout.setStyle("-fx-background-color: black");
        descriptionLayout.setAlignment(Pos.CENTER);

        VBox contentContainer = new VBox(20);
        contentContainer.setPadding(new Insets(20));

        HeaderComponent headerComponent = new HeaderComponent();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
            return;
        }

        Node header = headerComponent.createHeader();

        Region topSpacer = new Region();
        Region bottomSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        contentLayout.getChildren().addAll(topSpacer, header, descriptionLayout, bottomSpacer);

        loadingStage = LoadingIndicatorUtil.showLoadingIndicator(primaryStage);

        Task<Void> loadDataTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                addExplanationsFromDatabase(contentContainer);
                return null;
            }

            @Override
            protected void succeeded() {
                LoadingIndicatorUtil.hideLoadingIndicator(loadingStage);

                if (contentContainer.getChildren().isEmpty()) {
                    Label emptyLabel = new Label("Page is empty");
                    emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
                    descriptionLayout.getChildren().add(emptyLabel);
                } else {
                    descriptionLayout.getChildren().add(contentContainer);
                }
            }

            @Override
            protected void failed() {
                LoadingIndicatorUtil.hideLoadingIndicator(loadingStage);
                alertService.showErrorAlert("Failed to load data from the database.");
            }
        };

        new Thread(loadDataTask).start();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(contentLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Description Page");
        primaryStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();
    }

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase == null) {
            throw new SQLException("Database connection is not established.");
        }
        return connectionToDataBase.getConnection();
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

    public class HeaderComponent {

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

            Button actionButton;
            if ("super_admin".equals(getEmployeeStatus(sessionManager.getCurrentManagerName()))) {
                actionButton = ButtonStyle.createStyledButton("Add Item");
                actionButton.setOnAction(event -> {
                    try {
                        new AddExplanationWindow().start(new Stage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                actionButton = ButtonStyle.createStyledButton("Suggest Item");
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
            topContent.getChildren().addAll(leftRegion, logoCircle, menuButton, supportButton, privacyButton, actionButton, accountButton);
            topContent.setAlignment(Pos.CENTER_RIGHT);
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

    private List<Explanation> fetchExplanationsFromDatabase() {
        List<Explanation> explanations = new ArrayList<>();

        try (Connection connection = establishDBConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM explanation")) {

            while (resultSet.next()) {
                int explanationId = resultSet.getInt("explanation_id");
                String title = resultSet.getString("explanation_title");
                String description = resultSet.getString("explanation_description");
                String iconPath = resultSet.getString("explanation_icon");

                explanations.add(new Explanation(explanationId, title, description, iconPath));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return explanations;
    }

    private void addExplanationsFromDatabase(VBox container) {
        List<Explanation> explanations = fetchExplanationsFromDatabase();

        for (Explanation explanation : explanations) {
            addDescriptionSection(container, explanation.getTitle(), explanation.getDescription(), explanation.getIconPath(), explanation.getId());
        }
    }

    private void addDescriptionSection(VBox container, String title, String description, String iconPath, int explanationId) {
        HBox descriptionSection = new HBox(10);
        descriptionSection.setStyle("-fx-background-color: black; -fx-padding: 20; -fx-border-color: #CCCCCC; -fx-border-radius: 15px;");
        descriptionSection.setPrefWidth(600);

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

        ImageView iconView = new ImageView(new Image(iconPath));
        iconView.setFitWidth(100);
        iconView.setFitHeight(100);

        imageContainer.getChildren().add(iconView);

        descriptionSection.getChildren().addAll(imageContainer, textContainer);

        DeleteOrEditDescriptionItem deleteOrEditDescriptionItem = new DeleteOrEditDescriptionItem(explanationId);

        Button editButton = ButtonStyle.expandPaneStyledButton(" Edit ");
        Button deleteButton = ButtonStyle.expandPaneStyledButton("Delete");

        String employeeStatus = getEmployeeStatus(sessionManager.getCurrentManagerName());

        if ("super_admin".equals(employeeStatus)) {
            editButton.setOnAction(event -> deleteOrEditDescriptionItem.handleEditExplanation(explanationId));
            deleteButton.setOnAction(event -> deleteOrEditDescriptionItem.handleDeleteExplanation(explanationId));
            editButton.setVisible(true);
            deleteButton.setVisible(true);
        } else {
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        }

        VBox buttonsContainer = new VBox(10);
        buttonsContainer.getChildren().addAll(editButton, deleteButton);
        buttonsContainer.setAlignment(Pos.CENTER_LEFT);

        descriptionSection.getChildren().add(buttonsContainer);

        VBox.setMargin(descriptionSection, new Insets(0, 0, 20, 0));

        container.getChildren().add(descriptionSection);
    }
}
