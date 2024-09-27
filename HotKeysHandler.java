import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class HotKeysHandler {

    MenuPage menuPage;
    private Stage primaryStage;
    private Scene scene;

    public HotKeysHandler(MenuPage menuPage, Stage primaryStage, Scene scene) {
        this.menuPage = menuPage;
        this.primaryStage = primaryStage;
        this.scene = scene;
    }

    public void addHotkeys() {
        scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.M, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                menuPage.showMenu();
            } else if (new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                menuPage.showSupportWindow();
            } else if (new KeyCodeCombination(KeyCode.T, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                menuPage.showPrivacyPolicyWindow();
            } else if (new KeyCodeCombination(KeyCode.P, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                menuPage.showRegistrationWindow(primaryStage);
            } else if (event.getCode() == KeyCode.F5) {
                System.out.println("scene updated");
                updatePage(primaryStage, scene, true);
            } else if (new KeyCodeCombination(KeyCode.TAB, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                menuPage.switchToHotKeysClientInfoPage();
            } else if (new KeyCodeCombination(KeyCode.TAB, KeyCodeCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN).match(event)) {
                if ("super_admin".equals(menuPage.getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    menuPage.lastButtonChoose(primaryStage);
                } else {
                    menuPage.switchToHotKeysEmployeeInfoPage();
                }
            } else if (new KeyCodeCombination(KeyCode.I, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                menuPage.showInstructionsPage(primaryStage);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                showConfirmationDialog(primaryStage);
            } else if (event.isControlDown() && event.getText().equals("1")) {
                showWindowByNumber(1);
            } else if (event.isControlDown() && event.getText().equals("2")) {
                showWindowByNumber(2);
            } else if (event.isControlDown() && event.getText().equals("3")) {
                showWindowByNumber(3);
            } else if (event.isControlDown() && event.getText().equals("4")) {
                showWindowByNumber(4);
            } else if (event.isControlDown() && event.getText().equals("5")) {
                showWindowByNumber(5);
            } else if (event.isControlDown() && event.getText().equals("6")) {
                showWindowByNumber(6);
            } else if (event.isControlDown() && event.getText().equals("7")) {
                showWindowByNumber(7);
            } else if (event.isControlDown() && event.getText().equals("8")) {
                showWindowByNumber(8);
            } else if (event.isControlDown() && event.getText().equals("9")) {
                showWindowByNumber(9);
            } else if (event.isControlDown() && event.getText().equals("0")) {
                showWindowByNumber(0);
            } else if (event.isControlDown() && event.getText().equals("-")) {
                showWindowByNumber(-1);
            } else if (new KeyCodeCombination(KeyCode.F, KeyCodeCombination.CONTROL_DOWN).match(event)) {
                showSearchDialog(primaryStage);
            }
        });
    }

    private void showWindowByNumber(int windowNumber) {
        switch (windowNumber) {
            case 1:
                menuPage.showHomePage(primaryStage);
                break;
            case 2:
                menuPage.showReviewsPage(primaryStage);
                break;
            case 3:
                if ("super_admin".equals(menuPage.getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    menuPage.showOrdersOrNegotiationsPage(primaryStage);
                } else {
                    menuPage.showServicesPage(primaryStage);
                }
                break;
            case 4:
                if ("super_admin".equals(menuPage.getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    menuPage.showCreateEmployeeAccountPage(primaryStage);
                } if ("main_manager".equals(menuPage.getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    menuPage.showOrdersOrNegotiationsPage(primaryStage);
                } else {
                    menuPage.showDescriptionPage(primaryStage);
                }
                break;
            case 5:
                menuPage.showRegistrationWindow(primaryStage);
                break;
            case 6:
                menuPage.showContractsPage(primaryStage);
                break;
            case 7:
                menuPage.showManagersListPage(primaryStage);
                break;
            case 8:
                if ("accountant".equals(menuPage.getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    menuPage.showBookkeepingPage(primaryStage);
                } else if ("super_admin".equals(menuPage.getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName())) || "manager".equals(menuPage.getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    menuPage.showClientsPage(primaryStage);
                } else if ("main_manager".equals(menuPage.getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    menuPage.showClientsPage(primaryStage);
                } else if ("manager".equals(menuPage.getEmployeeStatus(SessionManager.getInstance().getCurrentManagerName()))) {
                    menuPage.showClientsPage(primaryStage);
                } else {
                    menuPage.showInstructionsPage(primaryStage);
                }
                break;
            case 9:
                menuPage.showStatisticsPage(primaryStage);
                break;
            case 0:
                menuPage.showCatalogPage(primaryStage);
                break;
            case -1:
                if (SessionManager.getInstance().isManagerEnter()) {
                    menuPage.showEmployeerInstructionsPage(primaryStage);
                } else {
                    menuPage.showOtherAppsPage(primaryStage);
                }
                break;
            default:
                break;
        }
    }

    public static void showConfirmationDialog(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Are you sure you want to close the window?");
        alert.setContentText("Press OK to close the window.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            primaryStage.close();
        }
    }

    public static void updatePage(Stage primaryStage, Scene newScene, boolean closeOriginalStage) {
        if (primaryStage != null) {
            if (closeOriginalStage) {
                primaryStage.close();
            }
            primaryStage.setScene(newScene);
            primaryStage.show();
        }
    }

    public static void showSearchDialog(Stage primaryStage) {
        Stage searchStage = new Stage();
        searchStage.initModality(Modality.WINDOW_MODAL);
        searchStage.initOwner(primaryStage);
        searchStage.setTitle("Search in Catalog");

        VBox root = new VBox(10);
        root.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        root.setPadding(new Insets(10));

        TextField searchField = new TextField();
        searchField.setPromptText("Enter search term");
        searchField.setPrefWidth(400);
        searchField.setStyle(
                "-fx-background-color: black; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: white; " +
                        "-fx-background-radius: 5em; " +
                        "-fx-border-color: transparent; " +
                        "-fx-padding: 10px 0.25em 0.25em 0.25em; " +
                        "-fx-font-size: 1.0em;" +
                        "-fx-border-radius: 15px; " +
                        "-fx-border-color: white;"
        );

        Label promptLabel = new Label("Search in catalog");
        promptLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Gotham'; -fx-font-size: 20px;");

        HBox centeringBox = new HBox(promptLabel);
        centeringBox.setAlignment(Pos.CENTER);

        Button searchButton = createStyledButton("Search");
        Button cancelButton = createStyledButton("Cancel");

        HBox buttonBox = new HBox(10, createSpacer(), searchButton, cancelButton, createSpacer());
        buttonBox.setPadding(new Insets(10, 0, 10, 0));

        VBox.setMargin(searchField, new Insets(20, 0, 20, 0));

        root.getChildren().addAll(centeringBox, searchField, buttonBox);

        searchButton.setOnAction(event -> {
            CatalogPage catalogPage = new CatalogPage();
            catalogPage.performSearchAndUpdateCatalog(searchField.getText());
            searchStage.close();
        });

        cancelButton.setOnAction(event -> searchStage.close());

        Scene scene = new Scene(root);
        searchStage.setScene(scene);
        searchStage.showAndWait();
    }

    private static Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private static Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setTextFill(javafx.scene.paint.Color.WHITE);
        button.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 15));
        button.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 2px; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        button.setOpacity(1.0);

        FadeTransition colorIn = new FadeTransition(Duration.millis(300), button);
        colorIn.setToValue(1.0);

        FadeTransition colorOut = new FadeTransition(Duration.millis(300), button);
        colorOut.setToValue(0.7);

        button.setOnMouseEntered(e -> {
            colorIn.play();
            button.setStyle("-fx-background-color: #7331FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        });

        button.setOnMouseExited(e -> {
            colorOut.play();
            button.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: white; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        });

        return button;
    }
}