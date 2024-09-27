import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateSuppliersProfile {

    private final FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public CreateSuppliersProfile() {
        SessionManager sessionManager = SessionManager.getInstance();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void showAddSuppliersWindow() {
        Stage addSuppliersStage = new Stage();
        addSuppliersStage.setTitle("Add Suppliers");
        addSuppliersStage.initModality(Modality.APPLICATION_MODAL);

        Label titleLabel = new Label("Title:");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
        titleLabel.setTooltip(new Tooltip("Enter Supplier's Title"));

        TextField titleField = new TextField();
        titleField.setPromptText("Supplier's Title");
        titleField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        titleField.setTooltip(new Tooltip("Enter Supplier's Title"));

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Supplier's Description");
        descriptionField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        descriptionField.setTooltip(new Tooltip("Enter Supplier's Description"));

        Label descriptionLabel = new Label("Description:");
        descriptionLabel.setTextFill(Color.WHITE);
        descriptionLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
        descriptionLabel.setTooltip(new Tooltip("Enter Supplier's Description"));

        Label specializationLabel = new Label("Specialization:");
        specializationLabel.setTextFill(Color.WHITE);
        specializationLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
        specializationLabel.setTooltip(new Tooltip("Select Supplier's Specialization"));

        ComboBox<String> specializationComboBox = new ComboBox<>();
        specializationComboBox.setStyle("-fx-background-color: black; -fx-control-inner-background: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        specializationComboBox.setEditable(true);
        specializationComboBox.setPromptText("Supplier's Specialization");
        specializationComboBox.getItems().addAll("ALL", "NVIDIA", "AMD", "ASUS", "Gigabyte", "MSI", "EVGA", "Palit", "XFX", "PowerColor", "Inno3D", "GALAX");
        specializationComboBox.setTooltip(new Tooltip("Select Supplier's Specialization"));
        specializationComboBox.getEditor().setStyle("-fx-text-fill: white;");

        Label typeLabel = new Label("Supplier Type:");
        typeLabel.setTextFill(Color.WHITE);
        typeLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
        typeLabel.setTooltip(new Tooltip("Select Supplier's Type"));

        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.setStyle("-fx-background-color: black; -fx-control-inner-background: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        typeComboBox.setPromptText("Supplier's Segment");
        typeComboBox.getItems().addAll("Manufacturer", "Wholesaler", "Distributor");
        typeComboBox.setTooltip(new Tooltip("Select Supplier's Type"));
        typeComboBox.getEditor().setStyle("-fx-text-fill: white;");

        Label statusLabel = new Label("Status:");
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
        statusLabel.setTooltip(new Tooltip("Select Supplier's Status"));

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        statusComboBox.getItems().addAll("Under Review", "Approved", "Active");
        statusComboBox.setPromptText("Supplier's Status");
        statusComboBox.setTooltip(new Tooltip("Select Supplier's Status"));
        statusComboBox.getEditor().setStyle("-fx-text-fill: white;");

        Label contactNumberLabel = new Label("Contact Number:");
        contactNumberLabel.setTextFill(Color.WHITE);
        contactNumberLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
        contactNumberLabel.setTooltip(new Tooltip("Enter Supplier's Contact Number"));

        TextField contactNumberField = new TextField();
        contactNumberField.setPromptText("Supplier's Contact Number");
        contactNumberField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        contactNumberField.setTooltip(new Tooltip("Enter Supplier's Contact Number"));

        Label websiteLabel = new Label("Website:");
        websiteLabel.setTextFill(Color.WHITE);
        websiteLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

        TextField websiteField = new TextField();
        websiteField.setPromptText("Supplier's Website");
        websiteField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        websiteField.setTooltip(new Tooltip("Enter Supplier's Website"));

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

        Button addButton = ButtonStyle.expandPaneStyledButton("Add Supplier");
        addButton.setOnAction(event -> {

            String title = titleField.getText().trim();
            String type = typeComboBox.getValue();
            String description = descriptionField.getText().trim();
            String specialization = specializationComboBox.getValue();
            String status = statusComboBox.getValue();
            String contactNumber = contactNumberField.getText().trim();
            String website = websiteField.getText().trim();

            if (title.isEmpty() || type.isEmpty() || description.isEmpty() || specialization.isEmpty() || status.isEmpty() || contactNumber.isEmpty() || website.isEmpty()) {
                alertService.showErrorAlert("All fields are required.");
                return;
            }

            if (!isValidURL(website)) {
                alertService.showErrorAlert("Invalid website URL.");
                return;
            }

            Supplier newSupplier = new Supplier(-1, title, type, description, specialization, status, contactNumber, website);
            saveSupplier(newSupplier);

            titleField.clear();
            typeComboBox.setValue(null);
            descriptionField.clear();
            specializationComboBox.setValue(null);
            statusComboBox.setValue(null);
            contactNumberField.clear();
            websiteField.clear();
        });

        Button cancelButton = ButtonStyle.expandPaneStyledButton("Cancel");
        cancelButton.setOnAction(event -> addSuppliersStage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(20, 0, 0, 0));

        VBox layout = new VBox(10);
        layout.getChildren().addAll(fieldsBox, buttonBox);
        layout.setStyle("-fx-background-color: black;");
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 600);
        scene.setFill(Color.BLACK);
        addSuppliersStage.setScene(scene);
        addSuppliersStage.show();
    }

    private void saveSupplier(Supplier supplier) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = establishDBConnection();

            String query = "INSERT INTO suppliers (suppliers_title, supplier_type, suppliers_description, suppliers_specialization, suppliers_status, suppliers_contact_number, suppliers_website) VALUES (?, ?, ?, ?, ?, ?, ?)";

            statement = connection.prepareStatement(query);
            statement.setString(1, supplier.getTitle());
            statement.setString(2, supplier.getType());
            statement.setString(3, supplier.getDescription());
            statement.setString(4, supplier.getSpecialization());
            statement.setString(5, supplier.getStatus());
            statement.setString(6, supplier.getContactNumber());
            statement.setString(7, supplier.getWebsite());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                alertService.showSuccessAlert("A new supplier was successfully saved!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: Unable to save supplier.");
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

    private boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }
}
