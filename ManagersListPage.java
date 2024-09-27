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

public class ManagersListPage extends Application {

    private BorderPane root;
    private String managerName;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService;

    public ManagersListPage() {
        alertService = new AlertServiceImpl();
    }

    @Override
    public void start(Stage primaryStage) {
        MenuPage menuPage = new MenuPage();

        VBox layout = new VBox();
        layout.setStyle("-fx-background-color: black");
        layout.setPadding(new Insets(10));
        layout.setSpacing(10);

        HeaderComponent headerComponent = new HeaderComponent(managerName, primaryStage);
        VBox header = headerComponent.createHeader();
        layout.getChildren().add(header);

        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: black");
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);

        displayManagers(content);

        layout.getChildren().add(scrollPane);

        Scene scene = new Scene(layout, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Managers List");
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
        if (connectionToDataBase != null) {
            return connectionToDataBase.getConnection();
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    private void displayManagers(VBox container) {
        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT m.manager_name, m.manager_email, m.managers_contract, " +
                    "CASE " +
                    "    WHEN COUNT(c.status) > 20 THEN 'Overloaded' " +
                    "    ELSE 'open to contracts' " +
                    "END AS status " +
                    "FROM managers m " +
                    "LEFT JOIN contracts c ON m.manager_name = c.manager_id " +
                    "WHERE m.employee_status = 'manager' " +
                    "GROUP BY m.manager_name, m.manager_email, m.managers_contract " +
                    "UNION " +
                    "SELECT NULL AS manager_name, NULL AS manager_email, NULL AS managers_contract, 'Overloaded' AS status " +
                    "FROM dual " +
                    "WHERE (SELECT COUNT(status) FROM contracts) > 20";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String managerName = resultSet.getString("manager_name");
                    String managerEmail = resultSet.getString("manager_email");
                    String contractNumbers = resultSet.getString("managers_contract");
                    String status = resultSet.getString("status");

                    if (managerName != null && !managerName.isEmpty()) {
                        int contractCount = contractNumbers.isEmpty() ? 0 : contractNumbers.split(",").length;

                        Label managerLabel = createLabel("Manager: " + managerName + ", Email: " + managerEmail + ", Contracts: " + contractCount + ", Status: " + status, 14);
                        managerLabel.setPadding(new Insets(5));
                        managerLabel.setStyle("-fx-background-color: black;");
                        container.getChildren().add(managerLabel);

                        Separator separator = new Separator();
                        separator.setPadding(new Insets(10, 10, 10, 10));
                        container.getChildren().add(separator);
                    } else if ("Overloaded".equals(status)) {
                        Label overloadedLabel = createLabel("Status: " + status, 14);
                        overloadedLabel.setPadding(new Insets(5));
                        overloadedLabel.setStyle("-fx-background-color: black;");
                        container.getChildren().add(overloadedLabel);

                        Separator separator = new Separator();
                        separator.setStyle("-fx-background-color: white;");
                        container.getChildren().add(separator);
                    }
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error fetching managers: " + e.getMessage());
        }
    }

    private Label createLabel(String text, int fontSize) {
        Label label = new Label(text);
        label.setTextFill(javafx.scene.paint.Color.WHITE);
        label.setFont(javafx.scene.text.Font.font("Gotham", fontSize));
        return label;
    }

    public List<Integer> getContractsForManager(String managerName) {
        List<Integer> contractIds = new ArrayList<>();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT c.id_contracts FROM contracts c " +
                    "JOIN managers m ON c.manager_id = m.manager_name " +
                    "WHERE m.manager_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, managerName);
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

    public void showContractsForManager(String managerName) {
        List<Integer> contracts = getContractsForManager(managerName);
        StringBuilder result = new StringBuilder("Contracts for manager " + managerName + ":\n");
        if (contracts.isEmpty()) {
            result.append("No contracts found for this manager.");
        } else {
            for (int contractId : contracts) {
                result.append(contractId).append("\n");
            }
        }
        showResultWithScrollPane("Contracts for manager " + managerName, result.toString());
    }

    public void showContractsForManagerInput() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Enter Manager's Name");

        TextField managerNameField = new TextField();
        managerNameField.setPromptText("Enter manager's name");
        managerNameField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

        Button okButton = ButtonStyle.expandPaneStyledButton("OK");
        Button cancelButton = ButtonStyle.expandPaneStyledButton("Cancel");

        okButton.setOnAction(event -> {
            String managerName = managerNameField.getText().trim();
            if (!managerName.isEmpty()) {
                dialogStage.close();
                showContractsForManager(managerName);
            } else {
                alertService.showErrorAlert("Please enter the manager's name.");
            }
        });

        cancelButton.setOnAction(event -> dialogStage.close());

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10, 10, 10, 10));
        dialogVBox.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.getChildren().addAll(managerNameField, okButton, cancelButton);

        Scene dialogScene = new Scene(dialogVBox, 400, 400);
        dialogScene.setFill(Color.BLACK);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
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

    private void showLeader() {
        String leaderInfo = getLeaderInfo();

        if (!leaderInfo.isEmpty()) {
            showLeaderAlert("Leader Information", leaderInfo);
        } else {
            alertService.showErrorAlert("No manager found.");
        }
    }

    private String getLeaderInfo() {
        StringBuilder leaderInfo = new StringBuilder();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT manager_id, COUNT(*) AS contract_count " +
                    "FROM contracts " +
                    "GROUP BY manager_id " +
                    "HAVING COUNT(*) >= ALL " +
                    "      (SELECT COUNT(*) " +
                    "       FROM contracts " +
                    "       GROUP BY manager_id " +
                    "       HAVING manager_id <> manager_id)";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String leaderName = resultSet.getString("manager_id");
                    int contractCount = resultSet.getInt("contract_count");
                    leaderInfo.append("Manager: ").append(leaderName)
                            .append(", Contracts: ").append(contractCount);
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error fetching leader: " + e.getMessage());
        }

        return leaderInfo.toString();
    }

    private void showLeaderAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public class HeaderComponent {

        private Stage primaryStage;

        public HeaderComponent(String managerName, Stage primaryStage) {
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

            Button contractsButton = ButtonStyle.createStyledButton(" Contracts ");
            contractsButton.setOnAction(e -> showContractsForManagerInput());

            Button leaderButton = ButtonStyle.createStyledButton("   Leader   ");
            leaderButton.setOnAction(e -> showLeader());

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, contractsButton, leaderButton, accountButton, leftRegion);
            topContent.setAlignment(Pos.CENTER);
            VBox.setVgrow(topContent, Priority.ALWAYS);

            HBox.setMargin(logoCircle, new Insets(0, 70, 0, 0));

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
