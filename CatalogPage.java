import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class CatalogPage extends Application {

    private ListView<Announcement> announcementsListView;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService;
    private final CatalogDataBase catalogDataBase;

    public CatalogPage() {
        SessionManager.getInstance();
        new ProductCompareWindow();
        alertService = new AlertServiceImpl();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
        catalogDataBase = new CatalogDataBase();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void showCatalog(Stage stage) {
        start(stage);
    }

    @Override
    public void start(Stage primaryStage) {
        MenuPage menuPage = new MenuPage();
        BorderPane root = new BorderPane();

        List<Announcement> announcementsList = catalogDataBase.getAllAnnouncementsFromDatabase();

        announcementsListView = new ListView<>();
        announcementsListView.setPrefSize(890, 590);
        announcementsListView.setStyle("-fx-text-fill: white; -fx-control-inner-background: black;");
        announcementsListView.setCellFactory(param -> new AnnouncementListCell());

        announcementsListView.getItems().addAll(announcementsList);

        StackPane announcementsFlowPane = new StackPane();

        BorderPane borderPane = new BorderPane();
        root.setCenter(borderPane);
        CatalogHeader catalogHeader = new CatalogHeader(catalogDataBase, announcementsFlowPane, primaryStage, borderPane);
        catalogHeader.setAnnouncementsListView(announcementsListView);

        root.setStyle("-fx-background-color: black;");
        root.setTop(catalogHeader.createHeader());

        VBox catalogContent = new VBox(10);
        catalogContent.getChildren().addAll(announcementsListView);
        root.setCenter(catalogContent);

        new SortCatalog(announcementsList, announcementsListView);

        Scene scene = new Scene(root, 900, 600);
        scene.setFill(Color.WHITE);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Catalog");
        primaryStage.show();

        new AnnouncementCreateWindow(connectionToDataBase);

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();
    }

    public void performSearchAndUpdateCatalog(String searchText) {
        String searchSql = "SELECT * FROM catalog WHERE product_title LIKE ? OR graphics_chip LIKE ? OR " +
                "memory_type LIKE ? OR producing_country LIKE ? OR brand LIKE ?";

        List<Announcement> searchResults = catalogDataBase.searchAnnouncementsByFields(searchSql, searchText);

        announcementsListView.getItems().clear();
        announcementsListView.getItems().addAll(searchResults);
    }

    public class AnnouncementListCell extends ListCell<Announcement> {

        private final CreateContract createContract = new CreateContract();
        private final SessionManager sessionManager = SessionManager.getInstance();

        public AnnouncementListCell() {
            setOnMouseEntered(event -> {
                if (!isEmpty() && !isSelected()) {
                    setStyle("-fx-background-color: #202020;");
                }
            });

            setOnMouseExited(event -> {
                if (!isEmpty() && !isSelected()) {
                    setStyle("-fx-background-color: black;");
                }
            });
        }

        @Override
        protected void updateItem(Announcement item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setGraphic(createAnnouncementFrame(item));
            }
        }

        private VBox createAnnouncementFrame(Announcement announcement) {
            VBox frame = new VBox(10);
            frame.setStyle("-fx-border-color: gray; -fx-border-width: 2; -fx-padding: 10; -fx-background-radius: 15px; -fx-border-radius: 15px;");

            String boldTextStyle = "-fx-font-weight: bold;";
            String description = announcement.getDescription();

            Label titleAndProductIdLabel = createBoldLabel("Title: " + announcement.getTitle() + "  |  Product ID: " + announcement.getProductId(), boldTextStyle);
            Label graphicsAndMemoryFrequencyLabel = createBoldLabel("Graphics Chip: " + announcement.getGraphicsChip() + "  |  Memory Frequency: " + announcement.getMemoryFrequency(), boldTextStyle);
            Label coreAndMemoryCapacityLabel = createBoldLabel("Core Frequency: " + announcement.getCoreFrequency() + "  |  Memory Capacity: " + announcement.getMemoryCapacity(), boldTextStyle);
            Label bitSizeMemoryBusAndResolutionLabel = createBoldLabel("Bit Size Memory Bus: " + announcement.getBitSizeMemoryBus() + "  |  Max Supported Resolution: " + announcement.getMaxSupportedResolution(), boldTextStyle);
            Label requiredBZCapacityAndMemoryTypeLabel = createBoldLabel("Min Required BZ Capacity: " + announcement.getMinRequiredBZCapacity() + "  |  Memory Type: " + announcement.getMemoryType(), boldTextStyle);
            Label producingCountryAnd3DApisLabel = createBoldLabel("Producing Country: " + announcement.getProducingCountry() + "  |  Supported 3D APIs: " + announcement.getSupported3DApis(), boldTextStyle);
            Label formFactorAndCoolingSystemLabel = createBoldLabel("Form Factor: " + announcement.getFormFactor() + "  |  Cooling System Type: " + announcement.getCoolingSystemType(), boldTextStyle);
            Label guaranteeAndPriceLabel = createBoldLabel("Guarantee: " + announcement.getGuarantee() + "  |  Price: " + announcement.getPrice(), boldTextStyle);
            Label brandAndManagerLabel = createBoldLabel("Brand: " + announcement.getBrand() + "  |  Manager: " + announcement.getManagerUsername(), boldTextStyle);
            Label dateLabel = createBoldLabel("Date: " + announcement.getDate(), boldTextStyle);
            Label availableQuantityLabel = createBoldLabel("Available Quantity: " + announcement.getAvailableQuantity(), boldTextStyle);
            Label descriptionLabel = createBoldLabel("Description: " + description, boldTextStyle);
            descriptionLabel.setMaxWidth(600);
            descriptionLabel.setWrapText(true);

            Button addToFavoritesButton = ButtonStyle.createStyledButton("    Like    ");
            addToFavoritesButton.setOnAction(e -> handleAddToFavorites(announcement));

            Button buyButton = ButtonStyle.createStyledButton("    Buy    ");
            buyButton.setOnAction(e -> createContract.showContractFormOrAddToExistingContract(announcement, sessionManager.getCurrentClientName()));

            HBox buttonBox1 = new HBox(10);
            buttonBox1.getChildren().addAll(addToFavoritesButton, buyButton);

            if (announcement.getAvailableQuantity() == 0) {
                Button orderSupplyButton = ButtonStyle.createStyledButton(" Order Delivery ");
                orderSupplyButton.setOnAction(e -> new OrderApplication().handleOrderApplication(announcement));
                buttonBox1.getChildren().add(orderSupplyButton);
            }

            if (sessionManager.isManagerEnter() && sessionManager.getCurrentManagerName().equals(announcement.getManagerUsername())) {
                Button editButton = ButtonStyle.createStyledButton("    Edit    ");
                editButton.setOnAction(e -> new ProductEdit().handleEditProduct(announcement));

                Button deleteButton = ButtonStyle.createStyledButton("  Delete  ");
                deleteButton.setOnAction(e -> new ProductDelete().handleDeleteProduct(announcement));

                buttonBox1.getChildren().addAll(editButton, deleteButton);
            }

            frame.getChildren().addAll(
                    titleAndProductIdLabel, graphicsAndMemoryFrequencyLabel, coreAndMemoryCapacityLabel,
                    bitSizeMemoryBusAndResolutionLabel, requiredBZCapacityAndMemoryTypeLabel,
                    producingCountryAnd3DApisLabel, formFactorAndCoolingSystemLabel,
                    guaranteeAndPriceLabel, brandAndManagerLabel, availableQuantityLabel, dateLabel, descriptionLabel,
                    buttonBox1
            );

            return frame;
        }

        private Label createBoldLabel(String text, String style) {
            Label label = new Label(text);
            label.setStyle(style);
            label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            return label;
        }

        private void handleAddToFavorites(Announcement announcement) {
            String username = sessionManager.isManagerEnter() ? sessionManager.getCurrentManagerName() : sessionManager.getCurrentClientName();
            System.out.println("Adding to favorites. Username: " + username);

            try {
                if (announcement == null) {
                    throw new IllegalArgumentException("Announcement is null");
                }

                if (!sessionManager.isClientEnter() && !sessionManager.isManagerEnter()) {
                    System.out.println("User is not logged in");
                    alertService.showErrorAlert("Please log in or register to add items to favorites.");
                    return;
                }

                AddToFavorite addToFavorite = new AddToFavorite();
                addToFavorite.handleAddToFavoritesButtonClick(announcement, username);
            } catch (Exception ex) {
                ex.printStackTrace();
                alertService.showErrorAlert("Error handling add to favorites: " + ex.getMessage());
            }
        }
    }
}