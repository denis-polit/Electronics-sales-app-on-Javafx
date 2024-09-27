import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeleteClientAccount {

    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void showDeleteWindow() {
        Stage deleteStage = new Stage();
        deleteStage.initModality(Modality.APPLICATION_MODAL);
        deleteStage.setTitle("Delete User");

        VBox root = new VBox(10);
        root.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        root.setPadding(new Insets(10));

        Label instructionLabel = new Label("Enter the user name:");
        instructionLabel.setTextFill(Color.WHITE);
        instructionLabel.setStyle("-fx-font-family: 'Gotham'; -fx-font-size: 16px; -fx-text-fill: white;");

        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter user name");
        userNameField.setStyle("-fx-font-family: 'Gotham'; -fx-font-size: 14px; -fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        Tooltip userNameTooltip = new Tooltip("Enter the name of the user you want to delete");
        userNameField.setTooltip(userNameTooltip);

        CheckBox deleteCommentsCheckBox = new CheckBox("Delete user comments");
        deleteCommentsCheckBox.setTextFill(Color.WHITE);

        Button deleteButton = ButtonStyle.expandPaneStyledButton("Delete");
        Button cancelButton = ButtonStyle.expandPaneStyledButton("Cancel");

        deleteButton.setOnAction(event -> {
            String userName = userNameField.getText();
            boolean deleteComments = deleteCommentsCheckBox.isSelected();
            if (userExists(userName)) {
                deleteUser(userName, deleteComments);
            } else {
                alertService.showErrorAlert("User '" + userName + "' does not exist.");
            }
            deleteStage.close();
        });

        cancelButton.setOnAction(event -> deleteStage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(deleteButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(
                instructionLabel,
                userNameField,
                deleteCommentsCheckBox,
                buttonBox
        );

        Scene scene = new Scene(root, 400, 400);
        scene.setFill(Color.BLACK);
        deleteStage.setScene(scene);

        deleteStage.showAndWait();
    }

    private boolean userExists(String userName) {
        String query = "SELECT * FROM users WHERE user_name = ?";
        try (Connection connection = establishDBConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void deleteUser(String userName, boolean deleteComments) {
        String deleteUserQuery = "DROP USER ?";
        String deleteContractsQuery = "DELETE FROM contracts WHERE manager_id = ?";
        String deleteCommentsQuery = "DELETE FROM reviews WHERE creator_name = ?";

        try (Connection connection = establishDBConnection();
             PreparedStatement deleteUserStatement = connection.prepareStatement(deleteUserQuery);
             PreparedStatement deleteContractsStatement = connection.prepareStatement(deleteContractsQuery);
             PreparedStatement deleteCommentsStatement = connection.prepareStatement(deleteCommentsQuery)) {

            deleteUserStatement.setString(1, userName);
            deleteUserStatement.executeUpdate();

            deleteContractsStatement.setString(1, userName);
            deleteContractsStatement.executeUpdate();

            if (deleteComments) {
                deleteCommentsStatement.setString(1, userName);
                deleteCommentsStatement.executeUpdate();
            }

            alertService.showErrorAlert("User '" + userName + "' deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
