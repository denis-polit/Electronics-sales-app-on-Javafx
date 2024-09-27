import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Pos;

public class LoadingIndicatorUtil {

    public static Stage showLoadingIndicator(Stage ownerStage) {
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(50, 50);
        loadingIndicator.setStyle("-fx-progress-color: white;");

        StackPane loadingPane = new StackPane(loadingIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setStyle("-fx-background-color: black; -fx-padding: 50;");

        Scene loadingScene = new Scene(loadingPane, 900, 600);
        Stage loadingStage = new Stage();
        loadingStage.setScene(loadingScene);
        loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.initOwner(ownerStage);
        loadingStage.setAlwaysOnTop(true);
        loadingStage.show();

        return loadingStage;
    }

    public static void hideLoadingIndicator(Stage loadingStage) {
        if (loadingStage != null) {
            loadingStage.close();
        }
    }
}