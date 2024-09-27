import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.SQLException;

public class ParseToSuppliersCatalogs {

    private final SessionManager sessionManager;
    private final FirstConnectionToDataBase connectionToDataBase;
    private final Application application;

    public ParseToSuppliersCatalogs(Application application) {
        this.application = application;
        sessionManager = SessionManager.getInstance();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            showErrorAlert("Failed to establish database connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void fetchMOYOProducts(Supplier supplier) {
        System.out.println("opened moyo");

        Stage productStage = new Stage();
        productStage.setTitle("List of Products");
        productStage.initModality(Modality.APPLICATION_MODAL);

        VBox productLayout = new VBox(10);
        productLayout.setPadding(new Insets(10));
        productLayout.setStyle("-fx-background-color: black;");
        productLayout.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(productLayout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 300, 600);
        productStage.setScene(scene);

        productStage.show();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int totalProducts = 0;
                int addedProducts = 0;
                int skippedProducts = 0;
                for (int page = 1; page <= 20; page++) {
                    String url = "https://www.moyo.ua/comp-and-periphery/periphery-and-compon/videokarty/?sort=novinka-desc&page=" + page;
                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .referrer("https://www.moyo.ua/")
                            .get();

                    Elements productCards = doc.select("div.product-card");
                    totalProducts += productCards.size();

                    for (Element product : productCards) {
                        Element titleElement = product.selectFirst("a.product-card_title.gtm-link-product");
                        String title = titleElement.text();
                        String link = titleElement.absUrl("href");

                        Element priceElement = product.selectFirst("div.product-card_price_current");
                        String price = priceElement.text();

                        Element statusElement = product.selectFirst("div.product-card_price_text");
                        String status = statusElement != null ? statusElement.text() : "";

                        if (title.isEmpty() || link.isEmpty()) {
                            skippedProducts++;
                            System.out.println("Продукт пропущен: " + title);
                            continue;
                        }

                        System.out.println("Название: " + title);
                        System.out.println("Цена: " + price);
                        System.out.println("Ссылка: " + link);
                        System.out.println("Статус: " + status);

                        Platform.runLater(() -> {
                            Label titleLabel = new Label(title);
                            titleLabel.setTextFill(Color.WHITE);
                            titleLabel.setFont(Font.font("Gotham", 16));
                            titleLabel.setWrapText(true);

                            Label priceLabel = new Label(price);
                            priceLabel.setTextFill(Color.WHITE);
                            priceLabel.setFont(Font.font("Gotham", 14));

                            Label statusLabel = new Label(status);
                            statusLabel.setTextFill(Color.WHITE);
                            statusLabel.setFont(Font.font("Gotham", 14));

                            Hyperlink productLink = new Hyperlink("Order Here");
                            productLink.setTextFill(Color.WHITE);
                            productLink.setFont(Font.font("Gotham", 14));
                            productLink.setStyle("-fx-underline: true;");
                            productLink.setOnMouseEntered(e -> productLink.setTextFill(Color.BLUE));
                            productLink.setOnMouseExited(e -> productLink.setTextFill(Color.WHITE));
                            productLink.setOnAction(e -> application.getHostServices().showDocument(link));

                            VBox productBox = new VBox(5, titleLabel, priceLabel, statusLabel, productLink);
                            productBox.setAlignment(Pos.TOP_LEFT);
                            productBox.setStyle("-fx-border-color: white; -fx-border-radius: 15px; -fx-border-width: 1px; -fx-padding: 10px;");

                            productLayout.getChildren().add(productBox);
                        });

                        addedProducts++;
                    }
                }
                System.out.println("Общее количество продуктов: " + totalProducts);
                System.out.println("Добавлено продуктов на экран: " + addedProducts);
                System.out.println("Пропущено продуктов: " + skippedProducts);
                return null;
            }
        };

        task.setOnFailed(event -> {
            Throwable throwable = task.getException();
            System.err.println("Error in fetchMOYOProducts: " + throwable.getMessage());
            throwable.printStackTrace();
        });

        new Thread(task).start();
    }

    public void fetchCLICKProducts(Supplier supplier) {
        System.out.println("opened click.ua");

        Stage productStage = new Stage();
        productStage.setTitle("List of Products - click.ua");
        productStage.initModality(Modality.APPLICATION_MODAL);

        VBox productLayout = new VBox(10);
        productLayout.setPadding(new Insets(10));
        productLayout.setStyle("-fx-background-color: black;");
        productLayout.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(productLayout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 300, 600);
        productStage.setScene(scene);


        productStage.show();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int totalProducts = 0;
                int addedProducts = 0;
                int skippedProducts = 0;
                String baseUrl = "https://click.ua/ru/shop/videokartyi/page=";

                for (int page = 1; page <= 18; page++) {
                    String url = baseUrl + page;

                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .referrer("https://click.ua/")
                            .get();

                    Elements productItems = doc.select("div.product");
                    totalProducts += productItems.size();

                    for (Element product : productItems) {
                        String title = product.select("div.product__title a").text();
                        String link = product.select("div.product__title a").attr("href");
                        String price = product.select("div.product__price b").text();
                        String status = product.select("div.product__price div.fs12.color").text();
                        String description = product.select("div.product__description").text();

                        if (title.isEmpty() || link.isEmpty()) {
                            skippedProducts++;
                            System.out.println("Product skipped: " + title);
                            continue;
                        }

                        System.out.println("Title: " + title);
                        System.out.println("Price: " + price);
                        System.out.println("Link: " + link);
                        System.out.println("Status: " + status);
                        System.out.println("Description: " + description);

                        Platform.runLater(() -> {
                            Label titleLabel = new Label(title);
                            titleLabel.setTextFill(Color.WHITE);
                            titleLabel.setFont(Font.font("Gotham", 16));
                            titleLabel.setWrapText(true);

                            Label priceLabel = new Label(price);
                            priceLabel.setTextFill(Color.WHITE);
                            priceLabel.setFont(Font.font("Gotham", 14));

                            Label statusLabel = new Label(status);
                            statusLabel.setTextFill(Color.WHITE);
                            statusLabel.setFont(Font.font("Gotham", 14));

                            Label descriptionLabel = new Label(description);
                            descriptionLabel.setTextFill(Color.WHITE);
                            descriptionLabel.setFont(Font.font("Gotham", 14));
                            descriptionLabel.setWrapText(true);

                            Hyperlink productLink = new Hyperlink("Order Here");
                            productLink.setTextFill(Color.WHITE);
                            productLink.setFont(Font.font("Gotham", 14));
                            productLink.setStyle("-fx-underline: true;");
                            productLink.setOnMouseEntered(e -> productLink.setTextFill(Color.BLUE));
                            productLink.setOnMouseExited(e -> productLink.setTextFill(Color.WHITE));
                            productLink.setOnAction(e -> application.getHostServices().showDocument(link));

                            VBox productBox = new VBox(5, titleLabel, priceLabel, statusLabel, descriptionLabel, productLink);
                            productBox.setAlignment(Pos.TOP_LEFT);
                            productBox.setStyle("-fx-border-color: white; -fx-border-radius: 15px; -fx-border-width: 1px; -fx-padding: 10px;");

                            productLayout.getChildren().add(productBox);
                        });

                        addedProducts++;
                    }
                }
                System.out.println("Total number of products: " + totalProducts);
                System.out.println("Added products to screen: " + addedProducts);
                System.out.println("Skipped products: " + skippedProducts);
                return null;
            }
        };

        task.setOnFailed(event -> {
            Throwable throwable = task.getException();
            System.err.println("Error in fetchCLICKProducts: " + throwable.getMessage());
            throwable.printStackTrace();
        });

        new Thread(task).start();
    }

    public void fetchAKSProducts(Supplier supplier) {
        System.out.println("opened aks");

        Stage productStage = new Stage();
        productStage.setTitle("List of Products - AKS");
        productStage.initModality(Modality.APPLICATION_MODAL);

        VBox productLayout = new VBox(10);
        productLayout.setPadding(new Insets(10));
        productLayout.setStyle("-fx-background-color: black;");
        productLayout.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(productLayout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 300, 600);
        productStage.setScene(scene);

        productStage.show();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int totalProducts = 0;
                int addedProducts = 0;
                int skippedProducts = 0;
                String url = "https://www.aks.ua/catalog/videokarta/sort/latest/page=";

                for (int page = 1; page <= 20; page++) {
                    Document doc = Jsoup.connect(url + page)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .referrer("https://www.aks.ua/")
                            .get();

                    Elements productItems = doc.select("div.catalog-item");
                    totalProducts += productItems.size();

                    for (Element product : productItems) {
                        String title = product.select("div.catalog-name a").text();
                        String link = "https://www.aks.ua" + product.select("div.catalog-name a").attr("href");
                        String price = product.select("div.catalog-price-new").text();
                        String status = product.select("div.catalog-bottom-box div.catalog-item-id").text();
                        String description = product.select("div.short-description").text();

                        if (title.isEmpty() || link.isEmpty()) {
                            skippedProducts++;
                            System.out.println("Продукт пропущен: " + title);
                            continue;
                        }

                        System.out.println("Название: " + title);
                        System.out.println("Цена: " + price);
                        System.out.println("Ссылка: " + link);
                        System.out.println("Статус: " + status);
                        System.out.println("Описание: " + description);

                        Platform.runLater(() -> {
                            Label titleLabel = new Label(title);
                            titleLabel.setTextFill(Color.WHITE);
                            titleLabel.setFont(Font.font("Gotham", 16));
                            titleLabel.setWrapText(true);

                            Label priceLabel = new Label(price);
                            priceLabel.setTextFill(Color.WHITE);
                            priceLabel.setFont(Font.font("Gotham", 14));

                            Label statusLabel = new Label(status);
                            statusLabel.setTextFill(Color.WHITE);
                            statusLabel.setFont(Font.font("Gotham", 14));

                            Label descriptionLabel = new Label(description);
                            descriptionLabel.setTextFill(Color.WHITE);
                            descriptionLabel.setFont(Font.font("Gotham", 14));
                            descriptionLabel.setWrapText(true);

                            Hyperlink productLink = new Hyperlink("Order Here");
                            productLink.setTextFill(Color.WHITE);
                            productLink.setFont(Font.font("Gotham", 14));
                            productLink.setStyle("-fx-underline: true;");
                            productLink.setOnMouseEntered(e -> productLink.setTextFill(Color.BLUE));
                            productLink.setOnMouseExited(e -> productLink.setTextFill(Color.WHITE));
                            productLink.setOnAction(e -> application.getHostServices().showDocument(link));

                            VBox productBox = new VBox(5, titleLabel, priceLabel, statusLabel, descriptionLabel, productLink);
                            productBox.setAlignment(Pos.TOP_LEFT);
                            productBox.setStyle("-fx-border-color: white; -fx-border-radius: 15px; -fx-border-width: 1px; -fx-padding: 10px;");

                            productLayout.getChildren().add(productBox);
                        });

                        addedProducts++;
                    }
                }
                System.out.println("Общее количество продуктов: " + totalProducts);
                System.out.println("Добавлено продуктов на экран: " + addedProducts);
                System.out.println("Пропущено продуктов: " + skippedProducts);
                return null;
            }
        };

        task.setOnFailed(event -> {
            Throwable throwable = task.getException();
            System.err.println("Error in fetchAKSProducts: " + throwable.getMessage());
            throwable.printStackTrace();
        });

        new Thread(task).start();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }
}
