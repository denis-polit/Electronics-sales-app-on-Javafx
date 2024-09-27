import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
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

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CatalogHeader {
    public final SearchField searchField;
    private final Stage primaryStage;
    private final AlertService alertService = new AlertServiceImpl();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private FirstConnectionToDataBase connectionToDataBase;
    private final BorderPane root;


    private ProductCompareWindow productCompare;
    private AnnouncementCreateWindow announcementCreateWindow;
    private SortCatalog sortCatalog;
    private FilterCatalog filterCatalog;


    public CatalogHeader(CatalogDataBase catalogDatabase, StackPane announcementsFlowPane, Stage primaryStage, BorderPane root) {
        this.primaryStage = primaryStage;
        this.searchField = new SearchField(catalogDatabase, announcementsFlowPane);
        this.root = root;
        createHeader();
    }

    public void setAnnouncementsListView(ListView<Announcement> listView) {
        this.searchField.setAnnouncementsListView(listView);
    }

    public VBox createHeader() {
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

        Button privacyButton = ButtonStyle.createStyledButton("Sort");
        privacyButton.setOnAction(event -> sortCatalog.showSortForm());

        Button filterButton = ButtonStyle.createStyledButton("Filter");
        filterButton.setOnAction(e -> filterCatalog.showFilterForm());

        Button accountButton = ButtonStyle.createStyledButton("Account");
        accountButton.setOnAction(e -> showRegistrationWindow());

        if (sessionManager.isManagerEnter()) {
            String currentManagerName = sessionManager.getCurrentManagerName();
            String employeeStatus = sessionManager.getEmployeeStatusByName(currentManagerName);
            System.out.println("Current Manager Name: " + currentManagerName);
            System.out.println("Employee Status: " + employeeStatus);

            Button addAnnouncementButton = ButtonStyle.createStyledButton("Add Product");
            addAnnouncementButton.setOnAction(e -> handleAddAnnouncementAction(employeeStatus));

            headerChooseBox.getChildren().add(addAnnouncementButton);
        } else {
            Button compareButton = ButtonStyle.createStyledButton("Compare");
            compareButton.setOnAction(e -> productCompare.showCompareForm());
            headerChooseBox.getChildren().add(compareButton);
        }

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
        headerBox.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, searchBox, headerChooseBox, filterButton, accountButton);

        HBox.setMargin(logoCircle, new Insets(0, 50, 0, 0));

        header.getChildren().addAll(headerBox);

        return header;
    }

    private void handleAddAnnouncementAction(String employeeStatus) {
        Stage newStage = new Stage();
        StockList stockList = new StockList();

        if ("super_admin".equals(employeeStatus) || "main_manager".equals(employeeStatus)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Choose Action");
            alert.setHeaderText("Select the action you want to perform:");
            System.out.println("Displaying choice dialog for super_admin or main_manager");

            ButtonType buttonTypeOne = new ButtonType("Open AvaliableProductForCatalogList");
            ButtonType buttonTypeTwo = new ButtonType("Open Add Announcement Form");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == buttonTypeOne) {
                    System.out.println("Opening AvaliableProductForCatalogList for super_admin or main_manager");
                    Platform.runLater(() -> {
                        try {
                            stockList.start(newStage);
                        } catch (Exception ex) {
                            System.err.println("Failed to open AvaliableProductForCatalogList: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    });
                } else if (result.get() == buttonTypeTwo) {
                    System.out.println("Opening Add Announcement Form for super_admin or main_manager");
                    openAddAnnouncementForm();
                }
            }
        } else {
            System.out.println("Opening AvaliableProductForCatalogList for regular manager");
            Platform.runLater(() -> {
                try {
                    stockList.start(newStage);
                } catch (Exception ex) {
                    System.err.println("Failed to open AvaliableProductForCatalogList: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }
    }

    private void openAddAnnouncementForm() {
        try {
            FirstConnectionToDataBase firstConnection = FirstConnectionToDataBase.getInstance();
            AnnouncementCreateWindow createWindow = new AnnouncementCreateWindow(firstConnection);
            createWindow.showAddAnnouncementForm();
        } catch (SQLException e) {
            alertService.showErrorAlert("Database connection error: " + e.getMessage());
        }
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
        if (root != null) {
            RegistrationWindow registrationWindow = new RegistrationWindow(root);
            Stage registrationStage = new Stage();
            registrationWindow.start(registrationStage);
        } else {
            System.err.println("Root pane is not initialized");
        }
    }

    public void search() {
        String searchText = searchField.getSearchTextField().getText();
        searchField.performSearch(searchText);
    }

    public class SearchField {
        private ListView<Announcement> announcementsListView;
        private TextField searchTextField;
        private final CatalogDataBase catalogDatabase;

        public SearchField(CatalogDataBase catalogDatabase, StackPane announcementsFlowPane) {
            this.catalogDatabase = catalogDatabase;
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

        public void setAnnouncementsListView(ListView<Announcement> announcementsListView) {
            this.announcementsListView = announcementsListView;
        }

        public void performSearch(String searchText) {
            if (searchText.isEmpty()) {
                updateDisplayedAnnouncements(catalogDatabase.getAllAnnouncementsFromDatabase());
            } else {
                String searchSql = "SELECT * FROM catalog WHERE " +
                        "graphics_chip LIKE ? OR " +
                        "memory_type LIKE ? OR " +
                        "form_factor LIKE ? OR " +
                        "type_of_cooling_system LIKE ? OR " +
                        "price LIKE ? OR " +
                        "brand LIKE ? OR " +
                        "product_title LIKE ? OR " +
                        "product_description LIKE ? OR " +
                        "creator_name LIKE ?";

                List<Announcement> searchResults = catalogDatabase.searchAnnouncementsByFields(searchSql, searchText);

                if (!searchResults.isEmpty()) {
                    updateDisplayedAnnouncements(searchResults);
                    System.out.println("Search Results: " + searchResults);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("No Results");
                    alert.setHeaderText("No announcements found");
                    alert.setContentText("No announcements matched your search term: " + searchText);
                    alert.showAndWait();
                }
            }
        }

        private void updateDisplayedAnnouncements(List<Announcement> announcements) {
            ObservableList<Announcement> observableAnnouncements = FXCollections.observableArrayList(announcements);
            announcementsListView.setItems(observableAnnouncements);
        }
    }
}