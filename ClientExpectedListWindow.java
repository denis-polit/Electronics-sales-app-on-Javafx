import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientExpectedListWindow extends Application {
    private int clientId;
    private VBox itemListLayout;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public ClientExpectedListWindow(int clientId) {
        this.clientId = clientId;
        try {
            FirstConnectionToDataBase connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        HeaderComponent headerComponent = new HeaderComponent(primaryStage);
        VBox header = headerComponent.createHeader();
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setBorder(new Border(new BorderStroke(Color.GRAY,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0))));
        root.setTop(header);

        ScrollPane scrollPane = new ScrollPane();
        this.itemListLayout = new VBox(10);
        this.itemListLayout.setPadding(new Insets(10));
        this.itemListLayout.setStyle("-fx-background-color: black;");
        this.itemListLayout.setAlignment(Pos.CENTER);

        scrollPane.setContent(this.itemListLayout);
        root.setCenter(scrollPane);
        scrollPane.setStyle("-fx-background: black;");

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("User Expected List");
        primaryStage.show();

        fetchAndDisplayExpectedItems(clientId);
    }

    private void fetchAndDisplayExpectedItems(int clientId) {
        try {
            Connection connection = establishDBConnection();
            String sql = "SELECT * FROM buy_application WHERE client_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, clientId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int applicationId = resultSet.getInt("buy_application_id");
                int productId = resultSet.getInt("product_id");
                int productCount = resultSet.getInt("product_count");
                String deadline = resultSet.getString("desired_deadline");
                String status = resultSet.getString("application_status");

                Text itemDetails = new Text("Product ID: " + productId + ", Count: " + productCount + ", Deadline: " + deadline + ", Status: " + status);
                itemDetails.setFont(Font.font("Arial", 16));
                itemDetails.setFill(Color.WHITE);

                Button deleteButton = ButtonStyle.createStyledButton(" - ");
                deleteButton.setOnAction(e -> deleteExpectedItem(applicationId, clientId));

                Button detailsButton = ButtonStyle.createStyledButton("Details");
                detailsButton.setOnAction(event -> showProductDetails(productId));

                Button changeButton = ButtonStyle.createStyledButton("Change");
                changeButton.setOnAction(e -> openChangeItemWindow(applicationId, clientId));

                HBox itemLayout = new HBox(10, itemDetails, deleteButton, changeButton, detailsButton);
                itemLayout.setAlignment(Pos.CENTER_LEFT);
                itemLayout.setPadding(new Insets(10));
                itemLayout.setStyle("-fx-background-color: black;");

                itemListLayout.getChildren().add(itemLayout);
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to fetch expected items: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return FirstConnectionToDataBase.getInstance().getConnection();
    }

    private void deleteExpectedItem(int applicationId, int clientId) {
        try {
            Connection connection = establishDBConnection();

            String selectSql = "SELECT * FROM buy_application WHERE buy_application_id = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setInt(1, applicationId);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                int productCount = resultSet.getInt("product_count");
                String deadline = resultSet.getString("desired_deadline");
                String status = resultSet.getString("application_status");
                String paymentMethod = resultSet.getString("application_payment_method");
                String deliveryMethod = resultSet.getString("application_delivery_method");

                String deleteSql = "DELETE FROM buy_application WHERE buy_application_id = ?";
                PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
                deleteStatement.setInt(1, applicationId);
                deleteStatement.executeUpdate();

                int userId = clientId;
                String logMessage = String.format("Deleted item (ID: %d): Product ID: %d, Count: %d, Deadline: %s, Status: %s, Payment Method: %s, Delivery Method: %s",
                        applicationId, productId, productCount, deadline, status, paymentMethod, deliveryMethod);
                SessionManager.getInstance().logActivity(userId, "Delete", "ExpectedItem", logMessage);

                itemListLayout.getChildren().clear();
                fetchAndDisplayExpectedItems(clientId);
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to delete item: " + e.getMessage());
        }
    }

    private void openChangeItemWindow(int applicationId, int clientId) {
        try {
            Connection connection = establishDBConnection();
            String sql = "SELECT * FROM buy_application WHERE buy_application_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, applicationId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int currentProductId = resultSet.getInt("product_id");
                int currentProductCount = resultSet.getInt("product_count");
                String currentDeadline = resultSet.getString("desired_deadline");
                String currentStatus = resultSet.getString("application_status");
                String currentPaymentMethod = resultSet.getString("application_payment_method");
                String currentDeliveryMethod = resultSet.getString("application_delivery_method");

                Stage changeItemStage = new Stage();
                changeItemStage.setTitle("Change Item");

                StackPane root = new StackPane();
                root.setStyle("-fx-background-color: black;");

                GridPane grid = new GridPane();
                grid.setHgap(20);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                Label productIdLabel = new Label("Product ID:");
                productIdLabel.setFont(Font.font("Arial", 16));
                productIdLabel.setTextFill(Color.WHITE);
                productIdLabel.setMinWidth(150);

                TextField productIdField = new TextField(String.valueOf(currentProductId));
                productIdField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

                Label productCountLabel = new Label("Product Count:");
                productCountLabel.setFont(Font.font("Arial", 16));
                productCountLabel.setTextFill(Color.WHITE);
                productCountLabel.setMinWidth(150);

                TextField productCountField = new TextField(String.valueOf(currentProductCount));
                productCountField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

                Label deadlineLabel = new Label("Deadline:");
                deadlineLabel.setFont(Font.font("Arial", 16));
                deadlineLabel.setTextFill(Color.WHITE);
                deadlineLabel.setMinWidth(150);

                TextField deadlineField = new TextField(currentDeadline);
                deadlineField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

                Label statusLabel = new Label("Status:");
                statusLabel.setFont(Font.font("Arial", 16));
                statusLabel.setTextFill(Color.WHITE);
                statusLabel.setMinWidth(150);

                Label statusValueLabel = new Label(currentStatus);
                statusValueLabel.setFont(Font.font("Arial", 16));
                statusValueLabel.setTextFill(Color.WHITE);
                statusValueLabel.setMinWidth(150);

                Label paymentMethodLabel = new Label("Payment Method:");
                paymentMethodLabel.setFont(Font.font("Arial", 16));
                paymentMethodLabel.setTextFill(Color.WHITE);
                paymentMethodLabel.setMinWidth(150);

                ComboBox<String> paymentMethodComboBox = new ComboBox<>();
                paymentMethodComboBox.getItems().addAll("Cash", "Credit card", "Bank transfer");
                paymentMethodComboBox.setValue(currentPaymentMethod);
                paymentMethodComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
                paymentMethodComboBox.getEditor().setStyle("-fx-text-fill: white;");
                paymentMethodComboBox.setPrefWidth(150);

                Label deliveryMethodLabel = new Label("Delivery Method:");
                deliveryMethodLabel.setFont(Font.font("Arial", 16));
                deliveryMethodLabel.setTextFill(Color.WHITE);
                deliveryMethodLabel.setMinWidth(150);

                ComboBox<String> deliveryMethodComboBox = new ComboBox<>();
                deliveryMethodComboBox.getItems().addAll("Courier", "Postal", "Self pickup");
                deliveryMethodComboBox.setValue(currentDeliveryMethod);
                deliveryMethodComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
                deliveryMethodComboBox.getEditor().setStyle("-fx-text-fill: white;");
                deliveryMethodComboBox.setPrefWidth(150);

                Button updateButton = ButtonStyle.createStyledButton("Update");
                updateButton.setOnAction(event -> updateExpectedItem(applicationId, Integer.parseInt(productIdField.getText()), Integer.parseInt(productCountField.getText()),
                        deadlineField.getText(), statusValueLabel.getText(), paymentMethodComboBox.getValue(), deliveryMethodComboBox.getValue(), clientId, changeItemStage));

                grid.add(productIdLabel, 0, 0);
                grid.add(productIdField, 1, 0);
                grid.add(productCountLabel, 0, 1);
                grid.add(productCountField, 1, 1);
                grid.add(deadlineLabel, 0, 2);
                grid.add(deadlineField, 1, 2);
                grid.add(statusLabel, 0, 3);
                grid.add(statusValueLabel, 1, 3);
                grid.add(paymentMethodLabel, 0, 4);
                grid.add(paymentMethodComboBox, 1, 4);
                grid.add(deliveryMethodLabel, 0, 5);
                grid.add(deliveryMethodComboBox, 1, 5);
                grid.add(updateButton, 0, 6, 2, 1);

                root.getChildren().add(grid);
                Scene scene = new Scene(root, 600, 400);
                changeItemStage.setScene(scene);
                changeItemStage.show();
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to open change item window: " + e.getMessage());
        }
    }

    private void updateExpectedItem(int applicationId, int newProductId, int newProductCount, String newDeadline,
                                    String newStatus, String newPaymentMethod, String newDeliveryMethod,
                                    int clientId, Stage changeItemStage) {
        try {
            Connection connection = establishDBConnection();
            String updateSql = "UPDATE buy_application SET product_id = ?, product_count = ?, desired_deadline = ?, application_status = ?, application_payment_method = ?, application_delivery_method = ? WHERE buy_application_id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);
            updateStatement.setInt(1, newProductId);
            updateStatement.setInt(2, newProductCount);
            updateStatement.setString(3, newDeadline);
            updateStatement.setString(4, newStatus);
            updateStatement.setString(5, newPaymentMethod);
            updateStatement.setString(6, newDeliveryMethod);
            updateStatement.setInt(7, applicationId);
            updateStatement.executeUpdate();

            int userId = clientId;
            String logMessage = String.format("Updated item (ID: %d): New Product ID: %d, New Count: %d, New Deadline: %s, New Status: %s, New Payment Method: %s, New Delivery Method: %s",
                    applicationId, newProductId, newProductCount, newDeadline, newStatus, newPaymentMethod, newDeliveryMethod);
            SessionManager.getInstance().logActivity(userId, "Update", "ExpectedItem", logMessage);

            changeItemStage.close();

            itemListLayout.getChildren().clear();
            fetchAndDisplayExpectedItems(clientId);
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to update item: " + e.getMessage());
        }
    }

    private void showProductDetails(int productId) {
        Stage detailStage = new Stage();
        detailStage.setTitle("Product Details");

        VBox detailVBox = new VBox(10);
        detailVBox.setAlignment(Pos.CENTER);
        detailVBox.setStyle("-fx-background-color: black; -fx-padding: 20px;");

        try (Connection connection = establishDBConnection()) {
            String selectSql = "SELECT * FROM catalog WHERE product_id = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
                selectStatement.setInt(1, productId);
                ResultSet resultSet = selectStatement.executeQuery();
                if (resultSet.next()) {
                    StringBuilder details = new StringBuilder();
                    details.append("Graphics Chip: ").append(resultSet.getString("graphics_chip")).append("\n");
                    details.append("Memory Frequency: ").append(resultSet.getString("memory_frequency")).append("\n");
                    details.append("Core Frequency: ").append(resultSet.getString("core_frequency")).append("\n");
                    details.append("Memory Capacity: ").append(resultSet.getString("memory_capacity")).append("\n");
                    details.append("Bit Size Memory Bus: ").append(resultSet.getString("bit_size_memory_bus")).append("\n");
                    details.append("Maximum Supported Resolution: ").append(resultSet.getString("maximum_supported_resolution")).append("\n");
                    details.append("Minimum Required BZ Capacity: ").append(resultSet.getString("minimum_required_BZ_capacity")).append("\n");
                    details.append("Memory Type: ").append(resultSet.getString("memory_type")).append("\n");
                    details.append("Producing Country: ").append(resultSet.getString("producing_country")).append("\n");
                    details.append("Supported 3D APIs: ").append(resultSet.getString("supported_3D_APIs")).append("\n");
                    details.append("Form Factor: ").append(resultSet.getString("form_factor")).append("\n");
                    details.append("Type of Cooling System: ").append(resultSet.getString("type_of_cooling_system")).append("\n");
                    details.append("Guarantee: ").append(resultSet.getString("guarantee")).append("\n");
                    details.append("Price: ").append(resultSet.getString("price")).append("\n");
                    details.append("Wholesale Price: ").append(resultSet.getString("wholesale_price")).append("\n");
                    details.append("Brand: ").append(resultSet.getString("brand")).append("\n");
                    details.append("Product Title: ").append(resultSet.getString("product_title")).append("\n");
                    details.append("Creation Date: ").append(resultSet.getString("creation_data")).append("\n");
                    details.append("Product Description: ").append(resultSet.getString("product_description")).append("\n");
                    details.append("Creator Name: ").append(resultSet.getString("creator_name")).append("\n");
                    details.append("Available Quantity: ").append(resultSet.getString("available_quantity")).append("\n");
                    details.append("Wholesale Quantity: ").append(resultSet.getString("wholesale_quantity")).append("\n");

                    // Displaying product details
                    Label detailsLabel = new Label(details.toString());
                    detailsLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
                    detailsLabel.setTextFill(Color.WHITE);
                    detailsLabel.setLineSpacing(5);

                    detailVBox.getChildren().add(detailsLabel);
                } else {
                    alertService.showErrorAlert("Product details not found for ID: " + productId);
                    return;
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error fetching product details: " + e.getMessage());
            return;
        }

        ScrollPane scrollPane = new ScrollPane(detailVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: black;");

        Scene detailScene = new Scene(scrollPane, 600, 400);
        detailStage.setScene(detailScene);
        detailStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class HeaderComponent {
        private final Stage primaryStage;

        public HeaderComponent(Stage primaryStage) {
            this.primaryStage = primaryStage;
        }

        private VBox createHeader() {
            VBox header = new VBox(10);
            header.setPadding(new Insets(5));
            header.setStyle("-fx-background-color: black");

            Image logoImage = new Image("file:icons/LOGO_our.jpg");
            ImageView logoImageView = new ImageView(logoImage);
            logoImageView.setFitWidth(50);
            logoImageView.setFitHeight(50);

            Circle logoCircle = new Circle(25);
            logoCircle.setFill(new ImagePattern(logoImage));
            logoCircle.setCursor(javafx.scene.Cursor.HAND);

            Region leftRegion = new Region();
            HBox.setHgrow(leftRegion, Priority.ALWAYS);

            Button menuButton = ButtonStyle.createStyledButton("Menu");
            menuButton.setOnAction(e -> showMenu());

            Button supportButton = ButtonStyle.createStyledButton("Support");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = ButtonStyle.createStyledButton("Privacy Policy");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button backButton = ButtonStyle.createStyledButton("Back");
            backButton.setOnAction(event -> primaryStage.close());

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, backButton, leftRegion);
            topContent.setAlignment(Pos.CENTER);
            topContent.setSpacing(10);

            HBox.setMargin(logoCircle, new Insets(0, 370, 0, 0));

            header.getChildren().addAll(topContent);

            return header;
        }

        private void showMenu() {
            primaryStage.close();
            Stage menuStage = new Stage();
            MenuPage menuPage = new MenuPage();
            menuPage.start(menuStage);
        }

        private void showSupportWindow() {
            SupportWindow supportWindow = new SupportWindow();
            Stage supportStage = new Stage();
            supportWindow.start(supportStage);
            supportStage.show();
        }

        private void showPrivacyPolicyWindow() {
            PrivacyPolicyWindow privacyPolicyWindow = new PrivacyPolicyWindow();
            Stage privacyStage = new Stage();
            privacyPolicyWindow.start(privacyStage);
            privacyStage.show();
        }
    }
}