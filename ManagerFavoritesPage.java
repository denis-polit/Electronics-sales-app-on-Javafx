import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManagerFavoritesPage extends Application {

    private Stage primaryStage;
    private int managerId;
    private MenuPage menuPage;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private EmendEmployeeFavoritesList emendEmployeeFavoritesList;

    public ManagerFavoritesPage(int managerId, String managerName, List<Integer> favoriteProductIds) {
        this.managerId = managerId;
        this.menuPage = new MenuPage();
        emendEmployeeFavoritesList = new EmendEmployeeFavoritesList(managerId, favoriteProductIds);
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        String managerName = SessionManager.getInstance().getCurrentManagerName();

        HeaderComponent headerComponent = new HeaderComponent();
        VBox header = headerComponent.createHeader();
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setBorder(new Border(new BorderStroke(Color.GRAY,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0))));
        root.setTop(header);

        ScrollPane scrollPane = new ScrollPane();
        VBox favoritesContent = createFavoritesContent(managerName);
        scrollPane.setContent(favoritesContent);

        root.setCenter(scrollPane);
        scrollPane.setStyle("-fx-background: black;");

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("User Favorites");
        primaryStage.show();

        getManagerNameById(managerId);

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();

        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            return connectionToDataBase.getConnection();
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    private String getManagerNameById(int managerId) {
        String managerName = null;
        try (Connection connection = establishDBConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT manager_name FROM managers WHERE id_managers = ?")) {

            statement.setInt(1, managerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    managerName = resultSet.getString("manager_name");
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error fetching manager name: " + e.getMessage());
        }
        return managerName;
    }

    public class HeaderComponent {

        private VBox createHeader() {
            VBox header = new VBox(10);
            header.setPadding(new Insets(5));
            header.setStyle("-fx-background-color: black");

            Image logoImage = new Image("file:icons/LOGO_our.jpg");
            ImageView logoImageView = new ImageView(logoImage);
            logoImageView.setFitWidth(50);
            logoImageView.setFitHeight(50);

            Circle logoCircle = new Circle(25);
            logoCircle.setFill(new ImagePattern(logoImage));
            logoCircle.setCursor(Cursor.HAND);

            Region leftRegion = new Region();
            HBox.setHgrow(leftRegion, Priority.ALWAYS);

            Button menuButton = ButtonStyle.createStyledButton("  Menu  ");
            menuButton.setOnAction(e -> showMenu());

            Button supportButton = ButtonStyle.createStyledButton("  Support  ");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = ButtonStyle.createStyledButton("  Privacy Policy  ");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button editFavoritesButton = ButtonStyle.createStyledButton("  Edit Favorites  ");
            editFavoritesButton.setOnAction(event -> emendEmployeeFavoritesList.showEditFavoritesDialog(SessionManager.getInstance().getCurrentClientName()));

            Button backButton = ButtonStyle.createStyledButton("  Back  ");
            backButton.setOnAction(event -> {
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.close();
            });

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, supportButton, privacyButton, editFavoritesButton, backButton, leftRegion);
            topContent.setAlignment(Pos.CENTER);
            topContent.setSpacing(10);

            HBox.setMargin(logoCircle, new Insets(0, 350, 0, 0));

            header.getChildren().addAll(topContent);

            return header;
        }

        private void showMenu() {
            primaryStage.close();
            Stage menuStage = new Stage();
            MenuPage menuPage = new MenuPage();
            menuPage.start(menuStage);
        }

        private void showSupportWindow() {
            SupportWindow supportWindow = new SupportWindow();
            Stage supportStage = new Stage();
            supportWindow.start(supportStage);
            supportStage.show();
        }

        private void showPrivacyPolicyWindow() {
            PrivacyPolicyWindow privacyPolicyWindow = new PrivacyPolicyWindow();
            Stage privacyStage = new Stage();
            privacyPolicyWindow.start(privacyStage);
            privacyStage.show();
        }
    }

    private VBox createFavoritesContent(String managerName) {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setStyle("-fx-background-color: black;");

        List<HBox> favoriteLabels = createFavoritesLabels(managerName);
        vbox.getChildren().addAll(favoriteLabels);

        return vbox;
    }

    private List<HBox> createFavoritesLabels(String managerName) {
        List<HBox> labels = new ArrayList<>();
        try (Connection connection = establishDBConnection()) {
            String selectSql = "SELECT c.product_id, c.product_title, c.price " +
                    "FROM catalog c " +
                    "JOIN employee_favorites ef ON c.product_id = ef.product_id " +
                    "JOIN managers m ON ef.manager_id = m.id_managers " +
                    "WHERE m.manager_name = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
                selectStatement.setString(1, managerName);
                ResultSet resultSet = selectStatement.executeQuery();
                while (resultSet.next()) {
                    int productId = resultSet.getInt("product_id");
                    String productName = resultSet.getString("product_title");
                    double productPrice = resultSet.getDouble("price");
                    Label productLabel = createBoldLabel("Product ID: " + productId + ", Name: " + productName + ", Price: " + productPrice, "");

                    Button removeButton = ButtonStyle.expandPaneStyledButton(" - ");
                    removeButton.setOnAction(event -> {
                        boolean confirm = emendEmployeeFavoritesList.confirmDelete(productId);
                        if (confirm) {
                            emendEmployeeFavoritesList.removeProductFromFavorites(productId);
                        }
                    });

                    Button detailsButton = ButtonStyle.expandPaneStyledButton("Details");
                    detailsButton.setOnAction(event -> showProductDetails(productId));

                    HBox hbox = new HBox(10);
                    hbox.getChildren().addAll(productLabel, removeButton);
                    labels.add(hbox);
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error fetching favorite products: " + e.getMessage());
        }
        return labels;
    }

    private Label createBoldLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        return label;
    }

    private void showProductDetails(int productId) {
        Stage detailStage = new Stage();
        detailStage.setTitle("Product Details");

        VBox detailVBox = new VBox(10);
        detailVBox.setAlignment(Pos.CENTER);
        detailVBox.setStyle("-fx-background-color: black; -fx-padding: 20px;");

        try (Connection connection = establishDBConnection()) {
            String selectSql = "SELECT * FROM catalog WHERE product_id = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
                selectStatement.setInt(1, productId);
                ResultSet resultSet = selectStatement.executeQuery();
                if (resultSet.next()) {
                    StringBuilder details = new StringBuilder();
                    details.append("Graphics Chip: ").append(resultSet.getString("graphics_chip")).append("\n");
                    details.append("Memory Frequency: ").append(resultSet.getString("memory_frequency")).append("\n");
                    details.append("Core Frequency: ").append(resultSet.getString("core_frequency")).append("\n");
                    details.append("Memory Capacity: ").append(resultSet.getString("memory_capacity")).append("\n");
                    details.append("Bit Size Memory Bus: ").append(resultSet.getString("bit_size_memory_bus")).append("\n");
                    details.append("Maximum Supported Resolution: ").append(resultSet.getString("maximum_supported_resolution")).append("\n");
                    details.append("Minimum Required BZ Capacity: ").append(resultSet.getString("minimum_required_BZ_capacity")).append("\n");
                    details.append("Memory Type: ").append(resultSet.getString("memory_type")).append("\n");
                    details.append("Producing Country: ").append(resultSet.getString("producing_country")).append("\n");
                    details.append("Supported 3D APIs: ").append(resultSet.getString("supported_3D_APIs")).append("\n");
                    details.append("Form Factor: ").append(resultSet.getString("form_factor")).append("\n");
                    details.append("Type of Cooling System: ").append(resultSet.getString("type_of_cooling_system")).append("\n");
                    details.append("Guarantee: ").append(resultSet.getString("guarantee")).append("\n");
                    details.append("Price: ").append(resultSet.getString("price")).append("\n");
                    details.append("Wholesale Price: ").append(resultSet.getString("wholesale_price")).append("\n");
                    details.append("Brand: ").append(resultSet.getString("brand")).append("\n");
                    details.append("Product Title: ").append(resultSet.getString("product_title")).append("\n");
                    details.append("Creation Date: ").append(resultSet.getString("creation_data")).append("\n");
                    details.append("Product Description: ").append(resultSet.getString("product_description")).append("\n");
                    details.append("Creator Name: ").append(resultSet.getString("creator_name")).append("\n");
                    details.append("Available Quantity: ").append(resultSet.getString("available_quantity")).append("\n");
                    details.append("Wholesale Quantity: ").append(resultSet.getString("wholesale_quantity")).append("\n");

                    Label detailsLabel = new Label(details.toString());
                    detailsLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
                    detailsLabel.setTextFill(Color.WHITE);
                    detailsLabel.setLineSpacing(5);

                    detailVBox.getChildren().add(detailsLabel);
                } else {
                    alertService.showErrorAlert("Product details not found for ID: " + productId);
                    return;
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error fetching product details: " + e.getMessage());
            return;
        }

        ScrollPane scrollPane = new ScrollPane(detailVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: black;");

        Scene detailScene = new Scene(scrollPane, 600, 400);
        detailStage.setScene(detailScene);
        detailStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
