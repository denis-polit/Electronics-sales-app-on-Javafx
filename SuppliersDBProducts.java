import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

public class SuppliersDBProducts {

    private final SessionManager sessionManager;
    private final FirstConnectionToDataBase connectionToDataBase;

    public SuppliersDBProducts() {
        sessionManager = SessionManager.getInstance();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            showErrorAlert("Failed to establish database connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void fetchProductsFromDatabase(Supplier supplier, String supplierTitle) {
        System.out.println("Fetching products from database for supplier: " + supplierTitle);

        Stage productStage = new Stage();
        productStage.setTitle("List of Products - " + supplierTitle);
        productStage.initModality(Modality.APPLICATION_MODAL);

        VBox productLayout = new VBox(10);
        productLayout.setPadding(new Insets(10));
        productLayout.setStyle("-fx-background-color: black;");
        productLayout.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(productLayout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 300, 600);
        productStage.setScene(scene);

        productStage.show();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = establishDBConnection()) {
                    String query = "SELECT * FROM suppliers_product WHERE spp_supplier_name = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, supplierTitle);
                        ResultSet rs = stmt.executeQuery();

                        if (!rs.next()) {

                            Platform.runLater(() -> {
                                Label noResultsLabel = new Label("No advertisements found in the database for supplier: " + supplierTitle);
                                noResultsLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Gotham'; -fx-font-size: 14px;");
                                productLayout.getChildren().add(noResultsLabel);
                            });
                        } else {

                            do {
                                int productId = rs.getInt("spp_product_id");
                                int supplierId = rs.getInt("spp_suppliers_id");
                                String supplierName = rs.getString("spp_supplier_name");
                                String supplierContact = rs.getString("spp_supplier_contact");
                                String graphicsChip = rs.getString("spp_graphics_chip");
                                String memoryFrequency = rs.getString("spp_memory_frequency");
                                String coreFrequency = rs.getString("spp_core_frequency");
                                String memoryCapacity = rs.getString("spp_memory_capacity");
                                String bitSizeMemoryBus = rs.getString("spp_bit_size_memory_bus");
                                String maximumSupportedResolution = rs.getString("spp_maximum_supported_resolution");
                                String minimumRequiredBZCapacity = rs.getString("spp_minimum_required_BZ_capacity");
                                String memoryType = rs.getString("spp_memory_type");
                                String producingCountry = rs.getString("spp_producing_country");
                                String supported3DApis = rs.getString("spp_supported_3D_APIs");
                                String formFactor = rs.getString("spp_form_factor");
                                String typeOfCoolingSystem = rs.getString("spp_type_of_cooling_system");
                                String guarantee = rs.getString("spp_guarantee");
                                String price = rs.getString("spp_price");
                                String wholesalePrice = rs.getString("spp_wholesale_price");
                                String brand = rs.getString("spp_brand");
                                String productTitle = rs.getString("spp_product_title");
                                String creationDate = rs.getString("spp_creation_date");
                                String productDescription = rs.getString("spp_product_description");
                                String creatorName = rs.getString("spp_creator_name");
                                String availableQuantity = rs.getString("spp_available_quantity");
                                String wholesaleQuantity = rs.getString("spp_wholesale_quantity");
                                String sourceURL = rs.getString("spp_source");

                                Platform.runLater(() -> {
                                    Label productIdLabel = new Label("Product ID: " + productId);
                                    Label supplierIdLabel = new Label("Supplier ID: " + supplierId);
                                    Label supplierNameLabel = new Label("Supplier Name: " + supplierName);
                                    Label supplierContactLabel = new Label("Supplier Contact: " + supplierContact);
                                    Label graphicsChipLabel = new Label("Graphics Chip: " + graphicsChip);
                                    Label memoryFrequencyLabel = new Label("Memory Frequency: " + memoryFrequency);
                                    Label coreFrequencyLabel = new Label("Core Frequency: " + coreFrequency);
                                    Label memoryCapacityLabel = new Label("Memory Capacity: " + memoryCapacity);
                                    Label bitSizeMemoryBusLabel = new Label("Bit Size Memory Bus: " + bitSizeMemoryBus);
                                    Label maximumSupportedResolutionLabel = new Label("Maximum Supported Resolution: " + maximumSupportedResolution);
                                    Label minimumRequiredBZCapacityLabel = new Label("Minimum Required BZ Capacity: " + minimumRequiredBZCapacity);
                                    Label memoryTypeLabel = new Label("Memory Type: " + memoryType);
                                    Label producingCountryLabel = new Label("Producing Country: " + producingCountry);
                                    Label supported3DApisLabel = new Label("Supported 3D APIs: " + supported3DApis);
                                    Label formFactorLabel = new Label("Form Factor: " + formFactor);
                                    Label typeOfCoolingSystemLabel = new Label("Type Of Cooling System: " + typeOfCoolingSystem);
                                    Label guaranteeLabel = new Label("Guarantee: " + guarantee);
                                    Label priceLabel = new Label("Price: " + price);
                                    Label wholesalePriceLabel = new Label("Wholesale Price: " + wholesalePrice);
                                    Label brandLabel = new Label("Brand: " + brand);
                                    Label productTitleLabel = new Label("Product Title: " + productTitle);
                                    Label creationDateLabel = new Label("Creation Date: " + creationDate);
                                    Label productDescriptionLabel = new Label("Product Description: " + productDescription);
                                    Label creatorNameLabel = new Label("Creator Name: " + creatorName);
                                    Label availableQuantityLabel = new Label("Available Quantity: " + availableQuantity);
                                    Label wholesaleQuantityLabel = new Label("Wholesale Quantity: " + wholesaleQuantity);

                                    for (Label label : Arrays.asList(productIdLabel, supplierIdLabel, supplierNameLabel, supplierContactLabel, graphicsChipLabel,
                                            memoryFrequencyLabel, coreFrequencyLabel, memoryCapacityLabel, bitSizeMemoryBusLabel, maximumSupportedResolutionLabel,
                                            minimumRequiredBZCapacityLabel, memoryTypeLabel, producingCountryLabel, supported3DApisLabel, formFactorLabel,
                                            typeOfCoolingSystemLabel, guaranteeLabel, priceLabel, wholesalePriceLabel, brandLabel, productTitleLabel,
                                            creationDateLabel, productDescriptionLabel, creatorNameLabel, availableQuantityLabel, wholesaleQuantityLabel)) {
                                        label.setStyle("-fx-text-fill: white; -fx-font-family: 'Gotham'; -fx-font-size: 14px;");
                                    }

                                    Button orderButton = borderStyledButton("Order Now");
                                    orderButton.setOnAction(event -> {
                                        try {
                                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                                Desktop.getDesktop().browse(new URI(sourceURL));
                                            } else {
                                                System.out.println("Desktop browsing not supported.");
                                            }
                                        } catch (Exception e) {
                                            System.err.println("Error opening URL: " + e.getMessage());
                                            e.printStackTrace();
                                        }
                                    });

                                    Button editButton = borderStyledButton("Edit");
                                    editButton.setOnAction(event -> {
                                        EditSuppliersProduct editSuppliersProduct = new EditSuppliersProduct(productId);
                                        ScrollPane editFormScrollPane = editSuppliersProduct.createEditForm();

                                        VBox editForm = new VBox(editFormScrollPane);

                                        editForm.setStyle("-fx-background-color: black;");

                                        Stage editStage = new Stage();
                                        Scene editScene = new Scene(editForm, 400, 600);

                                        editScene.getRoot().setStyle("-fx-background-color: black;");
                                        editStage.setScene(editScene);
                                        editStage.setTitle("Edit Supplier's Product");
                                        editStage.show();
                                    });

                                    Button deleteButton = borderStyledButton("Delete");
                                    deleteButton.setOnAction(event -> {
                                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                        alert.setTitle("Confirmation Dialog");
                                        alert.setHeaderText(null);
                                        alert.setContentText("Are you sure you want to delete this advertisement?");

                                        Optional<ButtonType> result = alert.showAndWait();
                                        if (result.isPresent() && result.get() == ButtonType.OK) {
                                            int productIdToDelete = productId;

                                            String deleteQuery = "DELETE FROM suppliers_product WHERE spp_product_id = ?";
                                            try (Connection connection = establishDBConnection();
                                                 PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                                                deleteStmt.setInt(1, productIdToDelete);
                                                int rowsAffected = deleteStmt.executeUpdate();
                                                if (rowsAffected > 0) {
                                                    showSuccessAlert("Advertisement deleted successfully.");
                                                } else {
                                                    showErrorAlert("No advertisement found with ID: " + productIdToDelete);
                                                }
                                            } catch (SQLException e) {
                                                showErrorAlert("Error deleting advertisement: " + e.getMessage());
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    Button moveToWarehouseButton = borderStyledButton("Move to Stock");
                                    moveToWarehouseButton.setOnAction(event -> moveToWarehouse(productId));

                                    HBox actionButtons = new HBox(10, orderButton, editButton, deleteButton, moveToWarehouseButton);
                                    actionButtons.setAlignment(Pos.CENTER);

                                    VBox productBox = new VBox(10, productIdLabel, graphicsChipLabel, memoryFrequencyLabel, coreFrequencyLabel, memoryCapacityLabel, bitSizeMemoryBusLabel,
                                            maximumSupportedResolutionLabel, minimumRequiredBZCapacityLabel, memoryTypeLabel, producingCountryLabel, supported3DApisLabel, formFactorLabel,
                                            typeOfCoolingSystemLabel, guaranteeLabel, priceLabel, wholesalePriceLabel, brandLabel, productTitleLabel, creationDateLabel, productDescriptionLabel,
                                            creatorNameLabel, availableQuantityLabel, wholesaleQuantityLabel, actionButtons);
                                    productBox.setStyle("-fx-border-color: gray; -fx-border-width: 2; -fx-padding: 10; -fx-background-radius: 15px; -fx-border-radius: 15px;");
                                    productLayout.getChildren().add(productBox);
                                });
                            } while (rs.next());
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error fetching products from database: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };

        task.setOnFailed(event -> {
            Throwable throwable = task.getException();
            System.err.println("Error in fetchProductsFromDatabase: " + throwable.getMessage());
            throwable.printStackTrace();
        });

        new Thread(task).start();
    }

    private void moveToWarehouse(int productId) {
        Alert alert = showConfirmAlert();

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection connection = establishDBConnection()) {
                String moveQuery = "INSERT INTO available_product_for_catalog " +
                        "(apfc_graphics_chip, apfc_memory_frequency, apfc_core_frequency, apfc_memory_capacity, apfc_bit_size_memory_bus, apfc_maximum_supported_resolution, " +
                        "apfc_minimum_required_BZ_capacity, apfc_memory_type, apfc_producing_country, apfc_supported_3D_APIs, apfc_form_factor, apfc_type_of_cooling_system, " +
                        "apfc_guarantee, apfc_price, apfc_wholesale_price, apfc_brand, apfc_product_title, apfc_creation_date, apfc_product_description, apfc_creator_name, " +
                        "apfc_available_quantity, apfc_wholesale_quantity, apfc_source, apfc_status) " +
                        "SELECT spp_graphics_chip, spp_memory_frequency, spp_core_frequency, spp_memory_capacity, spp_bit_size_memory_bus, spp_maximum_supported_resolution, " +
                        "spp_minimum_required_BZ_capacity, spp_memory_type, spp_producing_country, spp_supported_3D_APIs, spp_form_factor, spp_type_of_cooling_system, " +
                        "spp_guarantee, spp_price, spp_wholesale_price, spp_brand, spp_product_title, spp_creation_date, spp_product_description, spp_creator_name, " +
                        "spp_available_quantity, spp_wholesale_quantity, spp_source, 'awaiting delivery' " +
                        "FROM suppliers_product WHERE spp_product_id = ?";

                try (PreparedStatement moveStmt = connection.prepareStatement(moveQuery)) {
                    moveStmt.setInt(1, productId);
                    int rowsAffected = moveStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        showSuccessAlert("Product moved to warehouse successfully.");
                    } else {
                        showErrorAlert("Error moving product to warehouse.");
                    }
                }
            } catch (SQLException e) {
                showErrorAlert("Error moving product to warehouse: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Button borderStyledButton(String text) {
        Button button = new Button(text);
        button.setTextFill(javafx.scene.paint.Color.WHITE);
        button.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 15));
        button.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 2px; -fx-background-radius: 15px; -fx-border-radius: 15px;");
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
            button.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: white; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        });

        return button;
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    private Alert showConfirmAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to move this product to the warehouse?");
        return alert;
    }
}
