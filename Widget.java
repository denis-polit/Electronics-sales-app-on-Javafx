import javafx.scene.image.Image;

public class Widget {
    private String imagePath;
    private int orderNumber;

    public Widget(String imagePath, int orderNumber) {
        this.imagePath = imagePath;
        this.orderNumber = orderNumber;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public Image getImage() {
        try {
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                return new Image(imagePath);
            } else {
                String baseDir = System.getProperty("user.dir");

                String cleanedPath = imagePath.replaceAll("//", "/");

                if (cleanedPath.endsWith(".png")) {
                    cleanedPath = cleanedPath.replace(".png", ".jpg");
                }

                String fullPath = "file:" + baseDir + "/" + cleanedPath;

                return new Image(fullPath);
            }
        } catch (Exception e) {
            System.err.println("Unable to load image for widget with order number: " + orderNumber);
            return null;
        }
    }
}