import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StockDB {

    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public StockDB() {
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    public List<AvaliableAnnouncement> getAllAnnouncementsFromDatabase() {
        List<AvaliableAnnouncement> announcements = new ArrayList<>();

        try (Connection connection = establishDBConnection()) {
            String sql = "SELECT * FROM avaliable_product_for_catalog";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    AvaliableAnnouncement announcement = new AvaliableAnnouncement(
                            resultSet.getInt("apfc_id"),
                            resultSet.getString("apfc_graphics_chip"),
                            resultSet.getDouble("apfc_memory_frequency"),
                            resultSet.getDouble("apfc_core_frequency"),
                            resultSet.getInt("apfc_memory_capacity"),
                            resultSet.getInt("apfc_bit_size_memory_bus"),
                            resultSet.getString("apfc_maximum_supported_resolution"),
                            resultSet.getInt("apfc_minimum_required_BZ_capacity"),
                            resultSet.getString("apfc_memory_type"),
                            resultSet.getString("apfc_producing_country"),
                            resultSet.getString("apfc_supported_3D_APIs"),
                            resultSet.getString("apfc_form_factor"),
                            resultSet.getString("apfc_type_of_cooling_system"),
                            resultSet.getInt("apfc_guarantee"),
                            resultSet.getDouble("apfc_price"),
                            resultSet.getDouble("apfc_wholesale_price"),
                            resultSet.getString("apfc_brand"),
                            resultSet.getString("apfc_product_title"),
                            resultSet.getTimestamp("apfc_creation_data").toLocalDateTime(),
                            resultSet.getString("apfc_product_description"),
                            resultSet.getString("apfc_responsible_person"),
                            resultSet.getInt("apfc_available_quantity"),
                            resultSet.getInt("apfc_wholesale_quantity"),
                            resultSet.getString("apfc_status")
                    );
                    announcements.add(announcement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error: " + e.getMessage());
        }

        return announcements;
    }

    public List<AvaliableAnnouncement> searchAnnouncementsByFields(String searchSql, String searchText) {
        List<AvaliableAnnouncement> searchResults = new ArrayList<>();

        try (Connection connection = establishDBConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(searchSql)) {
                for (int i = 1; i <= 9; i++) {
                    statement.setString(i, "%" + searchText + "%");
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        AvaliableAnnouncement announcement = new AvaliableAnnouncement(
                                resultSet.getInt("apfc_id"),
                                resultSet.getString("apfc_graphics_chip"),
                                resultSet.getDouble("apfc_memory_frequency"),
                                resultSet.getDouble("apfc_core_frequency"),
                                resultSet.getInt("apfc_memory_capacity"),
                                resultSet.getInt("apfc_bit_size_memory_bus"),
                                resultSet.getString("apfc_maximum_supported_resolution"),
                                resultSet.getInt("apfc_minimum_required_BZ_capacity"),
                                resultSet.getString("apfc_memory_type"),
                                resultSet.getString("apfc_producing_country"),
                                resultSet.getString("apfc_supported_3D_APIs"),
                                resultSet.getString("apfc_form_factor"),
                                resultSet.getString("apfc_type_of_cooling_system"),
                                resultSet.getInt("apfc_guarantee"),
                                resultSet.getDouble("apfc_price"),
                                resultSet.getDouble("apfc_wholesale_price"),
                                resultSet.getString("apfc_brand"),
                                resultSet.getString("apfc_product_title"),
                                resultSet.getTimestamp("apfc_creation_data").toLocalDateTime(),
                                resultSet.getString("apfc_product_description"),
                                resultSet.getString("apfc_responsible_person"),
                                resultSet.getInt("apfc_available_quantity"),
                                resultSet.getInt("apfc_wholesale_quantity"),
                                resultSet.getString("apfc_status")
                        );
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
