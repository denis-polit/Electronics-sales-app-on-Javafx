import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class ProductEdit {

    private final SessionManager sessionManager;
    private final FirstConnectionToDataBase connectionToDataBase;
    private final AlertService alertService;

    public ProductEdit() {
        sessionManager = SessionManager.getInstance();
        alertService = new AlertServiceImpl();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void handleEditProduct(Announcement announcement) {
        String currentTitle = announcement.getTitle();
        String currentGraphicsChip = announcement.getGraphicsChip();
        double currentMemoryFrequency = announcement.getMemoryFrequency();
        double currentCoreFrequency = announcement.getCoreFrequency();
        int currentMemoryCapacity = announcement.getMemoryCapacity();
        int currentBitSizeMemoryBus = announcement.getBitSizeMemoryBus();
        String currentMaxSupportedResolution = announcement.getMaxSupportedResolution();
        int currentMinRequiredBZCapacity = announcement.getMinRequiredBZCapacity();
        String currentMemoryType = announcement.getMemoryType();
        String currentProducingCountry = announcement.getProducingCountry();
        String currentSupported3DApis = announcement.getSupported3DApis();
        String currentFormFactor = announcement.getFormFactor();
        String currentCoolingSystemType = announcement.getCoolingSystemType();
        int currentGuarantee = announcement.getGuarantee();
        double currentPrice = announcement.getPrice();
        double currentWholesalePrice = announcement.getWholesalePrice();
        int currentWholesaleQuantity = announcement.getWholesaleQuantity();
        String currentBrand = announcement.getBrand();
        String currentDescription = announcement.getDescription();
        int currentAvailableQuantity = announcement.getAvailableQuantity();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Announcement");
        dialog.setHeaderText("Edit the details of the announcement");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 20, 20, 20));

        ComboBox<String> memoryTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "GDDR1", "GDDR2", "GDDR3", "GDDR4", "GDDR5", "GDDR5X", "GDDR6", "GDDR6X", "DDR2", "DDR3", "DDR4", "DDR5", "HBM2", "SDDR3", "LPDDR4X"));
        memoryTypeComboBox.setValue(currentMemoryType);

        ComboBox<String> formFactorComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Mini", "Standard", "Low Profile", "External"));
        formFactorComboBox.setValue(currentFormFactor);

        ComboBox<String> coolingSystemTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Active", "Passive", "Water Cooling"));
        coolingSystemTypeComboBox.setValue(currentCoolingSystemType);

        TextField titleField = new TextField(currentTitle);
        TextField graphicsChipField = new TextField(currentGraphicsChip);
        TextField memoryFrequencyField = new TextField(Double.toString(currentMemoryFrequency));
        TextField coreFrequencyField = new TextField(Double.toString(currentCoreFrequency));
        TextField memoryCapacityField = new TextField(Integer.toString(currentMemoryCapacity));
        TextField supported3DApisField = new TextField(currentSupported3DApis);
        TextField bitSizeMemoryBusField = new TextField(Integer.toString(currentBitSizeMemoryBus));
        TextField minRequiredBZCapacityField = new TextField(Integer.toString(currentMinRequiredBZCapacity));
        TextField guaranteeField = new TextField(Integer.toString(currentGuarantee));
        TextField priceField = new TextField(Double.toString(currentPrice));
        TextField wholesalePriceField = new TextField(Double.toString(currentWholesalePrice));
        TextField wholesaleQuantityField = new TextField(Integer.toString(currentWholesaleQuantity));
        TextField brandField = new TextField(currentBrand);
        TextField descriptionField = new TextField(currentDescription);
        TextField availableQuantityField = new TextField(Integer.toString(currentAvailableQuantity));

        ComboBox<String> resolutionComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "1280x720", "1366x768", "1600x900", "1920x1080", "2560x1440", "3840x2160",
                "4096x2160", "5120x2880", "7680x4320", "15360x8640", "15360x2160"));
        resolutionComboBox.setValue(currentMaxSupportedResolution);

        gridPane.add(new Label("Title:"), 0, 0);
        gridPane.add(titleField, 1, 0);
        gridPane.add(new Label("Graphics Chip:"), 0, 1);
        gridPane.add(graphicsChipField, 1, 1);
        gridPane.add(new Label("Memory Frequency:"), 0, 2);
        gridPane.add(memoryFrequencyField, 1, 2);
        gridPane.add(new Label("Core Frequency:"), 0, 3);
        gridPane.add(coreFrequencyField, 1, 3);
        gridPane.add(new Label("Memory Capacity:"), 0, 4);
        gridPane.add(memoryCapacityField, 1, 4);
        gridPane.add(new Label("Bit Size Memory Bus:"), 0, 5);
        gridPane.add(bitSizeMemoryBusField, 1, 5);
        gridPane.add(new Label("Max Supported Resolution:"), 0, 6);
        gridPane.add(resolutionComboBox, 1, 6);
        gridPane.add(new Label("Min Required BZ Capacity:"), 0, 7);
        gridPane.add(minRequiredBZCapacityField, 1, 7);
        gridPane.add(new Label("Memory Type:"), 0, 8);
        gridPane.add(memoryTypeComboBox, 1, 8);
        gridPane.add(new Label("Producing Country:"), 0, 9);
        gridPane.add(new TextField(currentProducingCountry), 1, 9);
        gridPane.add(new Label("Supported 3D APIs:"), 0, 10);
        gridPane.add(supported3DApisField, 1, 10);
        gridPane.add(new Label("Form Factor:"), 0, 11);
        gridPane.add(formFactorComboBox, 1, 11);
        gridPane.add(new Label("Cooling System Type:"), 0, 12);
        gridPane.add(coolingSystemTypeComboBox, 1, 12);
        gridPane.add(new Label("Guarantee:"), 0, 13);
        gridPane.add(guaranteeField, 1, 13);
        gridPane.add(new Label("Price:"), 0, 14);
        gridPane.add(priceField, 1, 14);
        gridPane.add(new Label("Wholesale Price:"), 0, 15);
        gridPane.add(wholesalePriceField, 1, 15);
        gridPane.add(new Label("Wholesale Quantity:"), 0, 16);
        gridPane.add(wholesaleQuantityField, 1, 16);
        gridPane.add(new Label("Brand:"), 0, 17);
        gridPane.add(brandField, 1, 17);
        gridPane.add(new Label("Description:"), 0, 18);
        gridPane.add(descriptionField, 1, 18);
        gridPane.add(new Label("Available Quantity:"), 0, 19);
        gridPane.add(availableQuantityField, 1, 19);

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        dialog.getDialogPane().setContent(scrollPane);

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                try {
                    String newTitle = titleField.getText();
                    String newGraphicsChip = graphicsChipField.getText();
                    double newMemoryFrequency = Double.parseDouble(memoryFrequencyField.getText());
                    double newCoreFrequency = Double.parseDouble(coreFrequencyField.getText());
                    int newMemoryCapacity = Integer.parseInt(memoryCapacityField.getText());
                    int newBitSizeMemoryBus = Integer.parseInt(bitSizeMemoryBusField.getText());
                    String newMaxSupportedResolution = resolutionComboBox.getValue();
                    int newMinRequiredBZCapacity = Integer.parseInt(minRequiredBZCapacityField.getText());
                    String newMemoryType = memoryTypeComboBox.getValue();
                    String newProducingCountry = ((TextField) gridPane.getChildren().get(19)).getText();
                    String newSupported3DApis = supported3DApisField.getText();
                    String newFormFactor = formFactorComboBox.getValue();
                    String newCoolingSystemType = coolingSystemTypeComboBox.getValue();
                    int newGuarantee = Integer.parseInt(guaranteeField.getText());
                    double newPrice = Double.parseDouble(priceField.getText());
                    double newWholesalePrice = Double.parseDouble(wholesalePriceField.getText());
                    int newWholesaleQuantity = Integer.parseInt(wholesaleQuantityField.getText());
                    String newBrand = brandField.getText();
                    String newDescription = descriptionField.getText();
                    int newAvailableQuantity = Integer.parseInt(availableQuantityField.getText());

                    if (newTitle.isEmpty() || newGraphicsChip.isEmpty() || newMaxSupportedResolution.isEmpty() ||
                            newProducingCountry.isEmpty() || newSupported3DApis.isEmpty() || newBrand.isEmpty() || newDescription.isEmpty()) {
                        alertService.showErrorAlert("All fields must be filled in.");
                        return null;
                    }

                    if (newWholesalePrice > newPrice) {
                        alertService.showErrorAlert("Wholesale price cannot be greater than regular price.");
                        return null;
                    }

                    Announcement updatedAnnouncement = new Announcement(newTitle, announcement.getProductId(),
                            newGraphicsChip, newMemoryFrequency, newCoreFrequency, newMemoryCapacity,
                            newBitSizeMemoryBus, newMaxSupportedResolution, newMinRequiredBZCapacity,
                            newMemoryType, newProducingCountry, newSupported3DApis, newFormFactor,
                            newCoolingSystemType, newGuarantee, newPrice, newWholesalePrice, newWholesaleQuantity,
                            newBrand, newDescription, announcement.getDate(), announcement.getManagerUsername(), newAvailableQuantity);

                    updateAnnouncement(announcement, updatedAnnouncement);

                    return ButtonType.OK;
                } catch (NumberFormatException e) {
                    alertService.showErrorAlert("Invalid input. Please enter valid data in numeric fields.");
                    return null;
                }
            }
            return null;
        });

        Optional<ButtonType> result = dialog.showAndWait();

        result.ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                alertService.showSuccessAlert("Announcement successfully updated.");
            }
        });
    }

    private void updateAnnouncement(Announcement oldAnnouncement, Announcement updatedAnnouncement) {
        double wholesalePrice = updatedAnnouncement.getWholesalePrice();
        double price = updatedAnnouncement.getPrice();

        if (wholesalePrice > price) {
            alertService.showErrorAlert("Wholesale price cannot be greater than regular price.");
            return;
        }

        String updateSql = "UPDATE catalog SET product_title = ?, graphics_chip = ?, memory_frequency = ?, " +
                "core_frequency = ?, memory_capacity = ?, bit_size_memory_bus = ?, maximum_supported_resolution = ?, " +
                "minimum_required_BZ_capacity = ?, memory_type = ?, producing_country = ?, supported_3D_APIS = ?, " +
                "form_factor = ?, type_of_cooling_system = ?, guarantee = ?, price = ?, wholesale_price = ?, " +
                "brand = ?, product_description = ?, available_quantity = ?, wholesale_quantity = ? " +
                "WHERE product_id = ?";

        try (Connection connection = establishDBConnection();
             PreparedStatement statement = connection.prepareStatement(updateSql)) {

            statement.setString(1, updatedAnnouncement.getTitle());
            statement.setString(2, updatedAnnouncement.getGraphicsChip());
            statement.setDouble(3, updatedAnnouncement.getMemoryFrequency());
            statement.setDouble(4, updatedAnnouncement.getCoreFrequency());
            statement.setInt(5, updatedAnnouncement.getMemoryCapacity());
            statement.setInt(6, updatedAnnouncement.getBitSizeMemoryBus());
            statement.setString(7, updatedAnnouncement.getMaxSupportedResolution());
            statement.setInt(8, updatedAnnouncement.getMinRequiredBZCapacity());
            statement.setString(9, updatedAnnouncement.getMemoryType());
            statement.setString(10, updatedAnnouncement.getProducingCountry());
            statement.setString(11, updatedAnnouncement.getSupported3DApis());
            statement.setString(12, updatedAnnouncement.getFormFactor());
            statement.setString(13, updatedAnnouncement.getCoolingSystemType());
            statement.setInt(14, updatedAnnouncement.getGuarantee());
            statement.setDouble(15, updatedAnnouncement.getPrice());
            statement.setDouble(16, updatedAnnouncement.getWholesalePrice());
            statement.setString(17, updatedAnnouncement.getBrand());
            statement.setString(18, updatedAnnouncement.getDescription());
            statement.setInt(19, updatedAnnouncement.getAvailableQuantity());
            statement.setInt(20, updatedAnnouncement.getWholesaleQuantity());
            statement.setInt(21, updatedAnnouncement.getProductId());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                alertService.showSuccessAlert("Announcement successfully updated.");
                logChanges(oldAnnouncement, updatedAnnouncement);
            } else {
                alertService.showErrorAlert("Failed to update the announcement. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while updating the announcement. Please try again later.");
        }
    }

    private void logChanges(Announcement oldAnnouncement, Announcement newAnnouncement) {
        StringBuilder changes = new StringBuilder();

        if (!oldAnnouncement.getTitle().equals(newAnnouncement.getTitle())) {
            changes.append(String.format("Title changed from '%s' to '%s'. ", oldAnnouncement.getTitle(), newAnnouncement.getTitle()));
        }
        if (!oldAnnouncement.getGraphicsChip().equals(newAnnouncement.getGraphicsChip())) {
            changes.append(String.format("Graphics Chip changed from '%s' to '%s'. ", oldAnnouncement.getGraphicsChip(), newAnnouncement.getGraphicsChip()));
        }
        if (oldAnnouncement.getMemoryFrequency() != newAnnouncement.getMemoryFrequency()) {
            changes.append(String.format("Memory Frequency changed from %.2f to %.2f. ", oldAnnouncement.getMemoryFrequency(), newAnnouncement.getMemoryFrequency()));
        }
        if (oldAnnouncement.getCoreFrequency() != newAnnouncement.getCoreFrequency()) {
            changes.append(String.format("Core Frequency changed from %.2f to %.2f. ", oldAnnouncement.getCoreFrequency(), newAnnouncement.getCoreFrequency()));
        }
        if (oldAnnouncement.getMemoryCapacity() != newAnnouncement.getMemoryCapacity()) {
            changes.append(String.format("Memory Capacity changed from %d to %d. ", oldAnnouncement.getMemoryCapacity(), newAnnouncement.getMemoryCapacity()));
        }
        if (oldAnnouncement.getBitSizeMemoryBus() != newAnnouncement.getBitSizeMemoryBus()) {
            changes.append(String.format("Bit Size Memory Bus changed from %d to %d. ", oldAnnouncement.getBitSizeMemoryBus(), newAnnouncement.getBitSizeMemoryBus()));
        }
        if (!oldAnnouncement.getMaxSupportedResolution().equals(newAnnouncement.getMaxSupportedResolution())) {
            changes.append(String.format("Max Supported Resolution changed from '%s' to '%s'. ", oldAnnouncement.getMaxSupportedResolution(), newAnnouncement.getMaxSupportedResolution()));
        }
        if (oldAnnouncement.getMinRequiredBZCapacity() != newAnnouncement.getMinRequiredBZCapacity()) {
            changes.append(String.format("Min Required BZ Capacity changed from %d to %d. ", oldAnnouncement.getMinRequiredBZCapacity(), newAnnouncement.getMinRequiredBZCapacity()));
        }
        if (!oldAnnouncement.getMemoryType().equals(newAnnouncement.getMemoryType())) {
            changes.append(String.format("Memory Type changed from '%s' to '%s'. ", oldAnnouncement.getMemoryType(), newAnnouncement.getMemoryType()));
        }
        if (!oldAnnouncement.getProducingCountry().equals(newAnnouncement.getProducingCountry())) {
            changes.append(String.format("Producing Country changed from '%s' to '%s'. ", oldAnnouncement.getProducingCountry(), newAnnouncement.getProducingCountry()));
        }
        if (!oldAnnouncement.getSupported3DApis().equals(newAnnouncement.getSupported3DApis())) {
            changes.append(String.format("Supported 3D APIs changed from '%s' to '%s'. ", oldAnnouncement.getSupported3DApis(), newAnnouncement.getSupported3DApis()));
        }
        if (!oldAnnouncement.getFormFactor().equals(newAnnouncement.getFormFactor())) {
            changes.append(String.format("Form Factor changed from '%s' to '%s'. ", oldAnnouncement.getFormFactor(), newAnnouncement.getFormFactor()));
        }
        if (!oldAnnouncement.getCoolingSystemType().equals(newAnnouncement.getCoolingSystemType())) {
            changes.append(String.format("Cooling System Type changed from '%s' to '%s'. ", oldAnnouncement.getCoolingSystemType(), newAnnouncement.getCoolingSystemType()));
        }
        if (oldAnnouncement.getGuarantee() != newAnnouncement.getGuarantee()) {
            changes.append(String.format("Guarantee changed from %d to %d. ", oldAnnouncement.getGuarantee(), newAnnouncement.getGuarantee()));
        }
        if (oldAnnouncement.getPrice() != newAnnouncement.getPrice()) {
            changes.append(String.format("Price changed from %.2f to %.2f. ", oldAnnouncement.getPrice(), newAnnouncement.getPrice()));
        }
        if (oldAnnouncement.getWholesalePrice() != newAnnouncement.getWholesalePrice()) {
            changes.append(String.format("Wholesale Price changed from %.2f to %.2f. ", oldAnnouncement.getWholesalePrice(), newAnnouncement.getWholesalePrice()));
        }
        if (oldAnnouncement.getWholesaleQuantity() != newAnnouncement.getWholesaleQuantity()) {
            changes.append(String.format("Wholesale Quantity changed from %d to %d. ", oldAnnouncement.getWholesaleQuantity(), newAnnouncement.getWholesaleQuantity()));
        }
        if (!oldAnnouncement.getBrand().equals(newAnnouncement.getBrand())) {
            changes.append(String.format("Brand changed from '%s' to '%s'. ", oldAnnouncement.getBrand(), newAnnouncement.getBrand()));
        }
        if (!oldAnnouncement.getDescription().equals(newAnnouncement.getDescription())) {
            changes.append(String.format("Description changed from '%s' to '%s'. ", oldAnnouncement.getDescription(), newAnnouncement.getDescription()));
        }
        if (oldAnnouncement.getAvailableQuantity() != newAnnouncement.getAvailableQuantity()) {
            changes.append(String.format("Available Quantity changed from %d to %d. ", oldAnnouncement.getAvailableQuantity(), newAnnouncement.getAvailableQuantity()));
        }

        String details = changes.toString();
        if (sessionManager.isManagerEnter()) {
            int managerId = sessionManager.getEmployeeIdByName(sessionManager.getCurrentManagerName());
            sessionManager.logManagerActivity(managerId, "EDIT_ANNOUNCEMENT", "ANNOUNCEMENT", details);
        } else if (sessionManager.isClientEnter()) {
            int clientId = sessionManager.getClientIdByName(sessionManager.getCurrentClientName());
            sessionManager.logActivity(clientId, "EDIT_ANNOUNCEMENT", "ANNOUNCEMENT", details);
        }
    }
}
