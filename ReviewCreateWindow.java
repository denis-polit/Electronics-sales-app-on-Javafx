import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReviewCreateWindow {

    private final SessionManager sessionManager;
    private final FirstConnectionToDataBase connectionToDataBase;
    private final AlertService alertService;
    private final ReviewsPage reviewsPage;

    public ReviewCreateWindow(SessionManager sessionManager, FirstConnectionToDataBase connectionToDataBase, AlertService alertService, ReviewsPage reviewsPage) {
        this.sessionManager = sessionManager;
        this.connectionToDataBase = connectionToDataBase;
        this.alertService = alertService;
        this.reviewsPage = reviewsPage;
    }

    public void createCommentDialog() {
        if (!sessionManager.isClientEnter() && !sessionManager.isManagerEnter()) {
            alertService.showErrorAlert("Please log in to your account to add a review.");
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add a comment");
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        Label contentLabel = new Label("Content:");
        contentLabel.setTextFill(Color.WHITE);
        contentLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
        TextArea contentTextArea = new TextArea();
        contentTextArea.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

        Label ratingLabel = new Label("Rating:");
        ratingLabel.setTextFill(Color.WHITE);
        ratingLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
        ComboBox<Integer> ratingComboBox = new ComboBox<>();
        ratingComboBox.getItems().addAll(1, 2, 3, 4, 5);
        ratingComboBox.getSelectionModel().selectFirst();
        ratingComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        ratingComboBox.getEditor().setStyle("-fx-text-fill: white;");

        Button addButton = ButtonStyle.expandPaneStyledButton("    Add    ");
        addButton.setOnAction(e -> {
            String content = contentTextArea.getText().trim();
            int rating = ratingComboBox.getValue();

            if (content.isEmpty()) {
                alertService.showErrorAlert("Review cannot be empty. Please enter review content.");
            } else {
                String creatorName;
                String creatorStatus;
                int creatorId;

                if (sessionManager.isManagerEnter()) {
                    creatorName = sessionManager.getCurrentManagerName();
                    creatorStatus = "Manager";
                    creatorId = sessionManager.getEmployeeIdByName(creatorName);
                } else {
                    creatorName = sessionManager.getCurrentClientName();
                    creatorStatus = "Client";
                    creatorId = sessionManager.getClientIdByName(creatorName);
                }

                Review review = new Review(0, creatorId, creatorStatus, content, rating, new Date(), new Date());
                addReview(review);

                dialogStage.close();
                reviewsPage.updateDisplayedReviews();
            }
        });

        VBox dialogLayout = new VBox(10);
        dialogLayout.setPadding(new Insets(10, 10, 10, 10));
        dialogLayout.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        dialogLayout.setAlignment(Pos.CENTER);
        dialogLayout.getChildren().addAll(contentLabel, contentTextArea, ratingLabel, ratingComboBox, addButton);

        Scene dialogScene = new Scene(dialogLayout, 400, 400);
        dialogScene.setFill(Color.BLACK);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    public void addReview(Review review) {
        String query = "INSERT INTO reviews (creator_id, creator_status, review_content, review_grade, review_date, last_modified_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = establishDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, review.getAuthorId());
            preparedStatement.setString(2, review.getStatus());
            preparedStatement.setString(3, review.getContent());
            preparedStatement.setString(4, String.valueOf(review.getRating())); // Changed to String since review_grade is varchar
            preparedStatement.setString(5, new SimpleDateFormat("yyyy-MM-dd").format(review.getDate())); // Formatting date as String
            preparedStatement.setString(6, new SimpleDateFormat("yyyy-MM-dd").format(new Date())); // Using current date for last_modified_date

            preparedStatement.executeUpdate();
            alertService.showSuccessAlert("Review added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error adding review to the database.");
        }
    }

    private Connection establishDBConnection() throws SQLException {
        Connection connection = connectionToDataBase.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is null or closed.");
        }
        return connection;
    }
}