import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteOrEditDescriptionItem {

    private int explanationId;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public DeleteOrEditDescriptionItem(int explanationId) {
        this.explanationId = explanationId;
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void handleEditExplanation(int explanationId) {
        try {
            Stage editDialogStage = new Stage();
            editDialogStage.setTitle("Edit Explanation");

            Label titleLabel = new Label("Title:");
            titleLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
            titleLabel.setTextFill(Color.WHITE);
            TextField titleField = new TextField();
            titleField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
            titleField.setPromptText("Enter explanation title");

            Label descriptionLabel = new Label("Description:");
            descriptionLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
            descriptionLabel.setTextFill(Color.WHITE);
            TextArea descriptionArea = new TextArea();
            descriptionArea.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px; -fx-control-inner-background: black;");
            descriptionArea.setPromptText("Enter explanation description");

            Label iconUrlLabel = new Label("Icon URL:");
            iconUrlLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
            iconUrlLabel.setTextFill(Color.WHITE);
            TextField iconUrlField = new TextField();
            iconUrlField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
            iconUrlField.setPromptText("Enter icon URL");

            Button saveButton = ButtonStyle.expandPaneStyledButton(" Save ");
            saveButton.setOnAction(event -> {
                String newTitle = titleField.getText();
                String newDescription = descriptionArea.getText();
                String newIconUrl = iconUrlField.getText();

                StringBuilder errorMessage = new StringBuilder();

                if (newTitle.length() > 45) {
                    errorMessage.append("Title exceeds maximum length of 45 characters.\n");
                }

                if (newDescription.length() > 5555) {
                    errorMessage.append("Description exceeds maximum length of 5555 characters.\n");
                }

                if (newIconUrl.length() > 5555) {
                    errorMessage.append("Icon URL exceeds maximum length of 5555 characters.\n");
                }

                if (!errorMessage.isEmpty()) {
                    alertService.showErrorAlert(errorMessage.toString());
                    return;
                }

                updateExplanationInDatabase(explanationId, newTitle, newDescription, newIconUrl);
                editDialogStage.close();
            });

            Button cancelButton = ButtonStyle.expandPaneStyledButton(" Cancel ");
            cancelButton.setOnAction(event -> editDialogStage.close());

            HBox buttonsContainer = new HBox(10);
            buttonsContainer.setAlignment(Pos.CENTER);
            buttonsContainer.setPadding(new Insets(20, 0, 0, 0));
            buttonsContainer.getChildren().addAll(saveButton, cancelButton);

            VBox dialogLayout = new VBox(10);
            dialogLayout.setPadding(new Insets(10));
            dialogLayout.setAlignment(Pos.CENTER);
            dialogLayout.setStyle("-fx-background-color: black;");
            dialogLayout.getChildren().addAll(titleLabel, titleField, descriptionLabel, descriptionArea, iconUrlLabel, iconUrlField, buttonsContainer);

            Scene editDialogScene = new Scene(dialogLayout, 400, 400);
            editDialogScene.setFill(Color.BLACK);
            editDialogStage.setScene(editDialogScene);
            editDialogStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateExplanationInDatabase(int explanationId, String title, String description, String iconUrl) {
        try (Connection connection = establishDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE explanation SET explanation_title=?, explanation_description=?, explanation_icon=? WHERE explanation_id=?")) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, iconUrl);
            preparedStatement.setInt(4, explanationId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                alertService.showSuccessAlert("Explanation updated successfully in the database.");
            } else {
                alertService.showErrorAlert("Failed to update explanation in the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleDeleteExplanation(int explanationId) {
        String message = "Are you sure you want to delete this explanation? This action cannot be undone.";
        if (alertService.showConfirmationAlert(message)) {
            deleteExplanationFromDatabase(explanationId);
        }
    }

    private void deleteExplanationFromDatabase(int explanationId) {
        try (Connection connection = establishDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM explanation WHERE explanation_id = ?")) {
            preparedStatement.setInt(1, explanationId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                alertService.showSuccessAlert("Explanation deleted successfully from the database.");
            } else {
                alertService.showErrorAlert("Failed to delete explanation from the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
