import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManagerContractsPage extends Application {

    private int managerId;
    private String managerName;
    private BorderPane root;
    private MenuPage menuPage;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    EditEmployeeContracts editEmployeeContracts;

    public ManagerContractsPage(Integer managerId) {
        this.managerId = managerId;
        this.menuPage = new MenuPage();
        editEmployeeContracts = new EditEmployeeContracts();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            return connectionToDataBase.getConnection();
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.root = new BorderPane();

        HeaderComponent headerComponent = new HeaderComponent(primaryStage);
        VBox header = headerComponent.createHeader();
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setBorder(new Border(new BorderStroke(Color.GRAY,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0))));
        root.setTop(header);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        root.setCenter(scrollPane);
        scrollPane.setStyle("-fx-background: black;");

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Manager Contracts");
        primaryStage.show();

        getManagerNameById(managerId);

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();

        displayContractInfo(managerName, scrollPane);

        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private String getManagerNameById(int managerId) {
        String managerName = null;
        try (Connection connection = establishDBConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT manager_name FROM managers WHERE id_managers = ?")) {

            statement.setInt(1, managerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    managerName = resultSet.getString("manager_name");
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error fetching manager name: " + e.getMessage());
        }
        return managerName;
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

            Button menuButton = ButtonStyle.createStyledButton("  Menu  ");
            menuButton.setOnAction(e -> showMenu());

            Button supportButton = ButtonStyle.createStyledButton("  Support  ");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = ButtonStyle.createStyledButton("  Privacy Policy  ");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button backButton = ButtonStyle.createStyledButton("  Back  ");
            backButton.setOnAction(event -> {
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.close();
            });

            Button accountButton = ButtonStyle.createStyledButton("  Personal Account  ");
            accountButton.setOnAction(e -> menuPage.showRegistrationWindow(primaryStage));

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, backButton, accountButton, leftRegion);
            topContent.setAlignment(Pos.CENTER);
            topContent.setSpacing(10);

            HBox.setMargin(logoCircle, new Insets(0, 220, 0, 0));

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

    private void displayContractInfo(String managerName, ScrollPane scrollPane) {
        try {
            Connection connection = establishDBConnection();

            String contractIdSql = "SELECT managers_contract FROM managers WHERE manager_name = ?";
            try (PreparedStatement contractIdStatement = connection.prepareStatement(contractIdSql)) {
                contractIdStatement.setString(1, managerName);
                try (ResultSet contractIdResultSet = contractIdStatement.executeQuery()) {
                    VBox contractInfoVBox = new VBox(10);

                    List<Integer> contractIds = new ArrayList<>();

                    while (contractIdResultSet.next()) {
                        String[] contractIdsStr = contractIdResultSet.getString("managers_contract").split(", ");

                        for (String contractIdStr : contractIdsStr) {
                            int managerContractId = Integer.parseInt(contractIdStr);
                            contractIds.add(managerContractId);
                        }
                    }
                    List<Contract> contracts = new ArrayList<>();

                    for (int contractId : contractIds) {
                        Contract contract = fetchContractDetails(connection, contractId);
                        if (contract != null) {
                            contracts.add(contract);
                        } else {
                            alertService.showErrorAlert("Contract details not found for ID: " + contractId);
                        }
                    }

                    for (Contract contract : contracts) {
                        Label statusLabel = createBoldLabel("Status: " + contract.getStatus(), "-fx-text-fill: white;");
                        Label deadlineLabel = createBoldLabel("Deadline: " + contract.getDeadline(), "-fx-text-fill: white;");
                        Label totalAmountLabel = createBoldLabel("Total Amount: " + contract.getTotalAmount(), "-fx-text-fill: white;");

                        Button expandButton = ButtonStyle.createStyledButton("Expand Contract");
                        expandButton.setOnAction(e -> {
                            showContractDetails(contract.getId());
                        });

                        Button editStatusButton = ButtonStyle.createStyledButton("Edit Status");
                        editStatusButton.setOnAction(e -> {
                            int contractId = contract.getId();
                            editEmployeeContracts.showEditContractsWindow(contractId);
                        });

                        HBox contractInfoHBox = new HBox(10);
                        contractInfoHBox.getChildren().addAll(statusLabel, deadlineLabel, totalAmountLabel, expandButton, editStatusButton);

                        contractInfoVBox.getChildren().add(contractInfoHBox);
                    }

                    scrollPane.setContent(contractInfoVBox);
                }
            }

        } catch (SQLException e) {
            alertService.showErrorAlert("Error fetching manager details: " + e.getMessage());
        }
    }

    private Contract fetchContractDetails(Connection connection, int contractId) throws SQLException {
        String contractDetailsSql = "SELECT * FROM contracts WHERE id_contracts = ?";
        try (PreparedStatement contractDetailsStatement = connection.prepareStatement(contractDetailsSql)) {
            contractDetailsStatement.setInt(1, contractId);
            try (ResultSet resultSet = contractDetailsStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id_contracts");
                    String clientName = resultSet.getString("client_id");
                    String managerName = resultSet.getString("manager_id");
                    int productCount = resultSet.getInt("product_count");
                    String status = resultSet.getString("status");
                    String deliveryMethod = resultSet.getString("delivery_method");
                    String paymentMethod = resultSet.getString("pay_method");
                    String deadline = resultSet.getString("deadline");
                    double totalAmount = resultSet.getDouble("total_amount");
                    String additionalProducts = resultSet.getString("additional_products");

                    return new Contract(id, clientName, managerName, productCount, status, deliveryMethod, paymentMethod, null, additionalProducts, deadline, totalAmount);
                }
            }
        }
        return null;
    }

    private void showContractDetails(int contractId) {
        try (Connection connection = establishDBConnection();
             PreparedStatement contractDetailsStatement = connection.prepareStatement("SELECT * FROM contracts WHERE id_contracts = ?")) {

            contractDetailsStatement.setInt(1, contractId);

            try (ResultSet resultSet = contractDetailsStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id_contracts");
                    String productId = resultSet.getString("product_id");
                    String productCount = resultSet.getString("product_count");
                    String deliveryMethod = resultSet.getString("delivery_method");
                    String status = resultSet.getString("status");
                    String deadline = resultSet.getString("deadline");
                    String managerName = resultSet.getString("manager_id");
                    String clientName = resultSet.getString("client_id");
                    double totalAmount = resultSet.getDouble("total_amount");

                    Label idLabel = createBoldLabel("Contract ID: " + id, "-fx-text-fill: white;");
                    Label productIdLabel = createBoldLabel("Product ID: " + productId, "-fx-text-fill: white;");
                    Label productCountLabel = createBoldLabel("Product Count: " + productCount, "-fx-text-fill: white;");
                    Label deliveryMethodLabel = createBoldLabel("Delivery Method: " + deliveryMethod, "-fx-text-fill: white;");
                    Label statusLabel = createBoldLabel("Status: " + status, "-fx-text-fill: white;");
                    Label deadlineLabel = createBoldLabel("Deadline: " + deadline, "-fx-text-fill: white;");
                    Label managerNameLabel = createBoldLabel("Manager Name: " + managerName, "-fx-text-fill: white;");
                    Label clientNameLabel = createBoldLabel("Client Name: " + clientName, "-fx-text-fill: white;");
                    Label totalAmountLabel = createBoldLabel("Total Amount: " + totalAmount, "-fx-text-fill: white;");

                    Button editStatusButton = ButtonStyle.expandPaneStyledButton("Edit Status");
                    editStatusButton.setOnAction(e -> {
                        String newStatus = editEmployeeContracts.showEditStatusDialog();
                        if (newStatus != null) {
                            editEmployeeContracts.editContractStatus(contractId, newStatus);
                            statusLabel.setText("Status: " + newStatus);
                        }
                    });

                    Button deleteContractButton = ButtonStyle.expandPaneStyledButton("Delete Contract");
                    deleteContractButton.setOnAction(e -> {
                        boolean confirmDelete = editEmployeeContracts.showDeleteConfirmationDialog();
                        if (confirmDelete) {
                            editEmployeeContracts.deleteContract(contractId);
                        }
                    });

                    VBox contractDetailsVBox = new VBox(10);
                    contractDetailsVBox.setStyle("-fx-background-color: black;");
                    contractDetailsVBox.getChildren().addAll(
                            idLabel, productIdLabel, productCountLabel, deliveryMethodLabel,
                            statusLabel, deadlineLabel, managerNameLabel, clientNameLabel, totalAmountLabel,
                            editStatusButton, deleteContractButton
                    );
                    contractDetailsVBox.setAlignment(Pos.CENTER);

                    Stage detailsStage = new Stage();
                    detailsStage.setTitle("Contract Details");
                    detailsStage.setScene(new Scene(contractDetailsVBox, 400, 400));
                    detailsStage.show();
                } else {
                    alertService.showErrorAlert("Contract details not found for ID: " + contractId);
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error fetching contract details: " + e.getMessage());
        }
    }

    private Label createBoldLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        return label;
    }
}
