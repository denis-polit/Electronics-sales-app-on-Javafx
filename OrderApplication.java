import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class OrderApplication {

    private final FirstConnectionToDataBase connectionToDataBase;
    private final AlertService alertService;

    public OrderApplication() {
        SessionManager sessionManager = SessionManager.getInstance();
        alertService = new AlertServiceImpl();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish database connection: " + e.getMessage(), e);
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void handleOrderApplication(Announcement announcement) {
        int productId = announcement.getProductId();
        SessionManager sessionManager = SessionManager.getInstance();

        if (!sessionManager.isClientEnter()) {
            alertService.showErrorAlert("Please log in before placing an order.");
            return;
        }

        Stage orderStage = new Stage();
        orderStage.setTitle("Order Supply");
        orderStage.setWidth(400);
        orderStage.setHeight(450);

        VBox orderLayout = new VBox(10);
        orderLayout.setPadding(new Insets(20));
        orderLayout.setAlignment(Pos.CENTER);
        orderLayout.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        Label productCountLabel = new Label("Product Count:");
        productCountLabel.setTextFill(Color.WHITE);
        TextField productCountField = new TextField();
        productCountField.setPromptText("Enter product count");
        productCountField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

        Label totalPriceLabel = new Label("Total Price: ");
        totalPriceLabel.setTextFill(Color.WHITE);

        Label deadlineLabel = new Label("Desired Deadline:");
        deadlineLabel.setTextFill(Color.WHITE);
        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Select desired deadline");
        deadlinePicker.setStyle("-fx-background-color: black; -fx-control-inner-background: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        deadlinePicker.getEditor().setStyle("-fx-text-fill: white;");

        Label deliveryMethodLabel = new Label("Delivery Method:");
        deliveryMethodLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
        ComboBox<String> deliveryMethodComboBox = new ComboBox<>();
        deliveryMethodComboBox.getItems().addAll("Courier delivery", "Mail delivery", "Self pick-up");
        deliveryMethodComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        deliveryMethodComboBox.getEditor().setStyle("-fx-text-fill: white;");

        Label paymentMethodLabel = new Label("Payment Method:");
        paymentMethodLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
        ComboBox<String> paymentMethodComboBox = new ComboBox<>();
        paymentMethodComboBox.getItems().addAll("Cash", "Credit card", "Bank transfer");
        paymentMethodComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        paymentMethodComboBox.getEditor().setStyle("-fx-text-fill: white;");

        Button submitButton = ButtonStyle.expandPaneStyledButton(" Submit ");
        submitButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        submitButton.setOnAction(event -> {
            try {
                int productCount = Integer.parseInt(productCountField.getText());
                LocalDate deadline = deadlinePicker.getValue();
                String deliveryMethod = deliveryMethodComboBox.getValue();
                String paymentMethod = paymentMethodComboBox.getValue();

                if (deadline.isBefore(LocalDate.now().plusDays(3))) {
                    alertService.showErrorAlert("The desired delivery date should be no earlier than 3 days from the current date.");
                    return;
                }

                String clientName = sessionManager.getCurrentClientName();
                int clientId = sessionManager.getClientIdByName(clientName);

                if (clientId == -1) {
                    alertService.showErrorAlert("Client ID not found for the given client name.");
                    return;
                }

                double applicationAmount = calculateApplicationAmount(announcement, productCount);
                saveSupplyOrder(productId, productCount, deadline, clientId, applicationAmount, deliveryMethod, paymentMethod);

                orderStage.close();
            } catch (NumberFormatException e) {
                alertService.showErrorAlert("Enter the correct quantity of the product.");
            }
        });

        productCountField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int productCount = Integer.parseInt(newValue);
                double applicationAmount = calculateApplicationAmount(announcement, productCount);
                totalPriceLabel.setText("Total Price: " + String.format("%.2f", applicationAmount));
            } catch (NumberFormatException ignored) {
            }
        });

        orderLayout.getChildren().addAll(
                productCountLabel, productCountField,
                totalPriceLabel,
                deadlineLabel, deadlinePicker,
                deliveryMethodLabel, deliveryMethodComboBox,
                paymentMethodLabel, paymentMethodComboBox,
                submitButton
        );

        Scene orderScene = new Scene(orderLayout);
        orderStage.setScene(orderScene);
        orderStage.show();
    }

    private void saveSupplyOrder(int productId, int productCount, LocalDate desiredDeadline, int clientId, double applicationAmount, String deliveryMethod, String paymentMethod) {
        try (Connection connection = establishDBConnection()) {
            String sql = "INSERT INTO buy_application (product_id, product_count, desired_deadline, client_id, application_amount, application_status, application_delivery_method, application_payment_method) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);
            statement.setInt(2, productCount);
            statement.setDate(3, java.sql.Date.valueOf(desiredDeadline));
            statement.setInt(4, clientId);
            statement.setString(5, String.valueOf(applicationAmount));
            statement.setString(6, "under consideration");
            statement.setString(7, deliveryMethod);
            statement.setString(8, paymentMethod);

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                alertService.showSuccessAlert("Supply order successfully saved!");
            } else {
                alertService.showErrorAlert("Failed to save supply order.");
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error saving supply order: " + e.getMessage());
        }
    }

    private double calculateApplicationAmount(Announcement announcement, int productCount) {
        if (productCount >= announcement.getWholesaleQuantity()) {
            return productCount * announcement.getWholesalePrice();
        } else {
            return productCount * announcement.getPrice();
        }
    }
}
