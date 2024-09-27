import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditSuppliersProfile {

    private final SessionManager sessionManager;
    private final FirstConnectionToDataBase connectionToDataBase;

    public EditSuppliersProfile() {
        sessionManager = SessionManager.getInstance();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            showErrorAlert("Failed to establish database connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void showEditSupplierWindow(Supplier supplier) {

        Stage editSupplierStage = new Stage();
        editSupplierStage.setTitle("Edit Supplier");
        editSupplierStage.initModality(Modality.APPLICATION_MODAL);

        Label titleLabel = new Label("Title:");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

        TextField titleField = new TextField(supplier.getTitle());
        titleField.setPromptText("Supplier's Title");
        titleField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

        Label descriptionLabel = new Label("Description:");
        descriptionLabel.setTextFill(Color.WHITE);
        descriptionLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

        TextField descriptionField = new TextField(supplier.getDescription());
        descriptionField.setPromptText("Supplier's Description");
        descriptionField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

        Label specializationLabel = new Label("Specialization:");
        specializationLabel.setTextFill(Color.WHITE);
        specializationLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

        ComboBox<String> specializationComboBox = new ComboBox<>();
        specializationComboBox.setStyle("-fx-background-color: black; -fx-control-inner-background: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        specializationComboBox.setEditable(true);
        specializationComboBox.setPromptText("Supplier's Specialization");
        specializationComboBox.setValue(supplier.getSpecialization());
        specializationComboBox.getItems().addAll( "ALL", "NVIDIA", "AMD", "ASUS", "Gigabyte", "MSI", "EVGA", "Palit", "XFX", "PowerColor", "Inno3D", "GALAX");
        specializationComboBox.getEditor().setStyle("-fx-text-fill: white;");

        Label typeLabel = new Label("Supplier Type:");
        typeLabel.setTextFill(Color.WHITE);
        typeLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.setStyle("-fx-background-color: black; -fx-control-inner-background: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        typeComboBox.setPromptText("Supplier's Segment");
        typeComboBox.setValue(supplier.getType());
        typeComboBox.getItems().addAll("Manufacturer", "Wholesaler", "Distributor");
        typeComboBox.getEditor().setStyle("-fx-text-fill: white;");

        Label statusLabel = new Label("Status:");
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        statusComboBox.setValue(supplier.getStatus());
        statusComboBox.getItems().addAll("Active", "Inactive", "Suspended", "Under Review", "Approved", "Pending", "Blocked");
        statusComboBox.setPromptText("Supplier's Status");
        statusComboBox.getEditor().setStyle("-fx-text-fill: white;");

        Label contactNumberLabel = new Label("Contact Number:");
        contactNumberLabel.setTextFill(Color.WHITE);
        contactNumberLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

        TextField contactNumberField = new TextField(supplier.getContactNumber());
        contactNumberField.setPromptText("Supplier's Contact Number");
        contactNumberField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

        Label websiteLabel = new Label("Website:");
        websiteLabel.setTextFill(Color.WHITE);
        websiteLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

        TextField websiteField = new TextField(supplier.getWebsite());
        websiteField.setPromptText("Supplier's Website");
        websiteField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

        VBox fieldsBox = new VBox(10);
        fieldsBox.getChildren().addAll(
                titleLabel, titleField,
                descriptionLabel, descriptionField,
                specializationLabel, specializationComboBox,
                typeLabel, typeComboBox,
                statusLabel, statusComboBox,
                contactNumberLabel, contactNumberField,
                websiteLabel, websiteField
        );
        fieldsBox.setAlignment(Pos.TOP_CENTER);

        Button saveButton = borderStyledButton("Save Changes");
        saveButton.setOnAction(event -> {
            String newTitle = titleField.getText().trim();
            String type = typeComboBox.getValue();
            String description = descriptionField.getText().trim();
            String specialization = specializationComboBox.getValue();
            String status = statusComboBox.getValue();
            String contactNumber = contactNumberField.getText().trim();
            String website = websiteField.getText().trim();

            if (newTitle.isEmpty() || type.isEmpty() || description.isEmpty() || specialization.isEmpty() || status.isEmpty() || contactNumber.isEmpty() || website.isEmpty()) {
                showErrorAlert("All fields are required.");
                return;
            }

            if (!isValidURL(website)) {
                showErrorAlert("Invalid website URL.");
                return;
            }

            Supplier editedSupplier = new Supplier(supplier.getId(), newTitle, type, description, specialization, status, contactNumber, website);
            updateSupplier(editedSupplier);
            editSupplierStage.close();
        });

        Button cancelButton = borderStyledButton("Cancel");
        cancelButton.setOnAction(event -> editSupplierStage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(50, 0, 0, 0));

        VBox layout = new VBox(10);
        layout.getChildren().addAll(fieldsBox, buttonBox);
        layout.setStyle("-fx-background-color: black;");
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 600);
        scene.setFill(Color.BLACK);
        editSupplierStage.setScene(scene);
        editSupplierStage.show();
    }

    private void updateSupplier(Supplier supplier) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = establishDBConnection();

            String query = "UPDATE suppliers SET suppliers_title = ?, supplier_type = ?, suppliers_description = ?, suppliers_specialization = ?, suppliers_status = ?, suppliers_contact_number = ? WHERE suppliers_id = ?";

            statement = connection.prepareStatement(query);
            statement.setString(1, supplier.getTitle());
            statement.setString(2, supplier.getType());
            statement.setString(3, supplier.getDescription());
            statement.setString(4, supplier.getSpecialization());
            statement.setString(5, supplier.getStatus());
            statement.setString(6, supplier.getContactNumber());
            statement.setInt(7, supplier.getId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                showSuccessAlert("Supplier details updated successfully!");
            } else {
                showErrorAlert("No supplier found with the provided ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error: Unable to update supplier details.");
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Button borderStyledButton(String text) {
        Button button = new Button(text);
        button.setTextFill(javafx.scene.paint.Color.WHITE);
        button.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 15));
        button.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 2px; -fx-background-radius: 15px; -fx-border-radius: 15px;");
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
            button.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: white; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        });

        return button;
    }

    private boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    private void showErrorAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    private void showSuccessAlert(String successMessage) {
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText(null);
        successAlert.setContentText(successMessage);
        successAlert.showAndWait();
    }
}
