import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientContractsPage extends Application {

    private String clientName;
    private BorderPane root;
    private MenuPage menuPage;
    EditClientContract editClientContract = new EditClientContract();
    private FirstConnectionToDataBase connectionToDataBase;

    public ClientContractsPage(String clientName) {
        this.clientName = clientName;
        this.menuPage = new MenuPage();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.root = new BorderPane();

        root.setStyle("-fx-background-color: black;");

        HeaderComponent headerComponent = new HeaderComponent(primaryStage);
        VBox header = headerComponent.createHeader();
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setBorder(new Border(new BorderStroke(Color.GRAY,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0))));
        root.setTop(header);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: black;");
        root.setCenter(scrollPane);

        CreateContract createContract = new CreateContract();

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Client Contracts");
        primaryStage.show();

        showClientContracts(clientName, scrollPane);

        try {
            // Создаем экземпляр FirstConnectionToDataBase
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    private void showErrorAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    public class HeaderComponent {

        private Stage primaryStage;

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
            logoCircle.setCursor(Cursor.HAND);

            Region leftRegion = new Region();
            HBox.setHgrow(leftRegion, Priority.ALWAYS);

            Button menuButton = createStyledButton("  Menu  ");
            menuButton.setOnAction(e -> showMenu());

            Button supportButton = createStyledButton("  Support  ");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = createStyledButton("  Privacy Policy  ");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button backButton = createStyledButton("  Back  ");
            backButton.setOnAction(event -> {
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.close();
            });

            Button accountButton = createStyledButton("  Personal Account  ");
            accountButton.setOnAction(e -> menuPage.showRegistrationWindow(primaryStage));

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, backButton, accountButton, leftRegion);
            topContent.setAlignment(Pos.CENTER);
            topContent.setSpacing(10);

            HBox.setMargin(logoCircle, new Insets(0, 230, 0, 0));

            header.getChildren().addAll(topContent);

            return header;
        }

        private Button createStyledButton(String text) {
            Button button = new Button(text);
            button.setTextFill(javafx.scene.paint.Color.WHITE);
            button.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 14));
            button.setStyle("-fx-background-color: black; -fx-background-radius: 15px;");
            button.setOpacity(1.0);

            FadeTransition colorIn = new FadeTransition(Duration.millis(300), button);
            colorIn.setToValue(1.0);

            FadeTransition colorOut = new FadeTransition(Duration.millis(300), button);
            colorOut.setToValue(0.7);

            button.setOnMouseEntered(e -> {
                colorIn.play();
                button.setStyle("-fx-background-color: blue; -fx-background-radius: 15px;");
            });

            button.setOnMouseExited(e -> {
                colorOut.play();
                button.setStyle("-fx-background-color: black; -fx-background-radius: 15px;");
            });

            return button;
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

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setTextFill(javafx.scene.paint.Color.WHITE);
        button.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 14));
        button.setStyle("-fx-background-color: black; -fx-background-radius: 15px;");
        button.setOpacity(1.0);

        FadeTransition colorIn = new FadeTransition(Duration.millis(300), button);
        colorIn.setToValue(1.0);

        FadeTransition colorOut = new FadeTransition(Duration.millis(300), button);
        colorOut.setToValue(0.7);

        button.setOnMouseEntered(e -> {
            colorIn.play();
            button.setStyle("-fx-background-color: blue; -fx-background-radius: 15px;");
        });

        button.setOnMouseExited(e -> {
            colorOut.play();
            button.setStyle("-fx-background-color: black; -fx-background-radius: 15px;");
        });

        return button;
    }

    private void showClientContracts(String clientName, ScrollPane scrollPane) {
        try {
            List<Contract> contracts = getClientContracts(clientName);
            VBox contractInfoVBox = new VBox(10);

            for (Contract contract : contracts) {
                Label contractIdLabel = createBoldLabel("ID: " + contract.getId(), "-fx-text-fill: white;");
                Label statusLabel = createBoldLabel("Status: " + contract.getStatus(), "-fx-text-fill: white;");
                Label deadlineLabel = createBoldLabel("Deadline: " + contract.getDeadline(), "-fx-text-fill: white;");
                Label totalAmountLabel = createBoldLabel("Total Amount: " + contract.getTotalAmount(), "-fx-text-fill: white;");

                Button expandButton = createStyledButton(" Expand ");
                expandButton.setOnAction(e -> {
                    try {
                        showContractDetails(contract.getId());
                    } catch (SQLException ex) {
                        showErrorAlert("Error fetching contract details: " + ex.getMessage());
                    }
                });

                Button deleteButton = createStyledButton(" - ");
                deleteButton.setOnAction(e -> {
                    try {
                        String contractStatus = contract.getStatus();
                        if (contractStatus.equals("Under consideration")) {
                            deleteContract(contract.getId());
                            showClientContracts(clientName, scrollPane);
                        } else {
                            showErrorAlert("To delete this contract, please contact the manager.");
                        }
                    } catch (SQLException ex) {
                        showErrorAlert("Error deleting contract: " + ex.getMessage());
                    }
                });

                Button editButton = createStyledButton("Edit");
                editButton.setOnAction(e -> {
                    try {
                        editClientContract.handleEditContract(contract.getId());
                    } catch (Exception ex) {
                        showErrorAlert("Error handling edit action: " + ex.getMessage());
                    }
                });

                HBox contractDetailsHBox = new HBox(10);
                contractDetailsHBox.getChildren().addAll(statusLabel, deadlineLabel, totalAmountLabel, expandButton, editButton, deleteButton);

                contractInfoVBox.getChildren().add(contractDetailsHBox);
            }

            scrollPane.setContent(contractInfoVBox);
        } catch (SQLException e) {
            showErrorAlert("Error fetching client contracts: " + e.getMessage());
        }
    }

    private List<Contract> getClientContracts(String clientName) throws SQLException {
        List<Contract> contracts = new ArrayList<>();
        try (Connection connection = establishDBConnection();
             PreparedStatement contactIdStatement = connection.prepareStatement("SELECT contact_id FROM user_contacts WHERE user_id = (SELECT id FROM users WHERE user_name = ?)");
        ) {
            contactIdStatement.setString(1, clientName);
            try (ResultSet contactIdResultSet = contactIdStatement.executeQuery()) {
                while (contactIdResultSet.next()) {
                    int contactId = contactIdResultSet.getInt("contact_id");
                    try (PreparedStatement contractDetailsStatement = connection.prepareStatement("SELECT * FROM contracts WHERE id_contracts = ?")) {
                        contractDetailsStatement.setInt(1, contactId);
                        try (ResultSet resultSet = contractDetailsStatement.executeQuery()) {
                            while (resultSet.next()) {
                                Contract contract = new Contract(
                                        resultSet.getInt("id_contracts"),
                                        clientName,
                                        resultSet.getString("manager_id"),
                                        resultSet.getInt("product_count"),
                                        resultSet.getString("status"),
                                        resultSet.getString("delivery_method"),
                                        resultSet.getString("pay_method"),
                                        null,
                                        resultSet.getString("additional_products"),
                                        resultSet.getString("deadline"),
                                        resultSet.getDouble("total_amount")
                                );
                                contracts.add(contract);
                            }
                        }
                    }
                }
            }
        }
        return contracts;
    }

    private void deleteContract(int contractId) throws SQLException {
        Connection connection = establishDBConnection();
        String managerId = null;
        String clientId = null;

        try (PreparedStatement getIdStatement = connection.prepareStatement("SELECT manager_id, client_id FROM contracts WHERE id_contracts = ?")) {
            getIdStatement.setInt(1, contractId);
            try (ResultSet idResultSet = getIdStatement.executeQuery()) {
                if (idResultSet.next()) {
                    managerId = idResultSet.getString("manager_id");
                    clientId = idResultSet.getString("client_id");
                }
            }
        }

        if (managerId != null) {
            try (PreparedStatement updateManagerStatement = connection.prepareStatement("UPDATE managers SET managers_contract = REPLACE(REPLACE(managers_contract, ?, ''), ',,', '') WHERE manager_name = ?")) {
                updateManagerStatement.setString(1, "," + contractId);
                updateManagerStatement.setString(2, managerId);
                updateManagerStatement.executeUpdate();
            }

            try (PreparedStatement checkManagerStatement = connection.prepareStatement("SELECT managers_contract FROM managers WHERE manager_name = ?")) {
                checkManagerStatement.setString(1, managerId);
                try (ResultSet resultSet = checkManagerStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String managerContracts = resultSet.getString("managers_contract");
                        if (managerContracts.contains("," + contractId + ",")) {
                            showErrorAlert("Failed to remove contract from manager's contracts list.");
                            return;
                        }
                    } else {
                        showErrorAlert("Manager not found: " + managerId);
                        return;
                    }
                }
            }
        }

        if (clientId != null) {
            try (PreparedStatement updateUserStatement = connection.prepareStatement("UPDATE users SET user_contacts = REPLACE(REPLACE(user_contacts, ?, ''), ',,', '') WHERE user_name = ?")) {
                updateUserStatement.setString(1, "," + contractId);
                updateUserStatement.setString(2, clientName);
                updateUserStatement.executeUpdate();
            }

            try (PreparedStatement checkUserStatement = connection.prepareStatement("SELECT user_contacts FROM users WHERE user_name = ?")) {
                checkUserStatement.setString(1, clientName);
                try (ResultSet resultSet = checkUserStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String userContracts = resultSet.getString("user_contacts");
                        if (userContracts.contains("," + contractId + ",")) {
                            showErrorAlert("Failed to remove contract from client's contracts list.");
                            return;
                        }
                    } else {
                        showErrorAlert("Client not found: " + clientName);
                        return;
                    }
                }
            }
        }

        String deleteContractSql = "DELETE FROM contracts WHERE id_contracts = ?";
        try (PreparedStatement deleteContractStatement = connection.prepareStatement(deleteContractSql)) {
            deleteContractStatement.setInt(1, contractId);
            int rowsAffected = deleteContractStatement.executeUpdate();
            if (rowsAffected > 0) {
                showSuccessAlert("Contract successfully deleted.");

                // Log the deletion action
                SessionManager sessionManager = SessionManager.getInstance();
                int userId = sessionManager.getClientIdByName(sessionManager.getCurrentClientName());
                String actionType = "Delete";
                String objectType = "Contract";
                String details = "Deleted contract with ID: " + contractId;

                sessionManager.logActivity(userId, actionType, objectType, details);
            } else {
                showErrorAlert("Failed to delete contract with ID: " + contractId);
            }
        }

        connection.close();
    }

    private void showContractDetails(int contractId) throws SQLException {
        Connection connection = establishDBConnection();

        String contractDetailsSql = "SELECT * FROM contracts WHERE id_contracts = ?";
        try (PreparedStatement contractDetailsStatement = connection.prepareStatement(contractDetailsSql)) {
            contractDetailsStatement.setInt(1, contractId);
            try (ResultSet resultSet = contractDetailsStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id_contracts");
                    String productId = resultSet.getString("product_id");
                    int productCount = resultSet.getInt("product_count");
                    String deliveryMethod = resultSet.getString("delivery_method");
                    String status = resultSet.getString("status");
                    String deadline = resultSet.getString("deadline");
                    String managerName = resultSet.getString("manager_id");
                    String clientName = resultSet.getString("client_id");
                    double totalAmount = resultSet.getDouble("total_amount");
                    String additionalProducts = resultSet.getString("additional_products");

                    Label idLabel = createBoldLabel("Contract ID: " + id, "-fx-text-fill: white;");
                    Label productIdLabel = createBoldLabel("Product ID: " + productId, "-fx-text-fill: white;");
                    Label productCountLabel = createBoldLabel("Product Count: " + productCount, "-fx-text-fill: white;");
                    Label deliveryMethodLabel = createBoldLabel("Delivery Method: " + deliveryMethod, "-fx-text-fill: white;");
                    Label statusLabel = createBoldLabel("Status: " + status, "-fx-text-fill: white;");
                    Label deadlineLabel = createBoldLabel("Deadline: " + deadline, "-fx-text-fill: white;");
                    Label managerNameLabel = createBoldLabel("Manager Name: " + managerName, "-fx-text-fill: white;");
                    Label clientNameLabel = createBoldLabel("Client Name: " + clientName, "-fx-text-fill: white;");
                    Label totalAmountLabel = createBoldLabel("Total Amount: " + totalAmount, "-fx-text-fill: white;");
                    Label additionalProductsLabel = createBoldLabel("Additional Products: " + additionalProducts, "-fx-text-fill: white;");

                    VBox contractDetailsVBox = new VBox(10);
                    contractDetailsVBox.getChildren().addAll(
                            idLabel, productIdLabel, productCountLabel, deliveryMethodLabel,
                            statusLabel, deadlineLabel, managerNameLabel, clientNameLabel, totalAmountLabel, additionalProductsLabel
                    );

                    contractDetailsVBox.setAlignment(Pos.CENTER);
                    contractDetailsVBox.setStyle("-fx-background-color: black;");
                    Scene detailsScene = new Scene(contractDetailsVBox, 400, 400);

                    Stage detailsStage = new Stage();
                    detailsStage.setTitle("Contract Details");
                    detailsStage.setScene(detailsScene);
                    detailsStage.show();
                } else {
                    showErrorAlert("Contract details not found for ID: " + contractId);
                }
            }
        }
    }

    private Label createBoldLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        return label;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
