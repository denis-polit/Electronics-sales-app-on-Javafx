import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EditSuppliersProduct {
    private int productId;
    private int supplierId;
    private String supplierContact;
    private LocalDate creationDate = LocalDate.now();
    private String creatorName;
    private Font labelFont = new Font("Arial", 14);
    private FirstConnectionToDataBase connectionToDataBase;

    private TextField graphicsChipTextField = new TextField();
    private TextField memoryFrequencyTextField = new TextField();
    private TextField coreFrequencyTextField = new TextField();
    private TextField memoryCapacityTextField = new TextField();
    private TextField bitSizeMemoryBusTextField = new TextField();
    private TextField maxSupportedResolutionTextField = new TextField();
    private TextField minRequiredBZCapacityTextField = new TextField();
    private TextField memoryTypeTextField = new TextField();
    private TextField producingCountryTextField = new TextField();
    private TextField supported3DAPIsTextField = new TextField();
    private TextField formFactorTextField = new TextField();
    private TextField coolingSystemTypeTextField = new TextField();
    private TextField guaranteeTextField = new TextField();
    private TextField priceTextField = new TextField();
    private TextField wholesalePriceTextField = new TextField();
    private TextField brandTextField = new TextField();
    private TextField productTitleTextField = new TextField();
    private TextField productDescriptionTextField = new TextField();
    private TextField availableQuantityTextField = new TextField();
    private TextField wholesaleQuantityTextField = new TextField();
    private TextField sourceTextField = new TextField();

    public EditSuppliersProduct(int productId) {
        this.productId = productId;
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public ScrollPane createEditForm() {
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

        try {
            retrieveInitialValuesFromDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Label graphicsChipLabel = new Label("Graphics Chip:");
        graphicsChipLabel.setFont(labelFont);
        graphicsChipLabel.setTextFill(Color.WHITE);
        graphicsChipTextField = new TextField();
        graphicsChipTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(graphicsChipTextField, "Enter the graphics chip");

        Label memoryFrequencyLabel = new Label("Memory Frequency:");
        memoryFrequencyLabel.setFont(labelFont);
        memoryFrequencyLabel.setTextFill(Color.WHITE);
        memoryFrequencyTextField = new TextField();
        memoryFrequencyTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(memoryFrequencyTextField, "Enter the memory frequency");

        Label coreFrequencyLabel = new Label("Core Frequency:");
        coreFrequencyLabel.setFont(labelFont);
        coreFrequencyLabel.setTextFill(Color.WHITE);
        coreFrequencyTextField = new TextField();
        coreFrequencyTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(coreFrequencyTextField, "Enter the core frequency");

        Label memoryCapacityLabel = new Label("Memory Capacity:");
        memoryCapacityLabel.setFont(labelFont);
        memoryCapacityLabel.setTextFill(Color.WHITE);
        memoryCapacityTextField = new TextField();
        memoryCapacityTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(memoryCapacityTextField, "Enter the memory capacity");

        Label bitSizeMemoryBusLabel = new Label("Bit Size Memory Bus:");
        bitSizeMemoryBusLabel.setFont(labelFont);
        bitSizeMemoryBusLabel.setTextFill(Color.WHITE);
        bitSizeMemoryBusTextField = new TextField();
        bitSizeMemoryBusTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(bitSizeMemoryBusTextField, "Enter the bit size of the memory bus");

        Label maxSupportedResolutionLabel = new Label("Max Supported Resolution:");
        maxSupportedResolutionLabel.setFont(labelFont);
        maxSupportedResolutionLabel.setTextFill(Color.WHITE);
        maxSupportedResolutionTextField = new TextField();
        maxSupportedResolutionTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(maxSupportedResolutionTextField, "Select max supported resolution");

        Label minRequiredBZCapacityLabel = new Label("Min Required BZ Capacity:");
        minRequiredBZCapacityLabel.setFont(labelFont);
        minRequiredBZCapacityLabel.setTextFill(Color.WHITE);
        minRequiredBZCapacityTextField = new TextField();
        minRequiredBZCapacityTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(minRequiredBZCapacityTextField, "Enter the minimum required BZ capacity");

        Label memoryTypeLabel = new Label("Memory Type:");
        memoryTypeLabel.setFont(labelFont);
        memoryTypeLabel.setTextFill(Color.WHITE);
        memoryTypeTextField = new TextField();
        memoryTypeTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(memoryTypeTextField, "Select memory type");

        Label producingCountryLabel = new Label("Producing Country:");
        producingCountryLabel.setFont(labelFont);
        producingCountryLabel.setTextFill(Color.WHITE);
        producingCountryTextField = new TextField();
        producingCountryTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(producingCountryTextField, "Enter the producing country");

        Label supported3DAPIsLabel = new Label("Supported 3D APIs:");
        supported3DAPIsLabel.setFont(labelFont);
        supported3DAPIsLabel.setTextFill(Color.WHITE);
        supported3DAPIsTextField = new TextField();
        supported3DAPIsTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(supported3DAPIsTextField, "Enter the supported 3D APIs");

        Label formFactorLabel = new Label("Form Factor:");
        formFactorLabel.setFont(labelFont);
        formFactorLabel.setTextFill(Color.WHITE);
        formFactorTextField = new TextField();
        formFactorTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(formFactorTextField, "Select form factor");

        Label coolingSystemTypeLabel = new Label("Cooling System Type:");
        coolingSystemTypeLabel.setFont(labelFont);
        coolingSystemTypeLabel.setTextFill(Color.WHITE);
        coolingSystemTypeTextField = new TextField();
        coolingSystemTypeTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(coolingSystemTypeTextField, "Enter the cooling system type");

        Label guaranteeLabel = new Label("Guarantee:");
        guaranteeLabel.setFont(labelFont);
        guaranteeLabel.setTextFill(Color.WHITE);
        guaranteeTextField = new TextField();
        guaranteeTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(guaranteeTextField, "Enter the guarantee period");

        Label priceLabel = new Label("Price:");
        priceLabel.setFont(labelFont);
        priceLabel.setTextFill(Color.WHITE);
        priceTextField = new TextField();
        priceTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(priceTextField, "Enter the price");

        Label wholesalePriceLabel = new Label("Wholesale Price:");
        wholesalePriceLabel.setFont(labelFont);
        wholesalePriceLabel.setTextFill(Color.WHITE);
        wholesalePriceTextField = new TextField();
        wholesalePriceTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(wholesalePriceTextField, "Enter the wholesale price");

        Label brandLabel = new Label("Brand:");
        brandLabel.setFont(labelFont);
        brandLabel.setTextFill(Color.WHITE);
        brandTextField = new TextField();
        brandTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(brandTextField, "Enter the brand");

        Label productTitleLabel = new Label("Product Title:");
        productTitleLabel.setFont(labelFont);
        productTitleLabel.setTextFill(Color.WHITE);
        productTitleTextField = new TextField();
        productTitleTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(productTitleTextField, "Enter the product title");

        Label productDescriptionLabel = new Label("Product Description:");
        productDescriptionLabel.setFont(labelFont);
        productDescriptionLabel.setTextFill(Color.WHITE);
        productDescriptionTextField = new TextField();
        productDescriptionTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(productDescriptionTextField, "Enter the product description");

        Label availableQuantityLabel = new Label("Available Quantity:");
        availableQuantityLabel.setFont(labelFont);
        availableQuantityLabel.setTextFill(Color.WHITE);
        availableQuantityTextField = new TextField();
        availableQuantityTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(availableQuantityTextField, "Enter the available quantity");

        Label wholesaleQuantityLabel = new Label("Wholesale Quantity:");
        wholesaleQuantityLabel.setFont(labelFont);
        wholesaleQuantityLabel.setTextFill(Color.WHITE);
        wholesaleQuantityTextField = new TextField();
        wholesaleQuantityTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(wholesaleQuantityTextField, "Enter the wholesale quantity");

        Label sourceLabel = new Label("Source:");
        sourceLabel.setFont(labelFont);
        sourceLabel.setTextFill(Color.WHITE);
        sourceTextField = new TextField();
        sourceTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(sourceTextField, "Enter the source URL");

        Button cancelButton = borderStyledButton("Cancel");
        cancelButton.setOnAction(event -> clearFields(
                graphicsChipTextField, memoryFrequencyTextField, coreFrequencyTextField,
                memoryCapacityTextField, bitSizeMemoryBusTextField, maxSupportedResolutionTextField,
                minRequiredBZCapacityTextField, memoryTypeTextField, producingCountryTextField,
                supported3DAPIsTextField, formFactorTextField, coolingSystemTypeTextField,
                guaranteeTextField, priceTextField, wholesalePriceTextField, brandTextField,
                productTitleTextField, productDescriptionTextField, availableQuantityTextField,
                wholesaleQuantityTextField, sourceTextField
        ));

        Button saveButton = borderStyledButton("Save");
        saveButton.setOnAction(event -> {

            String supplierName = supplierNameComboBox.getValue();
            double retailPrice = Double.parseDouble(priceTextField.getText());
            double wholesalePrice = Double.parseDouble(wholesalePriceTextField.getText());

            TextField[] textFields = {
                    graphicsChipTextField, memoryFrequencyTextField, coreFrequencyTextField,
                    memoryCapacityTextField, bitSizeMemoryBusTextField, maxSupportedResolutionTextField,
                    minRequiredBZCapacityTextField, memoryTypeTextField, producingCountryTextField,
                    supported3DAPIsTextField, formFactorTextField, coolingSystemTypeTextField,
                    guaranteeTextField, priceTextField, wholesalePriceTextField, brandTextField,
                    productTitleTextField, productDescriptionTextField, availableQuantityTextField,
                    wholesaleQuantityTextField, sourceTextField
            };

            for (TextField textField : textFields) {
                if (isEmptyField(textField)) {
                    showErrorAlert("Please fill in all fields.");
                    return;
                }
            }

            if (wholesalePrice >= retailPrice) {
                showErrorAlert("Wholesale price should be lower than retail price.");
                return;
            }

            if (!isValidURL(sourceTextField.getText())) {
                showErrorAlert("Please enter a valid URL for the source.");
                return;
            }

            try {
                updateProductInfo(
                        productId, supplierId, supplierName, supplierContact,
                        graphicsChipTextField.getText(), memoryFrequencyTextField.getText(), coreFrequencyTextField.getText(),
                        memoryCapacityTextField.getText(), bitSizeMemoryBusTextField.getText(), maxSupportedResolutionTextField.getText(),
                        minRequiredBZCapacityTextField.getText(), memoryTypeTextField.getText(), producingCountryTextField.getText(),
                        supported3DAPIsTextField.getText(), formFactorTextField.getText(), coolingSystemTypeTextField.getText(),
                        guaranteeTextField.getText(), priceTextField.getText(), wholesalePriceTextField.getText(),
                        brandTextField.getText(), productTitleTextField.getText(), creationDate, productDescriptionTextField.getText(),
                        creatorName, availableQuantityTextField.getText(), wholesaleQuantityTextField.getText(), sourceTextField.getText());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().addAll(cancelButton, saveButton);

        VBox formVBox = new VBox(10);
        formVBox.setStyle("-fx-background-color: black;");
        formVBox.setAlignment(Pos.CENTER);
        formVBox.setPadding(new Insets(20));
        formVBox.getChildren().addAll(
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
                buttonsBox
        );

        ScrollPane scrollPane = new ScrollPane(formVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: black;");
        scrollPane.setPadding(new Insets(20));

        return scrollPane;
    }

    private void updateProductInfo(int productId, int supplierId, String supplierName, String supplierContact, String graphicsChip,
                                   String memoryFrequency, String coreFrequency, String memoryCapacity, String bitSizeMemoryBus,
                                   String maxSupportedResolution, String minRequiredBZCapacity, String memoryType,
                                   String producingCountry, String supported3DAPIs, String formFactor, String coolingSystemType,
                                   String guarantee, String price, String wholesalePrice, String brand, String productTitle,
                                   LocalDate creationDate, String productDescription, String creatorName, String availableQuantity,
                                   String wholesaleQuantity, String source) throws SQLException {
        String query = "UPDATE suppliers_product SET spp_suppliers_id = ?, spp_supplier_name = ?, spp_supplier_contact = ?, spp_graphics_chip = ?, spp_memory_frequency = ?, spp_core_frequency = ?, spp_memory_capacity = ?, spp_bit_size_memory_bus = ?, spp_maximum_supported_resolution = ?, spp_minimum_required_BZ_capacity = ?, spp_memory_type = ?, spp_producing_country = ?, spp_supported_3D_APIs = ?, spp_form_factor = ?, spp_type_of_cooling_system = ?, spp_guarantee = ?, spp_price = ?, spp_wholesale_price = ?, spp_brand = ?, spp_product_title = ?, spp_creation_date = ?, spp_product_description = ?, spp_creator_name = ?, spp_available_quantity = ?, spp_wholesale_quantity = ?, spp_source = ? WHERE spp_product_id = ?";

        Date sqlDate = Date.valueOf(creationDate);

        try (Connection connection = establishDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, supplierId);
            preparedStatement.setString(2, supplierName);
            preparedStatement.setString(3, supplierContact);
            preparedStatement.setString(4, graphicsChip);
            preparedStatement.setString(5, memoryFrequency);
            preparedStatement.setString(6, coreFrequency);
            preparedStatement.setString(7, memoryCapacity);
            preparedStatement.setString(8, bitSizeMemoryBus);
            preparedStatement.setString(9, maxSupportedResolution);
            preparedStatement.setString(10, minRequiredBZCapacity);
            preparedStatement.setString(11, memoryType);
            preparedStatement.setString(12, producingCountry);
            preparedStatement.setString(13, supported3DAPIs);
            preparedStatement.setString(14, formFactor);
            preparedStatement.setString(15, coolingSystemType);
            preparedStatement.setString(16, guarantee);
            preparedStatement.setString(17, price);
            preparedStatement.setString(18, wholesalePrice);
            preparedStatement.setString(19, brand);
            preparedStatement.setString(20, productTitle);
            preparedStatement.setDate(21, sqlDate);
            preparedStatement.setString(22, productDescription);
            preparedStatement.setString(23, creatorName);
            preparedStatement.setString(24, availableQuantity);
            preparedStatement.setString(25, wholesaleQuantity);
            preparedStatement.setString(26, source);
            preparedStatement.setInt(27, productId);

            preparedStatement.executeUpdate();

            showSuccessAlert("Product information updated successfully!");
        }
    }

    private void retrieveInitialValuesFromDB() throws SQLException {
        String sql = "SELECT * FROM suppliers_product WHERE spp_product_id = ?";
        try (Connection connection = establishDBConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("Data retrieved from DB for productId: " + productId);

                    graphicsChipTextField.setText(resultSet.getString("spp_graphics_chip"));
                    memoryFrequencyTextField.setText(resultSet.getString("spp_memory_frequency"));
                    coreFrequencyTextField.setText(resultSet.getString("spp_core_frequency"));
                    memoryCapacityTextField.setText(resultSet.getString("spp_memory_capacity"));
                    bitSizeMemoryBusTextField.setText(resultSet.getString("spp_bit_size_memory_bus"));
                    maxSupportedResolutionTextField.setText(resultSet.getString("spp_maximum_supported_resolution"));
                    minRequiredBZCapacityTextField.setText(resultSet.getString("spp_minimum_required_BZ_capacity"));
                    memoryTypeTextField.setText(resultSet.getString("spp_memory_type"));
                    producingCountryTextField.setText(resultSet.getString("spp_producing_country"));
                    supported3DAPIsTextField.setText(resultSet.getString("spp_supported_3D_APIs"));
                    formFactorTextField.setText(resultSet.getString("spp_form_factor"));
                    coolingSystemTypeTextField.setText(resultSet.getString("spp_type_of_cooling_system"));
                    guaranteeTextField.setText(resultSet.getString("spp_guarantee"));
                    priceTextField.setText(resultSet.getString("spp_price"));
                    wholesalePriceTextField.setText(resultSet.getString("spp_wholesale_price"));
                    brandTextField.setText(resultSet.getString("spp_brand"));
                    productTitleTextField.setText(resultSet.getString("spp_product_title"));
                    productDescriptionTextField.setText(resultSet.getString("spp_product_description"));
                    availableQuantityTextField.setText(resultSet.getString("spp_available_quantity"));
                    wholesaleQuantityTextField.setText(resultSet.getString("spp_wholesale_quantity"));
                    sourceTextField.setText(resultSet.getString("spp_source"));
                } else {
                    System.out.println("No data found for productId: " + productId);
                }
            }
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
        try (Connection connection = establishDBConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
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
        try (Connection connection = establishDBConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
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