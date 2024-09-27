import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientsPage extends Application {

    private BorderPane root;
    private String clientName;
    private MenuPage menuPage;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public ClientsPage() {

    }

    @Override
    public void start(Stage primaryStage) {
        this.menuPage = new MenuPage();

        VBox layout = new VBox();
        layout.setStyle("-fx-background-color: black");
        layout.setPadding(new Insets(10));
        layout.setSpacing(10);

        HeaderComponent headerComponent = new HeaderComponent(clientName, primaryStage);
        VBox header = headerComponent.createHeader();
        layout.getChildren().add(header);

        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: black");
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);

        displayHeavyClients(content);

        layout.getChildren().add(scrollPane);

        Scene scene = new Scene(layout, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Clients List");
        primaryStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();

        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    private void displayHeavyClients(VBox container) {
        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT u.user_name, u.user_email, u.user_contacts, " +
                    "CASE " +
                    "    WHEN contract_count > 18 THEN 'Heavy Client' " +
                    "    ELSE 'Regular Client' " +
                    "END AS status " +
                    "FROM users u " +
                    "LEFT JOIN (SELECT client_id, COUNT(*) AS contract_count FROM contracts GROUP BY client_id) c " +
                    "ON u.user_name = c.client_id";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String userName = resultSet.getString("user_name");
                    String userEmail = resultSet.getString("user_email");
                    String userContacts = resultSet.getString("user_contacts");
                    String status = resultSet.getString("status");

                    if (userName != null && !userName.isEmpty()) {
                        int contactCount = 0;
                        if (userContacts != null) {
                            contactCount = userContacts.isEmpty() ? 0 : userContacts.split(",").length;
                        }

                        Label userLabel = createLabel("User: " + userName + ", Email: " + userEmail + ", Contacts: " + contactCount + ", Status: " + status, 14);
                        userLabel.setPadding(new Insets(5));
                        userLabel.setStyle("-fx-background-color: black;");
                        container.getChildren().add(userLabel);

                        Button blockButton = ButtonStyle.createStyledButton("Block");
                        blockButton.setOnAction(event -> showBlockWindow(userName, userEmail, userContacts));
                        container.getChildren().add(blockButton);

                        Separator separator = new Separator();
                        separator.setPadding(new Insets(10, 10, 10, 10));
                        container.getChildren().add(separator);
                    } else if ("Heavy Client".equals(status)) {
                        Label heavyClientLabel = createLabel("Status: " + status, 14);
                        heavyClientLabel.setPadding(new Insets(5));
                        heavyClientLabel.setStyle("-fx-background-color: black;");
                        container.getChildren().add(heavyClientLabel);

                        Separator separator = new Separator();
                        separator.setStyle("-fx-background-color: white;");
                        container.getChildren().add(separator);
                    }
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error fetching heavy clients: " + e.getMessage());
        }
    }

    private void showBlockWindow(String clientName, String clientEmail, String clientContacts) {
        BlockWindow blockWindow = new BlockWindow(clientName, clientEmail, clientContacts);
        blockWindow.show();
    }

    private void showClientContractInput() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Enter Client's Name");

        TextField clientNameField = new TextField();
        clientNameField.setPromptText("Enter client's name");
        clientNameField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

        Button okButton = ButtonStyle.expandPaneStyledButton("OK");
        Button cancelButton = ButtonStyle.expandPaneStyledButton("Cancel");

        okButton.setOnAction(event -> {
            String clientName = clientNameField.getText().trim();
            if (!clientName.isEmpty()) {
                dialogStage.close();
                showClientContract(clientName);
            } else {
                alertService.showErrorAlert("Please enter the client's name.");
            }
        });

        cancelButton.setOnAction(event -> {
            dialogStage.close();
        });

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10, 10, 10, 10));
        dialogVBox.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.getChildren().addAll(clientNameField, okButton, cancelButton);

        Scene dialogScene = new Scene(dialogVBox, 400, 400);
        dialogScene.setFill(Color.BLACK);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    private void showClientContract(String clientName) {
        List<Integer> contracts = getContractsForClient(clientName);
        StringBuilder result = new StringBuilder("Contracts for client " + clientName + ":\n");
        if (contracts.isEmpty()) {
            result.append("No contracts found for this client.");
        } else {
            for (int contractId : contracts) {
                Contract contractInfo = getContract(contractId);
                if (contractInfo != null) {
                    result.append("Contract ID: ").append(contractInfo.getId())
                            .append(", Client: ").append(contractInfo.getClientName())
                            .append(", Amount: ").append(contractInfo.getTotalAmount())
                            .append(", Status: ").append(contractInfo.getStatus()).append("\n");
                }
            }
        }
        showResultWithScrollPane("Contracts for client " + clientName, result.toString());
    }

    public void showResultWithScrollPane(String title, String message) {
        ScrollPane scrollPane = new ScrollPane();
        VBox vBox = new VBox();
        vBox.getChildren().add(new Label(message));
        scrollPane.setContent(vBox);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(scrollPane);
        alert.showAndWait();
    }

    private Label createLabel(String text, int fontSize) {
        Label label = new Label(text);
        label.setTextFill(javafx.scene.paint.Color.WHITE);
        label.setFont(javafx.scene.text.Font.font("Gotham", fontSize));
        return label;
    }

    private List<Integer> getContractsForClient(String clientName) {
        List<Integer> contractIds = new ArrayList<>();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT id_contracts FROM contracts WHERE client_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, clientName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int contractId = resultSet.getInt("id_contracts");
                        contractIds.add(contractId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        return contractIds;
    }

    private Contract getContract(int contractId) {
        Contract contract = null;

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT * FROM contracts WHERE id_contracts = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, contractId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int id = resultSet.getInt("id_contracts");
                        String clientName = resultSet.getString("client_id");
                        String managerName = resultSet.getString("manager_id");
                        int productCount = resultSet.getInt("product_count");
                        String status = resultSet.getString("status");
                        String deliveryMethod = resultSet.getString("delivery_method");
                        String paymentMethod = resultSet.getString("pay_method");

                        List<Integer> productIds = new ArrayList<>();
                        String additionalProducts = resultSet.getString("additional_products");
                        String deadline = resultSet.getString("deadline");
                        double totalAmount = resultSet.getDouble("total_amount");

                        contract = new Contract(id, clientName, managerName, productCount, status, deliveryMethod, paymentMethod, productIds, additionalProducts, deadline, totalAmount);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        return contract;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public class HeaderComponent {

        private String clientName;
        private Stage primaryStage;

        public HeaderComponent(String clientName, Stage primaryStage) {
            this.clientName = clientName;
            this.primaryStage = primaryStage;
        }

        private VBox createHeader() {
            VBox header = new VBox(10);
            header.setPadding(new Insets(10));
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

            Button menuButton = ButtonStyle.createStyledButton("     Menu     ");
            menuButton.setOnAction(e -> showMenu());

            Button supportButton = ButtonStyle.createStyledButton("  Support  ");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = ButtonStyle.createStyledButton("  Privacy Policy  ");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button accountButton = ButtonStyle.createStyledButton("  Personal Account  ");
            accountButton.setOnAction(e -> showRegistrationWindow());

            Button contractsButton = ButtonStyle.createStyledButton(" Client`s Contracts ");
            contractsButton.setOnAction(e -> showClientContractInput());

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, contractsButton, accountButton, leftRegion);
            topContent.setAlignment(Pos.CENTER);
            VBox.setVgrow(topContent, Priority.ALWAYS);

            HBox.setMargin(logoCircle, new Insets(0, 120, 0, 0));

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

        private void showRegistrationWindow() {
            RegistrationWindow registrationWindow = new RegistrationWindow(root);
            Stage registrationStage = new Stage();
            registrationWindow.start(registrationStage);
        }
    }
}