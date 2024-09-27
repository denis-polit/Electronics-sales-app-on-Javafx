import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class SortCatalog {

    private final List<Announcement> announcementsList;
    private final ListView<Announcement> announcementsListView;
    private final FirstConnectionToDataBase connectionToDataBase;

    public SortCatalog(List<Announcement> announcementsList, ListView<Announcement> announcementsListView) {
        this.announcementsList = announcementsList;
        this.announcementsListView = announcementsListView;
        SessionManager sessionManager = SessionManager.getInstance();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish database connection: " + e.getMessage(), e);
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void showSortForm() {
        Stage sortStage = new Stage();
        VBox sortLayout = new VBox(10);
        sortLayout.setPadding(new Insets(20));
        sortLayout.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        sortLayout.setAlignment(Pos.CENTER);

        Button sortByPriceAscending = ButtonStyle.expandPaneStyledButton("From cheap to expensive");
        sortByPriceAscending.setOnAction(e -> sortAnnouncementsByPriceAscending());

        Button sortByPriceDescending = ButtonStyle.expandPaneStyledButton("From expensive to cheap");
        sortByPriceDescending.setOnAction(e -> sortAnnouncementsByPriceDescending());

        Button sortByDateAscending = ButtonStyle.expandPaneStyledButton("        From new to old        ");
        sortByDateAscending.setOnAction(e -> sortAnnouncementsByIdAscending());

        Button sortByDateDescending = ButtonStyle.expandPaneStyledButton("        From old to new        ");
        sortByDateDescending.setOnAction(e -> sortAnnouncementsByIdDescending());

        sortLayout.getChildren().addAll(
                sortByPriceAscending,
                sortByPriceDescending,
                sortByDateAscending,
                sortByDateDescending
        );

        Scene sortScene = new Scene(sortLayout, 400, 400);
        sortScene.setFill(Color.BLACK);

        sortStage.setScene(sortScene);
        sortStage.setTitle("Sort form");
        sortStage.show();
    }

    private void sortAnnouncementsByPriceAscending() {
        announcementsList.sort(Comparator.comparingDouble(Announcement::getPrice));
        announcementsListView.getItems().setAll(announcementsList);
    }

    private void sortAnnouncementsByPriceDescending() {
        announcementsList.sort((a1, a2) -> Double.compare(a2.getPrice(), a1.getPrice()));
        announcementsListView.getItems().setAll(announcementsList);
    }

    private void sortAnnouncementsByIdAscending() {
        announcementsList.sort((a1, a2) -> Integer.compare(a2.getProductId(), a1.getProductId()));
        announcementsListView.getItems().setAll(announcementsList);
    }

    private void sortAnnouncementsByIdDescending() {
        announcementsList.sort(Comparator.comparingInt(Announcement::getProductId));
        announcementsListView.getItems().setAll(announcementsList);
    }
}
