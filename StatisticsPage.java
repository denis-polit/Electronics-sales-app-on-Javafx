import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Date;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;

public class StatisticsPage extends Application {
    Stage primaryStage;
    private BorderPane root;
    private FirstConnectionToDataBase connectionToDataBase;
    private Text statisticsOutput;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    public StatisticsPage() {

    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.root = new BorderPane();
        MenuPage menuPage = new MenuPage();

        VBox root = new VBox();
        root.setStyle("-fx-background-color: black");

        HeaderComponent headerComponent = new HeaderComponent();
        VBox header = headerComponent.createHeader();
        VBox.setVgrow(header, Priority.NEVER);

        VBox contentLayout = new VBox(20);
        root.setStyle("-fx-background-color: black");
        contentLayout.setAlignment(Pos.CENTER);

        VBox statisticsOptions = createStatisticsPanel();

        statisticsOutput = new Text();
        statisticsOutput.setFont(Font.font("Arial", 20));
        statisticsOutput.setFill(Color.WHITE);

        VBox.setMargin(statisticsOptions, new Insets(50, 0, 0, 0));
        contentLayout.getChildren().addAll(statisticsOptions, statisticsOutput);

        root.getChildren().addAll(header, contentLayout);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Market Statistics");
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
        return connectionToDataBase.getConnection();
    }

    public class HeaderComponent {

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

            Button supportButton = ButtonStyle.createStyledButton("  Support  ");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = ButtonStyle.createStyledButton("  Privacy Policy  ");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button accountButton = ButtonStyle.createStyledButton("  Personal Account  ");
            accountButton.setOnAction(e -> showRegistrationWindow());

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, supportButton, privacyButton, accountButton, leftRegion);
            topContent.setAlignment(Pos.CENTER);
            VBox.setVgrow(topContent, Priority.ALWAYS);

            HBox.setMargin(logoCircle, new Insets(0, 270, 0, 0));

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

        private void showRegistrationWindow() {
            RegistrationWindow registrationWindow = new RegistrationWindow(root);
            Stage registrationStage = new Stage();
            registrationWindow.start(registrationStage);
        }
    }

    private VBox createStatisticsPanel() {
        VBox statisticsOptions = new VBox(10);
        statisticsOptions.setAlignment(Pos.CENTER);

        Button btn1 = ButtonStyle.expandPaneStyledButton("1. Number of ads for brand?");
        btn1.setOnAction(e -> countAdvertisementsPerBrand());

        Button btn2 = ButtonStyle.expandPaneStyledButton("2. Market share");
        btn2.setOnAction(e -> calculateMarketSharePerBrand(primaryStage, statisticsOutput));

        Button btn3 = ButtonStyle.expandPaneStyledButton("3. Average price on the market");
        btn3.setOnAction(e -> getPriceStatistics());

        Button btn4 = ButtonStyle.expandPaneStyledButton("4. Median graphics card price");
        btn4.setOnAction(e -> getMedianPriceStatistics());

        Button btn5 = ButtonStyle.expandPaneStyledButton("5. Best in price/performance ratio");
        btn5.setOnAction(e -> getBestPricePerformanceModel());

        Button btn6 = ButtonStyle.expandPaneStyledButton("6. Average memory capacity");
        btn6.setOnAction(e -> getMemoryStatistics());

        Button btn7 = ButtonStyle.expandPaneStyledButton("7. Country Distribution");
        btn7.setOnAction(e -> getCountryDistribution());

        Button btn8 = ButtonStyle.expandPaneStyledButton("8. Popular model for contract");
        btn8.setOnAction(e -> getPopularModelInfo());

        Button btn9 = ButtonStyle.expandPaneStyledButton("9. Contracts per month");
        btn9.setOnAction(e -> countContractsLastMonth());

        Button btn10 = ButtonStyle.expandPaneStyledButton("10. Average price above overall average per graphics chip");
        btn10.setOnAction(e -> getAveragePriceAboveAveragePerGraphicsChip());

        Button btn11 = ButtonStyle.expandPaneStyledButton("11. Average price above overall average per brand");
        btn11.setOnAction(e -> getAveragePriceAboveAverage());

        btn1.setPrefWidth(250);
        btn2.setPrefWidth(250);
        btn3.setPrefWidth(250);
        btn4.setPrefWidth(250);
        btn5.setPrefWidth(250);
        btn6.setPrefWidth(250);
        btn7.setPrefWidth(250);
        btn8.setPrefWidth(250);
        btn9.setPrefWidth(250);
        btn10.setPrefWidth(250);
        btn11.setPrefWidth(250);

        statisticsOptions.getChildren().addAll(btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn10, btn11);

        return statisticsOptions;
    }

    public void countAdvertisementsPerBrand() {
        Map<String, Integer> brandCounts = new HashMap<>();

        try {
            String sql = "SELECT brand, COUNT(*) AS count FROM catalog GROUP BY brand";
            try (PreparedStatement statement = connectionToDataBase.getConnection().prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String brand = resultSet.getString("brand");
                    int count = resultSet.getInt("count");
                    brandCounts.put(brand, count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("Advertisements by brand:\n");
        for (Map.Entry<String, Integer> entry : brandCounts.entrySet()) {
            resultBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        String result = resultBuilder.toString();

        showStatisticsResult("Statistics of advertisements by brand", result);
    }

    public void calculateMarketSharePerBrand(Stage stage, Text statisticsOutput) {
        Map<String, Double> marketShare = new HashMap<>();
        Map<String, Double> averagePricePerBrand = new HashMap<>();
        Map<String, Integer> brandCounts = new HashMap<>();

        try {
            Connection connection = connectionToDataBase.getConnection();
            String totalSql = "SELECT COUNT(*) AS total FROM catalog";
            int total = 0;
            try (PreparedStatement totalStatement = connection.prepareStatement(totalSql);
                 ResultSet totalResultSet = totalStatement.executeQuery()) {
                if (totalResultSet.next()) {
                    total = totalResultSet.getInt("total");
                }
            }

            String brandSql = "SELECT brand, COUNT(*) AS count, AVG(CAST(price AS DECIMAL(10, 2))) AS average_price " +
                    "FROM catalog " +
                    "GROUP BY brand";
            try (PreparedStatement statement = connection.prepareStatement(brandSql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String brand = resultSet.getString("brand");
                    int count = resultSet.getInt("count");
                    double share = (double) count / total * 100;
                    double avgPrice = resultSet.getDouble("average_price");

                    marketShare.put(brand, share);
                    averagePricePerBrand.put(brand, avgPrice);
                    brandCounts.put(brand, count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        PieChart pieChart = new PieChart();
        pieChart.setStyle("-fx-background-color: black;");
        pieChart.setLabelLineLength(10);
        for (String brand : marketShare.keySet()) {
            PieChart.Data slice = new PieChart.Data(brand, marketShare.get(brand));
            pieChart.getData().add(slice);
            slice.nameProperty().set(slice.getName() + " (" + String.format("%.2f", slice.getPieValue()) + "%)");
        }

        pieChart.getData().forEach(data -> data.nodeProperty().addListener((observable, oldNode, newNode) -> {
            if (newNode != null) {

                Text textNode = (Text) newNode.lookup(".chart-pie-label");
                if (textNode != null) {
                    textNode.setFill(Color.WHITE);
                }
            }
        }));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Brand");
        xAxis.setTickLabelFill(Color.WHITE);
        xAxis.setStyle("-fx-text-fill: white;");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Average Price");
        yAxis.setTickLabelFill(Color.WHITE);
        yAxis.setStyle("-fx-text-fill: white;");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setStyle("-fx-background-color: black;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (String brand : averagePricePerBrand.keySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(brand, averagePricePerBrand.get(brand));
            series.getData().add(data);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: " + getColorForBrand(brand) + ";");
                    Tooltip.install(newNode, new Tooltip("Price: " + averagePricePerBrand.get(brand)));

                    Text dataText = new Text(String.format("%.2f", averagePricePerBrand.get(brand)));
                    dataText.setFill(Color.WHITE);
                    dataText.setStyle("-fx-font-size: 12px;");
                    StackPane stackPane = (StackPane) newNode;
                    stackPane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
                    stackPane.getChildren().add(dataText);
                    StackPane.setAlignment(dataText, Pos.CENTER);

                    newNode.setScaleY(0);
                    ScaleTransition st = new ScaleTransition(Duration.seconds(1), newNode);
                    st.setFromY(0);
                    st.setToY(1);
                    st.play();
                }
            });
        }
        barChart.getData().add(series);

        VBox vbox = new VBox(pieChart, barChart);
        Scene scene = new Scene(vbox, 900, 600);
        vbox.setStyle("-fx-background-color: black;");

        stage.setScene(scene);
        stage.setTitle("Market Share and Average Price Analysis");
        stage.show();

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("Market share, average price, and count of advertisements per brand:\n");
        for (String brand : marketShare.keySet()) {
            double share = marketShare.getOrDefault(brand, 0.0);
            double avgPrice = averagePricePerBrand.getOrDefault(brand, 0.0);
            int count = brandCounts.getOrDefault(brand, 0);
            resultBuilder.append(brand).append(": Market Share = ").append(String.format("%.2f", share))
                    .append("%, Average Price = ").append(String.format("%.2f", avgPrice))
                    .append(", Count = ").append(count).append("\n");
        }
        statisticsOutput.setFill(Color.WHITE);
        statisticsOutput.setText(resultBuilder.toString());
    }

    private String getColorForBrand(String brand) {
        switch (brand) {
            case "ASUS":
                return "blue";
            case "MSI":
                return "red";
            case "Gigabyte":
                return "green";
            case "ASRock":
                return "orange";
            default:
                return "white";
        }
    }

    public void getPriceStatistics() {
        Map<String, Double> priceStatistics = new HashMap<>();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT AVG(price) AS average_price, MIN(price) AS min_price, MAX(price) AS max_price FROM catalog";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    double averagePrice = resultSet.getDouble("average_price");
                    double minPrice = resultSet.getDouble("min_price");
                    double maxPrice = resultSet.getDouble("max_price");

                    priceStatistics.put("average_price", averagePrice);
                    priceStatistics.put("min_price", minPrice);
                    priceStatistics.put("max_price", maxPrice);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        String result = "Price statistics:\n";
        result += "Average price: " + priceStatistics.get("average_price") + "\n";
        result += "Minimum price: " + priceStatistics.get("min_price") + "\n";
        result += "Maximum price: " + priceStatistics.get("max_price") + "\n";
        showStatisticsResult("Price statistics", result);
    }

    public void getMedianPriceStatistics() {
        List<Double> prices = new ArrayList<>();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT price FROM catalog";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    double price = resultSet.getDouble("price");
                    prices.add(price);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        Collections.sort(prices);
        double median;
        if (prices.size() % 2 == 0) {
            median = (prices.get(prices.size() / 2 - 1) + prices.get(prices.size() / 2)) / 2;
        } else {
            median = prices.get(prices.size() / 2);
        }

        String result = "Median graphics card price: " + median;
        showStatisticsResult("Median price", result);
    }

    public void getMemoryStatistics() {
        Map<String, Double> memoryStatistics = new HashMap<>();
        int totalCount = 0;
        double minMemory = 0.0;
        double maxMemory = 0.0;
        String minMemoryCard = "";
        String maxMemoryCard = "";

        try (Connection connection = establishDBConnection()) {

            String sql = "SELECT AVG(CAST(memory_capacity AS DECIMAL(10,2))) AS average_memory, " +
                    "MIN(CAST(memory_capacity AS DECIMAL(10,2))) AS min_memory, " +
                    "MAX(CAST(memory_capacity AS DECIMAL(10,2))) AS max_memory " +
                    "FROM catalog";

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    double averageMemory = resultSet.getDouble("average_memory");
                    minMemory = resultSet.getDouble("min_memory");
                    maxMemory = resultSet.getDouble("max_memory");

                    memoryStatistics.put("average_memory", averageMemory);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                alertService.showErrorAlert("Error: " + e.getMessage());
            }

            sql = "SELECT COUNT(*) AS total_count FROM catalog";
            try (PreparedStatement countStatement = connection.prepareStatement(sql);
                 ResultSet countResultSet = countStatement.executeQuery()) {

                if (countResultSet.next()) {
                    totalCount = countResultSet.getInt("total_count");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                alertService.showErrorAlert("Error: " + e.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        DecimalFormat df = new DecimalFormat("#.##");
        String result = "Average memory capacity: " + df.format(memoryStatistics.get("average_memory")) + " GB\n" +
                "Total listings: " + totalCount + "\n" +
                "Minimum memory capacity: " + minMemory + " GB\n" +
                "Maximum memory capacity: " + maxMemory + " GB";

        showStatisticsResult("Memory Statistics", result);
    }

    public void getCountryDistribution() {
        Map<String, Integer> countryListingCounts = new HashMap<>();
        Map<String, Double> countryAveragePrices = new HashMap<>();
        int totalListings = 0;

        try (Connection connection = establishDBConnection()) {

            String sql = "SELECT producing_country, COUNT(*) AS count, AVG(CAST(price AS DECIMAL(10,2))) AS avg_price " +
                    "FROM catalog GROUP BY producing_country";

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String country = resultSet.getString("producing_country");
                    int count = resultSet.getInt("count");
                    double avgPrice = resultSet.getDouble("avg_price");

                    countryListingCounts.put(country, count);
                    countryAveragePrices.put(country, avgPrice);
                    totalListings += count;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
            return;
        }

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("Country distribution of contracts:\n");

        for (Map.Entry<String, Integer> entry : countryListingCounts.entrySet()) {
            String country = entry.getKey();
            int count = entry.getValue();
            double avgPrice = countryAveragePrices.getOrDefault(country, 0.0);
            double percentage = (double) count / totalListings * 100.0;

            resultBuilder.append(country).append(": ")
                    .append(count).append(" listings (")
                    .append(String.format("%.2f", percentage)).append("%), ")
                    .append("Average Price: $").append(String.format("%.2f", avgPrice))
                    .append("\n");
        }

        String result = resultBuilder.toString();
        showStatisticsResult("Country distribution", result);
    }

    public void getPopularModelInfo() {
        Map<String, Integer> popularModelInfo = new HashMap<>();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT product_id, COUNT(*) AS count " +
                    "FROM contracts " +
                    "GROUP BY product_id " +
                    "ORDER BY count DESC " +
                    "LIMIT 1";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    String productId = resultSet.getString("product_id");
                    int count = resultSet.getInt("count");
                    popularModelInfo.put(productId, count);

                    String catalogSql = "SELECT * FROM catalog WHERE product_id = ?";
                    try (PreparedStatement catalogStatement = connection.prepareStatement(catalogSql)) {
                        catalogStatement.setString(1, productId);
                        ResultSet catalogResult = catalogStatement.executeQuery();
                        StringBuilder resultBuilder = new StringBuilder();

                        if (catalogResult.next()) {
                            String productName = catalogResult.getString("product_title");
                            String productPrice = catalogResult.getString("price");
                            String productBrand = catalogResult.getString("brand");
                            String graphicsChip = catalogResult.getString("graphics_chip");
                            String memoryFrequency = catalogResult.getString("memory_frequency");
                            String coreFrequency = catalogResult.getString("core_frequency");
                            String memoryCapacity = catalogResult.getString("memory_capacity");
                            String bitSizeMemoryBus = catalogResult.getString("bit_size_memory_bus");
                            String maxResolution = catalogResult.getString("maximum_supported_resolution");
                            String minPowerSupply = catalogResult.getString("minimum_required_BZ_capacity");
                            String memoryType = catalogResult.getString("memory_type");
                            String countryOfOrigin = catalogResult.getString("producing_country");
                            String supported3DAPIs = catalogResult.getString("supported_3D_APIs");
                            String formFactor = catalogResult.getString("form_factor");
                            String coolingSystem = catalogResult.getString("type_of_cooling_system");
                            String guarantee = catalogResult.getString("guarantee");
                            String wholesalePrice = catalogResult.getString("wholesale_price");
                            String creationDate = catalogResult.getString("creation_data");
                            String productDescription = catalogResult.getString("product_description");
                            String creatorName = catalogResult.getString("creator_name");
                            String availableQuantity = catalogResult.getString("available_quantity");
                            String wholesaleQuantity = catalogResult.getString("wholesale_quantity");

                            resultBuilder.append("Most popular product details:\n");
                            resultBuilder.append("Product ID: ").append(productId).append("\n");
                            resultBuilder.append("Product Name: ").append(productName).append("\n");
                            resultBuilder.append("Product Price: ").append(productPrice).append("\n");
                            resultBuilder.append("Product Brand: ").append(productBrand).append("\n");
                            resultBuilder.append("Number of Contracts: ").append(count).append("\n");
                            resultBuilder.append("Graphics Chip: ").append(graphicsChip).append("\n");
                            resultBuilder.append("Memory Frequency: ").append(memoryFrequency).append("\n");
                            resultBuilder.append("Core Frequency: ").append(coreFrequency).append("\n");
                            resultBuilder.append("Memory Capacity: ").append(memoryCapacity).append("\n");
                            resultBuilder.append("Memory Bus Size: ").append(bitSizeMemoryBus).append("\n");
                            resultBuilder.append("Max Supported Resolution: ").append(maxResolution).append("\n");
                            resultBuilder.append("Min Required Power Supply Capacity: ").append(minPowerSupply).append("\n");
                            resultBuilder.append("Memory Type: ").append(memoryType).append("\n");
                            resultBuilder.append("Producing Country: ").append(countryOfOrigin).append("\n");
                            resultBuilder.append("Supported 3D APIs: ").append(supported3DAPIs).append("\n");
                            resultBuilder.append("Form Factor: ").append(formFactor).append("\n");
                            resultBuilder.append("Type of Cooling System: ").append(coolingSystem).append("\n");
                            resultBuilder.append("Guarantee: ").append(guarantee).append("\n");
                            resultBuilder.append("Wholesale Price: ").append(wholesalePrice).append("\n");
                            resultBuilder.append("Creation Date: ").append(creationDate).append("\n");
                            resultBuilder.append("Product Description: ").append(productDescription).append("\n");
                            resultBuilder.append("Creator Name: ").append(creatorName).append("\n");
                            resultBuilder.append("Available Quantity: ").append(availableQuantity).append("\n");
                            resultBuilder.append("Wholesale Quantity: ").append(wholesaleQuantity).append("\n");
                        }

                        String result = resultBuilder.toString();
                        showStatisticsResult("Popular model for contracts", result);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }
    }

    public void countContractsLastMonth() {
        int contractsCount = 0;

        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT COUNT(*) AS count FROM contracts WHERE contract_date >= ? AND contract_date <= ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDate(1, Date.valueOf(startDate));
                statement.setDate(2, Date.valueOf(endDate));

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        contractsCount = resultSet.getInt("count");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        String result = "Contracts in the last month: " + contractsCount;
        showStatisticsResult("Contracts in last month", result);
    }

    public void getAveragePriceAboveAveragePerGraphicsChip() {
        Map<String, Double> averagePriceAboveAveragePerGraphicsChip = new HashMap<>();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT graphics_chip, AVG(price) AS average_price FROM catalog GROUP BY graphics_chip";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                double overallAveragePrice = getOverallAveragePrice(connection);

                while (resultSet.next()) {
                    String graphicsChip = resultSet.getString("graphics_chip");
                    double averagePrice = resultSet.getDouble("average_price");

                    if (averagePrice > overallAveragePrice) {
                        averagePriceAboveAveragePerGraphicsChip.put(graphicsChip, averagePrice);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("Average price above overall average per graphics chip:\n");
        for (Map.Entry<String, Double> entry : averagePriceAboveAveragePerGraphicsChip.entrySet()) {
            resultBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        String result = resultBuilder.toString();

        showStatisticsResult("Average price above average per graphics chip", result);
    }

    private double getOverallAveragePrice(Connection connection) throws SQLException {
        double overallAveragePrice = 0;

        String sql = "SELECT AVG(price) AS overall_average_price FROM catalog";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                overallAveragePrice = resultSet.getDouble("overall_average_price");
            }
        }

        return overallAveragePrice;
    }

    public void getAveragePriceAboveAverage() {
        Map<String, Double> averagePriceAboveAverage = new HashMap<>();

        try (Connection connection = establishDBConnection()) {
            double overallAveragePrice = getOverallAveragePrice(connection);

            String sql = "SELECT brand, AVG(price) AS average_price FROM catalog GROUP BY brand";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String brand = resultSet.getString("brand");
                    double averagePrice = resultSet.getDouble("average_price");

                    if (averagePrice > overallAveragePrice) {
                        averagePriceAboveAverage.put(brand, averagePrice);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("Average price above overall average per brand:\n");
        for (Map.Entry<String, Double> entry : averagePriceAboveAverage.entrySet()) {
            resultBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        String result = resultBuilder.toString();

        showStatisticsResult("Average price above average per brand", result);
    }

    private void getBestPricePerformanceModel() {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT product_title, memory_capacity, core_frequency, memory_frequency, bit_size_memory_bus, maximum_supported_resolution, price FROM catalog";

            try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                 ResultSet rs = stmt.executeQuery(query)) {

                double bestRatio = Double.MIN_VALUE;
                String bestModel = "";
                double bestPerformance = 0.0;
                double bestPrice = 0.0;

                List<Double> memoryCapacities = new ArrayList<>();
                List<Double> coreFrequencies = new ArrayList<>();
                List<Double> memoryFrequencies = new ArrayList<>();
                List<Double> busSizes = new ArrayList<>();
                List<Double> resolutions = new ArrayList<>();

                int count = 0;

                while (rs.next()) {
                    memoryCapacities.add(parseCapacity(rs.getString("memory_capacity")));
                    coreFrequencies.add(parseFrequency(rs.getString("core_frequency")));
                    memoryFrequencies.add(parseFrequency(rs.getString("memory_frequency")));
                    busSizes.add(parseBusSize(rs.getString("bit_size_memory_bus")));
                    resolutions.add(parseResolution(rs.getString("maximum_supported_resolution")));
                    count++;
                }

                double maxMemoryCapacity = getMax(memoryCapacities);
                double maxCoreFrequency = getMax(coreFrequencies);
                double maxMemoryFrequency = getMax(memoryFrequencies);
                double maxBusSize = getMax(busSizes);
                double maxResolution = getMax(resolutions);

                rs.beforeFirst();

                while (rs.next()) {
                    String productTitle = rs.getString("product_title");
                    double memoryCapacity = parseCapacity(rs.getString("memory_capacity"));
                    double coreFrequency = parseFrequency(rs.getString("core_frequency"));
                    double memoryFrequency = parseFrequency(rs.getString("memory_frequency"));
                    double busSize = parseBusSize(rs.getString("bit_size_memory_bus"));
                    double resolution = parseResolution(rs.getString("maximum_supported_resolution"));
                    double price = parsePrice(rs.getString("price"));

                    double normalizedMemoryCapacity = memoryCapacity / maxMemoryCapacity;
                    double normalizedCoreFrequency = coreFrequency / maxCoreFrequency;
                    double normalizedMemoryFrequency = memoryFrequency / maxMemoryFrequency;
                    double normalizedBusSize = busSize / maxBusSize;
                    double normalizedResolution = resolution / maxResolution;

                    double performance = normalizedMemoryCapacity + normalizedCoreFrequency +
                            normalizedMemoryFrequency + normalizedBusSize + normalizedResolution;

                    double ratio = performance / price;

                    System.out.println("Title: " + productTitle + ", Normalized Performance: " + performance + ", Ratio: " + ratio);

                    if (ratio > bestRatio) {
                        bestRatio = ratio;
                        bestModel = productTitle;
                        bestPerformance = performance;
                        bestPrice = price;
                    }
                }

                DecimalFormat df = new DecimalFormat("#.##");
                DecimalFormat scientificFormat = new DecimalFormat("0.00E0");

                StringBuilder resultMessage = new StringBuilder();
                resultMessage.append("Model: ").append(bestModel).append("\n");
                resultMessage.append("Performance: ").append(df.format(bestPerformance)).append("\n");
                resultMessage.append("Price: ").append(bestPrice).append("\n");
                resultMessage.append("Ratio: ").append(scientificFormat.format(bestRatio)).append("\n\n");
                resultMessage.append("Criteria used for evaluation:\n");
                resultMessage.append("- Memory Capacity\n");
                resultMessage.append("- Core Frequency\n");
                resultMessage.append("- Memory Frequency\n");
                resultMessage.append("- Bus Size\n");
                resultMessage.append("- Resolution\n\n");
                resultMessage.append("Total models analyzed: ").append(count).append("\n");

                showStatisticsResult("Best Price/Performance Model", resultMessage.toString());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double parseCapacity(String capacityStr) {
        try {
            return Double.parseDouble(capacityStr.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseFrequency(String frequencyStr) {
        try {
            return Double.parseDouble(frequencyStr.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseBusSize(String busSizeStr) {
        try {
            return Double.parseDouble(busSizeStr.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseResolution(String resolutionStr) {
        try {
            String[] parts = resolutionStr.split("x");
            return Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    private double parsePrice(String priceStr) {
        try {
            return Double.parseDouble(priceStr.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private double getMax(List<Double> values) {
        return values.stream().mapToDouble(v -> v).max().orElse(1);
    }

    private void showStatisticsResult(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistics Result");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
