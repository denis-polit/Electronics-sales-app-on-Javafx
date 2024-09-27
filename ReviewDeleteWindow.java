import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReviewDeleteWindow {

    private final FirstConnectionToDataBase connectionToDataBase;
    private final AlertService alertService;
    private final ReviewsPage reviewsPage;

    public ReviewDeleteWindow(FirstConnectionToDataBase connectionToDataBase, AlertService alertService, ReviewsPage reviewsPage) {
        this.connectionToDataBase = connectionToDataBase;
        this.alertService = alertService;
        this.reviewsPage = reviewsPage;
    }

    public void deleteReview(Review review) {
        if (alertService.showConfirmationAlert("Are you sure you want to delete this comment?")) {
            try {
                Connection connection = establishDBConnection();
                String query = "DELETE FROM reviews WHERE id_review = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setInt(1, review.getId());
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        alertService.showSuccessAlert("Review deleted successfully.");
                        reviewsPage.updateDisplayedReviews(); // Call to update the displayed reviews
                    } else {
                        alertService.showErrorAlert("Review not found.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                alertService.showErrorAlert("Error deleting comment from database: " + e.getMessage());
            }
        }
    }

    private Connection establishDBConnection() throws SQLException {
        Connection connection = connectionToDataBase.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Connection is null or closed.");
        }
        return connection;
    }
}