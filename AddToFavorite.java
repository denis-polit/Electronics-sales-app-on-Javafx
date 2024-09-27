import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class AddToFavorite {

    private final SessionManager sessionManager;
    private final FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService;

    public AddToFavorite() {
        sessionManager = SessionManager.getInstance();
        alertService = new AlertServiceImpl();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void handleAddToFavoritesButtonClick(Announcement announcement, String username) {
        if (!sessionManager.isClientEnter() && !sessionManager.isManagerEnter()) {
            alertService.showErrorAlert("Please log in or register to add items to favorites.");
            return;
        }

        String updateFavoritesQuery;
        if (sessionManager.isManagerEnter()) {
            int managerId = sessionManager.getEmployeeIdByName(sessionManager.getCurrentManagerName());
            updateFavoritesQuery = "INSERT INTO employee_favorites (manager_id, product_id) VALUES (?, ?)";
            try (Connection connection = establishDBConnection();
                 PreparedStatement statement = connection.prepareStatement(updateFavoritesQuery)) {

                statement.setInt(1, managerId);
                statement.setInt(2, announcement.getProductId());

                try {
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        alertService.showSuccessAlert("The product has been successfully added to the favorites list.");
                        logActivity(announcement);
                    } else {
                        alertService.showErrorAlert("The item has not been added to the favorites list. Please try again.");
                    }
                } catch (SQLIntegrityConstraintViolationException e) {
                    alertService.showErrorAlert("This product is already in your favorites list.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    alertService.showErrorAlert("Error adding to favorites: " + e.getMessage());
                }

            } catch (SQLException e) {
                e.printStackTrace();
                alertService.showErrorAlert("Error establishing database connection: " + e.getMessage());
            }

        } else {
            int clientId = sessionManager.getClientIdByName(sessionManager.getCurrentClientName());
            updateFavoritesQuery = "INSERT INTO user_favorites (user_id, product_id) VALUES (?, ?)";
            try (Connection connection = establishDBConnection();
                 PreparedStatement statement = connection.prepareStatement(updateFavoritesQuery)) {

                statement.setInt(1, clientId);
                statement.setInt(2, announcement.getProductId());

                try {
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        alertService.showSuccessAlert("The product has been successfully added to the favorites list.");
                        logActivity(announcement);
                    } else {
                        alertService.showErrorAlert("The item has not been added to the favorites list. Please try again.");
                    }
                } catch (SQLIntegrityConstraintViolationException e) {
                    alertService.showErrorAlert("This product is already in your favorites list.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    alertService.showErrorAlert("Error adding to favorites: " + e.getMessage());
                }

            } catch (SQLException e) {
                e.printStackTrace();
                alertService.showErrorAlert("Error establishing database connection: " + e.getMessage());
            }
        }
    }

    private void logActivity(Announcement announcement) {
        String actionType = "AddToFavorites";
        String objectType = "Product";
        String details = (sessionManager.isManagerEnter() ? "Manager " + sessionManager.getCurrentManagerName() : "User " + sessionManager.getCurrentClientName())
                + " added product ID " + announcement.getProductId() + " to favorites.";

        if (sessionManager.isManagerEnter()) {
            int managerId = sessionManager.getEmployeeIdByName(sessionManager.getCurrentManagerName());
            sessionManager.logManagerActivity(managerId, actionType, objectType, details);
        } else if (sessionManager.isClientEnter()) {
            int clientId = sessionManager.getClientIdByName(sessionManager.getCurrentClientName());
            sessionManager.logActivity(clientId, actionType, objectType, details);
        }
    }
}