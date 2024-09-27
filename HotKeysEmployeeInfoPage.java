import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HotKeysEmployeeInfoPage extends Application {
    private BorderPane root;
    MenuPage menuPage = new MenuPage();
    Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.root = new BorderPane();
        this.menuPage = new MenuPage();

        VBox layout = new VBox();
        layout.setStyle("-fx-background-color: black");
        layout.setPadding(new Insets(10));
        layout.setSpacing(10);

        HeaderComponent headerComponent = new HeaderComponent();
        Node header = headerComponent.createHeader();
        layout.getChildren().add(header);

        VBox hotKeysInfoContainer = new VBox();
        hotKeysInfoContainer.setPadding(new Insets(10));
        hotKeysInfoContainer.setSpacing(10);
        hotKeysInfoContainer.setStyle("-fx-background-color: black");

        hotKeysInfoContainer.getChildren().add(createBoldLabel("How to Use Hotkeys In Our Application:", "-fx-font-size: 24px;"));

        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + M: Show Menu", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + S: Show Support Window", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + T: Show Privacy Policy Window", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + P: Show Registration Window", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("F5: Refresh Scene", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("ESC: Close Scene", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + Tab: Show Hotkeys Info Page", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + I: Show Instructions Page", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + 0: Switch to Catalog Page", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + 1: Switch to Home Page", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + 2: Switch to Reviews Page", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + 3: Switch to Services Page", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + 4: Switch to Description Page", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + 5: Switch to Account Menu Page", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + 6: Switch to Contracts List Page", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + 7: Switch to Managers List Page", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + 8: Switch to Instructions Page", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + 9: Switch to Statistics Page", "-fx-font-size: 16px;"));
        hotKeysInfoContainer.getChildren().add(createBoldLabel("Ctrl + -: Switch to Previous Window", "-fx-font-size: 16px;"));

        layout.getChildren().add(hotKeysInfoContainer);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 450, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Hotkeys Info Page");
        primaryStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();
    }

    private Label createBoldLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style + "; -fx-text-fill: white;");
        label.setFont(Font.font("Gotham", FontWeight.BOLD, 20));
        return label;
    }

    private class HeaderComponent {

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

            Button privacyButton = ButtonStyle.createStyledButton("  Terms  ");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button accountButton = ButtonStyle.createStyledButton("  Account  ");
            accountButton.setOnAction(e -> showRegistrationWindow());

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, accountButton, leftRegion);
            topContent.setAlignment(Pos.CENTER);
            VBox.setVgrow(topContent, Priority.ALWAYS);

            HBox.setMargin(logoCircle, new Insets(0, 0, 0, 0));

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
