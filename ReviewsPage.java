import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReviewsPage extends Application {

    private BorderPane root;
    private ListView<Review> reviewsListView;
    private Connection connection;
    private SessionManager sessionManager;
    private Stage primaryStage;
    private FirstConnectionToDataBase connectionToDataBase;
    private Stage loadingStage;
    private ReviewCreateWindow reviewCreateWindow;
    private AlertService alertService;
    private ButtonStyle buttonStyle;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        MenuPage menuPage = new MenuPage();
        this.alertService = new AlertServiceImpl();  // Initialize your AlertService
        this.buttonStyle = new ButtonStyle();  // Initialize your ButtonStyle

        root = new BorderPane();
        Scene scene = new Scene(root, 900, 600);
        root.setStyle("-fx-background-color: black;");

        // Create HotKeysHandler instance and add hotkey handlers
        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefSize(600, 400);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Reviews Page");
        primaryStage.show();

        HeaderComponent headerComponent = new HeaderComponent(this, primaryStage);
        VBox header = headerComponent.createHeader();
        root.setTop(header);

        sessionManager = SessionManager.getInstance();

        // Initialize database connection
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
            connection = connectionToDataBase.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Connection is null or closed after initialization.");
            }
        } catch (SQLException e) {
            showErrorAlert("Failed to establish database connection: " + e.getMessage());
            return; // Exit the method to prevent further operations
        }

        createReviewsPage();
    }

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            connection = connectionToDataBase.getConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Connection is null or closed.");
            }
            return connection;
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    public class HeaderComponent {

        public HeaderComponent(ReviewsPage reviewsPage, Stage primaryStage) {
            createHeader();
        }

        private VBox createHeader() {
            VBox header = new VBox(10);
            header.setPadding(new Insets(10));
            header.setStyle("-fx-background-color: black");
            header.setAlignment(Pos.CENTER);

            Image logoImage = new Image("file:icons/LOGO_our.jpg");
            ImageView logoImageView = new ImageView(logoImage);
            logoImageView.setFitWidth(50);
            logoImageView.setFitHeight(50);

            Circle logoCircle = new Circle(25);
            logoCircle.setFill(new ImagePattern(logoImage));
            logoCircle.setCursor(Cursor.HAND);

            Button menuButton = createStyledButton(" Menu ");
            menuButton.setOnAction(e -> showMenu());

            Button supportButton = createStyledButton(" Support  ");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = createStyledButton("  Privacy Policy  ");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button addCommentButton = createStyledButton(" Add review: ");
            addCommentButton.setOnAction(e -> reviewCreateWindow.createCommentDialog());

            Button accountButton = createStyledButton(" Personal Account ");
            accountButton.setOnAction(e -> showRegistrationWindow());

            HBox bottomBar = new HBox(10);
            bottomBar.setAlignment(Pos.CENTER_RIGHT);
            bottomBar.getChildren().addAll(addCommentButton, accountButton);

            HBox.setMargin(addCommentButton, new Insets(0, 10, 0, 0));

            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER);
            headerBox.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, bottomBar);
            HBox.setMargin(logoCircle, new Insets(0, 200, 0, 0));

            header.getChildren().addAll(headerBox);

            return header;
        }

        private Button createStyledButton(String text) {
            Button button = new Button(text);
            button.setTextFill(javafx.scene.paint.Color.WHITE);
            button.setFont(javafx.scene.text.Font.font("Open Sans", FontWeight.NORMAL, 14));
            button.setStyle("-fx-background-color: black; -fx-background-radius: 15px;");
            button.setOpacity(1.0);

            FadeTransition colorIn = new FadeTransition(Duration.millis(300), button);
            colorIn.setToValue(1.0);

            FadeTransition colorOut = new FadeTransition(Duration.millis(300), button);
            colorOut.setToValue(0.7);

            button.setOnMouseEntered(e -> {
                colorIn.play();
                button.setStyle("-fx-background-color: blue; -fx-background-radius: 15px;");
            });

            button.setOnMouseExited(e -> {
                colorOut.play();
                button.setStyle("-fx-background-color: black; -fx-background-radius: 15px;");
            });

            return button;
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

    public List<Review> getReviews() {
        List<Review> reviews = new ArrayList<>();
        String query = "SELECT id_review, creator_id, creator_status, review_content, review_grade, review_date, last_modified_date FROM reviews";

        // Define the date format used in your database
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust format as needed

        try {
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Connection is null or closed when fetching reviews.");
            }

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    int id = resultSet.getInt("id_review");
                    int creatorId = resultSet.getInt("creator_id");
                    String status = resultSet.getString("creator_status");
                    String content = resultSet.getString("review_content");
                    int rating = Integer.parseInt(resultSet.getString("review_grade")); // Assuming review_grade is numeric

                    String dateStr = resultSet.getString("review_date");
                    String lastModifiedStr = resultSet.getString("last_modified_date");

                    // Convert date strings to Date objects with null checks
                    Date date = null;
                    Date lastModified = null;
                    if (dateStr != null && !dateStr.isEmpty()) {
                        try {
                            date = dateFormat.parse(dateStr);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            // Handle the parse exception as needed
                        }
                    }
                    if (lastModifiedStr != null && !lastModifiedStr.isEmpty()) {
                        try {
                            lastModified = dateFormat.parse(lastModifiedStr);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            // Handle the parse exception as needed
                        }
                    }

                    // Create Review object with lastModified
                    Review review = new Review(id, creatorId, status, content, rating, date, lastModified);
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error fetching reviews from the database.");
        }

        return reviews;
    }

    public void createReviewsPage() {
        reviewsListView = new ListView<>();
        reviewsListView.setPrefSize(850, 600);

        reviewsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Review review, boolean empty) {
                super.updateItem(review, empty);

                if (empty || review == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String content = "Author ID: " + review.getAuthorId() + "\nStatus: " + review.getStatus() + "\nRating: " + review.getRating() + "\nDate: " + review.getDate() + "\n\nContent: " + review.getContent();
                    setStyle("-fx-background-color: black;");
                    setPadding(new Insets(5));
                    setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(0, 0, 1, 0))));
                    setWrapText(true);

                    setOnMouseEntered(event -> {
                        setTextFill(Color.WHITE);
                        setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    });

                    setOnMouseExited(event -> {
                        setTextFill(Color.WHITE);
                        setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                    });

                    boolean isOwner = false;
                    boolean isSuperAdmin = false;
                    String currentUser = sessionManager.getCurrentClientName();
                    String currentEmployeeStatus = getEmployeeStatus();
                    if (currentUser != null && currentUser.equals(review.getStatus())) {
                        isOwner = true;
                    }
                    if ("super_admin".equals(currentEmployeeStatus)) {
                        isSuperAdmin = true;
                    }

                    Button complainButton = ButtonStyle.createStyledButton("Complain");
                    complainButton.setOnAction(event -> showComplaintDialog(review.getId()));

                    HBox buttonBox = new HBox(10);
                    buttonBox.getChildren().add(complainButton);

                    if (isOwner || isSuperAdmin) {
                        Button editButton = ButtonStyle.createStyledButton("Edit");
                        editButton.setOnAction(event -> editReview(review));

                        Button deleteButton = ButtonStyle.createStyledButton("Remove");
                        deleteButton.setOnAction(event -> deleteReview(review));

                        buttonBox.getChildren().addAll(editButton, deleteButton);
                    }

                    VBox contentBox = new VBox(10);
                    contentBox.setStyle("-fx-background-color: black; -fx-padding: 20; -fx-border-color: #CCCCCC; -fx-border-radius: 15px;");
                    Text contentText = createWrappedText(content);
                    contentBox.getChildren().addAll(contentText, buttonBox);

                    setGraphic(contentBox);
                }
            }
        });

        VBox reviewsContent = new VBox(10);
        reviewsContent.setStyle("-fx-background-color: black;");
        reviewsContent.getChildren().addAll(reviewsListView);
        reviewsContent.setAlignment(Pos.CENTER);

        root.setCenter(reviewsContent);

        loadingStage = LoadingIndicatorUtil.showLoadingIndicator(primaryStage);

        new Thread(() -> {
            updateDisplayedReviews();
            Platform.runLater(() -> LoadingIndicatorUtil.hideLoadingIndicator(loadingStage));
        }).start();
    }

    private void deleteReview(Review review) {
        ReviewDeleteWindow reviewDeleteWindow = new ReviewDeleteWindow(connectionToDataBase, alertService, this);
        reviewDeleteWindow.deleteReview(review);
    }

    private void editReview(Review review) {
        ReviewEditWindow reviewEditWindow = new ReviewEditWindow(alertService, buttonStyle, connection, this);
        reviewEditWindow.editReview(review);
    }

    private void showComplaintDialog(int commentId) {
        int userId = sessionManager.getCurrentUserId();
        Integer employeeId = sessionManager.getCurrentEmployeeId();

        ReviewComplaintWindow complaintWindow = new ReviewComplaintWindow(
                primaryStage,
                new AlertServiceImpl(),
                connectionToDataBase,
                userId,
                employeeId
        );

        complaintWindow.show(commentId);
    }

    private Text createWrappedText(String text) {
        Text wrappedText = new Text(text);
        wrappedText.setFill(Color.WHITE);
        wrappedText.setFont(Font.font("Gotham", FontWeight.NORMAL, 14));
        wrappedText.setWrappingWidth(600);
        wrappedText.setStyle("-fx-text-indent: 20px;");
        return wrappedText;
    }

    private String getEmployeeStatus() {
        try (Connection connection = establishDBConnection()) {
            String managerName = sessionManager.getCurrentManagerName();
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

    public void updateDisplayedReviews() {
        List<Review> reviews = getReviews();

        reviewsListView.getItems().clear();
        reviewsListView.getItems().addAll(reviews);
    }

    private void showErrorAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }
}
