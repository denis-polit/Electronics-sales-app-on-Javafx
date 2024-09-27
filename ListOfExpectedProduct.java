import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class ListOfExpectedProduct {

    private final SessionManager sessionManager;
    private final FirstConnectionToDataBase connectionToDataBase;

    public ListOfExpectedProduct() {
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

    public void showExpectedOrdersWindow() {
        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT * FROM buy_application WHERE desired_deadline >= ? AND application_status = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            statement.setString(2, "under consideration");
            ResultSet resultSet = statement.executeQuery();

            Stage stage = new Stage();
            stage.setTitle("Expected Orders");

            VBox layout = new VBox(10);
            layout.setPadding(new Insets(20));
            layout.setStyle("-fx-background-color: black;");

            boolean hasOrders = false;

            while (resultSet.next()) {
                hasOrders = true;

                int buyApplicationId = resultSet.getInt("buy_application_id");
                int productId = resultSet.getInt("product_id");
                int productCount = resultSet.getInt("product_count");
                LocalDate desiredDeadline = resultSet.getDate("desired_deadline").toLocalDate();
                int clientId = resultSet.getInt("client_id");
                double applicationAmount = Double.parseDouble(resultSet.getString("application_amount"));

                Label orderLabel = new Label("Product ID: " + productId +
                        ", Count: " + productCount +
                        ", Deadline: " + desiredDeadline +
                        ", Client ID: " + clientId +
                        ", Amount: $" + applicationAmount);
                orderLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Gotham'; -fx-font-size: 16px;");
                orderLabel.setWrapText(true);
                orderLabel.setMaxWidth(360);

                Button deleteButton = borderStyledButton("Delete");
                deleteButton.setStyle("-fx-background-color: white; -fx-text-fill: black;");

                Button editStatusButton = borderStyledButton("Edit Status");
                editStatusButton.setStyle("-fx-background-color: white; -fx-text-fill: black;");

                HBox buttonBox = new HBox(10);
                buttonBox.getChildren().addAll(deleteButton, editStatusButton);

                VBox orderBox = new VBox(5);
                orderBox.getChildren().addAll(orderLabel, buttonBox);

                deleteButton.setOnAction(e -> {
                    showDeleteConfirmation(buyApplicationId, orderBox, layout);
                });

                editStatusButton.setOnAction(e -> editOrderStatus(buyApplicationId));

                Separator separator = new Separator();

                layout.getChildren().addAll(orderBox, separator);
            }

            if (!hasOrders) {
                Label noOrdersLabel = new Label("No expected orders found.");
                noOrdersLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Gotham'; -fx-font-size: 16px;");
                layout.getChildren().add(noOrdersLabel);
            }

            Scene scene = new Scene(layout, 350, 600);
            stage.setScene(scene);
            stage.show();

        } catch (SQLException e) {
            showErrorAlert("Error fetching expected orders: " + e.getMessage());
        }
    }

    private void showDeleteConfirmation(int buyApplicationId, VBox orderBox, VBox layout) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this order?");

        ButtonType confirmButton = new ButtonType("Yes");
        ButtonType cancelButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == confirmButton) {
                deleteOrder(buyApplicationId);
                layout.getChildren().remove(orderBox);
            }
        });
    }

    private void deleteOrder(int buyApplicationId) {
        try (Connection connection = establishDBConnection()) {
            String sql = "DELETE FROM buy_application WHERE buy_application_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, buyApplicationId);
            statement.executeUpdate();

            showSuccessAlert("Order deleted successfully!");
        } catch (SQLException e) {
            showErrorAlert("Error deleting order: " + e.getMessage());
        }
    }

    private void editOrderStatus(int buyApplicationId) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Order Status");
        dialog.setHeaderText(null);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("under consideration", "requalified", "completed successfully", "completed with errors");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().add(comboBox);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return comboBox.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newStatus -> {
            if (newStatus != null) {
                if ("requalified".equals(newStatus)) {
                    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmationAlert.setTitle("Confirmation");
                    confirmationAlert.setHeaderText(null);
                    confirmationAlert.setContentText("This order will be moved to contracts. Do you want to proceed?");

                    Optional<ButtonType> result = confirmationAlert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        moveOrderToContracts(buyApplicationId, newStatus);
                    }
                } else {
                    updateOrderStatus(buyApplicationId, newStatus);
                }
            }
        });
    }

    private void moveOrderToContracts(int buyApplicationId, String newStatus) {
        try (Connection connection = establishDBConnection()) {
            connection.setAutoCommit(false);

            String selectApplicationSql = "SELECT * FROM buy_application WHERE buy_application_id = ?";
            PreparedStatement selectApplicationStatement = connection.prepareStatement(selectApplicationSql);
            selectApplicationStatement.setInt(1, buyApplicationId);
            ResultSet applicationResultSet = selectApplicationStatement.executeQuery();

            if (applicationResultSet.next()) {
                int productId = applicationResultSet.getInt("product_id");
                int productCount = applicationResultSet.getInt("product_count");
                LocalDate desiredDeadline = applicationResultSet.getDate("desired_deadline").toLocalDate();
                int clientId = applicationResultSet.getInt("client_id");
                double applicationAmount = Double.parseDouble(applicationResultSet.getString("application_amount"));
                String applicationDeliveryMethod = applicationResultSet.getString("application_delivery_method");
                String applicationPaymentMethod = applicationResultSet.getString("application_payment_method");

                String selectCatalogSql = "SELECT * FROM catalog WHERE product_id = ?";
                PreparedStatement selectCatalogStatement = connection.prepareStatement(selectCatalogSql);
                selectCatalogStatement.setInt(1, productId);
                ResultSet catalogResultSet = selectCatalogStatement.executeQuery();

                if (catalogResultSet.next()) {
                    String productTitle = catalogResultSet.getString("product_title");
                    String managerId = catalogResultSet.getString("creator_name");

                    String insertContractSql = "INSERT INTO contracts (product_id, product_count, delivery_method, status, deadline, total_amount, manager_id, client_id, pay_method, additional_products, creation_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement insertContractStatement = connection.prepareStatement(insertContractSql);
                    insertContractStatement.setInt(1, productId);
                    insertContractStatement.setString(2, String.valueOf(productCount));
                    insertContractStatement.setString(3, applicationDeliveryMethod);
                    insertContractStatement.setString(4, "requalified");
                    insertContractStatement.setString(5, String.valueOf(desiredDeadline));
                    insertContractStatement.setString(6, String.valueOf(applicationAmount));
                    insertContractStatement.setString(7, managerId);
                    insertContractStatement.setString(8, String.valueOf(clientId));
                    insertContractStatement.setString(9, applicationPaymentMethod);
                    insertContractStatement.setString(10, "");
                    insertContractStatement.setString(11, String.valueOf(LocalDate.now()));

                    insertContractStatement.executeUpdate();
                }
            }

            String updateStatusSql = "UPDATE buy_application SET application_status = ? WHERE buy_application_id = ?";
            PreparedStatement updateStatusStatement = connection.prepareStatement(updateStatusSql);
            updateStatusStatement.setString(1, newStatus);
            updateStatusStatement.setInt(2, buyApplicationId);
            updateStatusStatement.executeUpdate();

            connection.commit();
            showSuccessAlert("Order status updated and moved to contracts successfully!");
        } catch (SQLException e) {
            showErrorAlert("Error moving order to contracts: " + e.getMessage());
        }
    }

    private void updateOrderStatus(int buyApplicationId, String newStatus) {
        try (Connection connection = establishDBConnection()) {
            String updateStatusSql = "UPDATE buy_application SET application_status = ? WHERE buy_application_id = ?";
            PreparedStatement updateStatusStatement = connection.prepareStatement(updateStatusSql);
            updateStatusStatement.setString(1, newStatus);
            updateStatusStatement.setInt(2, buyApplicationId);
            updateStatusStatement.executeUpdate();

            showSuccessAlert("Order status updated successfully!");
        } catch (SQLException e) {
            showErrorAlert("Error updating order status: " + e.getMessage());
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
