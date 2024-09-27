import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class AnnouncementCreateWindow {
    private final FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService;

    public AnnouncementCreateWindow(FirstConnectionToDataBase connectionToDataBase) {
        this.connectionToDataBase = connectionToDataBase;
        alertService = new AlertServiceImpl();
        try {

            connectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    public void showAddAnnouncementForm() {
        if (!SessionManager.getInstance().isManagerEnter()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("You cannot add announcements. Please contact a manager to propose a product for sale.");
            alert.showAndWait();
            return;
        }

        Stage addAnnouncementStage = new Stage();
        addAnnouncementStage.setTitle("Add Announcement");
        addAnnouncementStage.initStyle(StageStyle.DECORATED);

        Font labelFont = Font.font("Gotham", FontWeight.NORMAL, 16);

        Label titleLabel = new Label("Announcement Title:");
        titleLabel.setFont(labelFont);
        titleLabel.setTextFill(Color.WHITE);
        TextField titleTextField = new TextField();
        addTooltip(titleTextField, "Enter the title of the announcement");

        Label memoryTypeLabel = new Label("Memory Type:");
        memoryTypeLabel.setFont(labelFont);
        memoryTypeLabel.setTextFill(Color.WHITE);
        ComboBox<String> memoryTypeComboBox = new ComboBox<>();
        memoryTypeComboBox.getItems().addAll("GDDR1", "GDDR2", "GDDR3", "GDDR4", "GDDR5", "GDDR5X", "GDDR6", "GDDR6X", "DDR2", "DDR3", "DDR4", "DDR5", "HBM2", "SDDR3", "LPDDR4X");
        addTooltip(memoryTypeComboBox, "Select memory type");

        Label formFactorLabel = new Label("Form Factor:");
        formFactorLabel.setFont(labelFont);
        formFactorLabel.setTextFill(Color.WHITE);
        ComboBox<String> formFactorComboBox = new ComboBox<>();
        formFactorComboBox.getItems().addAll("Mini", "Standard", "Low Profile", "External");
        addTooltip(formFactorComboBox, "Select form factor");

        Label coolingSystemTypeLabel = new Label("Cooling System Type:");
        coolingSystemTypeLabel.setFont(labelFont);
        coolingSystemTypeLabel.setTextFill(Color.WHITE);
        ComboBox<String> coolingSystemTypeComboBox = new ComboBox<>();
        coolingSystemTypeComboBox.getItems().addAll("Active", "Passive", "Water Cooling");
        addTooltip(coolingSystemTypeComboBox, "Select cooling system type");

        Label maxSupportedResolutionLabel = new Label("Max Supported Resolution:");
        maxSupportedResolutionLabel.setFont(labelFont);
        maxSupportedResolutionLabel.setTextFill(Color.WHITE);
        ComboBox<String> resolutionComboBox = new ComboBox<>();
        resolutionComboBox.getItems().addAll(
                "1280x720", "1366x768", "1600x900", "1920x1080", "2560x1440", "3840x2160",
                "4096x2160", "5120x2880", "7680x4320", "15360x8640", "15360x2160");
        addTooltip(resolutionComboBox, "Select max supported resolution");

        applyComboBoxStyles(memoryTypeComboBox, formFactorComboBox, coolingSystemTypeComboBox, resolutionComboBox);

        Label graphicsChipLabel = new Label("Graphics Chip:");
        graphicsChipLabel.setFont(labelFont);
        graphicsChipLabel.setTextFill(Color.WHITE);
        TextField graphicsChipTextField = new TextField();
        addTooltip(graphicsChipTextField, "Enter the graphics chip");

        Label memoryFrequencyLabel = new Label("Memory Frequency:");
        memoryFrequencyLabel.setFont(labelFont);
        memoryFrequencyLabel.setTextFill(Color.WHITE);
        TextField memoryFrequencyTextField = new TextField();
        addTooltip(memoryFrequencyTextField, "Enter the memory frequency");

        Label coreFrequencyLabel = new Label("Core Frequency:");
        coreFrequencyLabel.setFont(labelFont);
        coreFrequencyLabel.setTextFill(Color.WHITE);
        TextField coreFrequencyTextField = new TextField();
        addTooltip(coreFrequencyTextField, "Enter the core frequency");

        Label memoryCapacityLabel = new Label("Memory Capacity:");
        memoryCapacityLabel.setFont(labelFont);
        memoryCapacityLabel.setTextFill(Color.WHITE);
        TextField memoryCapacityTextField = new TextField();
        addTooltip(memoryCapacityTextField, "Enter the memory capacity");

        Label minRequiredBZCapacityLabel = new Label("Min Required BZ Capacity:");
        minRequiredBZCapacityLabel.setFont(labelFont);
        minRequiredBZCapacityLabel.setTextFill(Color.WHITE);
        TextField minRequiredBZCapacityTextField = new TextField();
        minRequiredBZCapacityTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(minRequiredBZCapacityTextField, "Enter the minimum required BZ capacity");

        Label bitSizeMemoryBusLabel = new Label("Bit Size Memory Bus:");
        bitSizeMemoryBusLabel.setFont(labelFont);
        bitSizeMemoryBusLabel.setTextFill(Color.WHITE);
        TextField bitSizeMemoryBusTextField = new TextField();
        addTooltip(bitSizeMemoryBusTextField, "Enter the bit size of the memory bus");

        Label producingCountryLabel = new Label("Producing Country:");
        producingCountryLabel.setFont(labelFont);
        producingCountryLabel.setTextFill(Color.WHITE);
        TextField producingCountryTextField = new TextField();
        addTooltip(producingCountryTextField, "Enter the producing country");

        Label supported3DApisLabel = new Label("Supported 3D APIs:");
        supported3DApisLabel.setFont(labelFont);
        supported3DApisLabel.setTextFill(Color.WHITE);
        TextField supported3DApisTextField = new TextField();
        addTooltip(supported3DApisTextField, "Enter the supported 3D APIs");

        Label guaranteeLabel = new Label("Guarantee:");
        guaranteeLabel.setFont(labelFont);
        guaranteeLabel.setTextFill(Color.WHITE);
        TextField guaranteeTextField = new TextField();
        addTooltip(guaranteeTextField, "Enter the guarantee");

        Label priceLabel = new Label("Price:");
        priceLabel.setFont(labelFont);
        priceLabel.setTextFill(Color.WHITE);
        TextField priceTextField = new TextField();
        addTooltip(priceTextField, "Enter the price");

        Label availableQuantityLabel = new Label("Available Quantity:");
        availableQuantityLabel.setFont(labelFont);
        availableQuantityLabel.setTextFill(Color.WHITE);
        TextField availableQuantityTextField = new TextField();
        addTooltip(availableQuantityTextField, "Enter the available quantity");

        Label wholesaleQuantityLabel = new Label("Wholesale Quantity:");
        wholesaleQuantityLabel.setFont(labelFont);
        wholesaleQuantityLabel.setTextFill(Color.WHITE);
        TextField wholesaleQuantityTextField = new TextField();
        addTooltip(wholesaleQuantityTextField, "Enter the wholesale quantity");

        Label wholesalePriceLabel = new Label("Wholesale Price:");
        wholesalePriceLabel.setFont(labelFont);
        wholesalePriceLabel.setTextFill(Color.WHITE);
        TextField wholesalePriceTextField = new TextField();
        addTooltip(wholesalePriceTextField, "Enter the wholesale price");

        Label brandLabel = new Label("Brand:");
        brandLabel.setFont(labelFont);
        brandLabel.setTextFill(Color.WHITE);
        TextField brandTextField = new TextField();
        addTooltip(brandTextField, "Enter the brand");

        Label descriptionLabel = new Label("Description:");
        descriptionLabel.setFont(labelFont);
        descriptionLabel.setTextFill(Color.WHITE);
        TextArea descriptionTextArea = new TextArea();
        descriptionTextArea.setStyle("-fx-control-inner-background: black; -fx-text-fill: white;");
        descriptionTextArea.setPromptText("Enter the description of the announcement");
        addTooltip(descriptionTextArea, "Enter the description");

        Button cancelButton = ButtonStyle.expandPaneStyledButton("Cancel");
        cancelButton.setOnAction(cancelEvent -> {
            titleTextField.clear();
            graphicsChipTextField.clear();
            memoryFrequencyTextField.clear();
            coreFrequencyTextField.clear();
            memoryCapacityTextField.clear();
            resolutionComboBox.getItems().clear();
            minRequiredBZCapacityTextField.clear();
            bitSizeMemoryBusTextField.clear();
            producingCountryTextField.clear();
            supported3DApisTextField.clear();
            guaranteeTextField.clear();
            priceTextField.clear();
            brandTextField.clear();
            descriptionTextArea.clear();
            addAnnouncementStage.close();
        });

        Button addButton = ButtonStyle.expandPaneStyledButton("Add");
        addButton.setOnAction(e -> addAnnouncementToDatabase(
                addAnnouncementStage,
                titleTextField, graphicsChipTextField, memoryFrequencyTextField, coreFrequencyTextField,
                memoryCapacityTextField, bitSizeMemoryBusTextField, resolutionComboBox,
                minRequiredBZCapacityTextField, memoryTypeComboBox, producingCountryTextField,
                supported3DApisTextField, formFactorComboBox, coolingSystemTypeComboBox,
                guaranteeTextField, priceTextField, wholesalePriceTextField,
                wholesaleQuantityTextField, brandTextField, descriptionTextArea,
                availableQuantityTextField
        ));

        HBox buttonPane = new HBox(10);
        buttonPane.getChildren().addAll(cancelButton, addButton);

        VBox addFormLayout = new VBox(10);
        addFormLayout.setPadding(new Insets(10));
        addFormLayout.setStyle("-fx-background-color: black;");

        addFormLayout.getChildren().addAll(
                titleLabel, titleTextField,
                graphicsChipLabel, graphicsChipTextField,
                memoryFrequencyLabel, memoryFrequencyTextField,
                coreFrequencyLabel, coreFrequencyTextField,
                memoryCapacityLabel, memoryCapacityTextField,
                bitSizeMemoryBusLabel, bitSizeMemoryBusTextField,
                resolutionComboBox, minRequiredBZCapacityLabel, minRequiredBZCapacityTextField,
                memoryTypeComboBox, producingCountryLabel, producingCountryTextField,
                supported3DApisLabel, supported3DApisTextField,
                formFactorComboBox, coolingSystemTypeComboBox,
                guaranteeLabel, guaranteeTextField,
                priceLabel, priceTextField,
                brandLabel, brandTextField,
                descriptionLabel, descriptionTextArea,
                buttonPane
        );

        applyStyleToFields(titleTextField, graphicsChipTextField, memoryFrequencyTextField,
                coreFrequencyTextField, memoryCapacityTextField, bitSizeMemoryBusTextField,
                producingCountryTextField, supported3DApisTextField, guaranteeTextField,
                priceTextField, wholesalePriceTextField, wholesaleQuantityTextField,
                brandTextField, descriptionTextArea, availableQuantityTextField);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(addFormLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: black;");

        Scene addFormScene = new Scene(scrollPane, 400, 600);
        addFormScene.setFill(Color.BLACK);

        addAnnouncementStage.setScene(addFormScene);
        addAnnouncementStage.show();
    }

    private void addTooltip(Control control, String text) {
        Tooltip tooltip = new Tooltip(text);
        Tooltip.install(control, tooltip);
    }

    private void applyComboBoxStyles(ComboBox<String>... comboBoxes) {
        for (ComboBox<String> comboBox : comboBoxes) {
            comboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
            comboBox.getEditor().setStyle("-fx-text-fill: white;");
        }
    }

    private void addAnnouncementToDatabase(
            Stage addAnnouncementStage, TextField titleTextField,
            TextField graphicsChipTextField,
            TextField memoryFrequencyTextField,
            TextField coreFrequencyTextField,
            TextField memoryCapacityTextField,
            TextField bitSizeMemoryBusTextField,
            ComboBox<String> resolutionComboBox,
            TextField minRequiredBZCapacityTextField,
            ComboBox<String> memoryTypeComboBox,
            TextField producingCountryTextField,
            TextField supported3DApisTextField,
            ComboBox<String> formFactorComboBox,
            ComboBox<String> coolingSystemTypeComboBox,
            TextField guaranteeTextField,
            TextField priceTextField,
            TextField wholesalePriceTextField,
            TextField wholesaleQuantityTextField,
            TextField brandTextField,
            TextArea descriptionTextArea,
            TextField availableQuantityTextField
    ) {
        if (isEmptyField(titleTextField) || isEmptyField(graphicsChipTextField) ||
                isEmptyField(memoryFrequencyTextField) || isEmptyField(coreFrequencyTextField) ||
                isEmptyField(memoryCapacityTextField) || isEmptyField(bitSizeMemoryBusTextField) ||
                isEmptyField(producingCountryTextField) || isEmptyField(supported3DApisTextField) ||
                isEmptyField(guaranteeTextField) || isEmptyField(priceTextField) ||
                isEmptyField(brandTextField) || isEmptyField(descriptionTextArea) ||
                isEmptyField(wholesalePriceTextField) || isEmptyField(wholesaleQuantityTextField) ||
                isEmptyField(availableQuantityTextField)) {
            alertService.showErrorAlert("Please fill in all required fields before adding an ad.");
            return;
        }

        double wholesalePriceValue = Double.parseDouble(wholesalePriceTextField.getText());
        double priceValue = Double.parseDouble(priceTextField.getText());

        if (wholesalePriceValue >= priceValue) {
            alertService.showErrorAlert("Wholesale price should be less than the regular price.");
            return;
        }

        String creatorName = SessionManager.getInstance().getCurrentManagerName();

        String title = titleTextField.getText();
        String graphicsChip = graphicsChipTextField.getText();
        double memoryFrequency = Double.parseDouble(memoryFrequencyTextField.getText());
        double coreFrequency = Double.parseDouble(coreFrequencyTextField.getText());
        int memoryCapacity = Integer.parseInt(memoryCapacityTextField.getText());
        int bitSizeMemoryBus = Integer.parseInt(bitSizeMemoryBusTextField.getText());
        String maxSupportedResolution = resolutionComboBox.getValue();
        int minRequiredBZCapacity = Integer.parseInt(minRequiredBZCapacityTextField.getText());
        String memoryType = memoryTypeComboBox.getValue();
        String producingCountry = producingCountryTextField.getText();
        String supported3DApis = supported3DApisTextField.getText();
        String formFactor = formFactorComboBox.getValue();
        String coolingSystemType = coolingSystemTypeComboBox.getValue();
        int guarantee = Integer.parseInt(guaranteeTextField.getText());
        double price = Double.parseDouble(priceTextField.getText());
        double wholesalePrice = Double.parseDouble(wholesalePriceTextField.getText());
        int wholesaleQuantity = Integer.parseInt(wholesaleQuantityTextField.getText());
        String brand = brandTextField.getText();
        String description = descriptionTextArea.getText();
        int availableQuantity = Integer.parseInt(availableQuantityTextField.getText());

        try {
            String query = "INSERT INTO catalog (product_title, graphics_chip, memory_frequency, core_frequency, " +
                    "memory_capacity, bit_size_memory_bus, maximum_supported_resolution, minimum_required_BZ_capacity, " +
                    "memory_type, producing_country, supported_3D_APIS, form_factor, type_of_cooling_system, " +
                    "guarantee, price, wholesale_price, wholesale_quantity, brand, creation_data, " +
                    "product_description, creator_name, available_quantity) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connectionToDataBase.getConnection().prepareStatement(query)) {
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, graphicsChip);
                preparedStatement.setDouble(3, memoryFrequency);
                preparedStatement.setDouble(4, coreFrequency);
                preparedStatement.setInt(5, memoryCapacity);
                preparedStatement.setInt(6, bitSizeMemoryBus);
                preparedStatement.setString(7, maxSupportedResolution);
                preparedStatement.setInt(8, minRequiredBZCapacity);
                preparedStatement.setString(9, memoryType);
                preparedStatement.setString(10, producingCountry);
                preparedStatement.setString(11, supported3DApis);
                preparedStatement.setString(12, formFactor);
                preparedStatement.setString(13, coolingSystemType);
                preparedStatement.setInt(14, guarantee);
                preparedStatement.setDouble(15, price);
                preparedStatement.setDouble(16, wholesalePrice);
                preparedStatement.setInt(17, wholesaleQuantity);
                preparedStatement.setString(18, brand);
                preparedStatement.setDate(19, Date.valueOf(LocalDate.now()));
                preparedStatement.setString(20, description);
                preparedStatement.setString(21, creatorName);
                preparedStatement.setInt(22, availableQuantity);

                preparedStatement.executeUpdate();
            }

            clearFields(titleTextField, graphicsChipTextField, memoryFrequencyTextField, coreFrequencyTextField,
                    memoryCapacityTextField, minRequiredBZCapacityTextField, bitSizeMemoryBusTextField,
                    producingCountryTextField, supported3DApisTextField, guaranteeTextField,
                    priceTextField, wholesalePriceTextField, wholesaleQuantityTextField,
                    brandTextField, descriptionTextArea, availableQuantityTextField);

            addAnnouncementStage.close();
        } catch (SQLException ex) {
            System.err.println("Error while inserting announcement into the database");
            ex.printStackTrace();
            alertService.showErrorAlert("An error occurred while adding the ad to the database.");
        }
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
    private boolean isEmptyField(TextArea textArea) {
        return textArea.getText().trim().isEmpty();
    }
}