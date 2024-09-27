import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EmployeeSessionDetailsWindow extends Application {

    private Stage primaryStage;
    private BorderPane root;
    private int managerId;
    private MenuPage menuPage;
    private FirstConnectionToDataBase connectionToDataBase;
    private VBox sessionDetailsLayout;
    private List<VBox> sessionBoxes;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public EmployeeSessionDetailsWindow(int managerId) {
        this.managerId = managerId;
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        root = new BorderPane();
        VBox header = createHeader();
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0))));
        root.setTop(header);

        ScrollPane scrollPane = new ScrollPane();
        sessionDetailsLayout = new VBox(10);
        sessionDetailsLayout.setAlignment(Pos.CENTER_LEFT);
        sessionDetailsLayout.setPadding(new Insets(10));

        scrollPane.setContent(sessionDetailsLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: black; -fx-font-family: 'Montserrat'; -fx-font-size: 16px;");

        loadSessions();

        root.setCenter(scrollPane);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Session Details");
        primaryStage.show();

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

    private void loadSessions() {
        try (Connection connection = establishDBConnection()) {
            System.out.println("Database connection established.");

            String currentSessionsSql = "SELECT session_id, country, ip_address, device_type, start_time FROM employee_sessions WHERE manager_id = ? AND end_time IS NULL";
            PreparedStatement currentSessionsStatement = connection.prepareStatement(currentSessionsSql);
            currentSessionsStatement.setInt(1, managerId);
            ResultSet currentSessionsResultSet = currentSessionsStatement.executeQuery();

            Label currentSessionsLabel = new Label("Current Sessions:");
            currentSessionsLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
            sessionDetailsLayout.getChildren().add(currentSessionsLabel);

            boolean hasCurrentSessions = false;
            while (currentSessionsResultSet.next()) {
                hasCurrentSessions = true;
                int sessionId = currentSessionsResultSet.getInt("session_id");
                String country = currentSessionsResultSet.getString("country");
                String ipAddress = currentSessionsResultSet.getString("ip_address");
                String deviceType = currentSessionsResultSet.getString("device_type");
                String startTime = currentSessionsResultSet.getString("start_time");

                VBox currentSessionBox = createSessionBox("Current Session", country, ipAddress, deviceType, startTime, sessionId);
                sessionDetailsLayout.getChildren().add(currentSessionBox);
            }

            if (!hasCurrentSessions) {
                System.out.println("No current sessions found.");
            }

            Separator separator = new Separator();
            separator.setStyle("-fx-background-color: gray;");
            sessionDetailsLayout.getChildren().add(separator);

            HBox historyBox = new HBox(10);
            historyBox.setAlignment(Pos.CENTER_LEFT);

            Label historyLabel = new Label("Sessions History");
            historyLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

            Button sortButton = new Button();
            Image sortingIcon = new Image("file:icons/sorting.png");
            ImageView sortingImageView = new ImageView(sortingIcon);
            sortingImageView.setFitWidth(20);
            sortingImageView.setFitHeight(20);
            sortButton.setGraphic(sortingImageView);
            sortButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            sortButton.setOnAction(e -> sortSessionHistory());

            historyBox.getChildren().addAll(historyLabel, sortButton);
            sessionDetailsLayout.getChildren().add(historyBox);

            String allSessionsSql = "SELECT session_id, country, ip_address, device_type, start_time, end_time FROM employee_sessions WHERE manager_id = ?";
            PreparedStatement allSessionsStatement = connection.prepareStatement(allSessionsSql);
            allSessionsStatement.setInt(1, managerId);
            ResultSet allSessionsResultSet = allSessionsStatement.executeQuery();

            sessionBoxes = new ArrayList<>();
            int sessionNumber = 1;
            boolean hasSessions = false;
            while (allSessionsResultSet.next()) {
                hasSessions = true;
                int sessionId = allSessionsResultSet.getInt("session_id");
                String country = allSessionsResultSet.getString("country");
                String ipAddress = allSessionsResultSet.getString("ip_address");
                String deviceType = allSessionsResultSet.getString("device_type");
                String startTime = allSessionsResultSet.getString("start_time");
                String endTime = allSessionsResultSet.getString("end_time");

                System.out.println("Session - ID: " + sessionId + ", Country: " + country + ", IP: " + ipAddress + ", Device: " + deviceType + ", Time: " + startTime + " - " + endTime);

                String timeRange = (endTime != null) ? (startTime + " - " + endTime) : startTime;
                VBox sessionBox = createSessionBox("Session " + sessionNumber, country, ipAddress, deviceType, timeRange, sessionId);
                sessionBoxes.add(sessionBox);
                sessionNumber++;
            }

            if (!hasSessions) {
                System.out.println("No session history found.");
            }

            Collections.reverse(sessionBoxes);
            sessionDetailsLayout.getChildren().addAll(sessionBoxes);

        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Failed to fetch session details: " + e.getMessage());
        }
    }

    private void sortSessionHistory() {
        if (sessionDetailsLayout.getChildren().containsAll(sessionBoxes)) {
            Collections.reverse(sessionBoxes);
        } else {
            sessionBoxes.sort(Comparator.comparing(box -> {
                Label timeLabel = (Label) box.getChildren().get(4);
                return timeLabel.getText();
            }));
        }

        sessionDetailsLayout.getChildren().removeAll(sessionBoxes);
        sessionDetailsLayout.getChildren().addAll(sessionBoxes);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(10));
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

        Button menuButton = ButtonStyle.createStyledButton("     Menu     ");
        menuButton.setOnAction(e -> showMenu());

        Button supportButton = ButtonStyle.createStyledButton("  Support   ");
        supportButton.setOnAction(event -> showSupportWindow());

        Button privacyButton = ButtonStyle.createStyledButton("  Privacy Policy  ");
        privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

        Button backButton = ButtonStyle.createStyledButton("   Back   ");
        backButton.setOnAction(event -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();
        });

        Button accountButton = ButtonStyle.createStyledButton("  Personal Account  ");
        accountButton.setOnAction(e -> menuPage.showRegistrationWindow(primaryStage));

        HBox topContent = new HBox(10);
        topContent.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, backButton, accountButton, leftRegion);
        topContent.setAlignment(Pos.CENTER);
        VBox.setVgrow(topContent, Priority.ALWAYS);

        HBox.setMargin(logoCircle, new Insets(0, 220, 0, 0));

        header.getChildren().addAll(topContent);

        return header;
    }

    private VBox createSessionBox(String title, String country, String ipAddress, String deviceType, String time, int sessionId) {
        Label sessionLabel = new Label(title + ":");
        Label countryLabel = new Label("Country: " + country);
        Label ipAddressLabel = new Label("IP Address: " + ipAddress);
        Label deviceTypeLabel = new Label("Device Type: " + deviceType);
        Label timeLabel = new Label("Time: " + time);

        VBox sessionBox = new VBox(sessionLabel, countryLabel, ipAddressLabel, deviceTypeLabel, timeLabel);
        sessionBox.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 5px;");
        sessionBox.setSpacing(5);
        sessionBox.setAlignment(Pos.CENTER_LEFT);

        return sessionBox;
    }

    private void showMenu() {
        if (menuPage == null) {
            menuPage = new MenuPage();
        }
        Stage stage = (Stage) root.getScene().getWindow();
        menuPage.start(stage);
    }

    private void showSupportWindow() {
        SupportWindow supportWindow = new SupportWindow();
        supportWindow.start(new Stage());
    }

    private void showPrivacyPolicyWindow() {
        PrivacyPolicyWindow privacyPolicyWindow = new PrivacyPolicyWindow();
        privacyPolicyWindow.start(new Stage());
    }

    public static void main(String[] args) {
        launch(args);
    }
}