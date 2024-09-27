import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class StockList extends Application {

    private FirstConnectionToDataBase connectionToDataBase;
    private final SessionManager sessionManager;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    StockDB stockDB = new StockDB();

    public StockList() {
        sessionManager = SessionManager.getInstance();
        ProductCompareWindow productCompare = new ProductCompareWindow();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Starting AvaliableProductForCatalogList...");

        MenuPage menuPage = new MenuPage();
        BorderPane root = new BorderPane();

        ListView<AvaliableAnnouncement> announcementsListView = new ListView<>();
        announcementsListView.setPrefSize(890, 590);
        announcementsListView.setStyle("-fx-text-fill: white; -fx-control-inner-background: black;");
        announcementsListView.setCellFactory(param -> new AvaliableAnnouncementListCell());

        StackPane announcementsFlowPane = new StackPane();
        StockListHeader stockListHeader = new StockListHeader(announcementsFlowPane, primaryStage, sessionManager);
        stockListHeader.setAnnouncementsListView(announcementsListView);

        root.setStyle("-fx-background-color: black;");
        root.setTop(stockListHeader.getNode());

        try {
            List<AvaliableAnnouncement> announcementsList = stockDB.getAllAnnouncementsFromDatabase();
            announcementsListView.getItems().addAll(announcementsList);
            System.out.println("Announcements list populated successfully.");
        } catch (Exception e) {
            System.err.println("Failed to retrieve announcements: " + e.getMessage());
            e.printStackTrace();
        }

        VBox catalogContent = new VBox(10);
        catalogContent.getChildren().addAll(announcementsListView);
        root.setCenter(catalogContent);

        Scene scene = new Scene(root, 900, 600);
        scene.setFill(Color.WHITE);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Catalog");
        primaryStage.show();

        AnnouncementCreateWindow announcementCreateWindow = new AnnouncementCreateWindow(connectionToDataBase);

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();

        String currentManagerName = sessionManager.getCurrentManagerName();
        String employeeStatus = sessionManager.getEmployeeStatusByName(currentManagerName);
        System.out.println("Current Manager Name: " + currentManagerName);
        System.out.println("Employee Status: " + employeeStatus);
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    private class AvaliableAnnouncementListCell extends ListCell<AvaliableAnnouncement> {

        private final SessionManager sessionManager = SessionManager.getInstance();
        private final FirstConnectionToDataBase connectionToDataBase;

        public AvaliableAnnouncementListCell() {
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

            try {
                connectionToDataBase = FirstConnectionToDataBase.getInstance();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to establish database connection", e);
            }
        }

        @Override
        protected void updateItem(AvaliableAnnouncement item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setGraphic(createAnnouncementFrame(item));
            }
        }

        private VBox createAnnouncementFrame(AvaliableAnnouncement announcement) {
            VBox frame = new VBox(10);
            frame.setStyle("-fx-border-color: gray; -fx-border-width: 2; -fx-padding: 10; -fx-background-radius: 15px; -fx-border-radius: 15px;");

            String boldTextStyle = "-fx-font-weight: bold;";
            String description = announcement.getApfcProductDescription();

            Label titleAndProductIdLabel = createBoldLabel("Title: " + announcement.getApfcProductTitle() + "  |  Product ID: " + announcement.getApfcId(), boldTextStyle);
            Label graphicsAndMemoryFrequencyLabel = createBoldLabel("Graphics Chip: " + announcement.getApfcGraphicsChip() + "  |  Memory Frequency: " + announcement.getApfcMemoryFrequency(), boldTextStyle);
            Label coreAndMemoryCapacityLabel = createBoldLabel("Core Frequency: " + announcement.getApfcCoreFrequency() + "  |  Memory Capacity: " + announcement.getApfcMemoryCapacity(), boldTextStyle);
            Label bitSizeMemoryBusAndResolutionLabel = createBoldLabel("Bit Size Memory Bus: " + announcement.getApfcBitSizeMemoryBus() + "  |  Max Supported Resolution: " + announcement.getApfcMaximumSupportedResolution(), boldTextStyle);
            Label requiredBZCapacityAndMemoryTypeLabel = createBoldLabel("Min Required BZ Capacity: " + announcement.getApfcMinimumRequiredBZCapacity() + "  |  Memory Type: " + announcement.getApfcMemoryType(), boldTextStyle);
            Label producingCountryAnd3DApisLabel = createBoldLabel("Producing Country: " + announcement.getApfcProducingCountry() + "  |  Supported 3D APIs: " + announcement.getApfcSupported3DApis(), boldTextStyle);
            Label formFactorAndCoolingSystemLabel = createBoldLabel("Form Factor: " + announcement.getApfcFormFactor() + "  |  Cooling System: " + announcement.getApfcTypeOfCoolingSystem(), boldTextStyle);
            Label guaranteeAndPriceLabel = createBoldLabel("Guarantee: " + announcement.getApfcGuarantee() + "  |  Price: " + announcement.getApfcPrice(), boldTextStyle);
            Label wholesalePriceAndBrandLabel = createBoldLabel("Wholesale Price: " + announcement.getApfcWholesalePrice() + "  |  Brand: " + announcement.getApfcBrand(), boldTextStyle);
            Label creationDateAndResponsiblePersonLabel = createBoldLabel("Creation Date: " + announcement.getApfcCreationData() + "  |  Responsible Person: " + announcement.getApfcResponsiblePerson(), boldTextStyle);
            Label availableQuantityAndWholesaleQuantityLabel = createBoldLabel("Available Quantity: " + announcement.getApfcAvailableQuantity() + "  |  Wholesale Quantity: " + announcement.getApfcWholesaleQuantity(), boldTextStyle);
            Label productDescriptionLabel = createBoldLabel("Product Description: " + description, boldTextStyle);
            Label statusLabel = createBoldLabel("Status: " + announcement.getApfcStatus(), boldTextStyle);

            frame.getChildren().addAll(
                    titleAndProductIdLabel,
                    graphicsAndMemoryFrequencyLabel,
                    coreAndMemoryCapacityLabel,
                    bitSizeMemoryBusAndResolutionLabel,
                    requiredBZCapacityAndMemoryTypeLabel,
                    producingCountryAnd3DApisLabel,
                    formFactorAndCoolingSystemLabel,
                    guaranteeAndPriceLabel,
                    wholesalePriceAndBrandLabel,
                    creationDateAndResponsiblePersonLabel,
                    availableQuantityAndWholesaleQuantityLabel,
                    productDescriptionLabel,
                    statusLabel,
                    createShowMoreButton(announcement)
            );

            return frame;
        }

        private Button createShowMoreButton(AvaliableAnnouncement announcement) {
            Button button = ButtonStyle.expandPaneStyledButton("    Post    ");
            button.setOnAction(e -> handlePostButtonClick(announcement));
            return button;
        }

        private void handlePostButtonClick(AvaliableAnnouncement announcement) {
            String currentManagerName = sessionManager.getCurrentManagerName();

            if (sessionManager.isManagerEnter() && currentManagerName.equals(announcement.getApfcResponsiblePerson())) {
                try (Connection connection = connectionToDataBase.getConnection()) {
                    String insertSQL = "INSERT INTO catalog (product_id, graphics_chip, memory_frequency, core_frequency, memory_capacity, bit_size_memory_bus, " +
                            "maximum_supported_resolution, minimum_required_BZ_capacity, memory_type, producing_country, supported_3D_APIs, form_factor, type_of_cooling_system, " +
                            "guarantee, price, wholesale_price, brand, product_title, creation_data, product_description, creator_name, available_quantity, wholesale_quantity) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
                    preparedStatement.setInt(1, announcement.getApfcId());
                    preparedStatement.setString(2, announcement.getApfcGraphicsChip());
                    preparedStatement.setDouble(3, announcement.getApfcMemoryFrequency());
                    preparedStatement.setDouble(4, announcement.getApfcCoreFrequency());
                    preparedStatement.setInt(5, announcement.getApfcMemoryCapacity());
                    preparedStatement.setInt(6, announcement.getApfcBitSizeMemoryBus());
                    preparedStatement.setString(7, announcement.getApfcMaximumSupportedResolution());
                    preparedStatement.setInt(8, announcement.getApfcMinimumRequiredBZCapacity());
                    preparedStatement.setString(9, announcement.getApfcMemoryType());
                    preparedStatement.setString(10, announcement.getApfcProducingCountry());
                    preparedStatement.setString(11, announcement.getApfcSupported3DApis());
                    preparedStatement.setString(12, announcement.getApfcFormFactor());
                    preparedStatement.setString(13, announcement.getApfcTypeOfCoolingSystem());
                    preparedStatement.setInt(14, announcement.getApfcGuarantee());
                    preparedStatement.setDouble(15, announcement.getApfcPrice());
                    preparedStatement.setDouble(16, announcement.getApfcWholesalePrice());
                    preparedStatement.setString(17, announcement.getApfcBrand());
                    preparedStatement.setString(18, announcement.getApfcProductTitle());
                    preparedStatement.setObject(19, announcement.getApfcCreationData());
                    preparedStatement.setString(20, announcement.getApfcProductDescription());
                    preparedStatement.setString(21, announcement.getApfcResponsiblePerson());
                    preparedStatement.setInt(22, announcement.getApfcAvailableQuantity());
                    preparedStatement.setInt(23, announcement.getApfcWholesaleQuantity());

                    preparedStatement.executeUpdate();
                    alertService.showSuccessAlert("Announcement successfully posted to catalog!");

                } catch (SQLException e) {
                    alertService.showErrorAlert("Failed to post announcement: " + e.getMessage());
                }
            } else {
                alertService.showErrorAlert("You are not authorized to post this announcement.");
            }
        }

        private Label createBoldLabel(String text, String style) {
            Label label = new Label(text);
            label.setStyle(style);
            label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            return label;
        }
    }
}
