import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

public class ClientAccountDetailsPage extends Application {
    Stage primaryStage;
    private BorderPane root;
    private String clientName;
    private VBox userDetailsLayout;
    private MenuPage menuPage;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private ClientDataEdit clientDataEdit;

    public ClientAccountDetailsPage(String clientName) {
        this.clientName = clientName;
        this.menuPage = new MenuPage();
        clientDataEdit = new ClientDataEdit(clientName);
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
        this.userDetailsLayout = new VBox();

        root.setStyle("-fx-background-color: black;");
        userDetailsLayout.setStyle("-fx-background-color: black;");

        fetchAndDisplayUserDetails();

        HeaderComponent headerComponent = new HeaderComponent();
        VBox header = headerComponent.createHeader();
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setBorder(new Border(new BorderStroke(Color.GRAY,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0))));
        root.setTop(header);
        header.setStyle("-fx-background-color: black;");

        VBox centerContainer = new VBox(userDetailsLayout);
        root.setCenter(centerContainer);
        centerContainer.setStyle("-fx-background-color: black;");

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
        return connectionToDataBase.getConnection();
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

            Button supportButton = ButtonStyle.createStyledButton("  Support   ");
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

    private VBox createDetailWithEditButton(String labelText, Text detailText, Button editButton) {
        Text label = new Text(labelText);
        label.setFont(Font.font("Arial", 16));
        label.setFill(Color.WHITE);

        HBox detailLayout = new HBox(10);
        detailLayout.getChildren().addAll(label, detailText, editButton);
        detailLayout.setPadding(new Insets(10));

        VBox container = new VBox(detailLayout);
        container.setAlignment(Pos.CENTER_LEFT);
        return container;
    }

    public void fetchAndDisplayUserDetails() {
        try {
            Connection connection = establishDBConnection();
            String sql = "SELECT user_name, password, user_phone_number, user_email, id FROM users WHERE user_name = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, clientName);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String userName = resultSet.getString("user_name");
                String password = resultSet.getString("password");
                String phoneNumber = resultSet.getString("user_phone_number");
                String email = resultSet.getString("user_email");
                int clientId = resultSet.getInt("id");

                Text userNameText = new Text(userName);
                userNameText.setFont(Font.font("Arial", 16));
                userNameText.setFill(Color.WHITE);

                Text phoneNumberText = new Text(phoneNumber);
                phoneNumberText.setFont(Font.font("Arial", 16));
                phoneNumberText.setFill(Color.WHITE);

                Text emailText = new Text(email);
                emailText.setFont(Font.font("Arial", 16));
                emailText.setFill(Color.WHITE);

                int sessionCount = getSessionCount(clientName);
                Text sessionsText = new Text("Session Count: " + sessionCount + " sessions");
                sessionsText.setFont(Font.font("Arial", 16));
                sessionsText.setFill(Color.WHITE);

                int favoritesCount = getFavoritesCount(clientName);
                Text favoritesText = new Text("Favorites Count: " + favoritesCount);
                favoritesText.setFont(Font.font("Arial", 16));
                favoritesText.setFill(Color.WHITE);

                int contractsCount = getContractsCount(clientName);
                Text contractsText = new Text("Contracts Count: " + contractsCount);
                contractsText.setFont(Font.font("Arial", 16));
                contractsText.setFill(Color.WHITE);

                int expectedListCount = getExpectedListCount(clientId);
                Text expectedListText = new Text("Expected List: " + expectedListCount + " items");
                expectedListText.setFont(Font.font("Arial", 16));
                expectedListText.setFill(Color.WHITE);

                Button editUserNameButton = ButtonStyle.expandPaneStyledButton("Edit Name");
                editUserNameButton.setOnAction(e -> clientDataEdit.openEditUserNameWindow());

                Button editPhoneNumberButton = ButtonStyle.expandPaneStyledButton("Edit Phone Number");
                editPhoneNumberButton.setOnAction(e -> clientDataEdit.openEditUserPhoneNumberWindow());

                Button editPasswordButton = ButtonStyle.expandPaneStyledButton("Edit Password");
                editPasswordButton.setOnAction(e -> clientDataEdit.openEditUserPasswordWindow());

                Button editEmailButton = ButtonStyle.expandPaneStyledButton("Edit Email");
                editEmailButton.setOnAction(e -> clientDataEdit.openEditUserEmailWindow());

                Button openFavoritesButton = ButtonStyle.expandPaneStyledButton(">");
                openFavoritesButton.setOnAction(e -> openFavoritesPage());

                Button openContractsButton = ButtonStyle.expandPaneStyledButton(">");
                openContractsButton.setOnAction(e -> openUserContractsPage());

                Button viewSessionsButton = ButtonStyle.expandPaneStyledButton(">");
                viewSessionsButton.setOnAction(e -> openConnectedDevicesWindow(clientId));

                Button viewExpectedListButton = ButtonStyle.expandPaneStyledButton(">");
                viewExpectedListButton.setOnAction(e -> openExpectedListWindow(clientId));

                userDetailsLayout.getChildren().clear();

                userDetailsLayout.getChildren().addAll(
                        createDetailWithEditButton("User Name: ", userNameText, editUserNameButton),
                        createDetailWithEditButton("Phone Number: ", phoneNumberText, editPhoneNumberButton),
                        createDetailWithEditButton("Email: ", emailText, editEmailButton),
                        createDetailWithEditButton("Password: ", maskPassword(password), editPasswordButton),
                        createDetailWithEditButton("Favorites: ", favoritesText, openFavoritesButton),
                        createDetailWithEditButton("Contracts: ", contractsText, openContractsButton),
                        createDetailWithEditButton("Sessions: ", sessionsText, viewSessionsButton),
                        createDetailWithEditButton("Expected List: ", expectedListText, viewExpectedListButton)
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

    private Text maskPassword(String password) {
        int visibleLength = Math.min(password.length() / 2, 10);
        String visiblePart = password.substring(0, visibleLength);
        String maskedPart = "*".repeat(password.length() - visibleLength);

        Text maskedPasswordText = new Text(visiblePart + maskedPart);
        maskedPasswordText.setFont(Font.font("Arial", 16));
        maskedPasswordText.setFill(Color.WHITE);

        return maskedPasswordText;
    }

    private int getExpectedListCount(int clientId) {
        int count = 0;
        try {
            Connection connection = establishDBConnection();
            String sql = "SELECT COUNT(*) FROM buy_application WHERE client_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, clientId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to fetch expected list count: " + e.getMessage());
        }
        return count;
    }

    private int getSessionCount(String userName) {
        int sessionCount = 0;
        try {
            Connection connection = establishDBConnection();
            String sql = "SELECT COUNT(*) FROM sessions WHERE user_name = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userName);
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

    private int getFavoritesCount(String userName) {
        int count = 0;
        try {
            Connection connection = establishDBConnection();
            String sql = "SELECT COUNT(*) FROM user_favorites WHERE user_id = (SELECT id FROM users WHERE user_name = ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userName);
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

    private int getContractsCount(String userName) {
        int count = 0;
        try {
            Connection connection = establishDBConnection();
            String sql = "SELECT COUNT(*) FROM user_contracts WHERE user_id = (SELECT id FROM users WHERE user_name = ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userName);
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

    private void openConnectedDevicesWindow(int clientId) {
        Stage sessionStage = new Stage();
        UserSessionDetailsWindow userSessionDetailsWindow = new UserSessionDetailsWindow(clientId);
        userSessionDetailsWindow.start(sessionStage);
    }

    private void openExpectedListWindow(int clientId) {
        ClientExpectedListWindow expectedListWindow = new ClientExpectedListWindow(clientId);
        expectedListWindow.start(new Stage());
    }

    private void openFavoritesPage() {
        try {
            Connection connection = establishDBConnection();

            String checkUserSql = "SELECT COUNT(*) FROM user_favorites WHERE user_id = (SELECT id FROM users WHERE user_name = ?)";
            PreparedStatement checkUserStatement = connection.prepareStatement(checkUserSql);
            checkUserStatement.setString(1, clientName);
            ResultSet resultSet = checkUserStatement.executeQuery();

            if (resultSet.next()) {
                Stage favoritesStage = new Stage();
                UserFavoritesPage favoritesPage = new UserFavoritesPage(clientName);
                favoritesPage.start(favoritesStage);
            } else {
                alertService.showErrorAlert("No favorites found for user " + clientName);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while opening favorites list page.");
        }
    }

    private void openUserContractsPage() {
        try {
            Stage contractsStage = new Stage();
            ClientContractsPage contractsPage = new ClientContractsPage(clientName);
            contractsPage.start(contractsStage);
        } catch (Exception e) {
            e.printStackTrace();
            alertService.showErrorAlert("An error occurred while opening contracts page.");
        }
    }
}
