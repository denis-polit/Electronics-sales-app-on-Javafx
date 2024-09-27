import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.textfield.CustomTextField;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.List;
import java.util.*;
import java.io.File;

public class MainServiceApp extends Application {

    private Stage primaryStage;
    private BorderPane root;
    private ImageView currentImage;
    private Circle[] dots;
    private final int[] slideIndex = {0};
    private HeaderBaseComponent headerBaseComponent;
    private HostServices hostServices;
    private MenuPage menuPage = new MenuPage();
    private SessionManager sessionManager;
    private FirstConnectionToDataBase connectionToDataBase;
    private List<Widget> widgets;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public static void main(String[] args) throws URISyntaxException, IOException {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish database connection: " + e.getMessage(), e);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.sessionManager = SessionManager.getInstance();

        try {
            init();
        } catch (RuntimeException e) {
            Platform.runLater(() -> alertService.showErrorAlert(e.getMessage()));
            return;
        }

        headerBaseComponent = new HeaderBaseComponent(root);

        root = initImagesAndPages();

        Scene scene = new Scene(root, 895, 600);
        scene.setFill(Color.BLACK);
        root.setStyle("-fx-background-color: black;");

        VBox header = headerBaseComponent.createHeader();
        root.setTop(header);

        scene.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.ESCAPE).match(event)) {
                confirmClose();
            }
        });

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            confirmClose();
        });

        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setTitle("Home Page");
        primaryStage.show();

        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
    }

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            System.out.println("Attempting to establish DB connection...");
            Connection conn = connectionToDataBase.getConnection();
            if (conn != null) {
                System.out.println("Database connection established successfully.");
            } else {
                System.out.println("Failed to establish database connection.");
            }
            return conn;
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    private void confirmClose() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Close");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to exit the application?");

        ButtonType confirmButton = new ButtonType("Exit");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == confirmButton) {
            closeAllWindows();
            closeResources();
            System.exit(0);
        }
    }

    private void closeResources() {
        try {
            Connection connection = establishDBConnection();
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void closeAllWindows() {
        Platform.exit();
    }

    private BorderPane initImagesAndPages() throws Exception {

        MainPage mainPage = new MainPage();
        BorderPane newBorderPane = mainPage.start(primaryStage, headerBaseComponent.createHeader());

        List<Widget> widgets = mainPage.loadWidgetsFromDatabase();

        if (widgets.isEmpty()) {
            throw new Exception("No widgets found in the database.");
        }

        String baseDir = System.getProperty("user.dir");

        ImageView[] images = new ImageView[widgets.size()];

        for (int i = 0; i < widgets.size(); i++) {
            try {
                String imagePath = widgets.get(i).getImagePath();

                if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                    images[i] = new ImageView(new Image(imagePath));
                    System.out.println("Loading image from URL: " + imagePath);
                } else {
                    imagePath = imagePath.replaceAll("//", "/");

                    if (imagePath.endsWith(".png")) {
                        imagePath = imagePath.replace(".png", ".jpg");
                    }

                    String fullPath = "file:" + baseDir + "/" + imagePath;

                    File file = new File(fullPath);
                    if (file.exists()) {
                        images[i] = new ImageView(new Image("file:" + file.getAbsolutePath()));
                    } else {
                        images[i] = new ImageView();
                    }

                    images[i].setFitWidth(400);
                    images[i].setPreserveRatio(true);
                }

            } catch (Exception e) {
                System.out.println("Error loading image for widget with order number: " + widgets.get(i).getOrderNumber());
                images[i] = new ImageView();
            }
        }

        newBorderPane.setStyle("-fx-background-color: black;");

        return newBorderPane;
    }

    private class MainPage {

        private BorderPane start(Stage primaryStage, VBox header) {
            BorderPane newBorderPane = new BorderPane();
            newBorderPane.setStyle("-fx-background-color: black");

            VBox contentContainer = new VBox();
            contentContainer.setSpacing(20);
            contentContainer.setPadding(new Insets(10));
            contentContainer.setStyle("-fx-background-color: black;");

            VBox widgetsSection = createWidgetsSection(primaryStage);
            contentContainer.getChildren().add(widgetsSection);

            VBox newsSection = createNewsSection();
            contentContainer.getChildren().add(newsSection);

            VBox partnersSection = createPartnersSection();
            contentContainer.getChildren().add(partnersSection);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(contentContainer);
            scrollPane.setFitToHeight(true);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: black;");

            newBorderPane.setTop(header);
            newBorderPane.setCenter(scrollPane);

            contentContainer.prefWidthProperty().bind(scrollPane.widthProperty());
            contentContainer.prefHeightProperty().bind(scrollPane.heightProperty());

            return newBorderPane;
        }

        private VBox createWidgetsSection(Stage primaryStage) {
            VBox section = new VBox(10);
            section.setPadding(new Insets(20));
            section.setStyle("-fx-background-color: black;");

            widgets = loadWidgetsFromDatabase();

            if (widgets.isEmpty()) {
                return section;
            }

            currentImage = new ImageView();
            currentImage.setPreserveRatio(true);

            DoubleBinding paddingPercent = primaryStage.widthProperty().multiply(0.1); // Example padding: 10%
            currentImage.fitWidthProperty().bind(primaryStage.widthProperty().subtract(paddingPercent)); // Adjust width
            currentImage.fitHeightProperty().bind(currentImage.fitWidthProperty().multiply(9.0 / 16.0)); // Set height to maintain 16:9

            currentImage.setImage(widgets.get(0).getImage());

            HBox dotsContainer = new HBox(10);
            dotsContainer.setAlignment(Pos.CENTER);
            dotsContainer.setPadding(new Insets(10));

            DoubleProperty dotRadiusProperty = new SimpleDoubleProperty();

            primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
                double maxSize = 13;
                double newRadius = Math.min(maxSize, Math.max(10, newVal.doubleValue() * 0.01));
                dotRadiusProperty.set(newRadius);

                dotsContainer.setLayoutX((section.getWidth() - dotsContainer.getWidth()) / 2);
            });

            dots = new Circle[widgets.size()];
            for (int i = 0; i < widgets.size(); i++) {
                int index = i;
                dots[i] = new Circle();
                dots[i].radiusProperty().bind(dotRadiusProperty);
                dots[i].setFill(Color.WHITE);
                dotsContainer.getChildren().add(dots[i]);
                dots[i].setOnMouseClicked(event -> showSlide(index));
            }

            currentImage.setOnMouseClicked(event -> showCatalogPage());

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        slideIndex[0] = (slideIndex[0] + 1) % widgets.size();
                        showSlide(slideIndex[0]);
                    });
                }
            }, 0, 10000);

            section.getChildren().addAll(currentImage, dotsContainer);

            section.widthProperty().addListener((obs, oldVal, newVal) -> dotsContainer.setLayoutX((section.getWidth() - dotsContainer.getWidth()) / 2));

            section.layout();

            return section;
        }

        private void showSlide(int index) {
            currentImage.setImage(widgets.get(index).getImage());

            currentImage.fitHeightProperty().unbind();
            currentImage.setFitHeight(currentImage.getFitWidth() * 9.0 / 16.0);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), currentImage);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(event -> {
                currentImage.setImage(widgets.get(index).getImage());

                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), currentImage);
                fadeIn.setToValue(1.0);
                fadeIn.play();

                slideIndex[0] = index;
                updateDots();
            });

            fadeOut.play();
        }

        private void updateDots() {
            for (int i = 0; i < dots.length; i++) {
                dots[i].setFill(i == slideIndex[0] ? Color.BLUE : Color.WHITE);
            }
        }

        private List<Widget> loadWidgetsFromDatabase() {
            List<Widget> widgets = new ArrayList<>();
            String query = "SELECT image_path, order_number FROM widgets ORDER BY order_number";

            try (Connection conn = connectionToDataBase.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    String imagePath = rs.getString("image_path");
                    int orderNumber = rs.getInt("order_number");
                    widgets.add(new Widget(imagePath, orderNumber));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return widgets;
        }

        private void showCatalogPage() {
            CatalogPage catalogPage = new CatalogPage();
            Stage catalogStage = new Stage();

            catalogStage.initStyle(StageStyle.DECORATED);
            catalogPage.showCatalog(catalogStage);
        }

        private VBox createNewsSection() {
            VBox newsSection = new VBox(10);
            newsSection.setPadding(new Insets(20));
            newsSection.setStyle("-fx-background-color: #000000");

            Separator topSeparator = new Separator();
            topSeparator.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(topSeparator, new Insets(0, 0, 30, 0));

            Text newsTitle = new Text(" Actual news ");
            newsTitle.setFont(Font.font("Gotham", FontWeight.EXTRA_BOLD, Font.getDefault().getSize() * 1.9));
            newsTitle.setFill(Color.WHITE);

            HBox titleAndButtonBox = new HBox(10);
            titleAndButtonBox.setAlignment(Pos.CENTER_LEFT);
            titleAndButtonBox.getChildren().add(newsTitle);

            VBox.setMargin(titleAndButtonBox, new Insets(0, 0, 20, 0));

            if ("super_admin".equals(getEmployeeStatus(sessionManager.getCurrentManagerName()))) {
                Button addNewsButton = ButtonStyle.createStyledButton("+");
                addNewsButton.setOnAction(e -> openAddNewsDialog());
                titleAndButtonBox.getChildren().add(addNewsButton);
            }

            VBox newsContainer = new VBox(10);
            List<NewsItem> newsList = loadNewsFromDatabase();

            final int maxVisibleNews = 5;
            final int[] displayedNewsCount = {0};

            for (NewsItem newsItem : newsList) {
                if (displayedNewsCount[0] < maxVisibleNews) {
                    newsContainer.getChildren().add(newsItem);
                    displayedNewsCount[0]++;
                }
            }

            Button showMoreButton = ButtonStyle.expandPaneStyledButton("  Show 5 more news (" + (newsList.size() - displayedNewsCount[0]) + " news)  ");
            Button showLessButton = ButtonStyle.expandPaneStyledButton("  Show less news  ");
            showLessButton.setVisible(false);

            showMoreButton.setOnAction(e -> {
                int remainingNews = newsList.size() - displayedNewsCount[0];
                int newsToAdd = Math.min(remainingNews, 5);
                for (int i = 0; i < newsToAdd; i++) {
                    newsContainer.getChildren().add(newsList.get(displayedNewsCount[0]));
                    displayedNewsCount[0]++;
                }

                if (displayedNewsCount[0] >= newsList.size()) {
                    showMoreButton.setVisible(false);
                }

                if (displayedNewsCount[0] > maxVisibleNews) {
                    showLessButton.setVisible(true);
                }
            });

            showLessButton.setOnAction(e -> {
                newsContainer.getChildren().clear();
                displayedNewsCount[0] = 0;
                for (NewsItem newsItem : newsList) {
                    if (displayedNewsCount[0] < maxVisibleNews) {
                        newsContainer.getChildren().add(newsItem);
                        displayedNewsCount[0]++;
                    }
                }
                showMoreButton.setVisible(true);
                showLessButton.setVisible(false);
            });

            Separator bottomSeparator = new Separator();
            bottomSeparator.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(bottomSeparator, new Insets(20, 0, 0, 0));

            newsSection.getChildren().addAll(topSeparator, titleAndButtonBox, newsContainer, showMoreButton, showLessButton, bottomSeparator);

            return newsSection;
        }

        public String getEmployeeStatus(String managerName) {
            try (Connection connection = establishDBConnection()) {
                String query = "SELECT employee_status FROM managers WHERE manager_name = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, managerName);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getString("employee_status");
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void openAddNewsDialog() {
            AddNewsWindow addNewsWindow = new AddNewsWindow();
            Stage addNewsStage = new Stage();
            try {
                addNewsWindow.start(addNewsStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private List<NewsItem> loadNewsFromDatabase() {
            List<NewsItem> newsList = new ArrayList<>();
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;

            try {
                connection = establishDBConnection();
                statement = connection.createStatement();
                resultSet = statement.executeQuery("SELECT * FROM news");

                List<Map<String, Object>> resultList = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("news_id", resultSet.getInt("news_id"));
                    row.put("news_title", resultSet.getString("news_title"));
                    row.put("news_content", resultSet.getString("news_content"));
                    row.put("news_photo", resultSet.getString("news_photo"));
                    row.put("news_link", resultSet.getString("news_link"));
                    row.put("news_publication_date", resultSet.getString("news_publication_date"));
                    resultList.add(row);
                }

                for (Map<String, Object> row : resultList) {
                    int newsId = (int) row.get("news_id");
                    String title = (String) row.get("news_title");
                    String content = (String) row.get("news_content");
                    String imageUrl = (String) row.get("news_photo");
                    String link = (String) row.get("news_link");
                    String date = (String) row.get("news_publication_date");

                    NewsItem newsItem = createNewsItem(newsId, title, content, imageUrl, link, date, hostServices);
                    newsList.add(newsItem);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null && !resultSet.isClosed()) {
                        resultSet.close();
                    }
                    if (statement != null && !statement.isClosed()) {
                        statement.close();
                    }
                    if (connection != null && !connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return newsList;
        }

        private NewsItem createNewsItem(int newsId, String title, String content, String newsImageUrl, String newsLink, String date, HostServices hostServices) {
            News news = new News(newsId, title, content, newsImageUrl, newsLink, date, hostServices);
            NewsItem newsItem = new NewsItem(news, hostServices);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), newsItem);
            scaleIn.setFromX(1.0);
            scaleIn.setFromY(1.0);
            scaleIn.setToX(1.01);
            scaleIn.setToY(1.01);

            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), newsItem);
            scaleOut.setFromX(1.01);
            scaleOut.setFromY(1.01);
            scaleOut.setToX(1.0);
            scaleOut.setToY(1.0);

            newsItem.setOnMouseEntered(event -> scaleIn.play());
            newsItem.setOnMouseExited(event -> scaleOut.play());

            return newsItem;
        }

        private VBox createPartnersSection() {
            VBox partnersSection = new VBox(10);
            partnersSection.setPadding(new Insets(20));
            partnersSection.setStyle("-fx-background-color: #000000");
            VBox.setVgrow(partnersSection, Priority.ALWAYS);

            Text partnersTitle = new Text("Partners and references");
            partnersTitle.setFont(Font.font("Gotham", FontWeight.EXTRA_BOLD, Font.getDefault().getSize() * 1.9));
            partnersTitle.setFill(Color.WHITE);

            HBox titleAndButtonBox = new HBox(1);
            titleAndButtonBox.setAlignment(Pos.CENTER_LEFT);
            titleAndButtonBox.setPadding(new Insets(-20, 0, 20, 0));
            titleAndButtonBox.getChildren().add(partnersTitle);

            if ("super_admin".equals(getEmployeeStatus(sessionManager.getCurrentManagerName()))) {
                Button addPartnerButton = ButtonStyle.createStyledButton("+");
                addPartnerButton.setOnAction(e -> openAddPartnerWindow());

                Button editPartnerButton = ButtonStyle.createStyledButton("Edit");
                editPartnerButton.setOnAction(e -> openEditPartnerWindow());

                Button deletePartnerButton = ButtonStyle.createStyledButton("Delete");
                deletePartnerButton.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Delete Partner");
                    dialog.setHeaderText("Enter the name of the partner to delete:");
                    dialog.setContentText("Partner name:");

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(this::deletePartner);
                });

                titleAndButtonBox.getChildren().addAll(addPartnerButton, editPartnerButton, deletePartnerButton);
            }

            VBox.setMargin(titleAndButtonBox, new Insets(0, 0, 10, 0));
            VBox partnersRowsContainer = new VBox(10);
            partnersRowsContainer.setAlignment(Pos.CENTER);
            List<List<Partner>> partnersRows = splitPartnersIntoRows(loadPartnersFromDatabase());

            for (List<Partner> partnersRow : partnersRows) {
                HBox partnersRowBox = new HBox(20);
                partnersRowBox.setAlignment(Pos.CENTER);
                for (Partner partner : partnersRow) {
                    Circle partnerImage = createPartnerImage(partner);
                    addHoverEffect(partnerImage);
                    partnersRowBox.getChildren().add(partnerImage);
                }
                partnersRowsContainer.getChildren().add(partnersRowBox);
            }

            VBox.setVgrow(partnersRowsContainer, Priority.ALWAYS);
            partnersRowsContainer.setMaxHeight(Double.MAX_VALUE);

            partnersSection.getChildren().addAll(titleAndButtonBox, partnersRowsContainer);
            return partnersSection;
        }

        private List<List<Partner>> splitPartnersIntoRows(List<Partner> partners) {
            List<List<Partner>> rows = new ArrayList<>();
            for (int i = 0; i < partners.size(); i += 5) {
                rows.add(partners.subList(i, Math.min(i + 5, partners.size())));
            }
            return rows;
        }

        private List<Partner> loadPartnersFromDatabase() {
            List<Partner> partners = new ArrayList<>();
            try (Connection connection = establishDBConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT * FROM partner")) {
                while (resultSet.next()) {
                    String title = resultSet.getString("partner_title");
                    String link = resultSet.getString("partner_link");
                    String imagePath = resultSet.getString("partner_photo");
                    Partner partner = new Partner(title, link, imagePath);
                    partners.add(partner);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return partners;
        }

        private Circle createPartnerImage(Partner partner) {
            Circle partnerImage = new Circle(50);

            DoubleProperty radiusProperty = new SimpleDoubleProperty(50);
            partnerImage.radiusProperty().bind(radiusProperty);

            try {
                String imagePath = partner.getImagePath();
                if (imagePath != null && !imagePath.isEmpty()) {
                    Image image = new Image(imagePath, false);
                    if (image.isError()) {
                        throw new IllegalArgumentException("Failed to load image: " + imagePath);
                    }
                    partnerImage.setFill(new ImagePattern(image));
                } else {
                    throw new IllegalArgumentException("Image path is null or empty for partner: " + partner.getTitle());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error loading image for partner: " + partner.getTitle());
                partnerImage.setFill(Color.GRAY);
            }

            partnerImage.setCursor(Cursor.HAND);
            partnerImage.setOnMouseClicked(event -> openLink(partner.getWebsite()));

            partnerImage.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                        double maxSize = 150;
                        double newRadius = Math.min(maxSize / 2, Math.max(30, newWidth.doubleValue() / 20));
                        radiusProperty.set(newRadius);
                    });
                }
            });

            return partnerImage;
        }

        private void addHoverEffect(Circle circle) {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), circle);
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);

            ScaleTransition reverseTransition = new ScaleTransition(Duration.millis(200), circle);
            reverseTransition.setToX(1);
            reverseTransition.setToY(1);

            circle.setOnMouseEntered(event -> scaleTransition.playFromStart());
            circle.setOnMouseExited(event -> reverseTransition.playFromStart());
        }

        private void openAddPartnerWindow() {
            AddPartnerWindow addPartnerWindow = new AddPartnerWindow();
            Stage addPartnerStage = new Stage();
            try {
                addPartnerWindow.start(addPartnerStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void deletePartner(String partnerName) {
            if (alertService.showConfirmationAlert("Are you sure you want to delete the partner \"" + partnerName + "\"?")) {
                if (partnerExists(partnerName)) {
                    try (Connection connection = establishDBConnection()) {
                        if (connection != null) {
                            String query = "DELETE FROM partner WHERE partner_title = ?";
                            try (PreparedStatement statement = connection.prepareStatement(query)) {
                                statement.setString(1, partnerName);
                                int rowsAffected = statement.executeUpdate();
                                if (rowsAffected > 0) {
                                    alertService.showSuccessAlert("Partner deleted successfully.");
                                } else {
                                    alertService.showErrorAlert("Partner not found or already deleted.");
                                }
                            }
                        } else {
                            alertService.showErrorAlert("Failed to establish database connection.");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        alertService.showErrorAlert("Failed to delete partner.");
                    }
                } else {
                    alertService.showErrorAlert("Partner \"" + partnerName + "\" not found in the database.");
                }
            }
        }

        private void openEditPartnerWindow() {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Edit Partner");
            dialog.setHeaderText("Enter the name of the partner to edit:");
            dialog.setContentText("Partner name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(partnerName -> {
                if (partnerExists(partnerName)) {
                    Partner partner = getPartnerDetails(partnerName);
                    if (partner != null) {
                        displayEditPartnerForm(partner);
                    } else {
                        alertService.showErrorAlert("Failed to retrieve partner details.");
                    }
                } else {
                    alertService.showErrorAlert("Partner not found.");
                }
            });
        }


        private boolean partnerExists(String partnerName) {
            try (Connection connection = establishDBConnection();
                 PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM partner WHERE partner_title = ?")) {
                stmt.setString(1, partnerName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        private Partner getPartnerDetails(String partnerName) {
            try (Connection connection = establishDBConnection();
                 PreparedStatement stmt = connection.prepareStatement("SELECT * FROM partner WHERE partner_title = ?")) {
                stmt.setString(1, partnerName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Partner(
                                rs.getString("partner_title"),
                                rs.getString("partner_link"),
                                rs.getString("partner_photo")
                        );
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void displayEditPartnerForm(Partner partner) {
            Stage editStage = new Stage();
            VBox editBox = new VBox(10);
            editBox.setPadding(new Insets(20));
            editBox.setStyle("-fx-background-color: #000000");

            Text editTitle = new Text("Edit Partner: " + partner.getTitle());
            editTitle.setFont(Font.font("Gotham", FontWeight.EXTRA_BOLD, 24));
            editTitle.setFill(Color.WHITE);

            CustomTextField titleField = new CustomTextField();
            titleField.setText(partner.getTitle());
            titleField.setPromptText("Partner Title");

            CustomTextField linkField = new CustomTextField();
            linkField.setText(partner.getWebsite());
            linkField.setPromptText("Partner Website");

            CustomTextField imageField = new CustomTextField();
            imageField.setText(partner.getImagePath());
            imageField.setPromptText("Partner Image Path");

            Button saveButton = ButtonStyle.createStyledButton("Save");
            saveButton.setOnAction(e -> {
                partner.setTitle(titleField.getText());
                partner.setWebsite(linkField.getText());
                partner.setImagePath(imageField.getText());
                updatePartner(partner);
                editStage.close();
            });

            editBox.getChildren().addAll(editTitle, titleField, linkField, imageField, saveButton);
            Scene editScene = new Scene(editBox, 400, 300);
            editStage.setScene(editScene);
            editStage.setTitle("Edit Partner");
            editStage.show();
        }

        private void updatePartner(Partner partner) {
            try (Connection connection = establishDBConnection();
                 PreparedStatement stmt = connection.prepareStatement(
                         "UPDATE partner SET partner_title = ?, partner_link = ?, partner_photo = ? WHERE partner_title = ?")) {
                stmt.setString(1, partner.getTitle());
                stmt.setString(2, partner.getWebsite());
                stmt.setString(3, partner.getImagePath());
                stmt.setString(4, partner.getTitle());
                stmt.executeUpdate();
                alertService.showSuccessAlert("Partner updated successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                alertService.showErrorAlert("Failed to update partner.");
            }
        }

        private void openLink(String link) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(link));
                } else {
                    System.err.println("Desktop browsing is not supported.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to open link: " + link);
            }
        }
    }
}