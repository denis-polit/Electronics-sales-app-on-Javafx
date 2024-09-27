import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class OrdersOrNegotiationsPage extends Application {
    private BorderPane root;
    private Connection connection;
    private VBox suppliersAndOrdersContainer;
    private FirstConnectionToDataBase connectionToDataBase;

    private final AlertServiceImpl alertService = new AlertServiceImpl();

    private CreateSuppliersProfile createSuppliersProfile;
    private ListOfExpectedProduct listOfExpectedProduct;
    private EditSuppliersProfile editSuppliersProfile;
    private SuppliersDBProducts suppliersDBProducts;
    private ParseToSuppliersCatalogs parseToSuppliersCatalogs;

    @Override
    public void start(Stage primaryStage) {
        try {
            connection = establishDBConnection();
            MenuPage menuPage = new MenuPage();

            this.createSuppliersProfile = new CreateSuppliersProfile();
            this.listOfExpectedProduct = new ListOfExpectedProduct();
            this.editSuppliersProfile = new EditSuppliersProfile();
            this.suppliersDBProducts = new SuppliersDBProducts();
            this.parseToSuppliersCatalogs = new ParseToSuppliersCatalogs(this);

            root = new BorderPane();
            root.setStyle("-fx-background-color: black;");

            VBox center = new VBox(10);
            center.setStyle("-fx-background-color: black;");

            HeaderComponent headerComponent = new HeaderComponent(primaryStage);
            VBox headerContainer = new VBox(10);
            headerContainer.getChildren().add(headerComponent.createHeader());

            suppliersAndOrdersContainer = new VBox(10);
            suppliersAndOrdersContainer.setStyle("-fx-background-color: black;");

            ScrollPane scrollPane = new ScrollPane(suppliersAndOrdersContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            center.getChildren().addAll(headerContainer, scrollPane);

            root.setCenter(center);

            Scene scene = new Scene(root, 900, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Supplier Contracts and Delivery Details");
            primaryStage.show();

            searchSupplierContracts("");

            HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
            hotKeysHandler.addHotkeys();

            try {
                connectionToDataBase = FirstConnectionToDataBase.getInstance();
            } catch (SQLException e) {
                alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
            }

        } catch (SQLException e) {
            alertService.showErrorAlert("Error connecting to database: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            return connectionToDataBase.getConnection();
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
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

            Button menuButton = ButtonStyle.createStyledButton("Menu");
            menuButton.setOnAction(e -> showMenu());

            TextField searchField = new TextField();
            searchField.setPromptText("Search by Supplier Info");
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
                        searchSupplierContracts(query);
                    } else {
                        alertService.showErrorAlert("Please enter a search query.");
                    }
                }
            });

            Button clearButton = ButtonStyle.createStyledButton("X");
            clearButton.setOnAction(event -> {
                searchField.clear();
                searchSupplierContracts("");
            });

            Button supportButton = ButtonStyle.createStyledButton("Support");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = ButtonStyle.createStyledButton("Terms");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button addSuppliersButton = ButtonStyle.createStyledButton("Add Sp");
            addSuppliersButton.setOnAction(e -> createSuppliersProfile.showAddSuppliersWindow());

            Button addSpProductButton = ButtonStyle.createStyledButton("Add SPP");
            addSpProductButton.setOnAction(e -> {
                try {
                    AddSuppliersProductWindow addSuppliersProductWindow = new AddSuppliersProductWindow();
                    addSuppliersProductWindow.showAddSpProductForm();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            Button expectedOrdersButton = ButtonStyle.createStyledButton("Expected Orders");
            expectedOrdersButton.setOnAction(e -> listOfExpectedProduct.showExpectedOrdersWindow());

            Button accountButton = ButtonStyle.createStyledButton("Account");
            accountButton.setOnAction(e -> showRegistrationWindow());

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, searchField, clearButton, supportButton, privacyButton, addSuppliersButton, addSpProductButton, expectedOrdersButton, accountButton);
            topContent.setAlignment(Pos.CENTER_RIGHT);
            VBox.setVgrow(topContent, Priority.ALWAYS);

            HBox.setMargin(logoCircle, new Insets(0, 20, 0, 0));

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

    private Label createLabel(String text, int fontSize) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-family: 'Gotham'; -fx-font-size: " + fontSize + "px; -fx-padding: 0 30 0 30;");
        return label;
    }

    private void searchSupplierContracts(String query) {
        suppliersAndOrdersContainer.getChildren().clear();

        try {
            String sql = "SELECT * FROM suppliers WHERE " +
                    "suppliers_title LIKE ? OR " +
                    "suppliers_description LIKE ? OR " +
                    "suppliers_status LIKE ? OR " +
                    "suppliers_specialization LIKE ? OR " +
                    "supplier_type LIKE ? OR " +
                    "suppliers_contact_number LIKE ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int i = 1; i <= 6; i++) {
                statement.setString(i, "%" + query + "%");
            }

            ResultSet resultSet = statement.executeQuery();

            boolean found = false;

            while (resultSet.next()) {
                found = true;
                Supplier supplier = new Supplier(
                        resultSet.getInt("suppliers_id"),
                        resultSet.getString("suppliers_title"),
                        resultSet.getString("supplier_type"),
                        resultSet.getString("suppliers_description"),
                        resultSet.getString("suppliers_specialization"),
                        resultSet.getString("suppliers_status"),
                        resultSet.getString("suppliers_contact_number"),
                        resultSet.getString("suppliers_website")
                );

                String firstLineText = String.format("Title: %s", supplier.getTitle());
                String secondLineText = String.format("Description: %s", supplier.getDescription());
                String thirdLineText = String.format("Status: %s", supplier.getStatus());
                String fourthLineText = String.format("Specialization: %s", supplier.getSpecialization());
                String fifthLineText = String.format("Type: %s", supplier.getType());
                String sixthLineText = String.format("Contact Number: %s", supplier.getContactNumber());
                String seventhLineText = String.format("Website: %s", supplier.getWebsite());

                Label firstLineLabel = createLabel(firstLineText, 16);
                Label secondLineLabel = createLabel(secondLineText, 16);
                Label thirdLineLabel = createLabel(thirdLineText, 16);
                Label fourthLineLabel = createLabel(fourthLineText, 16);
                Label fifthLineLabel = createLabel(fifthLineText, 16);
                Label sixthLineLabel = createLabel(sixthLineText, 16);
                Label seventhLineLabel = createLabel(seventhLineText, 16);

                VBox contractBox = new VBox(5);
                contractBox.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 5px;");
                contractBox.getChildren().addAll(firstLineLabel, secondLineLabel, thirdLineLabel, fourthLineLabel, fifthLineLabel, sixthLineLabel, seventhLineLabel);

                Button websiteButton = ButtonStyle.expandPaneStyledButton("Go to Website");
                websiteButton.setOnAction(event -> openWebsite(supplier.getWebsite()));

                Button planDeliveryButton = ButtonStyle.expandPaneStyledButton("Supply planning");
                planDeliveryButton.setOnAction(event -> planDeliveryWindow(supplier));

                Button productListButton = ButtonStyle.expandPaneStyledButton("List of products");
                productListButton.setOnAction(event -> fetchProducts(supplier));

                Button editButton = ButtonStyle.expandPaneStyledButton("Edit");
                editButton.setOnAction(event -> editSuppliersProfile.showEditSupplierWindow(supplier));

                HBox buttonBox = new HBox(10);
                buttonBox.getChildren().addAll(planDeliveryButton, productListButton, editButton, websiteButton);
                buttonBox.setAlignment(Pos.CENTER_LEFT);
                buttonBox.setPadding(new Insets(0, 0, 0, 40));

                suppliersAndOrdersContainer.getChildren().addAll(contractBox, buttonBox);

                Separator separator = new Separator();
                separator.setPadding(new Insets(10, 20, 10, 20));
                suppliersAndOrdersContainer.getChildren().add(separator);
            }

            if (!found) {
                suppliersAndOrdersContainer.getChildren().clear();
                suppliersAndOrdersContainer.setAlignment(Pos.CENTER);

                Image emptySearchImage = new Image("file:icons/empty_search.png");
                ImageView emptySearchImageView = new ImageView(emptySearchImage);
                emptySearchImageView.setFitWidth(200);
                emptySearchImageView.setFitHeight(200);

                Insets margin = new Insets(150, 0, 150, 0);
                VBox.setMargin(emptySearchImageView, margin);

                suppliersAndOrdersContainer.getChildren().add(emptySearchImageView);
            }

        } catch (SQLException e) {
            alertService.showErrorAlert("Error when retrieving vendor data: " + e.getMessage());
        }
    }

    private void openWebsite(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                alertService.showErrorAlert("Error opening website: " + e.getMessage());
            }
        } else {
            alertService.showErrorAlert("Website URL is not available.");
        }
    }

    private void planDeliveryWindow(Supplier supplier) {
        Stage planDeliveryStage = new Stage();

        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: black;");
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        try {
            String sql = "SELECT DISTINCT c.product_id, p.graphics_chip " +
                    "FROM contracts c " +
                    "JOIN catalog p ON c.product_id = p.product_id";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            List<String> graphicsChips = new ArrayList<>();

            while (resultSet.next()) {
                String graphicsChip = resultSet.getString("graphics_chip");
                graphicsChips.add(graphicsChip);
            }

            graphicsChips.sort(Comparator.naturalOrder());

            for (String graphicsChip : graphicsChips) {
                Label graphicsChipLabel = new Label(graphicsChip);
                graphicsChipLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

                layout.getChildren().add(graphicsChipLabel);
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error when retrieving contract data: " + e.getMessage());
        }

        Scene scene = new Scene(layout, 300, 600);
        scene.setFill(Color.BLACK);
        planDeliveryStage.setScene(scene);
        planDeliveryStage.show();
    }

    public void fetchProducts(Supplier supplier) {
        System.out.println("Inside fetchProducts for supplier: " + supplier.getTitle());

        List<String> choices = new ArrayList<>();
        choices.add("Online Catalog");
        choices.add("Database");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Online Catalog", choices);
        dialog.setTitle("Select Source");
        dialog.setHeaderText("Choose the source of the products");
        dialog.setContentText("Source:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (result.get().equals("Online Catalog")) {
                if (supplier.getTitle().equals("MOYO")) {
                    System.out.println("Fetching MOYO products...");
                    Platform.runLater(() -> parseToSuppliersCatalogs.fetchMOYOProducts(supplier));
                } else if (supplier.getTitle().equals("AKS")) {
                    System.out.println("Fetching AKS products...");
                    Platform.runLater(() -> parseToSuppliersCatalogs.fetchAKSProducts(supplier));
                } else if (supplier.getTitle().equals("CLICK")) {
                    System.out.println("Fetching CLICK products...");
                    Platform.runLater(() -> parseToSuppliersCatalogs.fetchCLICKProducts(supplier));
                }
            } else if (result.get().equals("Database")) {
                if (supplier.getTitle().equals("MOYO")) {
                    System.out.println("Fetching MOYO products from database...");
                    Platform.runLater(() -> suppliersDBProducts.fetchProductsFromDatabase(supplier, "MOYO"));
                } else if (supplier.getTitle().equals("AKS")) {
                    System.out.println("Fetching AKS products from database...");
                    Platform.runLater(() -> suppliersDBProducts.fetchProductsFromDatabase(supplier, "AKS"));
                } else if (supplier.getTitle().equals("CLICK")) {
                    System.out.println("Fetching CLICK products from database...");
                    Platform.runLater(() -> suppliersDBProducts.fetchProductsFromDatabase(supplier, "CLICK"));
                }
            }
        }
    }
}