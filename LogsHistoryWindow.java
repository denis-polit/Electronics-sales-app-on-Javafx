import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogsHistoryWindow extends Application {

    private BorderPane root;
    private VBox recentLogsContainer;
    private VBox allLogsContainer;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    @Override
    public void start(Stage primaryStage) {
        try {
            MenuPage menuPage = new MenuPage();
            connectionToDataBase = FirstConnectionToDataBase.getInstance();

            StackPane rootStack = new StackPane();
            rootStack.setStyle("-fx-background-color: black;");

            Rectangle background = new Rectangle();
            background.widthProperty().bind(primaryStage.widthProperty());
            background.heightProperty().bind(primaryStage.heightProperty());
            background.setFill(Color.BLACK);

            root = new BorderPane();
            root.setStyle("-fx-background-color: black;");

            VBox center = new VBox(10);
            center.setStyle("-fx-background-color: black;");

            HeaderComponent headerComponent = new HeaderComponent(primaryStage);
            VBox headerContainer = new VBox(10);
            headerContainer.getChildren().add(headerComponent.createHeader());
            headerContainer.setStyle("-fx-background-color: black;");

            recentLogsContainer = new VBox(10);
            recentLogsContainer.setStyle("-fx-background-color: black;");
            recentLogsContainer.setPrefHeight(Double.MAX_VALUE);

            allLogsContainer = new VBox(10);
            allLogsContainer.setStyle("-fx-background-color: black;");
            allLogsContainer.setPrefHeight(Double.MAX_VALUE);

            VBox logsSections = new VBox(10);
            logsSections.getChildren().addAll(
                    createSection("Recent Logs", recentLogsContainer, true),
                    createSeparator(),
                    createSection("Logs History", allLogsContainer, false)
            );
            logsSections.setStyle("-fx-background-color: black;");

            recentLogsContainer.setPadding(new Insets(0, 0, 0, 0));
            allLogsContainer.setPadding(new Insets(0, 0, 0, 0));

            center.getChildren().addAll(headerContainer, logsSections);
            VBox.setVgrow(logsSections, Priority.ALWAYS);
            VBox.setMargin(recentLogsContainer, new Insets(0));
            VBox.setMargin(allLogsContainer, new Insets(0));

            ScrollPane scrollPane = new ScrollPane(center);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: black;");

            root.setCenter(scrollPane);

            rootStack.getChildren().addAll(background, root);

            Scene scene = new Scene(rootStack, 900, 600);
            scene.setFill(Color.BLACK);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Logs");
            primaryStage.show();

            searchLogs("");

            HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
            hotKeysHandler.addHotkeys();
        } catch (SQLException e) {
            alertService.showErrorAlert("Error initializing database connection: " + e.getMessage());
        }
    }

    private VBox createSection(String title, VBox contentContainer, boolean isRecent) {
        Label sectionTitle = new Label(title);
        sectionTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button sortButton = new Button();
        Image sortingIcon = new Image("file:icons/sorting.png");
        ImageView sortingImageView = new ImageView(sortingIcon);
        sortingImageView.setFitWidth(20);
        sortingImageView.setFitHeight(20);
        sortButton.setGraphic(sortingImageView);
        sortButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        sortButton.setOnAction(e -> sortLogs(contentContainer, isRecent));

        HBox sectionHeader = new HBox(10);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        sectionHeader.getChildren().addAll(sectionTitle, sortButton);
        sectionHeader.setStyle("-fx-background-color: black;");

        VBox sectionContainer = new VBox(10);
        sectionContainer.getChildren().addAll(sectionHeader, contentContainer);
        sectionContainer.setStyle("-fx-background-color: black;");
        VBox.setVgrow(contentContainer, Priority.ALWAYS);

        return sectionContainer;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();

        VBox.setMargin(separator, new Insets(10, 20, 10, 20));
        separator.setStyle(
                "-fx-background-color: gray;" +
                        "-fx-padding: 0;"
        );

        return separator;
    }

    private void searchLogs(String query) {
        recentLogsContainer.getChildren().clear();
        allLogsContainer.getChildren().clear();

        try (Connection connection = connectionToDataBase.getConnection();
             Statement statement = connection.createStatement()) {

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twentyFourHoursAgo = now.minusHours(24);

            String recentLogsSql = "SELECT * FROM activity_log WHERE timestamp >= ? AND " +
                    "(log_id LIKE '%" + query + "%' OR user_id LIKE '%" + query + "%' OR action_type LIKE '%" + query + "%' OR " +
                    "object_type LIKE '%" + query + "%' OR details LIKE '%" + query + "%' OR user_ip LIKE '%" + query + "%')";

            PreparedStatement recentLogsStatement = connection.prepareStatement(recentLogsSql);
            recentLogsStatement.setTimestamp(1, Timestamp.valueOf(twentyFourHoursAgo));
            ResultSet recentLogsResultSet = recentLogsStatement.executeQuery();

            int recentCount = 0;
            while (recentLogsResultSet.next()) {
                recentCount++;
                addLogToContainer(recentLogsResultSet, recentLogsContainer);
            }

            if (recentCount == 0) {
                Label noLogsLabel = createLabel("No logs in the last 24 hours");
                recentLogsContainer.setPadding(new Insets(5, 0, 5, 10));
                recentLogsContainer.getChildren().add(noLogsLabel);
            }

            String allLogsSql = "SELECT * FROM activity_log WHERE " +
                    "(log_id LIKE '%" + query + "%' OR user_id LIKE '%" + query + "%' OR action_type LIKE '%" + query + "%' OR " +
                    "object_type LIKE '%" + query + "%' OR details LIKE '%" + query + "%' OR user_ip LIKE '%" + query + "%')";

            ResultSet allLogsResultSet = statement.executeQuery(allLogsSql);

            int allCount = 0;
            while (allLogsResultSet.next()) {
                allCount++;
                addLogToContainer(allLogsResultSet, allLogsContainer);
            }

            if (allCount == 0) {
                displayNoResults(allLogsContainer);
            }

        } catch (SQLException e) {
            alertService.showErrorAlert("Error retrieving logs: " + e.getMessage());
        }
    }

    private void addLogToContainer(ResultSet resultSet, VBox container) throws SQLException {
        int logId = resultSet.getInt("log_id");
        int userId = resultSet.getInt("user_id");
        int managerId = resultSet.getInt("manager_id");
        String actionType = resultSet.getString("action_type");
        String objectType = resultSet.getString("object_type");
        String details = resultSet.getString("details");
        Timestamp timestamp = resultSet.getTimestamp("timestamp");
        String userIp = resultSet.getString("user_ip");
        String userDeviceType = resultSet.getString("user_device_type");

        String firstLineText = String.format("Log ID: %d, User ID: %d, Manager ID: %d", logId, userId, managerId);
        String secondLineText = String.format("Action Type: %s, Object Type: %s, Timestamp: %s", actionType, objectType, timestamp.toString());
        String thirdLineText = String.format("Details: %s", details != null ? details : "N/A");
        String fourthLineText = String.format("User IP: %s, Device Type: %s", userIp != null ? userIp : "N/A", userDeviceType != null ? userDeviceType : "N/A");

        Label firstLineLabel = createLabel(firstLineText);
        Label secondLineLabel = createLabel(secondLineText);
        Label thirdLineLabel = createLabel(thirdLineText);
        Label fourthLineLabel = createLabel(fourthLineText);

        VBox logBox = new VBox(5);
        logBox.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 5px; -fx-background-color: black;");
        logBox.getChildren().addAll(firstLineLabel, secondLineLabel, thirdLineLabel, fourthLineLabel);

        container.getChildren().add(logBox);

        Separator separator = createSeparator();
        container.getChildren().add(separator);
    }

    private void displayNoResults(VBox container) {
        container.getChildren().clear();
        container.setAlignment(Pos.CENTER);

        Image emptySearchImage = new Image("file:icons/empty_search.png");
        ImageView emptySearchImageView = new ImageView(emptySearchImage);
        emptySearchImageView.setFitWidth(200);
        emptySearchImageView.setFitHeight(200);

        Insets padding = new Insets(10, 20, 10, 20);
        container.setPadding(padding);

        Label noResultsLabel = new Label("No logs found");
        noResultsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        container.getChildren().addAll(emptySearchImageView, noResultsLabel);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), container);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private boolean isSortedDescending = true;

    private void sortLogs(VBox contentContainer, boolean isRecent) {
        List<Node> items = new ArrayList<>(contentContainer.getChildren());

        items.removeIf(node -> node instanceof Separator);

        items.sort((node1, node2) -> {
            VBox vbox1 = (VBox) node1;
            VBox vbox2 = (VBox) node2;

            Label logIdLabel1 = (Label) vbox1.getChildren().get(0);
            Label logIdLabel2 = (Label) vbox2.getChildren().get(0);

            String logIdText1 = logIdLabel1.getText().split(",")[0].split("Log ID: ")[1].trim();
            String logIdText2 = logIdLabel2.getText().split(",")[0].split("Log ID: ")[1].trim();

            int logId1 = Integer.parseInt(logIdText1);
            int logId2 = Integer.parseInt(logIdText2);

            if (isSortedDescending) {
                return Integer.compare(logId2, logId1);
            } else {
                return Integer.compare(logId1, logId2);
            }
        });

        contentContainer.getChildren().clear();

        for (Node item : items) {
            contentContainer.getChildren().add(item);

            Separator separator = new Separator();
            separator.setPadding(new Insets(10, 20, 10, 20));
            contentContainer.getChildren().add(separator);
        }

        isSortedDescending = !isSortedDescending;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
//        label.fontProperty().bind(Bindings.createObjectBinding(() ->
//                new Font("System", root.getWidth() / 60), root.widthProperty()));
        return label;
    }

    public class HeaderComponent {

        private Stage primaryStage;

        public HeaderComponent(Stage primaryStage) {
            this.primaryStage = primaryStage;
        }

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

            Region rightRegion = new Region();
            HBox.setHgrow(rightRegion, Priority.ALWAYS);

            Button menuButton = ButtonStyle.createStyledButton("     Menu     ");
            menuButton.setOnAction(e -> showMenu());

            TextField searchField = new TextField();
            searchField.setPromptText("Search by Product Title");
            searchField.getStyleClass().add("search-field");

            searchField.setStyle(
                    "-fx-background-radius: 5em; " +
                            "-fx-background-color: black; " +
                            "-fx-background-insets: 0, 1, 2; " +
                            "-fx-padding: 0.166667em 0.25em 0.25em 0.25em; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 1.0em; " +
                            "-fx-border-radius: 5em; " +
                            "-fx-border-color: white;"
            );

            searchField.setOnMousePressed(e -> searchField.setStyle(
                    "-fx-background-radius: 5em; " +
                            "-fx-background-color: black; " +
                            "-fx-background-insets: 0, 1, 2; " +
                            "-fx-padding: 0.166667em 0.25em 0.25em 0.25em; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 1.0em; " +
                            "-fx-border-radius: 5em; " +
                            "-fx-border-color: blue, -fx-focus-color, blue;"
            ));

            searchField.setOnMouseReleased(e -> searchField.setStyle(
                    "-fx-background-radius: 5em; " +
                            "-fx-background-color: black; " +
                            "-fx-background-insets: 0, 1, 2; " +
                            "-fx-padding: 0.166667em 0.25em 0.25em 0.25em; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 1.0em; " +
                            "-fx-border-radius: 5em; " +
                            "-fx-border-color: blue, -fx-focus-color, blue;"
            ));

            searchField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    String query = searchField.getText();
                    if (!query.isEmpty()) {
                        searchLogs(query);
                    } else {
                        alertService.showErrorAlert("Please enter a search query.");
                    }
                }
            });

            Button clearButton = ButtonStyle.createStyledButton("Clear Search");
            clearButton.setOnAction(event -> {
                searchField.clear();
                searchLogs("");
            });

            Button supportButton = ButtonStyle.createStyledButton("  Support  ");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = ButtonStyle.createStyledButton("  Privacy Policy  ");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button accountButton = ButtonStyle.createStyledButton("  Account  ");
            accountButton.setOnAction(e -> showRegistrationWindow());

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, searchField, clearButton, supportButton, privacyButton, accountButton, rightRegion);
            topContent.setAlignment(Pos.CENTER_RIGHT);
            VBox.setVgrow(topContent, Priority.ALWAYS);

            HBox.setMargin(logoCircle, new Insets(0, 90, 0, 0));

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

    public static void main(String[] args) {
        launch(args);
    }
}
