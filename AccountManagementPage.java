import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AccountManagementPage extends Application {
    private Stage primaryStage;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    BlockWindow blockWindow = new BlockWindow("", "", "");

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        BorderPane root = new BorderPane();
        MenuPage menuPage = new MenuPage();

        root.setStyle("-fx-background-color: black;");

        HeaderComponent headerComponent = new HeaderComponent(root);
        VBox header = headerComponent.createHeader();
        header.setPadding(new Insets(0, 0, 10, 0));
        root.setTop(header);
        header.setStyle("-fx-background-color: black;");

        VBox centerContainer = new VBox(10);
        centerContainer.setPadding(new Insets(10));
        centerContainer.setAlignment(Pos.CENTER);

        CreateEmployeeAccounts createEmployeeAccounts = new CreateEmployeeAccounts();
        ChangeBonusKeys changeBonusKeys = new ChangeBonusKeys();

        createButtonLayout(centerContainer);

        root.setCenter(centerContainer);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Account Management");
        primaryStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();

        try {
            FirstConnectionToDataBase connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private void createButtonLayout(VBox centerContainer) {
        Button createEmployeeButton = ButtonStyle.expandPaneStyledButton("Create Employee");
        Label createEmployeeDescription = new Label("Create .");
        createEmployeeDescription.setTextFill(Color.WHITE);
        createEmployeeButton.setOnAction(event -> createEmployeePage());

        Button editManagersButton = ButtonStyle.expandPaneStyledButton("Edit Employee");
        Label editManagersDescription = new Label("Edit the details and permissions of store managers.");
        editManagersDescription.setTextFill(Color.WHITE);
        editManagersButton.setOnAction(event -> openEditManagerDialog());

        Button editUsersButton = ButtonStyle.expandPaneStyledButton("Edit Clients");
        Label editUsersDescription = new Label("Edit user profiles, permissions, and other details.");
        editUsersDescription.setTextFill(Color.WHITE);
        editUsersButton.setOnAction(event -> openEditClientDialog());

        Button deleteButton = ButtonStyle.expandPaneStyledButton("Delete Clients");
        Label deleteDescription = new Label("Delete users from the system.");
        deleteDescription.setTextFill(Color.WHITE);
        deleteButton.setOnAction(event -> openDeleteClientDialog());

        Button blockButton = ButtonStyle.expandPaneStyledButton("Block Account");
        Label blockDescription = new Label("Delete users from the system.");
        blockDescription.setTextFill(Color.WHITE);
        blockButton.setOnAction(event -> blockWindow.showBlockDialog());

        Button changeKeysButton = ButtonStyle.expandPaneStyledButton("Change Keys");
        Label changeKeysDescription = new Label("Change the security keys for accessing the system.");
        changeKeysDescription.setTextFill(Color.WHITE);
        changeKeysButton.setOnAction(event -> showChangeKeysWindow());

        centerContainer.getChildren().addAll(
                editManagersButton, editManagersDescription,
                editUsersButton, editUsersDescription,
                deleteButton, deleteDescription,
                blockButton, blockDescription,
                changeKeysButton, changeKeysDescription
        );
    }

    private void createEmployeePage() {
        Stage employeeStage = new Stage();
        CreateEmployeeAccounts createEmployeeAccounts = new CreateEmployeeAccounts();
        try {
            createEmployeeAccounts.start(employeeStage);
        } catch (Exception e) {
            AlertServiceImpl alertService = new AlertServiceImpl();
            alertService.showErrorAlert("Failed to open employee account creation page: " + e.getMessage());
        }
    }

    private void openEditClientDialog() {
        EditClientAccount editClientAccount = new EditClientAccount();
        editClientAccount.editUserDialog();
    }

    private void openDeleteClientDialog() {
        DeleteClientAccount deleteClientAccount = new DeleteClientAccount();
        deleteClientAccount.showDeleteWindow();
    }

    private void openEditManagerDialog() {
        EditEmployeeAccount editEmployeeAccount = new EditEmployeeAccount();
        editEmployeeAccount.editEmployeeDialog();
    }

    private void showChangeKeysWindow() {
        ChangeBonusKeys changeBonusKeys = new ChangeBonusKeys();
        changeBonusKeys.showChangeKeysWindow();
    }

    public class HeaderComponent {

        private BorderPane root;

        public HeaderComponent(BorderPane root) {
            this.root = root;
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

            Button menuButton = ButtonStyle.createStyledButton("Menu");
            menuButton.setOnAction(e -> showMenu());

            Button supportButton = ButtonStyle.createStyledButton("Support");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = ButtonStyle.createStyledButton("Privacy Policy");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button accountButton = ButtonStyle.createStyledButton("  Personal Account  ");
            accountButton.setOnAction(e -> showRegistrationWindow());

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, accountButton, leftRegion);
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
