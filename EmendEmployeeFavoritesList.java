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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EmendEmployeeFavoritesList {

    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private int managerId;
    private List<Integer> favoriteProductIds;
    private VBox favoritesContent;

    public EmendEmployeeFavoritesList(int managerId, List<Integer> favoriteProductIds) {
        this.managerId = managerId;
        this.favoriteProductIds = favoriteProductIds;
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    public boolean confirmDelete(int productId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Delete Product");
        alert.setContentText("Are you sure you want to remove this product from favorites?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void removeProductFromFavorites(int productId) {
        try (Connection connection = connectionToDataBase.getConnection()) {
            String deleteSql = "DELETE FROM employee_favorites WHERE manager_id = ? AND product_id = ?";
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                deleteStatement.setInt(1, managerId);
                deleteStatement.setInt(2, productId);
                int rowsAffected = deleteStatement.executeUpdate();
                if (rowsAffected > 0) {
                    favoriteProductIds.remove(Integer.valueOf(productId));
                    alertService.showSuccessAlert("Product removed from favorites successfully.");
                    populateFavoritesContent();
                } else {
                    alertService.showErrorAlert("Product ID not found in favorites.");
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error removing product from favorites: " + e.getMessage());
        }
    }

    private void populateFavoritesContent() {
        favoritesContent.getChildren().clear();
        for (int productId : favoriteProductIds) {
            try {
                Connection connection = connectionToDataBase.getConnection();
                String productSql = "SELECT product_title FROM catalog WHERE product_id = ?";
                PreparedStatement productStatement = connection.prepareStatement(productSql);
                productStatement.setInt(1, productId);
                ResultSet resultSet = productStatement.executeQuery();

                if (resultSet.next()) {
                    String productName = resultSet.getString("product_title");
                    Label productLabel = new Label(productName);
                    productLabel.setStyle("-fx-font-size: 14px;");
                    Button removeButton = new Button(" - ");
                    removeButton.setOnAction(e -> removeProductFromFavorites(productId));
                    VBox productBox = new VBox(5, productLabel, removeButton);
                    favoritesContent.getChildren().add(productBox);
                }
                connection.close();
            } catch (SQLException e) {
                alertService.showErrorAlert("Error fetching product details: " + e.getMessage());
            }
        }
    }

    public void showEditFavoritesDialog(String managerName) {
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
                int productId = Integer.parseInt(productIdText);
                removeProductFromFavorites(productId);
            } else {
                alertService.showErrorAlert("Please enter Product ID.");
            }
        });

        Label userLabel = new Label("Removing product from favorites for user: " + managerName);
        gridPane.add(userLabel, 0, 0, 2, 1);

        gridPane.add(new Label("Product ID:"), 0, 1);
        gridPane.add(productIdField, 1, 1);
        gridPane.add(removeButton, 1, 2);

        Scene scene = new Scene(gridPane, 600, 150);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
}
