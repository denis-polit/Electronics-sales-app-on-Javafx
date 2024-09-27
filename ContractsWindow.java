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
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ContractsWindow extends Application {

    private BorderPane root;
    private VBox recentContractsContainer;
    private VBox allContractsContainer;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    @Override
    public void start(Stage primaryStage) {
        try {
            MenuPage menuPage = new MenuPage();
            connectionToDataBase = FirstConnectionToDataBase.getInstance();

            root = new BorderPane();
            root.setStyle("-fx-background-color: black;");

            VBox center = new VBox(10);
            center.setStyle("-fx-background-color: black;");

            HeaderComponent headerComponent = new HeaderComponent(primaryStage);
            VBox headerContainer = new VBox(10);
            headerContainer.getChildren().add(headerComponent.createHeader());

            recentContractsContainer = new VBox(10);
            recentContractsContainer.setStyle("-fx-background-color: black;");
            recentContractsContainer.setPrefHeight(Double.MAX_VALUE);

            allContractsContainer = new VBox(10);
            allContractsContainer.setStyle("-fx-background-color: black;");
            allContractsContainer.setPrefHeight(Double.MAX_VALUE);

            VBox contractsSections = new VBox(10);
            contractsSections.getChildren().addAll(
                    createSection("Recent Contracts", recentContractsContainer, true),
                    createSeparator(),
                    createSection("Contract History", allContractsContainer, false)
            );

            recentContractsContainer.setPadding(new Insets(0, 0, 0, 0));
            allContractsContainer.setPadding(new Insets(0, 0, 0, 0));

            center.getChildren().addAll(headerContainer, contractsSections);
            VBox.setVgrow(contractsSections, Priority.ALWAYS);
            VBox.setMargin(recentContractsContainer, new Insets(0));
            VBox.setMargin(allContractsContainer, new Insets(0));

            ScrollPane scrollPane = new ScrollPane(center);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: black; -fx-padding: 0;");

            root.setCenter(scrollPane);

            Scene scene = new Scene(root, 900, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Contracts");
            primaryStage.show();

            searchContracts("");

            HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
            hotKeysHandler.addHotkeys();
        } catch (SQLException e) {
            alertService.showErrorAlert("Error initializing database connection: " + e.getMessage());
        }
    }

    private VBox createSection(String title, VBox contentContainer, boolean isRecent) {
        Label sectionTitle = new Label(title);
        sectionTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 10 0 10 10;");

        Button sortButton = new Button();
        Image sortingIcon = new Image("file:icons/sorting.png");
        ImageView sortingImageView = new ImageView(sortingIcon);
        sortingImageView.setFitWidth(20);
        sortingImageView.setFitHeight(20);
        sortButton.setGraphic(sortingImageView);
        sortButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        sortButton.setOnAction(e -> sortContracts(contentContainer, isRecent));

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

    private void searchContracts(String query) {
        recentContractsContainer.getChildren().clear();
        allContractsContainer.getChildren().clear();

        try (Connection connection = connectionToDataBase.getConnection();
             Statement statement = connection.createStatement()) {

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twentyFourHoursAgo = now.minusHours(24);

            String recentContractsSql = "SELECT * FROM contracts WHERE creation_date >= ? AND " +
                    "(id_contracts LIKE '%" + query + "%' OR status LIKE '%" + query + "%' OR total_amount LIKE '%" + query + "%' OR " +
                    "delivery_method LIKE '%" + query + "%' OR pay_method LIKE '%" + query + "%' OR manager_id LIKE '%" + query + "%')";

            PreparedStatement recentContractsStatement = connection.prepareStatement(recentContractsSql);
            recentContractsStatement.setTimestamp(1, Timestamp.valueOf(twentyFourHoursAgo));
            ResultSet recentContractsResultSet = recentContractsStatement.executeQuery();

            int recentCount = 0;
            while (recentContractsResultSet.next()) {
                recentCount++;
                addContractToContainer(recentContractsResultSet, recentContractsContainer, connection, recentCount);
            }

            if (recentCount == 0) {
                Label noContractsLabel = createLabel("No contracts in the last 24 hours");
                noContractsLabel.setPadding(new Insets(5, 0, 5, 10));
                recentContractsContainer.getChildren().add(noContractsLabel);
            }

            String allContractsSql = "SELECT * FROM contracts WHERE " +
                    "(id_contracts LIKE '%" + query + "%' OR status LIKE '%" + query + "%' OR total_amount LIKE '%" + query + "%' OR " +
                    "delivery_method LIKE '%" + query + "%' OR pay_method LIKE '%" + query + "%' OR manager_id LIKE '%" + query + "%')";

            ResultSet allContractsResultSet = statement.executeQuery(allContractsSql);

            int allCount = 0;
            while (allContractsResultSet.next()) {
                allCount++;
                addContractToContainer(allContractsResultSet, allContractsContainer, connection, allCount);
            }

            if (allCount == 0) {
                displayNoResults(allContractsContainer);
            }

        } catch (SQLException e) {
            alertService.showErrorAlert("Error retrieving contracts: " + e.getMessage());
        }
    }

    private void addContractToContainer(ResultSet resultSet, VBox container, Connection connection, int count) throws SQLException {
        int contractId = resultSet.getInt("id_contracts");
        String status = resultSet.getString("status");
        double totalAmount = resultSet.getDouble("total_amount");
        String deliveryMethod = resultSet.getString("delivery_method");
        String payMethod = resultSet.getString("pay_method");
        String managerId = resultSet.getString("manager_id");
        int mainProductId = resultSet.getInt("product_id");
        int mainProductCount = resultSet.getInt("product_count");

        String firstLineText = String.format("№%d. ID: %d, Status: %s, Total Amount: %.2f",
                count, contractId, status, totalAmount);
        String secondLineText = String.format("Delivery Method: %s, Payment Method: %s, Manager Name: %s",
                deliveryMethod, payMethod, managerId);
        String mainProductText = String.format("Main Product (Id*count): %d*%d", mainProductId, mainProductCount);

        Label firstLineLabel = createLabel(firstLineText);
        Label secondLineLabel = createLabel(secondLineText);
        Label mainProductLabel = createLabel(mainProductText);

        VBox contractBox = new VBox(5);
        contractBox.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 5px;");
        contractBox.getChildren().addAll(firstLineLabel, secondLineLabel, mainProductLabel);

        String productsSql = "SELECT product_id, product_price, product_count FROM catalog_contract WHERE contract_id = ?";
        try (PreparedStatement productsStatement = connection.prepareStatement(productsSql)) {
            productsStatement.setInt(1, contractId);
            ResultSet productsResultSet = productsStatement.executeQuery();

            int productNumber = 1;
            while (productsResultSet.next()) {
                int productId = productsResultSet.getInt("product_id");
                double productPrice = productsResultSet.getDouble("product_price");
                int productCount = productsResultSet.getInt("product_count");

                String productLineText = String.format("Product %d (Id*count): %d*%d, Price: %.2f",
                        productNumber, productId, productCount, productPrice);

                Label productLineLabel = createLabel(productLineText);
                contractBox.getChildren().add(productLineLabel);

                productNumber++;
            }
        }

        container.getChildren().add(contractBox);

        Separator separator = createSeparator();
        separator.setPadding(new Insets(10, 20, 10, 20));
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

        Label noResultsLabel = new Label("No contracts found");
        noResultsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        container.getChildren().addAll(emptySearchImageView, noResultsLabel);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), container);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private void sortContracts(VBox container, boolean isRecent) {
        List<Node> items = new ArrayList<>(container.getChildren());
        items.removeIf(node -> node instanceof Separator);

        if (isRecent) {
            items.sort(Comparator.comparing(node -> {
                VBox vbox = (VBox) node;
                Label firstLabel = (Label) vbox.getChildren().get(0);
                return firstLabel.getText();
            }));
        } else {
            items.sort(Comparator.comparing(node -> {
                VBox vbox = (VBox) node;
                Label firstLabel = (Label) vbox.getChildren().get(0);
                return firstLabel.getText();
            }));
        }

        container.getChildren().clear();
        container.getChildren().addAll(items);
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
                        searchContracts(query);
                    } else {
                        alertService.showErrorAlert("Please enter a search query.");
                    }
                }
            });

            Button clearButton = ButtonStyle.createStyledButton("Clear Search");
            clearButton.setOnAction(event -> {
                searchField.clear();
                searchContracts("");
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
