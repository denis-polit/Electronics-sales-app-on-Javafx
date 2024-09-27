import javafx.animation.FadeTransition;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class NewsItem extends VBox {
    private static final int COLLAPSED_HEIGHT = 120;
    private final News news;
    private HostServices hostServices;
    private SessionManager sessionManager;
    private FirstConnectionToDataBase connectionToDataBase;

    public NewsItem(News news, HostServices hostServices) {
        this.news = news;
        this.hostServices = hostServices;
        this.sessionManager = SessionManager.getInstance();
        initializeConnection();

        HBox newsContent = new HBox(10);
        newsContent.setStyle("-fx-background-color: #000000");

        // Padding for HBox considering 2% on the left and 28% on the right
        newsContent.setPadding(new Insets(10, 0, 10, 0));
        newsContent.setPrefWidth(USE_COMPUTED_SIZE);

        // ImageView for the news photo
        ImageView newsImage = new ImageView();
        newsImage.setPreserveRatio(true);

        // Image width bound to 20% of HBox width
        newsImage.fitWidthProperty().bind(newsContent.widthProperty().multiply(0.20));

        // Image height calculated based on 16:9 aspect ratio
        newsImage.fitHeightProperty().bind(newsImage.fitWidthProperty().multiply(9.0 / 16.0));

        // Load the image by URL
        try {
            Image image = new Image(news.getImageUrl());
            newsImage.setImage(image);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid image URL: " + news.getImageUrl());
        }

        // VBox for news text content
        VBox newsText = new VBox(5);

        // Width of text bound to 50% of HBox width
        newsText.prefWidthProperty().bind(newsContent.widthProperty().multiply(0.50));

        // TextFlow for news content to handle wrapping
        TextFlow textFlow = new TextFlow();
        textFlow.setMaxWidth(newsText.prefWidthProperty().get());

        // News title
        Text newsItemTitle = new Text(news.getTitle());
        newsItemTitle.setFont(Font.font("Gotham", FontWeight.BOLD, 20));
        newsItemTitle.setFill(Color.WHITE);

        // News content
        Text newsItemContent = new Text(news.getContent());
        newsItemContent.setFont(Font.font("Gotham", 14));
        newsItemContent.setFill(Color.WHITE);

        // Add news content text to TextFlow
        textFlow.getChildren().add(newsItemContent);

        // Set wrapping width for TextFlow
        textFlow.setMaxWidth(300);

        // Date of publication
        Text newsDate = new Text("Date: " + news.getDate());
        newsDate.setFont(Font.font("Gotham", 14));
        newsDate.setFill(Color.WHITE);

        // "Read More" button
        Button readMoreButton = createStyledButton("Read More", news.getLink());
        readMoreButton.setOnAction(e -> {
            System.out.println("Attempting to open link: " + news.getLink());
            boolean opened = openLink(news.getLink());
            if (!opened) {
                System.out.println("Link opening failed.");
            }
        });

        // Edit and Delete buttons for super-admin
        Button editButton = createBorderButton(" Edit ");
        Button deleteButton = createBorderButton(" Delete ");

        if ("super_admin".equals(getEmployeeStatus(sessionManager.getCurrentManagerName()))) {
            editButton.setVisible(true);
            deleteButton.setVisible(true);
        } else {
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        }

        editButton.setOnAction(e -> handleEdit());
        deleteButton.setOnAction(e -> handleDelete());

        HBox buttonsContainer = new HBox(10);
        buttonsContainer.getChildren().addAll(readMoreButton, editButton, deleteButton);

        // Add all text elements and buttons to VBox newsText
        newsText.getChildren().addAll(newsItemTitle, newsDate, textFlow, buttonsContainer);

        // Add ImageView and VBox with text to main HBox
        newsContent.getChildren().addAll(newsImage, newsText);

        // Apply margins for sides: 2% on the left and 28% on the right
        HBox.setMargin(newsImage, new Insets(10, 0, 10, newsContent.getWidth() * 0.02));
        HBox.setMargin(newsText, new Insets(10, newsContent.getWidth() * 0.28, 10, 0));

        // Add HBox with news content to main VBox (NewsItem)
        getChildren().addAll(newsContent);
    }

    private void initializeConnection() {
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
            if (connectionToDataBase == null) {
                throw new SQLException("Failed to initialize database connection singleton.");
            }
        } catch (SQLException e) {
            showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            Connection connection = connectionToDataBase.getConnection();
            if (connection != null && !connection.isClosed()) {
                return connection;
            } else {
                throw new SQLException("Database connection is closed.");
            }
        } else {
            throw new SQLException("Database connection singleton is not initialized.");
        }
    }

    private String getEmployeeStatus(String managerName) {
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
            showErrorAlert("Failed to fetch employee status: " + e.getMessage());
        }
        return null;
    }

    private void showErrorAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    private Button createStyledButton(String text, String link) {
        Button button = new Button(text);
        button.setTextFill(Color.WHITE);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 1px; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        button.setOpacity(1.0);

        FadeTransition colorIn = new FadeTransition(Duration.millis(300), button);
        colorIn.setToValue(1.0);

        FadeTransition colorOut = new FadeTransition(Duration.millis(300), button);
        colorOut.setToValue(0.7);

        button.setOnMouseEntered(e -> {
            colorIn.play();
            button.setStyle("-fx-background-color: blue; -fx-border-color: white; -fx-border-width: 1px; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        });

        button.setOnMouseExited(e -> {
            colorOut.play();
            button.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 1px; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        });

        button.setOnAction(event -> openLink(link));
        return button;
    }

    private Button createBorderButton(String text) {
        Button button = new Button(text);
        button.setTextFill(Color.WHITE);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 1px; -fx-background-radius: 15px; -fx-border-radius: 15px;");
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
            button.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: white; -fx-border-radius: 15px;");
        });

        return button;
    }

    private boolean openLink(String link) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(link));
                return true;
            } else {
                System.err.println("Desktop browsing is not supported.");
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            System.err.println("Failed to open link: " + link);
        }
        return false;
    }

    private void handleEdit() {
        Stage editDialogStage = new Stage();
        editDialogStage.setTitle("Edit News");

        Label titleLabel = new Label("Title:");
        titleLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
        titleLabel.setTextFill(Color.WHITE);
        TextField titleField = new TextField();
        titleField.setText(news.getTitle());
        titleField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        titleField.setPromptText("Enter news title");

        Label contentLabel = new Label("Content:");
        contentLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
        contentLabel.setTextFill(Color.WHITE);
        TextArea contentArea = new TextArea();
        contentArea.setText(news.getContent());
        contentArea.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px; -fx-control-inner-background: black;");
        contentArea.setPromptText("Enter news content");

        Label imageUrlLabel = new Label("Image URL:");
        imageUrlLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
        imageUrlLabel.setTextFill(Color.WHITE);
        TextField imageUrlField = new TextField();
        imageUrlField.setText(news.getImageUrl());
        imageUrlField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        imageUrlField.setPromptText("Enter image URL");

        Label linkLabel = new Label("Link:");
        linkLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
        linkLabel.setTextFill(Color.WHITE);
        TextField linkField = new TextField();
        linkField.setText(news.getLink());
        linkField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        linkField.setPromptText("Enter news link");

        Button saveButton = createBorderButton(" Save ");
        saveButton.setOnAction(event -> {
            String newTitle = titleField.getText();
            String newContent = contentArea.getText();
            String newImageUrl = imageUrlField.getText();
            String newLink = linkField.getText();

            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            updateNewsInDatabase(news.getNewsId(), newTitle, newContent, newImageUrl, newLink, currentDate);

            editDialogStage.close();
        });
        saveButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15px; -fx-border-radius: 15px;");

        Button cancelButton = createBorderButton(" Cancel ");
        cancelButton.setOnAction(event -> editDialogStage.close());
        cancelButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15px; -fx-border-radius: 15px;");

        HBox buttonsContainer = new HBox(10);
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.setPadding(new Insets(20, 0, 0, 0));
        buttonsContainer.getChildren().addAll(saveButton, cancelButton);

        VBox dialogLayout = new VBox(10);
        dialogLayout.setPadding(new Insets(10));
        dialogLayout.setAlignment(Pos.CENTER);
        dialogLayout.setStyle("-fx-background-color: black;");
        dialogLayout.getChildren().addAll(titleLabel, titleField, contentLabel, contentArea, imageUrlLabel, imageUrlField, linkLabel, linkField, buttonsContainer);

        Scene editDialogScene = new Scene(dialogLayout, 400, 600);
        editDialogScene.setFill(Color.BLACK);
        editDialogStage.setScene(editDialogScene);
        editDialogStage.show();
    }

    private void updateNewsInDatabase(int newsId, String title, String content, String imageUrl, String link, String date) {
        if (title.isEmpty() || content.isEmpty() || link.isEmpty() || imageUrl.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill in all fields.");
            return;
        }

        String errorMessage = "";

        if (title.length() > 50) {
            errorMessage += "Title exceeds maximum length of 50 characters.\n";
        }

        if (content.length() > 255) {
            errorMessage += "Content exceeds maximum length of 255 characters.\n";
        }

        if (link.length() > 5555) {
            errorMessage += "Link exceeds maximum length of 5555 characters.\n";
        }

        if (imageUrl.length() > 5555) {
            errorMessage += "Image URL exceeds maximum length of 5555 characters.\n";
        }

        if (!errorMessage.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", errorMessage);
            return;
        }

        try (Connection connection = establishDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE news SET news_title=?, news_content=?, news_photo=?, news_link=?, news_publication_date=? WHERE news_id=?")) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, content);
            preparedStatement.setString(3, imageUrl);
            preparedStatement.setString(4, link);
            preparedStatement.setString(5, date);
            preparedStatement.setInt(6, newsId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "News updated successfully in the database.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update news in the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this news item?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteNewsFromDatabase(news.getNewsId());
        }
    }

    private void deleteNewsFromDatabase(int newsId) {
        try (Connection connection = establishDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM news WHERE news_id = ?")) {
            preparedStatement.setInt(1, newsId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                System.out.println("News deleted successfully from the database.");
                getChildren().clear();
            } else {
                System.out.println("Failed to delete news from the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}