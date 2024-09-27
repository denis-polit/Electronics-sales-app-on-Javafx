import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CatalogDataBase {
    private final AlertService alertService = new AlertServiceImpl();
    private FirstConnectionToDataBase connectionToDataBase;

    public CatalogDataBase() {
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to connect to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase == null) {
            throw new SQLException("Database connection is not established.");
        }
        return connectionToDataBase.getConnection();
    }

    public List<Announcement> getAllAnnouncementsFromDatabase() {
        List<Announcement> announcements = new ArrayList<>();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT * FROM catalog";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String title = resultSet.getString("product_title");
                    int productId = resultSet.getInt("product_id");
                    String graphicsChip = resultSet.getString("graphics_chip");
                    double memoryFrequency = resultSet.getDouble("memory_frequency");
                    double coreFrequency = resultSet.getDouble("core_frequency");
                    int memoryCapacity = resultSet.getInt("memory_capacity");
                    int bitSizeMemoryBus = resultSet.getInt("bit_size_memory_bus");
                    String maxSupportedResolution = resultSet.getString("maximum_supported_resolution");
                    int minRequiredBZCapacity = resultSet.getInt("minimum_required_BZ_capacity");
                    String memoryType = resultSet.getString("memory_type");
                    String producingCountry = resultSet.getString("producing_country");
                    String supported3DApis = resultSet.getString("supported_3D_APIS");
                    String formFactor = resultSet.getString("form_factor");
                    String coolingSystemType = resultSet.getString("type_of_cooling_system");
                    int guarantee = resultSet.getInt("guarantee");
                    double price = resultSet.getDouble("price");
                    double wholesalePrice = resultSet.getDouble("wholesale_price");
                    int wholesaleQuantity = resultSet.getInt("wholesale_quantity");
                    String brand = resultSet.getString("brand");
                    String description = resultSet.getString("product_description");
                    LocalDateTime date = resultSet.getTimestamp("creation_data").toLocalDateTime();
                    String creatorName = resultSet.getString("creator_name");
                    int availableQuantity = resultSet.getInt("available_quantity");

                    Announcement announcement = new Announcement(title, productId, graphicsChip,
                            memoryFrequency, coreFrequency, memoryCapacity, bitSizeMemoryBus,
                            maxSupportedResolution, minRequiredBZCapacity, memoryType,
                            producingCountry, supported3DApis, formFactor, coolingSystemType,
                            guarantee, price, wholesalePrice, wholesaleQuantity, brand, description,
                            date, creatorName, availableQuantity);
                    announcements.add(announcement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        return announcements;
    }

    public List<Announcement> searchAnnouncementsByFields(String searchSql, String searchText) {
        List<Announcement> searchResults = new ArrayList<>();

        try (Connection connection = establishDBConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(searchSql)) {
                for (int i = 1; i <= 9; i++) {
                    statement.setString(i, "%" + searchText + "%");
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String title = resultSet.getString("product_title");
                        int productId = resultSet.getInt("product_id");
                        String graphicsChip = resultSet.getString("graphics_chip");
                        double memoryFrequency = resultSet.getDouble("memory_frequency");
                        double coreFrequency = resultSet.getDouble("core_frequency");
                        int memoryCapacity = resultSet.getInt("memory_capacity");
                        int bitSizeMemoryBus = resultSet.getInt("bit_size_memory_bus");
                        String maxSupportedResolution = resultSet.getString("maximum_supported_resolution");
                        int minRequiredBZCapacity = resultSet.getInt("minimum_required_BZ_capacity");
                        String memoryType = resultSet.getString("memory_type");
                        String producingCountry = resultSet.getString("producing_country");
                        String supported3DApis = resultSet.getString("supported_3D_APIS");
                        String formFactor = resultSet.getString("form_factor");
                        String coolingSystemType = resultSet.getString("type_of_cooling_system");
                        int guarantee = resultSet.getInt("guarantee");
                        double price = resultSet.getDouble("price");
                        double wholesalePrice = resultSet.getDouble("wholesale_price");
                        int wholesaleQuantity = resultSet.getInt("wholesale_quantity");
                        String brand = resultSet.getString("brand");
                        String description = resultSet.getString("product_description");
                        LocalDateTime date = resultSet.getTimestamp("creation_data").toLocalDateTime();
                        String creatorName = resultSet.getString("creator_name");
                        int availableQuantity = resultSet.getInt("available_quantity");

                        Announcement announcement = new Announcement(title, productId, graphicsChip,
                                memoryFrequency, coreFrequency, memoryCapacity, bitSizeMemoryBus,
                                maxSupportedResolution, minRequiredBZCapacity, memoryType,
                                producingCountry, supported3DApis, formFactor, coolingSystemType,
                                guarantee, price, wholesalePrice, wholesaleQuantity, brand, description,
                                date, creatorName, availableQuantity);
                        searchResults.add(announcement);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        return searchResults;
    }
}