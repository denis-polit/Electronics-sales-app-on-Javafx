import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddSuppliersProductWindow {

    private final FirstConnectionToDataBase connectionToDataBase;
    private int supplierId = -1;
    private String supplierContact;
    private LocalDate currentDate = LocalDate.now();
    private String creatorName;

    public AddSuppliersProductWindow() {
        try {
            // Initialize database connection
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            showErrorAlert("Failed to establish database connection: " + e.getMessage());
            throw new RuntimeException("Failed to establish database connection", e);
        }

        // Determine creator name based on session
        if (SessionManager.getInstance().isManagerEnter()) {
            creatorName = SessionManager.getInstance().getCurrentManagerName();
        } else {
            creatorName = SessionManager.getInstance().getCurrentClientName();
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void showAddSpProductForm() {
        Stage addProductStage = new Stage();
        addProductStage.setTitle("Add Product");
        addProductStage.initStyle(StageStyle.DECORATED);

        Font labelFont = Font.font("Gotham", FontWeight.NORMAL, 16);

        Label supplierNameLabel = new Label("Supplier Name:");
        supplierNameLabel.setFont(labelFont);
        supplierNameLabel.setTextFill(Color.WHITE);

        ComboBox<String> supplierNameComboBox = new ComboBox<>();
        supplierNameComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

        List<String> supplierNames;

        try {
            supplierNames = getSupplierNames();
            supplierNameComboBox.setItems(FXCollections.observableArrayList(supplierNames));
            supplierNameComboBox.setOnAction(event -> {
                String selectedName = supplierNameComboBox.getValue();
                if (selectedName != null) {
                    try {
                        supplierId = getSupplierIdByName(selectedName);
                        System.out.println("Selected Supplier ID: " + supplierId);
                        supplierContact = getSupplierContactsByName(selectedName);
                        System.out.println("Selected Supplier Contact: " + supplierContact);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Label graphicsChipLabel = new Label("Graphics Chip:");
        graphicsChipLabel.setFont(labelFont);
        graphicsChipLabel.setTextFill(Color.WHITE);
        TextField graphicsChipTextField = new TextField();
        graphicsChipTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(graphicsChipTextField, "Enter the graphics chip");

        Label memoryFrequencyLabel = new Label("Memory Frequency:");
        memoryFrequencyLabel.setFont(labelFont);
        memoryFrequencyLabel.setTextFill(Color.WHITE);
        TextField memoryFrequencyTextField = new TextField();
        memoryFrequencyTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(memoryFrequencyTextField, "Enter the memory frequency");

        Label coreFrequencyLabel = new Label("Core Frequency:");
        coreFrequencyLabel.setFont(labelFont);
        coreFrequencyLabel.setTextFill(Color.WHITE);
        TextField coreFrequencyTextField = new TextField();
        coreFrequencyTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(coreFrequencyTextField, "Enter the core frequency");

        Label memoryCapacityLabel = new Label("Memory Capacity:");
        memoryCapacityLabel.setFont(labelFont);
        memoryCapacityLabel.setTextFill(Color.WHITE);
        TextField memoryCapacityTextField = new TextField();
        memoryCapacityTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(memoryCapacityTextField, "Enter the memory capacity");

        Label bitSizeMemoryBusLabel = new Label("Bit Size Memory Bus:");
        bitSizeMemoryBusLabel.setFont(labelFont);
        bitSizeMemoryBusLabel.setTextFill(Color.WHITE);
        TextField bitSizeMemoryBusTextField = new TextField();
        bitSizeMemoryBusTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(bitSizeMemoryBusTextField, "Enter the bit size of the memory bus");

        Label maxSupportedResolutionLabel = new Label("Max Supported Resolution:");
        maxSupportedResolutionLabel.setFont(labelFont);
        maxSupportedResolutionLabel.setTextFill(Color.WHITE);
        TextField maxSupportedResolutionTextField = new TextField();
        maxSupportedResolutionTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(maxSupportedResolutionTextField, "Select max supported resolution");

        Label minRequiredBZCapacityLabel = new Label("Min Required BZ Capacity:");
        minRequiredBZCapacityLabel.setFont(labelFont);
        minRequiredBZCapacityLabel.setTextFill(Color.WHITE);
        TextField minRequiredBZCapacityTextField = new TextField();
        minRequiredBZCapacityTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(minRequiredBZCapacityTextField, "Enter the minimum required BZ capacity");

        Label memoryTypeLabel = new Label("Memory Type:");
        memoryTypeLabel.setFont(labelFont);
        memoryTypeLabel.setTextFill(Color.WHITE);
        TextField memoryTypeTextField = new TextField();
        memoryTypeTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(memoryTypeTextField, "Select memory type");

        Label producingCountryLabel = new Label("Producing Country:");
        producingCountryLabel.setFont(labelFont);
        producingCountryLabel.setTextFill(Color.WHITE);
        TextField producingCountryTextField = new TextField();
        producingCountryTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(producingCountryTextField, "Enter the producing country");

        Label supported3DAPIsLabel = new Label("Supported 3D APIs:");
        supported3DAPIsLabel.setFont(labelFont);
        supported3DAPIsLabel.setTextFill(Color.WHITE);
        TextField supported3DAPIsTextField = new TextField();
        supported3DAPIsTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(supported3DAPIsTextField, "Enter the supported 3D APIs");

        Label formFactorLabel = new Label("Form Factor:");
        formFactorLabel.setFont(labelFont);
        formFactorLabel.setTextFill(Color.WHITE);
        TextField formFactorTextField = new TextField();
        formFactorTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(formFactorTextField, "Select form factor");

        Label coolingSystemTypeLabel = new Label("Cooling System Type:");
        coolingSystemTypeLabel.setFont(labelFont);
        coolingSystemTypeLabel.setTextFill(Color.WHITE);
        TextField coolingSystemTypeTextField = new TextField();
        coolingSystemTypeTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(coolingSystemTypeTextField, "Select cooling system type");

        Label guaranteeLabel = new Label("Guarantee:");
        guaranteeLabel.setFont(labelFont);
        guaranteeLabel.setTextFill(Color.WHITE);
        TextField guaranteeTextField = new TextField();
        guaranteeTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(guaranteeTextField, "Enter the guarantee");

        Label priceLabel = new Label("Price:");
        priceLabel.setFont(labelFont);
        priceLabel.setTextFill(Color.WHITE);
        TextField priceTextField = new TextField();
        priceTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(priceTextField, "Enter the price");

        Label wholesalePriceLabel = new Label("Wholesale Price:");
        wholesalePriceLabel.setFont(labelFont);
        wholesalePriceLabel.setTextFill(Color.WHITE);
        TextField wholesalePriceTextField = new TextField();
        wholesalePriceTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(wholesalePriceTextField, "Enter the wholesale price");

        Label brandLabel = new Label("Brand:");
        brandLabel.setFont(labelFont);
        brandLabel.setTextFill(Color.WHITE);
        TextField brandTextField = new TextField();
        brandTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(brandTextField, "Enter the brand");

        Label productTitleLabel = new Label("Product Title:");
        productTitleLabel.setFont(labelFont);
        productTitleLabel.setTextFill(Color.WHITE);
        TextField productTitleTextField = new TextField();
        productTitleTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(productTitleTextField, "Enter the title of the announcement");

        Label productDescriptionLabel = new Label("Product Description:");
        productDescriptionLabel.setFont(labelFont);
        productDescriptionLabel.setTextFill(Color.WHITE);
        TextField productDescriptionTextField = new TextField();
        productDescriptionTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(productDescriptionTextField, "Enter the description");

        Label availableQuantityLabel = new Label("Available Quantity:");
        availableQuantityLabel.setFont(labelFont);
        availableQuantityLabel.setTextFill(Color.WHITE);
        TextField availableQuantityTextField = new TextField();
        availableQuantityTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(availableQuantityTextField, "Enter the available quantity");

        Label wholesaleQuantityLabel = new Label("Wholesale Quantity:");
        wholesaleQuantityLabel.setFont(labelFont);
        wholesaleQuantityLabel.setTextFill(Color.WHITE);
        TextField wholesaleQuantityTextField = new TextField();
        wholesaleQuantityTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(wholesaleQuantityTextField, "Enter min wholesale quantity");

        Label sourceLabel = new Label("Source:");
        sourceLabel.setFont(labelFont);
        sourceLabel.setTextFill(Color.WHITE);
        TextField sourceTextField = new TextField();
        sourceTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(sourceTextField, "Enter website url");

        Button cancelButton = borderStyledButton("Cancel");
        cancelButton.setOnAction(cancelEvent -> {
            clearFields(
                    supplierNameComboBox, graphicsChipTextField,
                    memoryFrequencyTextField, coreFrequencyTextField, memoryCapacityTextField, bitSizeMemoryBusTextField,
                    maxSupportedResolutionTextField, minRequiredBZCapacityTextField, memoryTypeTextField,
                    producingCountryTextField, supported3DAPIsTextField, formFactorTextField, coolingSystemTypeTextField,
                    guaranteeTextField, priceTextField, wholesalePriceTextField, brandTextField, productTitleTextField,
                    productDescriptionTextField, availableQuantityTextField,
                    wholesaleQuantityTextField, sourceTextField
            );
            addProductStage.close();
        });

        Button addButton = borderStyledButton("Save");
        addButton.setOnAction(e -> {
            String supplierName = supplierNameComboBox.getValue();
            double retailPrice = Double.parseDouble(priceTextField.getText());
            double wholesalePrice = Double.parseDouble(wholesalePriceTextField.getText());

            if (supplierName == null || supplierName.isEmpty()) {
                showErrorAlert("Please select a supplier.");
                return;
            }

            TextField[] fields = {graphicsChipTextField, memoryFrequencyTextField, coreFrequencyTextField,
                    memoryCapacityTextField, bitSizeMemoryBusTextField, maxSupportedResolutionTextField,
                    minRequiredBZCapacityTextField, memoryTypeTextField, producingCountryTextField,
                    supported3DAPIsTextField, formFactorTextField, coolingSystemTypeTextField, guaranteeTextField,
                    priceTextField, wholesalePriceTextField, brandTextField, productTitleTextField,
                    productDescriptionTextField, availableQuantityTextField, wholesaleQuantityTextField,
                    sourceTextField};

            for (TextField field : fields) {
                if (isEmptyField(field)) {
                    showErrorAlert("Please fill in all fields.");
                    return;
                }
            }

            if (wholesalePrice >= retailPrice) {
                showErrorAlert("Wholesale price should be lower than retail price.");
                return;
            }

            String sourceUrl = sourceTextField.getText();
            if (!isValidURL(sourceUrl)) {
                showErrorAlert("Invalid URL format");
                return;
            }

            saveSupplierProductToDatabase(supplierId, supplierName, supplierContact, graphicsChipTextField,
                    memoryFrequencyTextField, coreFrequencyTextField, memoryCapacityTextField, bitSizeMemoryBusTextField,
                    maxSupportedResolutionTextField, minRequiredBZCapacityTextField, memoryTypeTextField,
                    producingCountryTextField, supported3DAPIsTextField, formFactorTextField,
                    coolingSystemTypeTextField, guaranteeTextField, priceTextField, wholesalePriceTextField,
                    brandTextField, productTitleTextField, currentDate, productDescriptionTextField,
                    creatorName, availableQuantityTextField, wholesaleQuantityTextField, sourceTextField);
        });

        HBox buttonPane = new HBox(10);
        buttonPane.getChildren().addAll(cancelButton, addButton);

        VBox addFormLayout = new VBox(10);
        addFormLayout.setAlignment(Pos.CENTER);
        addFormLayout.setPadding(new Insets(10));
        addFormLayout.setStyle("-fx-background-color: black;");

        addFormLayout.getChildren().addAll(
                supplierNameLabel, supplierNameComboBox,
                graphicsChipLabel, graphicsChipTextField,
                memoryFrequencyLabel, memoryFrequencyTextField,
                coreFrequencyLabel, coreFrequencyTextField,
                memoryCapacityLabel, memoryCapacityTextField,
                bitSizeMemoryBusLabel, bitSizeMemoryBusTextField,
                maxSupportedResolutionLabel, maxSupportedResolutionTextField,
                minRequiredBZCapacityLabel, minRequiredBZCapacityTextField,
                memoryTypeLabel, memoryTypeTextField,
                producingCountryLabel, producingCountryTextField,
                supported3DAPIsLabel, supported3DAPIsTextField,
                formFactorLabel, formFactorTextField,
                coolingSystemTypeLabel, coolingSystemTypeTextField,
                guaranteeLabel, guaranteeTextField,
                priceLabel, priceTextField,
                wholesalePriceLabel, wholesalePriceTextField,
                brandLabel, brandTextField,
                productTitleLabel, productTitleTextField,
                productDescriptionLabel, productDescriptionTextField,
                availableQuantityLabel, availableQuantityTextField,
                wholesaleQuantityLabel, wholesaleQuantityTextField,
                sourceLabel, sourceTextField,
                buttonPane
        );

        applyStyleToFields(
                supplierNameComboBox, graphicsChipTextField,
                memoryFrequencyTextField, coreFrequencyTextField, memoryCapacityTextField, bitSizeMemoryBusTextField,
                maxSupportedResolutionTextField, minRequiredBZCapacityTextField, memoryTypeTextField,
                producingCountryTextField, supported3DAPIsTextField, formFactorTextField, coolingSystemTypeTextField,
                guaranteeTextField, priceTextField, wholesalePriceTextField, brandTextField, productTitleTextField,
                productDescriptionTextField, availableQuantityTextField,
                wholesaleQuantityTextField, sourceTextField
        );

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(addFormLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: black;");

        Scene addFormScene = new Scene(scrollPane, 400, 600);
        addFormScene.setFill(Color.BLACK);

        addProductStage.setScene(addFormScene);
        addProductStage.show();
    }

    private void saveSupplierProductToDatabase(int supplierId, String supplierName, String supplierContract, TextField graphicsChipTextField,
                                               TextField memoryFrequencyTextField, TextField coreFrequencyTextField, TextField memoryCapacityTextField,
                                               TextField bitSizeMemoryBusTextField, TextField maxSupportedResolutionTextField, TextField minRequiredBZCapacityTextField,
                                               TextField memoryTypeTextField, TextField producingCountryTextField, TextField supported3DAPIsTextField,
                                               TextField formFactorTextField, TextField coolingSystemTypeTextField, TextField guaranteeTextField,
                                               TextField priceTextField, TextField wholesalePriceTextField, TextField brandTextField,
                                               TextField productTitleTextField, LocalDate currentDate, TextField productDescriptionTextField,
                                               String creatorName, TextField availableQuantityTextField, TextField wholesaleQuantityTextField,
                                               TextField sourceTextField) {
        try {
            String insertQuery = "INSERT INTO suppliers_product (spp_suppliers_id, spp_supplier_name, spp_supplier_contact, spp_graphics_chip, spp_memory_frequency, " +
                    "spp_core_frequency, spp_memory_capacity, spp_bit_size_memory_bus, spp_maximum_supported_resolution, spp_minimum_required_BZ_capacity, " +
                    "spp_memory_type, spp_producing_country, spp_supported_3D_APIs, spp_form_factor, spp_type_of_cooling_system, spp_guarantee, spp_price, " +
                    "spp_wholesale_price, spp_brand, spp_product_title, spp_creation_date, spp_product_description, spp_creator_name, spp_available_quantity, " +
                    "spp_wholesale_quantity, spp_source) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            java.sql.Date sqlDate = java.sql.Date.valueOf(currentDate);

            try (Connection conn = connectionToDataBase.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setInt(1, supplierId);
                pstmt.setString(2, supplierName);
                pstmt.setString(3, supplierContract);
                pstmt.setString(4, graphicsChipTextField.getText());
                pstmt.setString(5, memoryFrequencyTextField.getText());
                pstmt.setString(6, coreFrequencyTextField.getText());
                pstmt.setString(7, memoryCapacityTextField.getText());
                pstmt.setString(8, bitSizeMemoryBusTextField.getText());
                pstmt.setString(9, maxSupportedResolutionTextField.getText());
                pstmt.setString(10, minRequiredBZCapacityTextField.getText());
                pstmt.setString(11, memoryTypeTextField.getText());
                pstmt.setString(12, producingCountryTextField.getText());
                pstmt.setString(13, supported3DAPIsTextField.getText());
                pstmt.setString(14, formFactorTextField.getText());
                pstmt.setString(15, coolingSystemTypeTextField.getText());
                pstmt.setString(16, guaranteeTextField.getText());
                pstmt.setString(17, priceTextField.getText());
                pstmt.setString(18, wholesalePriceTextField.getText());
                pstmt.setString(19, brandTextField.getText());
                pstmt.setString(20, productTitleTextField.getText());
                pstmt.setDate(21, sqlDate);
                pstmt.setString(22, productDescriptionTextField.getText());
                pstmt.setString(23, creatorName);
                pstmt.setString(24, availableQuantityTextField.getText());
                pstmt.setString(25, wholesaleQuantityTextField.getText());
                pstmt.setString(26, sourceTextField.getText());

                pstmt.executeUpdate();

                showSuccessAlert("Product successfully added.");
                clearFields(
                        graphicsChipTextField, memoryFrequencyTextField, coreFrequencyTextField, memoryCapacityTextField,
                        bitSizeMemoryBusTextField, maxSupportedResolutionTextField, minRequiredBZCapacityTextField, memoryTypeTextField,
                        producingCountryTextField, supported3DAPIsTextField, formFactorTextField, coolingSystemTypeTextField, guaranteeTextField,
                        priceTextField, wholesalePriceTextField, brandTextField, productTitleTextField, productDescriptionTextField,
                        availableQuantityTextField, wholesaleQuantityTextField, sourceTextField
                );
            }
        } catch (SQLException e) {
            showErrorAlert("Error adding product: " + e.getMessage());
        }
    }

    private List<String> getSupplierNames() throws SQLException {
        List<String> supplierNames = new ArrayList<>();
        try (Connection connection = establishDBConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT suppliers_title FROM suppliers")) {
            while (resultSet.next()) {
                String supplierName = resultSet.getString("suppliers_title");
                supplierNames.add(supplierName);
            }
        }
        return supplierNames;
    }

    private String getSupplierContactsByName(String selectedName) throws SQLException {
        String query = "SELECT suppliers_contact_number FROM suppliers WHERE suppliers_title = ?";
        try (PreparedStatement statement = connectionToDataBase.getConnection().prepareStatement(query)) {
            statement.setString(1, selectedName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("suppliers_contact_number");
                }
            }
        }
        throw new SQLException("Supplier not found with name: " + selectedName);
    }

    private int getSupplierIdByName(String selectedName) throws SQLException {
        String query = "SELECT suppliers_id FROM suppliers WHERE suppliers_title = ?";
        try (PreparedStatement statement = connectionToDataBase.getConnection().prepareStatement(query)) {
            statement.setString(1, selectedName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("suppliers_id");
                }
            }
        }
        throw new SQLException("Supplier not found with name: " + selectedName);
    }

    private void addTooltip(Control control, String text) {
        Tooltip tooltip = new Tooltip(text);
        Tooltip.install(control, tooltip);
    }

    private void clearFields(Control... controls) {
        for (Control control : controls) {
            if (control instanceof TextField) {
                ((TextField) control).clear();
            } else if (control instanceof TextArea) {
                ((TextArea) control).clear();
            }
        }
    }

    private void applyStyleToFields(Control... controls) {
        for (Control control : controls) {
            control.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        }
    }

    private boolean isEmptyField(TextField textField) {
        return textField.getText().trim().isEmpty();
    }

    private boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
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

    private void showErrorAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    private void showSuccessAlert(String successMessage) {
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText(null);
        successAlert.setContentText(successMessage);
        successAlert.showAndWait();
    }
}
