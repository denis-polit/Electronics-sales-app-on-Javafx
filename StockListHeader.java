import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.List;

public class StockListHeader {

    private final SearchField searchField;
    private final Stage primaryStage;
    private BorderPane root;
    private final SessionManager sessionManager;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private final StockDB stockDB;

    public StockListHeader(StackPane announcementsFlowPane, Stage primaryStage, SessionManager sessionManager) {
        this.primaryStage = primaryStage;
        this.sessionManager = sessionManager;
        this.stockDB = new StockDB();
        this.searchField = new SearchField(stockDB, announcementsFlowPane);
        createHeader();
    }

    public Node getNode() {
        return createHeader();
    }

    public void setAnnouncementsListView(ListView<AvaliableAnnouncement> listView) {
        this.searchField.setAnnouncementsListView(listView);
    }

    private VBox createHeader() {
        HBox headerChooseBox = new HBox(10);
        headerChooseBox.setAlignment(Pos.CENTER);
        VBox header = new VBox(10);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: black");
        header.setAlignment(Pos.CENTER);

        Image logoImage = new Image("file:icons/LOGO_our.jpg");
        ImageView logoImageView = new ImageView(logoImage);
        logoImageView.setFitWidth(50);
        logoImageView.setFitHeight(50);

        Circle logoCircle = new Circle(25);
        logoCircle.setFill(new ImagePattern(logoImage));
        logoCircle.setCursor(javafx.scene.Cursor.HAND);

        Button menuButton = ButtonStyle.createStyledButton("Menu");
        menuButton.setOnAction(e -> showMenu());

        Button supportButton = ButtonStyle.createStyledButton(" Support  ");
        supportButton.setOnAction(event -> showSupportWindow());

        Button accountButton = ButtonStyle.createStyledButton("Account");
        accountButton.setOnAction(e -> showRegistrationWindow());

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color: black;");

        searchField.createSearchField();

        searchField.getSearchTextField().setPromptText("Search by Product Title");
        searchField.getSearchTextField().getStyleClass().add("search-field");

        searchField.getSearchTextField().setStyle(
                "-fx-background-color: black; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: white; " +
                        "-fx-background-radius: 5em; " +
                        "-fx-border-color: transparent; " +
                        "-fx-padding: 0.166667em 0.25em 0.25em 0.25em; " +
                        "-fx-font-size: 1.0em;" +
                        "-fx-border-radius: 15px; " +
                        "-fx-border-color: white;"
        );

        searchField.getSearchTextField().setPrefWidth(200);

        searchField.getSearchTextField().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                search();
            }
        });

        searchField.getSearchTextField().setOnMousePressed(e -> searchField.getSearchTextField().setStyle(
                "-fx-background-color: black; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: white; " +
                        "-fx-background-radius: 5em; " +
                        "-fx-border-color: transparent; " +
                        "-fx-padding: 0.166667em 0.25em 0.25em 0.25em; " +
                        "-fx-font-size: 1.0em;" +
                        "-fx-border-radius: 15px; " +
                        "-fx-border-color: #7331FF;"
        ));

        searchField.getSearchTextField().setOnMouseReleased(e -> searchField.getSearchTextField().setStyle(
                "-fx-background-color: black; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: white; " +
                        "-fx-background-radius: 5em; " +
                        "-fx-border-color: transparent; " +
                        "-fx-padding: 0.166667em 0.25em 0.25em 0.25em; " +
                        "-fx-font-size: 1.0em;" +
                        "-fx-border-radius: 15px; " +
                        "-fx-border-color: #7331FF;"
        ));

        searchBox.getChildren().addAll(searchField.getSearchTextField(), accountButton);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.getChildren().addAll(logoCircle, menuButton, supportButton, searchBox, headerChooseBox, accountButton);

        HBox.setMargin(logoCircle, new Insets(0, 50, 0, 0));

        header.getChildren().addAll(headerBox);

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

    private void showRegistrationWindow() {
        RegistrationWindow registrationWindow = new RegistrationWindow(root);
        Stage registrationStage = new Stage();
        registrationWindow.start(registrationStage);
    }

    private void search() {
        String searchText = searchField.getSearchTextField().getText();
        searchField.performSearch(searchText);
    }

    public class SearchField {
        private ListView<AvaliableAnnouncement> announcementsListView;
        private TextField searchTextField;
        private final StockDB stockDB;

        // Modified constructor to accept both StockDB and StackPane
        public SearchField(StockDB stockDB, StackPane announcementsFlowPane) {
            this.stockDB = stockDB;
            createSearchField();
        }

        public TextField getSearchTextField() {
            return this.searchTextField;
        }

        public void createSearchField() {
            this.searchTextField = new TextField();
            this.searchTextField.setPromptText("Search by keywords");

            this.searchTextField.setOnKeyPressed(event -> {
                if (event.isAltDown()) {
                    performSearch(this.searchTextField.getText());
                }
            });
        }

        public void setAnnouncementsListView(ListView<AvaliableAnnouncement> announcementsListView) {
            this.announcementsListView = announcementsListView;
        }

        public void performSearch(String searchText) {
            if (searchText.isEmpty()) {
                updateDisplayedAnnouncements(stockDB.getAllAnnouncementsFromDatabase());
            } else {
                String searchSql = "SELECT * FROM avaliable_product_for_catalog WHERE " +
                        "apfc_graphics_chip LIKE ? OR " +
                        "apfc_memory_type LIKE ? OR " +
                        "apfc_form_factor LIKE ? OR " +
                        "apfc_type_of_cooling_system LIKE ? OR " +
                        "apfc_price LIKE ? OR " +
                        "apfc_brand LIKE ? OR " +
                        "apfc_product_title LIKE ? OR " +
                        "apfc_product_description LIKE ? OR " +
                        "apfc_responsible_person LIKE ?";

                List<AvaliableAnnouncement> searchResults = stockDB.searchAnnouncementsByFields(searchSql, searchText);

                if (!searchResults.isEmpty()) {
                    updateDisplayedAnnouncements(searchResults);
                }
            }
        }

        private void updateDisplayedAnnouncements(List<AvaliableAnnouncement> announcements) {
            ObservableList<AvaliableAnnouncement> observableAnnouncements = FXCollections.observableArrayList(announcements);
            this.announcementsListView.setItems(observableAnnouncements);
        }
    }
}
