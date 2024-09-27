import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReviewComplaintWindow {

    private final Stage ownerStage;
    private final AlertService alertService;
    private final FirstConnectionToDataBase connectionToDataBase;
    private final int userId;
    private final Integer employeeId;

    public ReviewComplaintWindow(Stage ownerStage, AlertService alertService, FirstConnectionToDataBase connectionToDataBase, int userId, Integer employeeId) {
        this.ownerStage = ownerStage;
        this.alertService = alertService;
        this.connectionToDataBase = connectionToDataBase;
        this.userId = userId;
        this.employeeId = employeeId;
    }

    public void show(int commentId) {
        SessionManager sessionManager = SessionManager.getInstance();

        if (!sessionManager.isClientEnter() && !sessionManager.isManagerEnter()) {
            alertService.showErrorAlert("Please log in before submitting a complaint.");
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Complain");
        dialogStage.initOwner(ownerStage);

        ComboBox<String> complaintComboBox = createComplaintComboBox();
        Button confirmButton = ButtonStyle.expandPaneStyledButton("Confirm");
        confirmButton.setOnAction(event -> handleConfirmAction(dialogStage, complaintComboBox, commentId));

        VBox dialogLayout = createDialogLayout(complaintComboBox, confirmButton);

        Scene dialogScene = new Scene(dialogLayout, 300, 200);
        dialogScene.setFill(Color.BLACK);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    private ComboBox<String> createComplaintComboBox() {
        ComboBox<String> complaintComboBox = new ComboBox<>();
        complaintComboBox.getItems().addAll("Insults", "Content 18+", "Spam", "Other");
        complaintComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        complaintComboBox.getEditor().setStyle("-fx-text-fill: white;");
        return complaintComboBox;
    }

    private void handleConfirmAction(Stage dialogStage, ComboBox<String> complaintComboBox, int commentId) {
        if (complaintComboBox.getValue() != null && !complaintComboBox.getValue().isEmpty()) {
            String complaintType = complaintComboBox.getValue();
            dialogStage.close();
            try {
                Connection connection = establishDBConnection();
                if (saveComplaint(connection, commentId, userId, employeeId, complaintType)) {
                    alertService.showSuccessAlert("Your complaint has been sent successfully.");
                } else {
                    alertService.showErrorAlert("Failed to send your complaint.");
                }
            } catch (SQLException e) {
                alertService.showErrorAlert("Database error: " + e.getMessage());
            }
        } else {
            alertService.showErrorAlert("Please select a complaint type.");
        }
    }

    private Connection establishDBConnection() throws SQLException {
        Connection connection = connectionToDataBase.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is null or closed.");
        }
        return connection;
    }

    private boolean saveComplaint(Connection connection, int commentId, int userId, Integer employeeId, String complaintType) {
        String query = "INSERT INTO review_complaints (comment_id, user_id, employee_id, complaint_type) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, commentId);
            preparedStatement.setInt(2, userId);
            if (employeeId != null) {
                preparedStatement.setInt(3, employeeId);
            } else {
                preparedStatement.setNull(3, java.sql.Types.INTEGER);
            }
            preparedStatement.setString(4, complaintType);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private VBox createDialogLayout(ComboBox<String> complaintComboBox, Button confirmButton) {
        VBox dialogLayout = new VBox(10);
        dialogLayout.setPadding(new Insets(10, 10, 10, 10));
        dialogLayout.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        dialogLayout.setAlignment(Pos.CENTER);
        dialogLayout.getChildren().addAll(complaintComboBox, confirmButton);
        return dialogLayout;
    }
}