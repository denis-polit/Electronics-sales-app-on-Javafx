import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class AddToExistingContract {

    private FirstConnectionToDataBase connectionToDataBase;

    AddToExistingContract() {
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public boolean hasContractsInProcessing(String clientName) {
        try (Connection connection = establishDBConnection()) {
            String query = "SELECT COUNT(*) AS count FROM contracts WHERE client_id = ? AND (status = 'In processing' OR status = 'Under consideration')";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, clientName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt("count");
                        return count > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error checking user contracts: " + e.getMessage());
        }
        return false;
    }

    private List<Contract> getUserContracts(String username) {
        List<Contract> contracts = new ArrayList<>();

        try (Connection connection = establishDBConnection()) {
            String query = "SELECT * FROM contracts WHERE client_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int contractId = resultSet.getInt("id_contracts");
                        String clientName = resultSet.getString("client_id");
                        String managerName = resultSet.getString("manager_id");
                        int productCount = resultSet.getInt("product_count");
                        String status = resultSet.getString("status");
                        String deliveryMethod = resultSet.getString("delivery_method");
                        String paymentMethod = resultSet.getString("pay_method");
                        String productIdsStr = resultSet.getString("product_id");
                        List<Integer> productIds = parseProductIds(productIdsStr);
                        String additionalProducts = resultSet.getString("additional_products");
                        String deadline = resultSet.getString("deadline");
                        double totalAmount = resultSet.getDouble("total_amount");

                        Contract contract = new Contract(contractId, clientName, managerName, productCount, status, deliveryMethod, paymentMethod, productIds, additionalProducts, deadline, totalAmount);
                        contracts.add(contract);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error fetching user contracts: " + e.getMessage());
        }

        return contracts;
    }

    private List<Integer> parseProductIds(String productIdsStr) {
        List<Integer> productIds = new ArrayList<>();
        for (String idStr : productIdsStr.split("\\s+")) {
            productIds.add(Integer.parseInt(idStr.trim()));
        }
        return productIds;
    }

    private Map<String, Object> getCatalogItemById(int productId) {
        Map<String, Object> productInfo = new HashMap<>();
        try (Connection connection = establishDBConnection()) {
            String query = "SELECT * FROM catalog WHERE product_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, productId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String productName = resultSet.getString("product_title");
                        double price = resultSet.getDouble("price");
                        double wholesalePrice = resultSet.getDouble("wholesale_price");
                        int availableQuantity = resultSet.getInt("available_quantity");
                        int wholesaleQuantity = resultSet.getInt("wholesale_quantity");

                        productInfo.put("productName", productName);
                        productInfo.put("price", price);
                        productInfo.put("wholesalePrice", wholesalePrice);
                        productInfo.put("availableQuantity", availableQuantity);
                        productInfo.put("wholesaleQuantity", wholesaleQuantity);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productInfo;
    }

    public void showChooseContractForm(Announcement announcement, String username) {
        List<Contract> userContracts = getUserContracts(username);

        ChoiceDialog<Contract> dialog = new ChoiceDialog<>(null, userContracts);
        dialog.setTitle("Choose Contract");
        dialog.setHeaderText("Choose a contract to add the product to:");
        dialog.setContentText("Contract:");

        Optional<Contract> result = dialog.showAndWait();
        result.ifPresent(selectedContract -> showAddToContractForm(announcement, selectedContract));
    }

    public void showAddToContractForm(Announcement announcement, Contract selectedContract) {
        Map<String, Object> productInfo = getCatalogItemById(announcement.getProductId());

        if (!productInfo.isEmpty()) {
            String productName = (String) productInfo.get("productName");
            double price = (double) productInfo.get("price");
            double wholesalePrice = (double) productInfo.get("wholesalePrice");
            int availableQuantity = (int) productInfo.get("availableQuantity");
            int wholesaleQuantity = (int) productInfo.get("wholesaleQuantity");

            Label productNameLabel = new Label("Product: " + productName);
            productNameLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

            Label contractIdLabel = new Label("Contract ID: " + selectedContract.getId());
            contractIdLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

            Label clientNameLabel = new Label("Client Name: " + selectedContract.getClientName());
            clientNameLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

            Label managerNameLabel = new Label("Manager Name: " + selectedContract.getManagerName());
            managerNameLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

            Label quantityLabel = new Label("Quantity:");
            TextField quantityTextField = new TextField();
            quantityLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
            quantityTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

            Label totalAmountLabel = new Label();
            totalAmountLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

            Button addButton = borderStyledButton("Add");
            Button cancelButton = borderStyledButton("Cancel");

            double initialTotalAmount = getTotalAmountByContractId(selectedContract.getId());
            totalAmountLabel.setText("Total Amount: " + initialTotalAmount);

            quantityTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    quantityTextField.setText(newValue.replaceAll("[^\\d]", ""));
                }
                try {
                    int quantity = Integer.parseInt(quantityTextField.getText());
                    if (quantity <= 0) {
                        showErrorAlert("Quantity must be greater than zero.");
                        return;
                    }
                    double additionalTotalAmount = calculateTotalAmount(selectedContract, quantity, price, wholesalePrice, wholesaleQuantity);
                    double totalAmount = initialTotalAmount + additionalTotalAmount;
                    totalAmountLabel.setText("Total Amount: " + totalAmount);
                } catch (NumberFormatException e) {
                    totalAmountLabel.setText("Total Amount: " + initialTotalAmount);
                }
            });

            Tooltip quantityTooltip = new Tooltip("Enter the quantity (numeric only)");
            quantityTooltip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            quantityTextField.setTooltip(quantityTooltip);

            addButton.setOnAction(event -> {
                String quantityText = quantityTextField.getText().trim();
                if (quantityText.isEmpty()) {
                    showErrorAlert("Please enter the quantity.");
                    return;
                }
                try {
                    int quantity = Integer.parseInt(quantityText);

                    if (quantity > availableQuantity) {
                        showErrorAlert("You can only add up to " + availableQuantity + " units of this product to the contract.");
                        return;
                    }

                    selectedContract.setProductCount(selectedContract.getProductCount() + quantity);
                    double additionalTotalAmount = calculateTotalAmount(selectedContract, quantity, price, wholesalePrice, wholesaleQuantity);
                    double totalAmount = initialTotalAmount + additionalTotalAmount;
                    int productId = announcement.getProductId();

                    updateContractInfo(selectedContract, additionalTotalAmount, productId, quantity, price);
                    showSuccessAlert("Product successfully added to the contract.");

                    int remainingQuantity = availableQuantity - quantity;
                    updateAvailableQuantityInDatabase(productId, remainingQuantity);

                    totalAmountLabel.setText("Total Amount: " + totalAmount);
                    ((Stage) addButton.getScene().getWindow()).close();
                } catch (NumberFormatException e) {
                    showErrorAlert("Enter a valid number for the product quantity.");
                }
            });

            cancelButton.setOnAction(event -> ((Stage) cancelButton.getScene().getWindow()).close());

            VBox layout = new VBox(10);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(10));
            layout.setStyle("-fx-background-color: black;");
            layout.getChildren().addAll(
                    productNameLabel, contractIdLabel, clientNameLabel, managerNameLabel,
                    quantityLabel, quantityTextField, totalAmountLabel,
                    addButton, cancelButton
            );

            Scene scene = new Scene(layout, 600, 300);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Add Product to Contract");
            stage.show();
        }
    }

    private double calculateTotalAmount(Contract selectedContract, int quantity, double price, double wholesalePrice, int wholesaleQuantity) {
        double additionalTotalAmount = 0.0;

        if (quantity >= wholesaleQuantity) {
            additionalTotalAmount = wholesalePrice * quantity;
        } else {
            additionalTotalAmount = price * quantity;
        }

        return additionalTotalAmount;
    }

    public double getTotalAmountByContractId(int contractId) {
        double totalAmount = 0.0;
        try (Connection connection = establishDBConnection()) {
            String query = "SELECT total_amount FROM contracts WHERE id_contracts = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, contractId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        totalAmount = resultSet.getDouble("total_amount");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalAmount;
    }

    private void updateAvailableQuantityInDatabase(int productId, int remainingQuantity) {
        try (Connection connection = establishDBConnection()) {
            String updateQuantityQuery = "UPDATE catalog SET available_quantity = ? WHERE product_id = ?";
            try (PreparedStatement updateQuantityStatement = connection.prepareStatement(updateQuantityQuery)) {
                updateQuantityStatement.setInt(1, remainingQuantity);
                updateQuantityStatement.setInt(2, productId);
                updateQuantityStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error updating available quantity in database: " + e.getMessage());
        }
    }

    private void updateContractInfo(Contract selectedContract, double additionalTotalAmount, int productId, int quantity, double price) {
        int contractId = selectedContract.getId();
        double newTotalAmount = getTotalAmountByContractId(contractId) + additionalTotalAmount;

        try (Connection connection = establishDBConnection()) {
            String updateContractQuery = "UPDATE contracts SET total_amount = ? WHERE id_contracts = ?";
            try (PreparedStatement updateContractStatement = connection.prepareStatement(updateContractQuery)) {
                updateContractStatement.setDouble(1, newTotalAmount);
                updateContractStatement.setInt(2, contractId);
                int rowsAffected = updateContractStatement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Failed to update total amount for contract ID: " + contractId);
                }
            }

            String insertCatalogContractQuery = "INSERT INTO catalog_contract (contract_id, product_id, product_price, product_count) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertCatalogContractStatement = connection.prepareStatement(insertCatalogContractQuery)) {
                insertCatalogContractStatement.setInt(1, contractId);
                insertCatalogContractStatement.setInt(2, productId);
                insertCatalogContractStatement.setDouble(3, price);
                insertCatalogContractStatement.setInt(4, quantity);
                insertCatalogContractStatement.executeUpdate();
            }

            logAction(contractId, productId, quantity, additionalTotalAmount, newTotalAmount);

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error updating contract info: " + e.getMessage());
        }
    }

    private void logAction(int contractId, int productId, int quantity, double priceChange, double newTotalAmount) {
        int clientId = SessionManager.getInstance().getClientIdByName(SessionManager.getInstance().getCurrentClientName());
        String actionType = "Add Product";
        String objectType = "Contract";
        String details = "Client with ID " + clientId + " added product ID " + productId + " to contract ID " + contractId + ". Quantity: " + quantity + ", Price Change: " + priceChange + ", New Total Amount: " + newTotalAmount;
        String userIp = SessionManager.getInstance().getIpAddress();
        String userDeviceType = SessionManager.getInstance().getDeviceType();

        try (Connection connection = establishDBConnection()) {
            String insertLogQuery = "INSERT INTO activity_log (user_id, action_type, object_type, details, timestamp, user_ip, user_device_type) VALUES (?, ?, ?, ?, NOW(), ?, ?)";
            try (PreparedStatement logStatement = connection.prepareStatement(insertLogQuery)) {
                logStatement.setInt(1, clientId);
                logStatement.setString(2, actionType);
                logStatement.setString(3, objectType);
                logStatement.setString(4, details);
                logStatement.setString(5, userIp);
                logStatement.setString(6, userDeviceType);
                logStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error logging action: " + e.getMessage());
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
            button.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: white; -fx-border-radius: 15px;");
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

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
