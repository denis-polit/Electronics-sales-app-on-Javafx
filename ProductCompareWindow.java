import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductCompareWindow {

    private FirstConnectionToDataBase connectionToDataBase;
    private final LabelFactory labelFactory;

    ProductCompareWindow(){
        labelFactory = new LabelFactory();
        AlertService alertService = new AlertServiceImpl();
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

    public void showCompareForm() {
        Scene compareScene = new Scene(new StackPane(), 400, 400);
        compareScene.setFill(Color.BLACK);

        StackPane root = (StackPane) compareScene.getRoot();
        root.setStyle("-fx-background-color: black;");

        VBox compareLayout = new VBox(10);
        compareLayout.setAlignment(Pos.CENTER);

        Font labelFont = Font.font("Gotham", FontWeight.NORMAL, 16);

        Label productId1Label = new Label("Product ID 1:");
        productId1Label.setFont(labelFont);
        productId1Label.setTextFill(Color.WHITE);
        TextField productId1Field = new TextField();
        productId1Field.setPromptText("Enter Product ID 1");
        productId1Field.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(productId1Field, "Enter Product ID 1");

        Label productId2Label = new Label("Product ID 2:");
        productId2Label.setFont(labelFont);
        productId2Label.setTextFill(Color.WHITE);
        TextField productId2Field = new TextField();
        productId2Field.setPromptText("Enter Product ID 2");
        productId2Field.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(productId2Field, "Enter Product ID 2");

        Button compareButton = ButtonStyle.expandPaneStyledButton("Compare");
        compareButton.setOnAction(event -> {
            String productId1 = productId1Field.getText();
            String productId2 = productId2Field.getText();

            Announcement announcement1 = getAnnouncementByProductId(productId1);
            Announcement announcement2 = getAnnouncementByProductId(productId2);

            if (announcement1 != null && announcement2 != null) {
                compareProducts(announcement1, announcement2);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("One or both product IDs are invalid.");
                alert.showAndWait();
            }
        });

        compareLayout.getChildren().addAll(productId1Label, productId1Field, productId2Label, productId2Field, compareButton);

        root.getChildren().addAll(compareLayout);

        Stage compareStage = new Stage();
        compareStage.initModality(Modality.APPLICATION_MODAL);
        compareStage.setTitle("Compare Products");
        compareStage.setScene(compareScene);
        compareStage.showAndWait();
    }

    private Announcement getAnnouncementByProductId(String productId) {
        Announcement announcement = null;

        String sql = "SELECT * FROM catalog WHERE product_id = ?";

        try (Connection connection = establishDBConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, productId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                announcement = new Announcement(
                        resultSet.getString("product_title"),
                        resultSet.getInt("product_id"),
                        resultSet.getString("graphics_chip"),
                        resultSet.getDouble("memory_frequency"),
                        resultSet.getDouble("core_frequency"),
                        resultSet.getInt("memory_capacity"),
                        resultSet.getInt("bit_size_memory_bus"),
                        resultSet.getString("maximum_supported_resolution"),
                        resultSet.getInt("minimum_required_BZ_capacity"),
                        resultSet.getString("memory_type"),
                        resultSet.getString("producing_country"),
                        resultSet.getString("supported_3D_APIS"),
                        resultSet.getString("form_factor"),
                        resultSet.getString("type_of_cooling_system"),
                        resultSet.getInt("guarantee"),
                        resultSet.getDouble("price"),
                        resultSet.getDouble("wholesale_price"),
                        resultSet.getInt("wholesale_quantity"),
                        resultSet.getString("brand"),
                        resultSet.getString("product_description"),
                        resultSet.getTimestamp("creation_data").toLocalDateTime(),
                        resultSet.getString("creator_name"),
                        resultSet.getInt("available_quantity")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return announcement;
    }

    private void compareProducts(Announcement announcement1, Announcement announcement2) {
        String boldTextStyle = "-fx-text-fill: white";
        String highlightStyle = "-fx-background-color: dodgerblue; -fx-text-fill: white";

        GridPane comparisonLayout = new GridPane();
        comparisonLayout.setPadding(new Insets(20));
        comparisonLayout.setHgap(10);
        comparisonLayout.setVgap(10);
        comparisonLayout.setStyle("-fx-background-color: black;");

        Label titleAndProductIdLabel1 = labelFactory.createLabelWithHighlight("Title: " + announcement1.getTitle() + "  |  Product ID: " + announcement1.getProductId(), boldTextStyle);
        Label graphicsChipLabel1 = labelFactory.createLabelWithHighlightForChip("Graphics Chip: " + announcement1.getGraphicsChip(), announcement1.getGraphicsChip(), announcement2.getGraphicsChip(), boldTextStyle, highlightStyle);
        Label memoryFrequencyLabel1 = labelFactory.createLabelWithHighlight("Memory Frequency: " + announcement1.getMemoryFrequency(), announcement1.getMemoryFrequency(), announcement2.getMemoryFrequency(), boldTextStyle, highlightStyle);
        Label coreFrequencyLabel1 = labelFactory.createLabelWithHighlight("Core Frequency: " + announcement1.getCoreFrequency(), announcement1.getCoreFrequency(), announcement2.getCoreFrequency(), boldTextStyle, highlightStyle);
        Label memoryCapacityLabel1 = labelFactory.createLabelWithHighlight("Memory Capacity: " + announcement1.getMemoryCapacity(), announcement1.getMemoryCapacity(), announcement2.getMemoryCapacity(), boldTextStyle, highlightStyle);
        Label bitSizeMemoryBusLabel1 = labelFactory.createLabelWithHighlight("Bit Size Memory Bus: " + announcement1.getBitSizeMemoryBus(), announcement1.getBitSizeMemoryBus(), announcement2.getBitSizeMemoryBus(), boldTextStyle, highlightStyle);
        Label maxSupportedResolutionLabel1 = labelFactory.createLabelWithHighlight("Max Supported Resolution: " + announcement1.getMaxSupportedResolution(), announcement1.getMaxSupportedResolution(), announcement2.getMaxSupportedResolution(), boldTextStyle, highlightStyle);
        Label minRequiredBZCapacityLabel1 = labelFactory.createLabelWithHighlight("Min Required BZ Capacity: " + announcement1.getMinRequiredBZCapacity(), announcement1.getMinRequiredBZCapacity(), announcement2.getMinRequiredBZCapacity(), boldTextStyle, highlightStyle);
        Label memoryTypeLabel1 = labelFactory.createLabelWithHighlight("Memory Type: " + announcement1.getMemoryType(), boldTextStyle);
        Label producingCountryLabel1 = labelFactory.createLabelWithHighlight("Producing Country: " + announcement1.getProducingCountry(), boldTextStyle);
        Label supported3DApisLabel1 = labelFactory.createLabelWithHighlight("Supported 3D APIs: " + announcement1.getSupported3DApis(), boldTextStyle);
        Label formFactorLabel1 = labelFactory.createLabelWithHighlight("Form Factor: " + announcement1.getFormFactor(), boldTextStyle);
        Label coolingSystemLabel1 = labelFactory.createLabelWithHighlight("Cooling System Type: " + announcement1.getCoolingSystemType(), boldTextStyle);
        Label guaranteeLabel1 = labelFactory.createLabelWithHighlight("Guarantee: " + announcement1.getGuarantee(), announcement1.getGuarantee(), announcement2.getGuarantee(), boldTextStyle, highlightStyle);
        Label priceLabel1 = labelFactory.createLabelWithHighlightForPrice("Price: " + announcement1.getPrice(), announcement1.getPrice(), announcement2.getPrice(), boldTextStyle, highlightStyle, null, null);
        Label brandLabel1 = labelFactory.createLabelWithHighlight("Brand: " + announcement1.getBrand(), boldTextStyle);

        Label titleAndProductIdLabel2 = labelFactory.createLabelWithHighlight("Title: " + announcement2.getTitle() + "  |  Product ID: " + announcement2.getProductId(), boldTextStyle);
        Label graphicsChipLabel2 = labelFactory.createLabelWithHighlightForChip("Graphics Chip: " + announcement2.getGraphicsChip(), announcement2.getGraphicsChip(), announcement1.getGraphicsChip(), boldTextStyle, highlightStyle);
        Label memoryFrequencyLabel2 = labelFactory.createLabelWithHighlight("Memory Frequency: " + announcement2.getMemoryFrequency(), announcement2.getMemoryFrequency(), announcement1.getMemoryFrequency(), boldTextStyle, highlightStyle);
        Label coreFrequencyLabel2 = labelFactory.createLabelWithHighlight("Core Frequency: " + announcement2.getCoreFrequency(), announcement2.getCoreFrequency(), announcement1.getCoreFrequency(), boldTextStyle, highlightStyle);
        Label memoryCapacityLabel2 = labelFactory.createLabelWithHighlight("Memory Capacity: " + announcement2.getMemoryCapacity(), announcement2.getMemoryCapacity(), announcement1.getMemoryCapacity(), boldTextStyle, highlightStyle);
        Label bitSizeMemoryBusLabel2 = labelFactory.createLabelWithHighlight("Bit Size Memory Bus: " + announcement2.getBitSizeMemoryBus(), announcement2.getBitSizeMemoryBus(), announcement1.getBitSizeMemoryBus(), boldTextStyle, highlightStyle);
        Label maxSupportedResolutionLabel2 = labelFactory.createLabelWithHighlight("Max Supported Resolution: " + announcement2.getMaxSupportedResolution(), announcement2.getMaxSupportedResolution(), announcement1.getMaxSupportedResolution(), boldTextStyle, highlightStyle);
        Label minRequiredBZCapacityLabel2 = labelFactory.createLabelWithHighlight("Min Required BZ Capacity: " + announcement2.getMinRequiredBZCapacity(), announcement2.getMinRequiredBZCapacity(), announcement1.getMinRequiredBZCapacity(), boldTextStyle, highlightStyle);
        Label memoryTypeLabel2 = labelFactory.createLabelWithHighlight("Memory Type: " + announcement2.getMemoryType(), boldTextStyle);
        Label producingCountryLabel2 = labelFactory.createLabelWithHighlight("Producing Country: " + announcement2.getProducingCountry(), boldTextStyle);
        Label supported3DApisLabel2 = labelFactory.createLabelWithHighlight("Supported 3D APIs: " + announcement2.getSupported3DApis(), boldTextStyle);
        Label formFactorLabel2 = labelFactory.createLabelWithHighlight("Form Factor: " + announcement2.getFormFactor(), boldTextStyle);
        Label coolingSystemLabel2 = labelFactory.createLabelWithHighlight("Cooling System Type: " + announcement2.getCoolingSystemType(), boldTextStyle);
        Label guaranteeLabel2 = labelFactory.createLabelWithHighlight("Guarantee: " + announcement2.getGuarantee(), announcement2.getGuarantee(), announcement1.getGuarantee(), boldTextStyle, highlightStyle);
        Label priceLabel2 = labelFactory.createLabelWithHighlightForPrice("Price: " + announcement2.getPrice(), announcement1.getPrice(), announcement2.getPrice(), boldTextStyle, highlightStyle, priceLabel1, null);
        Label brandLabel2 = labelFactory.createLabelWithHighlight("Brand: " + announcement2.getBrand(), boldTextStyle);

        comparisonLayout.add(titleAndProductIdLabel1, 0, 0);
        comparisonLayout.add(graphicsChipLabel1, 0, 1);
        comparisonLayout.add(memoryFrequencyLabel1, 0, 2);
        comparisonLayout.add(coreFrequencyLabel1, 0, 3);
        comparisonLayout.add(memoryCapacityLabel1, 0, 4);
        comparisonLayout.add(bitSizeMemoryBusLabel1, 0, 5);
        comparisonLayout.add(maxSupportedResolutionLabel1, 0, 6);
        comparisonLayout.add(minRequiredBZCapacityLabel1, 0, 7);
        comparisonLayout.add(memoryTypeLabel1, 0, 8);
        comparisonLayout.add(producingCountryLabel1, 0, 9);
        comparisonLayout.add(supported3DApisLabel1, 0, 10);
        comparisonLayout.add(formFactorLabel1, 0, 11);
        comparisonLayout.add(coolingSystemLabel1, 0, 12);
        comparisonLayout.add(guaranteeLabel1, 0, 13);
        comparisonLayout.add(priceLabel1, 0, 14);
        comparisonLayout.add(brandLabel1, 0, 15);

        comparisonLayout.add(titleAndProductIdLabel2, 1, 0);
        comparisonLayout.add(graphicsChipLabel2, 1, 1);
        comparisonLayout.add(memoryFrequencyLabel2, 1, 2);
        comparisonLayout.add(coreFrequencyLabel2, 1, 3);
        comparisonLayout.add(memoryCapacityLabel2, 1, 4);
        comparisonLayout.add(bitSizeMemoryBusLabel2, 1, 5);
        comparisonLayout.add(maxSupportedResolutionLabel2, 1, 6);
        comparisonLayout.add(minRequiredBZCapacityLabel2, 1, 7);
        comparisonLayout.add(memoryTypeLabel2, 1, 8);
        comparisonLayout.add(producingCountryLabel2, 1, 9);
        comparisonLayout.add(supported3DApisLabel2, 1, 10);
        comparisonLayout.add(formFactorLabel2, 1, 11);
        comparisonLayout.add(coolingSystemLabel2, 1, 12);
        comparisonLayout.add(guaranteeLabel2, 1, 13);
        comparisonLayout.add(priceLabel2, 1, 14);
        comparisonLayout.add(brandLabel2, 1, 15);

        Scene comparisonScene = new Scene(comparisonLayout, 900, 600);
        Stage comparisonStage = new Stage();
        comparisonStage.initModality(Modality.APPLICATION_MODAL);
        comparisonStage.setTitle("Product Comparison");
        comparisonStage.setScene(comparisonScene);
        comparisonStage.show();
    }

    private void addTooltip(Control control, String text) {
        Tooltip tooltip = new Tooltip(text);
        Tooltip.install(control, tooltip);
    }
}
