import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

public class BlockWindow {

    private String clientName;
    private String clientEmail;
    private String clientContacts;
    private final AlertServiceImpl alertService;

    public BlockWindow(String clientName, String clientEmail, String clientContacts) {
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.clientContacts = clientContacts;
        alertService = new AlertServiceImpl();
    }

    public void showBlockDialog() {
        Stage blockDialogStage = new Stage();
        blockDialogStage.setTitle("Block Client");

        Label nameLabel = new Label("Client Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter client name");

        Label emailLabel = new Label("Client Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter client email");

        Label contactLabel = new Label("Client Contact:");
        TextField contactField = new TextField();
        contactField.setPromptText("Enter client contact");

        Button proceedButton = ButtonStyle.createStyledButton("Proceed");
        proceedButton.setOnAction(event -> {
            String clientName = nameField.getText();
            String clientEmail = emailField.getText();
            String clientContact = contactField.getText();

            if (clientName.isEmpty() || clientEmail.isEmpty() || clientContact.isEmpty()) {
                new AlertServiceImpl().showErrorAlert("All fields must be filled.");
            } else {
                BlockWindow blockWindow = new BlockWindow(clientName, clientEmail, clientContact);
                blockWindow.show();
                blockDialogStage.close();
            }
        });

        Button cancelButton = ButtonStyle.createStyledButton("Cancel");
        cancelButton.setOnAction(event -> blockDialogStage.close());

        VBox inputLayout = new VBox(10);
        inputLayout.setPadding(new Insets(10));
        inputLayout.getChildren().addAll(nameLabel, nameField, emailLabel, emailField, contactLabel, contactField, proceedButton, cancelButton);
        inputLayout.setAlignment(Pos.CENTER);
        inputLayout.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        Scene dialogScene = new Scene(inputLayout, 400, 300);
        blockDialogStage.setScene(dialogScene);
        blockDialogStage.show();
    }

    public void show() {
        Stage blockStage = new Stage();
        blockStage.setTitle("Block Client");

        Label clientNameLabel = new Label("Client: " + clientName);

        ComboBox<String> reasonComboBox = new ComboBox<>();
        reasonComboBox.getItems().addAll("Spam", "Abuse", "Fraud", "Other");
        reasonComboBox.setPromptText("Select reason");

        Button blockButton =  ButtonStyle.createStyledButton("Block");
        blockButton.setOnAction(event -> {
            String reason = reasonComboBox.getValue();
            if (reason != null) {
                saveBlockedData(clientName, clientEmail, clientContacts, reason);
                blockStage.close();
            } else {
                alertService.showErrorAlert("Please select a reason for blocking.");
            }
        });

        Button cancelButton = ButtonStyle.createStyledButton("Cancel");
        cancelButton.setOnAction(event -> blockStage.close());

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));
        dialogVBox.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.getChildren().addAll(clientNameLabel, reasonComboBox, blockButton, cancelButton);

        Scene dialogScene = new Scene(dialogVBox, 300, 200);
        blockStage.setScene(dialogScene);
        blockStage.show();
    }

    private void saveBlockedData(String name, String email, String phoneNumber, String reason) {
        try (Connection connection = establishDBConnection()) {
            String sql = "INSERT INTO blocked_data (phone_number, email, name, reason) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, phoneNumber);
                statement.setString(2, email);
                statement.setString(3, name);
                statement.setString(4, reason);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error saving blocked data: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return FirstConnectionToDataBase.getInstance().getConnection();
    }
}