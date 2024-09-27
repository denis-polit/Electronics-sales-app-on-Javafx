import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.time.LocalDate;
import java.util.Date;

public class BookkeepingPage extends Application {

    private BorderPane root;
    FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private ExportFormat exportFormat;

    @Override
    public void start(Stage primaryStage) {
        this.root = new BorderPane();
        MenuPage menuPage = new MenuPage();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        root.setCenter(scrollPane);
        scrollPane.setStyle("-fx-background: black;");

        HeaderComponent headerComponent = new HeaderComponent(primaryStage);
        VBox header = headerComponent.createHeader(scrollPane);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setBorder(new Border(new BorderStroke(Color.GRAY,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0))));
        root.setTop(header);

        Label periodLabel = new Label();
        periodLabel.setTextFill(Color.WHITE);
        periodLabel.setPadding(new Insets(10));
        root.setBottom(periodLabel);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bookkeeping");
        primaryStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();

        displayBookkeepingInfo(scrollPane, "Month");

        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private class HeaderComponent {

        private Stage primaryStage;

        public HeaderComponent(Stage primaryStage) {
            this.primaryStage = primaryStage;
        }

        private VBox createHeader(ScrollPane scrollPane) {
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

            Button editButton = ButtonStyle.createStyledButton(" Edit Bonus ");
            editButton.setOnAction(event -> editBonus ());

            Button exportToPDFButton = ButtonStyle.createStyledButton("To PDF");
            exportToPDFButton.setOnAction(event -> exportFormat.exportToPDF());

            Button exportToTableButton = ButtonStyle.createStyledButton("To Table");
            exportToTableButton.setOnAction(event -> exportFormat.exportToTable());

            Button filterButton = ButtonStyle.createStyledButton("  Filter  ");
            filterButton.setOnAction(event -> filterForm(scrollPane));

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, editButton, exportToPDFButton, exportToTableButton, filterButton, leftRegion);
            topContent.setAlignment(Pos.CENTER);
            topContent.setSpacing(10);

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

    private void filterForm(ScrollPane scrollPane) {
        Stage filterStage = new Stage();
        VBox filterBox = new VBox(10);
        filterBox.setPadding(new Insets(10));
        filterBox.setStyle("-fx-background-color: black;");

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button dayButton = ButtonStyle.expandPaneStyledButton("Day");
        Button weekButton = ButtonStyle.expandPaneStyledButton("Week");
        Button monthButton = ButtonStyle.expandPaneStyledButton("Month");
        Button yearButton = ButtonStyle.expandPaneStyledButton("Year");

        dayButton.setOnAction(event -> displayBookkeepingInfo(scrollPane, "Day"));
        weekButton.setOnAction(event -> displayBookkeepingInfo(scrollPane, "Week"));
        monthButton.setOnAction(event -> displayBookkeepingInfo(scrollPane, "Month"));
        yearButton.setOnAction(event -> displayBookkeepingInfo(scrollPane, "Year"));

        buttonsBox.getChildren().addAll(dayButton, weekButton, monthButton, yearButton);

        filterBox.getChildren().addAll(new Label("Select period:"), buttonsBox);
        filterBox.setAlignment(Pos.CENTER);

        Scene filterScene = new Scene(filterBox, 300, 200);
        filterStage.setScene(filterScene);
        filterStage.setTitle("Filter Period");
        filterStage.show();
    }

    private void displayBookkeepingInfo(ScrollPane scrollPane, String selectedPeriod) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: black;");

        String sql = "SELECT * FROM bookkeeping WHERE change_date >= ?";
        java.sql.Date startDate = getStartDateForPeriod(selectedPeriod);

        try (Connection connection = establishDBConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, startDate);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String employeeName = resultSet.getString("employee_name");
                    double employeeSalary = resultSet.getDouble("employee_salary");
                    Date changeDate = resultSet.getDate("change_date");
                    int successfulContracts = resultSet.getInt("successful_contracts");
                    double successfulContractsPrice = resultSet.getDouble("successful_contracts_price");
                    String bonus = resultSet.getString("bonus");

                    VBox employeeInfo = new VBox(5);
                    employeeInfo.setStyle("-fx-border-color: gray; -fx-border-width: 2; -fx-padding: 10; -fx-background-radius: 15px; -fx-border-radius: 15px;");

                    Label nameLabel = new Label("Name: " + employeeName);
                    nameLabel.setTextFill(Color.WHITE);
                    nameLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 14));

                    Label salaryLabel = new Label("Salary: $" + employeeSalary);
                    salaryLabel.setTextFill(Color.WHITE);
                    salaryLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 14));

                    Label dateLabel = new Label("Change Date: " + changeDate);
                    dateLabel.setTextFill(Color.WHITE);
                    dateLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 14));

                    Label contractsLabel = new Label("Successful Contracts: " + successfulContracts);
                    contractsLabel.setTextFill(Color.WHITE);
                    contractsLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 14));

                    Label contractsPriceLabel = new Label("Successful Contracts Price: $" + successfulContractsPrice);
                    contractsPriceLabel.setTextFill(Color.WHITE);
                    contractsPriceLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 14));

                    Label bonusLabel = new Label("Bonus: " + bonus);
                    bonusLabel.setTextFill(Color.WHITE);
                    bonusLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 14));

                    employeeInfo.getChildren().addAll(nameLabel, salaryLabel, dateLabel, contractsLabel, contractsPriceLabel, bonusLabel);
                    content.getChildren().add(employeeInfo);
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error displaying bookkeeping information: " + e.getMessage());
        }

        LocalDate now = LocalDate.now();
        LocalDate startDateLocal = startDate.toLocalDate();

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: white;");

        Label dateRangeLabel = new Label("Displaying data for: " + selectedPeriod + " (" + startDateLocal + " to " + now + ")");
        dateRangeLabel.setTextFill(Color.WHITE);
        dateRangeLabel.setFont(Font.font("Gotham", FontWeight.NORMAL, 18));

        VBox dateRangeBox = new VBox(separator, dateRangeLabel);
        dateRangeBox.setPadding(new Insets(10));
        dateRangeBox.setAlignment(Pos.CENTER);
        dateRangeBox.setStyle("-fx-background-color: black;");

        root.setBottom(dateRangeBox);
        scrollPane.setContent(content);
    }

    public java.sql.Date getStartDateForPeriod(String selectedPeriod) {
        LocalDate startDate;

        switch (selectedPeriod) {
            case "Day":
                startDate = LocalDate.now().minusDays(1);
                break;
            case "Week":
                startDate = LocalDate.now().minusWeeks(1);
                break;
            case "Month":
                startDate = LocalDate.now().minusMonths(1);
                break;
            case "Year":
                startDate = LocalDate.now().minusYears(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + selectedPeriod);
        }

        return java.sql.Date.valueOf(startDate);
    }

    private void editBonus() {
        try (Connection connection = establishDBConnection()) {
            String managerQuery = "SELECT manager_name FROM managers WHERE employee_status = 'manager'";
            try (PreparedStatement managerStatement = connection.prepareStatement(managerQuery);
                 ResultSet managerResult = managerStatement.executeQuery()) {

                VBox editBonusBox = new VBox(10);
                editBonusBox.setPadding(new Insets(10));
                editBonusBox.setStyle("-fx-background-color: #3c3f41; -fx-padding: 10px; -fx-background-radius: 10px;");

                ComboBox<String> managerComboBox = new ComboBox<>();
                managerComboBox.setPromptText("Select Manager");

                while (managerResult.next()) {
                    String managerName = managerResult.getString("manager_name");
                    managerComboBox.getItems().add(managerName);
                }

                TextField keyField = new TextField();
                keyField.setPromptText("Enter Bonus Key");

                TextField newBonusField = new TextField();
                newBonusField.setPromptText("Enter New Bonus");

                Button confirmButton = ButtonStyle.createStyledButton("Confirm");
                confirmButton.setOnAction(event -> {
                    String selectedManager = managerComboBox.getValue();
                    String bonusKey = keyField.getText();
                    String newBonus = newBonusField.getText();
                    if (selectedManager != null && bonusKey != null && newBonus != null) {
                        if (validateBonusKey(selectedManager, bonusKey)) {
                            updateBonus(selectedManager, newBonus);
                        } else {
                            alertService.showErrorAlert("Invalid Bonus Key");
                        }
                    } else {
                        alertService.showErrorAlert("Please fill in all fields");
                    }
                });

                editBonusBox.getChildren().addAll(managerComboBox, keyField, newBonusField, confirmButton);
                root.setCenter(editBonusBox);
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error editing bonus: " + e.getMessage());
        }
    }

    private boolean validateBonusKey(String managerName, String bonusKey) {
        try (Connection connection = establishDBConnection()) {
            String query = "SELECT bonus_key FROM bookkeeping WHERE employee_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, managerName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String storedKey = resultSet.getString("bonus_key");
                        return storedKey.equals(bonusKey);
                    }
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error validating bonus key: " + e.getMessage());
        }
        return false;
    }

    private void updateBonus(String managerName, String newBonus) {
        try (Connection connection = establishDBConnection()) {

            double bonusAmount = Double.parseDouble(newBonus);
            if (bonusAmount > 4000) {
                alertService.showErrorAlert("Bonus cannot exceed 4000 UAH.");
                return;
            }

            String query = "UPDATE bookkeeping SET bonus = ?, change_date = NOW() WHERE employee_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, newBonus);
                statement.setString(2, managerName);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    alertService.showSuccessAlert("Bonus updated successfully for manager: " + managerName);
                } else {
                    alertService.showErrorAlert("Failed to update bonus for manager: " + managerName);
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error updating bonus: " + e.getMessage());
        } catch (NumberFormatException e) {
            alertService.showErrorAlert("Invalid bonus amount. Please enter a valid number.");
        }
    }
}
