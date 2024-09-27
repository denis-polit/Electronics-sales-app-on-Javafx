import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FilterCatalog {

    private ListView<Announcement> announcementsListView;
    private FirstConnectionToDataBase connectionToDataBase;

    public FilterCatalog(ListView<Announcement> announcementsListView) {
        this.announcementsListView = announcementsListView;
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            return connectionToDataBase.getConnection();
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    private void applyGothamStyle(Label label) {
        label.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
    }

    public void showFilterForm() {
        Stage filterStage = new Stage();
        filterStage.setTitle("Filter Form");
        filterStage.setWidth(400);
        filterStage.setHeight(640);

        VBox filterLayout = new VBox(10);
        filterLayout.setPadding(new Insets(20));
        filterLayout.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        TextField minPriceField = new TextField();
        minPriceField.setPromptText("Min Price");
        minPriceField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(minPriceField, "Enter minimum price");

        TextField maxPriceField = new TextField();
        maxPriceField.setPromptText("Max Price");
        maxPriceField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(maxPriceField, "Enter maximum price");

        HBox priceRangeBox = new HBox(10);
        priceRangeBox.setAlignment(Pos.CENTER_LEFT);
        priceRangeBox.getChildren().addAll(minPriceField, maxPriceField);

        Label priceLabel = new Label("Price Filter:");
        priceLabel.setStyle("-fx-padding: 20 0 0 0;");
        applyGothamStyle(priceLabel);

        TextField minMemoryField = new TextField();
        minMemoryField.setPromptText("Min Memory (GB)");
        minMemoryField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(minMemoryField, "Enter minimum memory");

        TextField maxMemoryField = new TextField();
        maxMemoryField.setPromptText("Max Memory (GB)");
        maxMemoryField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(maxMemoryField, "Enter maximum memory");

        HBox memoryRangeBox = new HBox(10);
        memoryRangeBox.setAlignment(Pos.CENTER_LEFT);
        memoryRangeBox.getChildren().addAll(minMemoryField, maxMemoryField);

        Label memoryLabel = new Label("Memory Filter (GB):");
        memoryLabel.setStyle("-fx-padding: 20 0 0 0;");
        applyGothamStyle(memoryLabel);

        TextField minMemoryFrequencyField = new TextField();
        minMemoryFrequencyField.setPromptText("Min Memory Frequency (MHz)");
        minMemoryFrequencyField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(minMemoryFrequencyField, "Enter minimum memory frequency");

        TextField maxMemoryFrequencyField = new TextField();
        maxMemoryFrequencyField.setPromptText("Max Memory Frequency (MHz)");
        maxMemoryFrequencyField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(maxMemoryFrequencyField, "Enter maximum memory frequency");

        HBox MemoryFrequencyRangeBox = new HBox(10);
        MemoryFrequencyRangeBox.setAlignment(Pos.CENTER_LEFT);
        MemoryFrequencyRangeBox.getChildren().addAll(minMemoryFrequencyField, maxMemoryFrequencyField);

        Label memoryFrequencyLabel = new Label("Memory Frequency Filter (MHz):");
        memoryFrequencyLabel.setStyle("-fx-padding: 20 0 0 0;");
        applyGothamStyle(memoryFrequencyLabel);

        TextField minCoreFrequencyField = new TextField();
        minCoreFrequencyField.setPromptText("Min Core Frequency (MHz)");
        minCoreFrequencyField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(minCoreFrequencyField, "Enter minimum core frequency");

        TextField maxCoreFrequencyField = new TextField();
        maxCoreFrequencyField.setPromptText("Max Core Frequency (MHz)");
        maxCoreFrequencyField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(maxCoreFrequencyField, "Enter maximum core frequency");

        HBox coreFrequencyRangeBox = new HBox(10);
        coreFrequencyRangeBox.setAlignment(Pos.CENTER_LEFT);
        coreFrequencyRangeBox.getChildren().addAll(minCoreFrequencyField, maxCoreFrequencyField);

        Label coreFrequencyLabel = new Label("Core Frequency Filter (MHz):");
        coreFrequencyLabel.setStyle("-fx-padding: 20 0 0 0;");
        applyGothamStyle(coreFrequencyLabel);

        TextField minMemoryBusField = new TextField();
        minMemoryBusField.setPromptText("Min Memory Bus");
        minMemoryBusField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(minMemoryBusField, "Enter minimum memory bus");

        TextField maxMemoryBusField = new TextField();
        maxMemoryBusField.setPromptText("Max Memory Bus");
        maxMemoryBusField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(maxMemoryBusField, "Enter maximum memory bus");

        HBox memoryBusRangeBox = new HBox(10);
        memoryBusRangeBox.setAlignment(Pos.CENTER_LEFT);
        memoryBusRangeBox.getChildren().addAll(minMemoryBusField, maxMemoryBusField);

        Label memoryBusLabel = new Label("Memory Bus Filter:");
        memoryBusLabel.setStyle("-fx-padding: 20 0 0 0;");
        applyGothamStyle(memoryBusLabel);

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        startDatePicker.getEditor().setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white;");
        addTooltip(startDatePicker, "Select start date");

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        endDatePicker.getEditor().setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white;");
        addTooltip(endDatePicker, "Select end date");

        ComboBox<String> graphicsChipComboBox = new ComboBox<>();
        graphicsChipComboBox.getItems().add("All");
        graphicsChipComboBox.getItems().addAll(getUniqueGraphicsChipsFromDB());
        graphicsChipComboBox.setValue("All");
        graphicsChipComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(graphicsChipComboBox, "Select graphics chip");
        graphicsChipComboBox.getEditor().setStyle("-fx-text-fill: white;");

        ComboBox<String> excludeGraphicsChipComboBox = new ComboBox<>();
        excludeGraphicsChipComboBox.getItems().addAll(getUniqueGraphicsChipsFromDB());
        excludeGraphicsChipComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(excludeGraphicsChipComboBox, "Exclude graphics chip");
        excludeGraphicsChipComboBox.getEditor().setStyle("-fx-text-fill: white;");

        ComboBox<String> managerComboBox = new ComboBox<>();
        managerComboBox.getItems().add("All");
        managerComboBox.getItems().addAll(getManagerNamesFromDB());
        managerComboBox.setValue("All");
        managerComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(managerComboBox, "select the manager you want to exclude");
        managerComboBox.getEditor().setStyle("-fx-text-fill: white;");

        ComboBox<String> excludedCountryComboBox = new ComboBox<>();
        excludedCountryComboBox.getItems().addAll(getExcludedCountriesFromDB());
        excludedCountryComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
        addTooltip(excludedCountryComboBox, "Exclude country");
        excludedCountryComboBox.getEditor().setStyle("-fx-text-fill: white;");

        Button applyFilterButton = borderStyledButton("Apply Filter");
        applyFilterButton.setOnAction(e -> applyFilter(
                announcementsListView,
                parseDoubleOrDefault(minPriceField.getText(), 0),
                parseDoubleOrDefault(maxPriceField.getText(), Double.MAX_VALUE),
                parseDoubleOrDefault(minMemoryField.getText(), 0),
                parseDoubleOrDefault(maxMemoryField.getText(), Double.MAX_VALUE),
                parseDoubleOrDefault(minMemoryFrequencyField.getText(), 0),
                parseDoubleOrDefault(maxMemoryFrequencyField.getText(), Double.MAX_VALUE),
                parseDoubleOrDefault(minCoreFrequencyField.getText(), 0),
                parseDoubleOrDefault(maxCoreFrequencyField.getText(), Double.MAX_VALUE),
                parseIntOrDefault(minMemoryBusField.getText(), 0),
                parseIntOrDefault(maxMemoryBusField.getText(), Integer.MAX_VALUE),
                graphicsChipComboBox.getValue(),
                excludeGraphicsChipComboBox.getValue(),
                managerComboBox.getValue(),
                excludedCountryComboBox.getValue()
        ));

        Button resetFilterButton = borderStyledButton("Reset Filter");
        resetFilterButton.setOnAction(e -> resetFilter(
                minPriceField, maxPriceField,
                minMemoryField, maxMemoryField,
                startDatePicker, endDatePicker,
                graphicsChipComboBox, excludeGraphicsChipComboBox,
                managerComboBox, excludedCountryComboBox
        ));

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(applyFilterButton, resetFilterButton);

        Label timeLabel = new Label("Time Filter:");
        applyGothamStyle(timeLabel);
        Label graphicsChipLabel = new Label("Graphics Chip Filter:");
        applyGothamStyle(graphicsChipLabel);
        Label excludeGraphicsChipLabel = new Label("Exclude Graphics Chip:");
        applyGothamStyle(excludeGraphicsChipLabel);
        Label managerLabel = new Label("Exclude Manager:");
        applyGothamStyle(managerLabel);
        Label excludedCountryLabel = new Label("Excluded Country Filter:");
        applyGothamStyle(excludedCountryLabel);

        filterLayout.getChildren().addAll(
                priceLabel, priceRangeBox,
                memoryLabel, memoryRangeBox,
                memoryFrequencyLabel, MemoryFrequencyRangeBox,
                coreFrequencyLabel, coreFrequencyRangeBox,
                memoryBusLabel, memoryBusRangeBox,
                timeLabel, new HBox(10, startDatePicker, endDatePicker),
                graphicsChipLabel, graphicsChipComboBox,
                excludeGraphicsChipLabel, excludeGraphicsChipComboBox,
                managerLabel, managerComboBox,
                excludedCountryLabel, excludedCountryComboBox,
                buttonBox
        );

        ScrollPane scrollPane = new ScrollPane(filterLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: black;");

        Scene filterScene = new Scene(scrollPane);
        filterStage.setScene(filterScene);
        filterStage.show();
    }

    private void applyFilter(ListView<Announcement> announcementsListView, double minPrice, double maxPrice,
                             double minMemory, double maxMemory, double minMemoryFrequency, double maxMemoryFrequency,
                             double minCoreFrequency, double maxCoreFrequency, int minMemoryBus, int maxMemoryBus,
                             String selectedGraphicsChip, String excludeGraphicsChip, String manager, String excludedCountry) {
        List<Announcement> filteredAnnouncements = new ArrayList<>();

        if (minPrice > maxPrice || minMemory > maxMemory || minMemoryFrequency > maxMemoryFrequency ||
                minCoreFrequency > maxCoreFrequency || minMemoryBus > maxMemoryBus) {
            showErrorAlert("Error: Minimum value cannot be greater than maximum value.");
            return;
        }

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT c.* FROM catalog c";

            // Використовуємо предикат LEFT JOIN для виключення графічного чіпа
            if (excludeGraphicsChip != null && !excludeGraphicsChip.isEmpty()) {
                sql += " LEFT JOIN catalog c1 ON c.graphics_chip = c1.graphics_chip AND c1.graphics_chip LIKE ?";
            }

            sql += " WHERE 1=1";

            // Застосовуємо фільтр за мінімальною та максимальною ціною
            if (minPrice >= 0 && maxPrice > minPrice) {
                sql += " AND c.price BETWEEN ? AND ?";
            }

            // Застосовуємо фільтр за мінімальною та максимальною пам'яттю
            if (minMemory >= 0 && maxMemory > minMemory) {
                sql += " AND c.memory_capacity BETWEEN ? AND ?";
            }

            // Застосовуємо фільтр за мінімальною та максимальною частотою пам'яті
            if (minMemoryFrequency >= 0 && maxMemoryFrequency > minMemoryFrequency) {
                sql += " AND c.memory_frequency BETWEEN ? AND ?";
            }

            // Застосовуємо фільтр за мінімальною та максимальною частотою ядра
            if (minCoreFrequency >= 0 && maxCoreFrequency > minCoreFrequency) {
                sql += " AND c.core_frequency BETWEEN ? AND ?";
            }

            // Застосовуємо фільтр за мінімальною та максимальною шириною пам'яті
            if (minMemoryBus >= 0 && maxMemoryBus > minMemoryBus) {
                sql += " AND c.bit_size_memory_bus BETWEEN ? AND ?";
            }

            // Застосовуємо фільтр за обраним графічним чіпом
            if (selectedGraphicsChip != null && !selectedGraphicsChip.equals("All")) {
                sql += " AND c.graphics_chip LIKE ?";
            }

            // Фільтр для виключення графічного чіпу
            if (excludeGraphicsChip != null && !excludeGraphicsChip.isEmpty()) {
                sql += " AND c1.graphics_chip IS NULL";
            }

            // Застосовуємо предикат NOT EXISTS для виключення менеджера
            if (manager != null && !manager.isEmpty()) {
                sql += " AND NOT EXISTS (SELECT 1 FROM catalog c2 WHERE c2.creator_name LIKE ? AND c2.product_id = c.product_id)";
            }

            // Застосовуємо предикат NOT IN для виключення країни виробника
            if (excludedCountry != null && !excludedCountry.isEmpty()) {
                sql += " AND (c.producing_country IS NULL OR c.producing_country NOT IN (?))";
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int parameterIndex = 1;

                if (excludeGraphicsChip != null && !excludeGraphicsChip.isEmpty()) {
                    statement.setString(parameterIndex++, "%" + excludeGraphicsChip + "%");
                }

                if (minPrice >= 0 && maxPrice > minPrice) {
                    statement.setDouble(parameterIndex++, minPrice);
                    statement.setDouble(parameterIndex++, maxPrice);
                }

                if (minMemory >= 0 && maxMemory > minMemory) {
                    statement.setDouble(parameterIndex++, minMemory);
                    statement.setDouble(parameterIndex++, maxMemory);
                }

                if (minMemoryFrequency >= 0 && maxMemoryFrequency > minMemoryFrequency) {
                    statement.setDouble(parameterIndex++, minMemoryFrequency);
                    statement.setDouble(parameterIndex++, maxMemoryFrequency);
                }

                if (minCoreFrequency >= 0 && maxCoreFrequency > minCoreFrequency) {
                    statement.setDouble(parameterIndex++, minCoreFrequency);
                    statement.setDouble(parameterIndex++, maxCoreFrequency);
                }

                if (minMemoryBus >= 0 && maxMemoryBus > minMemoryBus) {
                    statement.setInt(parameterIndex++, minMemoryBus);
                    statement.setInt(parameterIndex++, maxMemoryBus);
                }

                if (selectedGraphicsChip != null && !selectedGraphicsChip.equals("All")) {
                    statement.setString(parameterIndex++, "%" + selectedGraphicsChip + "%");
                }

                if (manager != null && !manager.isEmpty()) {
                    statement.setString(parameterIndex++, "%" + manager + "%");
                }

                if (excludedCountry != null && !excludedCountry.isEmpty()) {
                    statement.setString(parameterIndex++, excludedCountry);
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Announcement announcement = new Announcement(
                                resultSet.getString("product_title"),
                                resultSet.getInt("product_id"),
                                resultSet.getString("graphics_chip"),
                                resultSet.getDouble("memory_frequency"),
                                resultSet.getDouble("core_frequency"),
                                resultSet.getInt("memory_capacity"),
                                resultSet.getInt("bit_size_memory_bus"),
                                resultSet.getString("maximum_supported_resolution"),
                                resultSet.getInt("minimum_required_BZ_capacity"),
                                resultSet.getString("memory_type"),
                                resultSet.getString("producing_country"),
                                resultSet.getString("supported_3D_APIS"),
                                resultSet.getString("form_factor"),
                                resultSet.getString("type_of_cooling_system"),
                                resultSet.getInt("guarantee"),
                                resultSet.getDouble("price"),
                                resultSet.getDouble("wholesale_price"),
                                resultSet.getInt("wholesale_quantity"),
                                resultSet.getString("brand"),
                                resultSet.getString("product_description"),
                                resultSet.getTimestamp("creation_data").toLocalDateTime(),
                                resultSet.getString("creator_name"),
                                resultSet.getInt("available_quantity")
                        );
                        filteredAnnouncements.add(announcement);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error: " + e.getMessage());
        }

        if (filteredAnnouncements.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Filtered Announcements");
            alert.setHeaderText(null);
            alert.setContentText("No announcements match the filter criteria.");
            alert.showAndWait();
        }

        announcementsListView.getItems().setAll(filteredAnnouncements);
    }

    private void resetFilter( TextField minPriceField, TextField maxPriceField,
    TextField minMemoryField, TextField maxMemoryField,
    DatePicker startDatePicker, DatePicker endDatePicker,
    ComboBox<String> graphicsChipComboBox, ComboBox<String> excludeGraphicsChipComboBox,
    ComboBox<String> managerComboBox, ComboBox<String> excludedCountryComboBox) {
        minPriceField.clear();
        maxPriceField.clear();
        minMemoryField.clear();
        maxMemoryField.clear();
        startDatePicker.getEditor().clear();
        endDatePicker.getEditor().clear();
        graphicsChipComboBox.getSelectionModel().clearSelection();
        excludeGraphicsChipComboBox.getSelectionModel().clearSelection();
        managerComboBox.getSelectionModel().clearSelection();
        excludedCountryComboBox.getSelectionModel().clearSelection();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT * FROM catalog";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Announcement> allAnnouncements = new ArrayList<>();
                    while (resultSet.next()) {
                        Announcement announcement = new Announcement(
                                resultSet.getString("product_title"),
                                resultSet.getInt("product_id"),
                                resultSet.getString("graphics_chip"),
                                resultSet.getDouble("memory_frequency"),
                                resultSet.getDouble("core_frequency"),
                                resultSet.getInt("memory_capacity"),
                                resultSet.getInt("bit_size_memory_bus"),
                                resultSet.getString("maximum_supported_resolution"),
                                resultSet.getInt("minimum_required_BZ_capacity"),
                                resultSet.getString("memory_type"),
                                resultSet.getString("producing_country"),
                                resultSet.getString("supported_3D_APIS"),
                                resultSet.getString("form_factor"),
                                resultSet.getString("type_of_cooling_system"),
                                resultSet.getInt("guarantee"),
                                resultSet.getDouble("price"),
                                resultSet.getDouble("wholesale_price"),
                                resultSet.getInt("wholesale_quantity"),
                                resultSet.getString("brand"),
                                resultSet.getString("product_description"),
                                resultSet.getTimestamp("creation_data").toLocalDateTime(),
                                resultSet.getString("creator_name"),
                                resultSet.getInt("available_quantity")
                        );
                        allAnnouncements.add(announcement);
                    }
                    announcementsListView.getItems().setAll(allAnnouncements);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error: " + e.getMessage());
        }
    }

    private List<String> getUniqueGraphicsChipsFromDB() {
        List<String> graphicsChips = new ArrayList<>();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT DISTINCT graphics_chip FROM catalog";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    graphicsChips.add(resultSet.getString("graphics_chip"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error: " + e.getMessage());
        }

        return graphicsChips;
    }

    private List<String> getManagerNamesFromDB() {
        List<String> managerNames = new ArrayList<>();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT DISTINCT creator_name FROM catalog";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    managerNames.add(resultSet.getString("creator_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error: " + e.getMessage());
        }

        return managerNames;
    }

    private List<String> getExcludedCountriesFromDB() {
        List<String> excludedCountries = new ArrayList<>();
        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT DISTINCT producing_country FROM catalog WHERE producing_country IS NOT NULL";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    excludedCountries.add(resultSet.getString("producing_country"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return excludedCountries;
    }

    private double parseDoubleOrDefault(String str, double defaultValue) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int parseIntOrDefault(String text, int defaultValue) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void addTooltip(Control control, String text) {
        Tooltip tooltip = new Tooltip(text);
        Tooltip.install(control, tooltip);
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

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
