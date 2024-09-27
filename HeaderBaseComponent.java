import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class HeaderBaseComponent {

    private BorderPane root;

    public HeaderBaseComponent(BorderPane root) {
        this.root = root;
    }

    public VBox createHeader() {
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

        HBox topContent = new HBox(10);
        topContent.getChildren().addAll(logoCircle, leftRegion, menuButton, supportButton, privacyButton, accountButton);
        topContent.setAlignment(Pos.CENTER);
        VBox.setVgrow(topContent, Priority.ALWAYS);

        header.getChildren().addAll(topContent);

        return header;
    }

    private void showMenu() {
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
