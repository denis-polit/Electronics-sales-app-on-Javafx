import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PrivacyPolicyWindow extends Application {

    private MenuPage menuPage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.menuPage = new MenuPage();
        primaryStage.setTitle("Privacy Policy");
        Scene scene = new Scene(new VBox(), 900, 600);
        showPrivacyPolicyWindow(primaryStage, scene);
    }

    private void showPrivacyPolicyWindow(Stage privacyStage, Scene scene) {
        privacyStage.setTitle("Privacy Policy");

        VBox privacyLayout = new VBox(10);
        privacyLayout.setAlignment(Pos.CENTER);
        privacyLayout.setPadding(new javafx.geometry.Insets(20));
        privacyLayout.setStyle("-fx-background-color: black;");

        Text policyTitle = createStyledLabel("PRIVACY POLICY: \n\n", "-fx-text-fill: white; -fx-font-size: 20;");

        Text policyText1 = createStyledLabel("""
        We, the CYBERSHOP team, understand the importance of safeguarding your information and are committed to protecting it. This policy describes what information we collect, how we use it, who we may share it with, and how we ensure the security of this data. Please read this policy carefully before using our services.
        """, "-fx-text-fill: white; -fx-font-size: 18;");

        Text policyText2 = createStyledLabel("""
        WHAT DATA DOES CYBERSHOP COLLECT?\s
        When using CYBERSHOP services, we collect the following information:
        1) Information provided by you during registration and through relevant forms in CYBERSHOP services. This may include your name, email address, phone number, and other personal information. This data is stored only after your input is confirmed and with your consent. We use this information to provide CYBERSHOP services, such as organizing appointments for the sale department of residential complexes or sending newsletters about residential complex news to your mobile device.
        2) Information we collect in our application includes data about your device, diagnostic data, information about the region and city, selected currency, and search history within the application. This information is used to ensure the functioning of the application, improve its performance, and conduct statistical research.
        3) Cookies - these are files transmitted to your web browser and stored on your device. They help identify you during subsequent visits to CYBERSHOP and facilitate interaction with our services. In most cases, cookies are used to improve user convenience, provide anonymous service information, and optimize application operation. You can prohibit the use of cookies in your browser, but this may result in certain features of CYBERSHOP not functioning correctly.
        4) Logs - while using CYBERSHOP services, our servers automatically store logs containing information about your interactions with our services, such as IP address, text data, time and date of the request, browser type, and more. This information is used for statistical research and improving our services.
        5) Correspondence - we keep records of your correspondence, inquiries, or complaints sent to us through the application, website, or email, as well as our responses. We may collect your email address and other additional information you provide to us. This allows us to provide better user support and fulfill our legal obligations.
        """, "-fx-text-fill: white; -fx-font-size: 18;");

        Text policyText3 = createStyledLabel("""
        LINKS\s
        CYBERSHOP services may contain links to websites on the Internet that may collect their own information, store their own cookies, and more. We do not control these sites or their privacy policies.
        """, "-fx-text-fill: white; -fx-font-size: 18;");

        Text policyText4 = createStyledLabel("""
        HOW DO WE USE THE COLLECTED INFORMATION?\s
        CYBERSHOP uses the collected information solely to fulfill our services and improve user experience. We may use your data to:
        - Organize appointments for the sale department of residential complexes;
        - Send newsletters about residential complex news to your mobile device;
        - Improve the functionality and interaction with the CYBERSHOP application;
        - Conduct statistical research to help us improve and maintain our services.
        """, "-fx-text-fill: white; -fx-font-size: 18;");

        Text policyText5 = createStyledLabel("""
        DATA DELETION\s
        You can request the deletion of your personal data (including email address, phone number, Telegram account) at any time by contacting us at support@cybershop.com. Your request will be processed within 10 working days from the moment of its receipt, and your data will be deleted. However, we may retain some of your personal data for as long as is reasonably necessary for our legitimate business interests or other purposes that comply with legal requirements, including fraud detection and prevention, as well as fulfilling our legal obligations, including tax, legal reporting, and undergoing audits.
        """, "-fx-text-fill: white; -fx-font-size: 18;");

        Text policyText6 = createStyledLabel("""
        WHO MAY WE SHARE YOUR DATA WITH?\s
        CYBERSHOP may share your information with third parties in the following cases:
        - If necessary to provide the services you requested, such as providing access to the sale department of residential complexes or receiving news;
        - For the purpose of improving our services and developing new features, we may engage external service providers who help us analyze data and optimize the application's operation;
        - If we have mandatory legislative requirements or in case of identifying violations of the terms of use of our services.
        """, "-fx-text-fill: white; -fx-font-size: 18;");

        Text policyText7 = createStyledLabel("""
        DATA PROTECTION\s
        CYBERSHOP pays great attention to protecting your information. We apply appropriate technical and organizational measures to ensure data security and prevent unauthorized access to it.
        Access to your information is provided only to CYBERSHOP employees who need this information to provide services and perform their duties.
        We also require our employees to comply with confidentiality obligations and may apply sanctions in case of violation of these obligations.
        """, "-fx-text-fill: white; -fx-font-size: 18;");

        Text policyText8 = createStyledLabel("""
        CHANGES TO THE PRIVACY POLICY\s
        CYBERSHOP constantly monitors compliance with the rules set out in this Privacy Policy.
        We may change this policy from time to time by updating its version on our website https://cybershop.com/terms#privacy. We recommend that you periodically review this policy to stay informed of any changes.
        """, "-fx-text-fill: white; -fx-font-size: 18;");

        Text policyText9 = createStyledLabel("""
        CONTACT US\s
        If you have any questions regarding this Privacy Policy or the use of your data, please contact us at 2024cybershop2024@gmail.com. We will be happy to assist you and answer all your questions.
        """, "-fx-text-fill: white; -fx-font-size: 18;");

        Text policyText10 = createStyledLabel("""
        This policy takes effect upon its publication and aims to describe CYBERSHOP's practices regarding the collection, use, and protection of your information. This means that from the moment you visit or use CYBERSHOP services, you agree to the terms outlined in this privacy policy.
        """, "-fx-text-fill: white; -fx-font-size: 18;");

        addHoverAnimation(policyText1);
        addHoverAnimation(policyText2);
        addHoverAnimation(policyText3);
        addHoverAnimation(policyText4);
        addHoverAnimation(policyText5);
        addHoverAnimation(policyText6);
        addHoverAnimation(policyText7);
        addHoverAnimation(policyText8);
        addHoverAnimation(policyText9);
        addHoverAnimation(policyText10);

        privacyLayout.getChildren().addAll(policyTitle, policyText1, policyText2, policyText3, policyText4, policyText5, policyText6, policyText7, policyText8, policyText9, policyText10);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(privacyLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene privacyScene = new Scene(scrollPane, 900, 600);
        privacyScene.setFill(Color.BLACK);
        privacyStage.setScene(privacyScene);
        privacyStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, privacyStage, scene);
        hotKeysHandler.addHotkeys();

    }

    private Text createStyledLabel(String text, String style) {
        Text label = new Text(text);
        label.setStyle(style);
        label.setFont(Font.font("Arial", 18));
        label.setFill(Color.DARKGRAY);
        label.setTextAlignment(TextAlignment.LEFT);
        label.setWrappingWidth(750);
        label.setLineSpacing(1.5);
        return label;
    }

    private void addHoverAnimation(Text text) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), text);
        scaleTransition.setToX(1.01);
        scaleTransition.setToY(1.01);

        text.setOnMouseEntered(event -> scaleTransition.playFromStart());

        scaleTransition.setOnFinished(event -> {
            ScaleTransition reverseTransition = new ScaleTransition(Duration.millis(200), text);
            reverseTransition.setToX(1.01);
            reverseTransition.setToY(1.01);
            reverseTransition.play();
        });

        text.setOnMouseExited(event -> scaleTransition.stop());
    }
}
