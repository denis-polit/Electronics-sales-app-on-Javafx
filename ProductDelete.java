import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProductDelete {
    private final SessionManager sessionManager;
    private final FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService;

    public ProductDelete() {
        sessionManager = SessionManager.getInstance();
        alertService = new AlertServiceImpl();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish database connection: " + e.getMessage(), e);
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void handleDeleteProduct(Announcement announcement) {
        int productId = announcement.getProductId();
        String sql = "DELETE FROM catalog WHERE product_id = ?";

        if (!sessionManager.isManagerEnter()) {
            alertService.showErrorAlert("You do not have permission to delete announcements.");
            return;
        }

        try (Connection connection = establishDBConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                alertService.showSuccessAlert("The announcement has been successfully deleted.");
                logDeletionActivity(announcement);
            } else {
                alertService.showErrorAlert("Failed to delete the announcement. Please try again.");
            }

        } catch (SQLException e) {
            alertService.showErrorAlert("An error occurred while deleting the announcement. Please try again later.");
        }
    }

    private void logDeletionActivity(Announcement announcement) {
        if (!sessionManager.isManagerEnter()) {
            return;
        }

        String actionType = "Delete";
        String objectType = "Announcement";
        String details = "Manager " + sessionManager.getCurrentManagerName() + " deleted announcement with product ID " + announcement.getProductId() + ".";

        int managerId = sessionManager.getEmployeeIdByName(sessionManager.getCurrentManagerName());
        sessionManager.logManagerActivity(managerId, actionType, objectType, details);
    }
}
