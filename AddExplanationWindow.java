import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.CustomTextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddExplanationWindow extends Application {

    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();  // Использование AlertServiceImpl

    private HBox createButtonBar(Button leftButton, Button rightButton) {
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.getChildren().addAll(leftButton, rightButton);
        return buttonBar;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        CustomTextField titleTextField = new CustomTextField();
        titleTextField.setPromptText("Enter the title of the service");
        titleTextField.setTooltip(new Tooltip("Enter the title of the service"));
        titleTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        CustomTextField descriptionTextField = new CustomTextField();
        descriptionTextField.setPromptText("Enter the description of the service");
        descriptionTextField.setTooltip(new Tooltip("Enter the description of the service"));
        descriptionTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        CustomTextField iconUrlTextField = new CustomTextField();
        iconUrlTextField.setPromptText("Enter the URL of the icon for the service");
        iconUrlTextField.setTooltip(new Tooltip("Enter the URL of the icon for the service"));
        iconUrlTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        Button saveButton = ButtonStyle.expandPaneStyledButton("Save");
        saveButton.setOnAction(event -> {
            try {
                // Подтверждение перед сохранением
                boolean confirm = alertService.showConfirmationAlert("Are you sure you want to save this explanation?");
                if (!confirm) {
                    return;  // Отменить сохранение, если не подтверждено
                }

                saveExplanation(titleTextField.getText(), descriptionTextField.getText(), iconUrlTextField.getText());
                primaryStage.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        Button cancelButton = ButtonStyle.expandPaneStyledButton("Cancel");
        cancelButton.setOnAction(event -> primaryStage.close());

        HBox buttonBar = createButtonBar(saveButton, cancelButton);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: black;");
        layout.getChildren().addAll(
                titleTextField, descriptionTextField, iconUrlTextField,
                buttonBar
        );

        Scene scene = new Scene(layout, 600, 250);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Add Service");
        primaryStage.show();

        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    private void saveExplanation(String title, String description, String iconUrl) throws SQLException {
        if (title.isEmpty() || description.isEmpty() || iconUrl.isEmpty()) {
            alertService.showErrorAlert("Please fill in all fields.");
            return;
        }

        Connection connection = establishDBConnection();
        if (connection != null) {
            String errorMessage = "";

            if (title.length() > 45) {
                errorMessage += "Title exceeds maximum length of 45 characters.\n";
            }

            if (description.length() > 5555) {
                errorMessage += "Description exceeds maximum length of 5555 characters.\n";
            }

            if (iconUrl.length() > 5555) {
                errorMessage += "Icon URL exceeds maximum length of 5555 characters.\n";
            }

            if (!errorMessage.isEmpty()) {
                alertService.showErrorAlert(errorMessage);
                return;
            }

            String query = "INSERT INTO explanation (explanation_title, explanation_description, explanation_icon) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, title);
                statement.setString(2, description);
                statement.setString(3, iconUrl);
                statement.executeUpdate();
            }
            connection.close();

            alertService.showSuccessAlert("Explanation saved successfully.");  // Использование AlertService для успеха
        } else {
            alertService.showErrorAlert("Failed to establish database connection.");  // Использование AlertService для ошибки
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}