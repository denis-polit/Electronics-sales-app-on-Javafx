import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManagerAccountDetailsPage extends Application {
    Stage primaryStage;
    private BorderPane root;
    private String managerName;
    private int managerId;
    private MenuPage menuPage;
    private VBox managerDetailsLayout;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private EmployeeEditOwnAccount employeeEditOwnAccount;

    public void init(String managerName, boolean managerEnter) {
        this.managerName = managerName;
        this.employeeEditOwnAccount = new EmployeeEditOwnAccount(managerName);
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.root = new BorderPane();
        this.managerDetailsLayout = new VBox();
        this.menuPage = new MenuPage();

        root.setStyle("-fx-background-color: black;");
        managerDetailsLayout.setStyle("-fx-background-color: black;");

        fetchManagerId();

        fetchAndDisplayManagerDetails();

        HeaderComponent headerComponent = new HeaderComponent();
        VBox header = headerComponent.createHeader();
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setBorder(new Border(new BorderStroke(Color.GRAY,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0))));
        root.setTop(header);
        header.setStyle("-fx-background-color: black;");

        VBox centerContainer = new VBox(managerDetailsLayout);
        root.setCenter(centerContainer);
        centerContainer.setStyle("-fx-background-color: black;");

        ScrollPane scrollPane = new ScrollPane(centerContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);

        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Account Details");
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

    private void fetchManagerId() {
        try {
            Connection connection = establishDBConnection();
            String sql = "SELECT id_managers FROM managers WHERE manager_name = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, managerName);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                managerId = resultSet.getInt("id_managers");
            } else {
                alertService.showErrorAlert("Manager ID not found for the given manager name.");
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to fetch manager ID: " + e.getMessage());
        }
    }

    public class HeaderComponent {

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

            Button supportButton = ButtonStyle.createStyledButton(" Support  ");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = ButtonStyle.createStyledButton("  Privacy Policy  ");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button backButton = ButtonStyle.createStyledButton("   Back   ");
            backButton.setOnAction(event -> {
                Stage stage = (Stage) root.getScene().getWindow();
                stage.close();
            });

            Button accountButton = ButtonStyle.createStyledButton("  Personal Account  ");
            accountButton.setOnAction(e -> menuPage.showRegistrationWindow(primaryStage));

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, backButton, accountButton, leftRegion);
            topContent.setAlignment(Pos.CENTER);
            VBox.setVgrow(topContent, Priority.ALWAYS);

            HBox.setMargin(logoCircle, new Insets(0, 200, 0, 0));

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

    private HBox createDetailWithEditButton(String labelText, Text detailText, Button editButton) {
        Text label = new Text(labelText);
        label.setFont(Font.font("Arial", 16));
        label.setFill(Color.WHITE);

        HBox detailLayout = new HBox(10);
        detailLayout.getChildren().addAll(label, detailText, editButton);
        detailLayout.setPadding(new Insets(10));
        return detailLayout;
    }

    public void fetchAndDisplayManagerDetails() {
        try {
            Connection connection = establishDBConnection();
            String sql = "SELECT manager_name, password, manager_phone_number, manager_email FROM managers WHERE id_managers = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, managerId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String managerName = resultSet.getString("manager_name");
                String password = resultSet.getString("password");
                String phoneNumber = resultSet.getString("manager_phone_number");
                String email = resultSet.getString("manager_email");

                Text managerNameText = new Text(managerName);
                managerNameText.setFont(Font.font("Arial", 16));
                managerNameText.setFill(Color.WHITE);

                Text phoneNumberText = new Text(phoneNumber);
                phoneNumberText.setFont(Font.font("Arial", 16));
                phoneNumberText.setFill(Color.WHITE);

                Text emailText = new Text(email);
                emailText.setFont(Font.font("Arial", 16));
                emailText.setFill(Color.WHITE);

                int halfLength = password.length() / 2;
                String maskedPassword = password.substring(0, halfLength) + "*".repeat(halfLength);

                Text passwordText = new Text(maskedPassword);
                passwordText.setFont(Font.font("Arial", 16));
                passwordText.setFill(Color.WHITE);

                int favoritesCount = getFavoritesCount(managerId);
                Text favoritesText = new Text("Favorites Count: " + favoritesCount);
                favoritesText.setFont(Font.font("Arial", 16));
                favoritesText.setFill(Color.WHITE);

                int contractsCount = getContractsCount(managerId);
                Text contractsText = new Text("Contracts Count: " + contractsCount);
                contractsText.setFont(Font.font("Arial", 16));
                contractsText.setFill(Color.WHITE);

                int expectedProductCount = getExpectedProductCount(managerId);
                Text expectedProductCountText = new Text("Expected Product Count: " + expectedProductCount);
                expectedProductCountText.setFont(Font.font("Arial", 16));
                expectedProductCountText.setFill(Color.WHITE);

                int sessionCount = getSessionCount(managerId);
                Text sessionsText = new Text("Session Count: " + sessionCount + " sessions");
                sessionsText.setFont(Font.font("Arial", 16));
                sessionsText.setFill(Color.WHITE);

                Button editManagerNameButton = ButtonStyle.expandPaneStyledButton("Edit Name");
                editManagerNameButton.setOnAction(e -> employeeEditOwnAccount.openEditManagerNameWindow());

                Button editPhoneNumberButton = ButtonStyle.expandPaneStyledButton("Edit Phone Number");
                editPhoneNumberButton.setOnAction(e -> employeeEditOwnAccount.openEditManagerPhoneNumberWindow());

                Button editPasswordButton = ButtonStyle.expandPaneStyledButton("Edit Password");
                editPasswordButton.setOnAction(e -> employeeEditOwnAccount.openEditManagerPasswordWindow());

                Button editEmailButton = ButtonStyle.expandPaneStyledButton("Edit Email");
                editEmailButton.setOnAction(e -> employeeEditOwnAccount.openEditManagerEmailWindow());

                Button openFavoritesButton = ButtonStyle.expandPaneStyledButton(">");
                openFavoritesButton.setOnAction(e -> openEmployeeFavoritesPage(managerId));

                Button openContractsButton = ButtonStyle.expandPaneStyledButton(">");
                openContractsButton.setOnAction(e -> openEmployeeContractsPage(managerId));

                Button viewSessionsButton = ButtonStyle.expandPaneStyledButton(">");
                viewSessionsButton.setOnAction(e -> openEmployeeSessionsWindow(managerId));

                Button viewExpectedListButton = ButtonStyle.expandPaneStyledButton(">");
                viewExpectedListButton.setOnAction(e -> openEmployeeExpectedListWindow(managerId));

                managerDetailsLayout.getChildren().clear();
                managerDetailsLayout.getChildren().addAll(
                        createDetailWithEditButton("Manager Name: ", managerNameText, editManagerNameButton),
                        createDetailWithEditButton("Phone Number: ", phoneNumberText, editPhoneNumberButton),
                        createDetailWithEditButton("Email: ", emailText, editEmailButton),
                        createDetailWithEditButton("Password: ", passwordText, editPasswordButton),
                        createDetailWithEditButton("Favorites: ", favoritesText, openFavoritesButton),
                        createDetailWithEditButton("Contracts: ", contractsText, openContractsButton),
                        createDetailWithEditButton("Sessions: ", sessionsText, viewSessionsButton),
                        createDetailWithEditButton("Expected List: ", expectedProductCountText, viewExpectedListButton)
                );
            } else {
                alertService.showErrorAlert("User details not found");
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while fetching user details.");
        }
    }

    private int getFavoritesCount(int managerId) {
        int count = 0;
        try {
            Connection connection = establishDBConnection();
            String sql = "SELECT COUNT(*) FROM employee_favorites WHERE manager_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, managerId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to fetch favorites count: " + e.getMessage());
        }
        return count;
    }

    private int getExpectedProductCount(int managerId) {
        int count = 0;
        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT COUNT(*) FROM employee_favorites WHERE manager_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, managerId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to fetch expected list count: " + e.getMessage());
        }
        return count;
    }

    private int getContractsCount(int managerId) {
        int count = 0;
        try {
            Connection connection = establishDBConnection();
            String sql = "SELECT COUNT(*) FROM employee_contracts WHERE manager_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, managerId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to fetch contracts count: " + e.getMessage());
        }
        return count;
    }

    private int getSessionCount(int managerId) {
        int sessionCount = 0;
        try {
            Connection connection = establishDBConnection();
            String sql = "SELECT COUNT(*) FROM employee_sessions WHERE manager_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, managerId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                sessionCount = resultSet.getInt(1);
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to fetch session count: " + e.getMessage());
        }
        return sessionCount;
    }

    private void openEmployeeSessionsWindow(int managerId) {
        Stage sessionStage = new Stage();
        EmployeeSessionDetailsWindow employeeSessionDetailsWindow = new EmployeeSessionDetailsWindow(managerId);
        employeeSessionDetailsWindow.start(sessionStage);
    }

    private void openEmployeeExpectedListWindow(int managerId) {
        EmployeeExpectedListWindow employeeExpectedListWindow = new EmployeeExpectedListWindow(managerId);
        employeeExpectedListWindow.start(new Stage());
    }

    private void openEmployeeContractsPage(int managerId) {
        try {
            Stage contractsStage = new Stage();
            ManagerContractsPage contractsPage = new ManagerContractsPage(managerId);
            contractsPage.start(contractsStage);
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while opening contracts page.");
        }
    }

    private void openEmployeeFavoritesPage(int managerId) {
        try {
            Connection connection = establishDBConnection();

            String favoritesSql = "SELECT product_id FROM employee_favorites WHERE manager_id = ?";
            PreparedStatement favoritesStatement = connection.prepareStatement(favoritesSql);
            favoritesStatement.setInt(1, managerId);
            ResultSet resultSet = favoritesStatement.executeQuery();

            List<Integer> favoriteProductIds = new ArrayList<>();
            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                favoriteProductIds.add(productId);
            }

            connection.close();

            if (!favoriteProductIds.isEmpty()) {
                Stage managerFavoritesStage = new Stage();
                ManagerFavoritesPage favoritesPage = new ManagerFavoritesPage(managerId, managerName, favoriteProductIds);
                favoritesPage.start(managerFavoritesStage);
            } else {
                alertService.showErrorAlert("Manager with the ID " + managerId + " has no favorite products.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while opening the favorites list page.");
        }
    }
}
