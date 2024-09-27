import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EmendClientFavoritesList {

    private final String clientName;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private int clientId;
    private List<Integer> favoriteProductIds;
    private UserFavoritesPage userFavoritesPage;
    private VBox favoritesContent;  // Reference to favoritesContent

    public EmendClientFavoritesList(int clientId, String clientName, UserFavoritesPage userFavoritesPage, VBox favoritesContent) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.userFavoritesPage = userFavoritesPage;
        this.favoritesContent = favoritesContent;  // Set the field
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public boolean confirmDelete(int productId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Delete Product");
        alert.setContentText("Are you sure you want to remove this product from favorites?");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void removeProductFromFavorites(int productId, String username) {
        try (Connection connection = establishDBConnection()) {
            String deleteSql = "DELETE FROM user_favorites WHERE user_id = (SELECT id FROM users WHERE user_name = ?) AND product_id = ?";
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                deleteStatement.setString(1, username);
                deleteStatement.setInt(2, productId);
                int rowsAffected = deleteStatement.executeUpdate();
                if (rowsAffected > 0) {
                    alertService.showSuccessAlert("Product removed from favorites successfully.");
                } else {
                    alertService.showErrorAlert("Failed to remove product from favorites.");
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error removing product from favorites: " + e.getMessage());
        }

        favoritesContent.getChildren().setAll(userFavoritesPage.createFavoritesLabels(clientName));
    }

    public void showEditFavoritesDialog(String clientName) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setTitle("Edit Favorites");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 20, 20));

        TextField productIdField = new TextField();
        productIdField.setPromptText("Enter Product ID");

        Button removeButton = new Button("Remove from Favorites");
        removeButton.setOnAction(event -> {
            String productIdText = productIdField.getText();
            if (!productIdText.isEmpty()) {
                try {
                    int productId = Integer.parseInt(productIdText);
                    removeProductFromFavorites(productId, clientName);
                } catch (NumberFormatException e) {
                    alertService.showErrorAlert("Invalid Product ID format.");
                }
            } else {
                alertService.showErrorAlert("Please enter Product ID.");
            }
        });

        Label userLabel = new Label("Removing product from favorites for user: " + clientName);
        gridPane.add(userLabel, 0, 0, 2, 1);

        gridPane.add(new Label("Product ID:"), 0, 1);
        gridPane.add(productIdField, 1, 1);
        gridPane.add(removeButton, 1, 2);

        Scene scene = new Scene(gridPane, 600, 150);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
}
