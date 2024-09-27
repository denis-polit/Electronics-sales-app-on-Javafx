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
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ReviewEditWindow {

    private final AlertService alertService;
    private final ButtonStyle buttonStyle;
    private final Connection connection;
    private final ReviewsPage reviewsPage;

    public ReviewEditWindow(AlertService alertService, ButtonStyle buttonStyle, Connection connection, ReviewsPage reviewsPage) {
        this.alertService = alertService;
        this.buttonStyle = buttonStyle;
        this.connection = connection;
        this.reviewsPage = reviewsPage;  // Initialize ReviewsPage
    }

    public void editReview(Review review) {
        Stage editDialogStage = createDialogStage();

        VBox dialogLayout = createDialogLayout(review, editDialogStage);

        Scene editDialogScene = new Scene(dialogLayout, 400, 300);
        editDialogScene.setFill(Color.BLACK);
        editDialogStage.setScene(editDialogScene);
        editDialogStage.show();
    }

    private Stage createDialogStage() {
        Stage stage = new Stage();
        stage.setTitle("Edit Comment");
        return stage;
    }

    private VBox createDialogLayout(Review review, Stage editDialogStage) {
        Label contentLabel = createLabel("Content:");
        TextArea contentTextArea = createContentTextArea(review);

        Label ratingLabel = createLabel("Rating:");
        ComboBox<Integer> ratingComboBox = createRatingComboBox(review);

        Button saveButton = buttonStyle.expandPaneStyledButton("    Save    ");
        saveButton.setOnAction(e -> handleSaveAction(review, contentTextArea, ratingComboBox, editDialogStage));

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(contentLabel, contentTextArea, ratingLabel, ratingComboBox, saveButton);

        return layout;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
        return label;
    }

    private TextArea createContentTextArea(Review review) {
        TextArea textArea = new TextArea(review.getContent());
        textArea.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        return textArea;
    }

    private ComboBox<Integer> createRatingComboBox(Review review) {
        ComboBox<Integer> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(1, 2, 3, 4, 5);
        comboBox.getSelectionModel().select(review.getRating());
        comboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        return comboBox;
    }

    private void handleSaveAction(Review review, TextArea contentTextArea, ComboBox<Integer> ratingComboBox, Stage editDialogStage) {
        String content = contentTextArea.getText().trim();
        int rating = ratingComboBox.getValue();

        if (content.isEmpty()) {
            alertService.showErrorAlert("Comment cannot be empty. Please enter the content of the comment.");
        } else {
            review.setContent(content);
            review.setRating(rating);
            review.setLastModified(Timestamp.valueOf(LocalDateTime.now()));  // Set last modified to the current timestamp

            if (updateReview(review)) {
                editDialogStage.close();
                reviewsPage.updateDisplayedReviews();  // Call update on the instance
                alertService.showSuccessAlert("Comment updated successfully.");
            } else {
                alertService.showErrorAlert("Error updating comment in database.");
            }
        }
    }

    private boolean updateReview(Review review) {
        String query = "UPDATE reviews SET review_content = ?, review_grade = ?, last_modified_date = ? WHERE id_review = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, review.getContent());
            preparedStatement.setInt(2, review.getRating());

            Timestamp lastModifiedTimestamp = new Timestamp(review.getLastModified().getTime());
            preparedStatement.setTimestamp(3, lastModifiedTimestamp);

            preparedStatement.setInt(4, review.getId());

            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}