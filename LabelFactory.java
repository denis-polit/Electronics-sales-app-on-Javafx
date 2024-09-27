import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.math.BigDecimal;

public class LabelFactory {

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public Label createLabelWithHighlight(String text, String baseStyle) {
        Label label = new Label(text);
        label.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
        label.setStyle(baseStyle);
        return label;
    }

    public Label createLabelWithHighlight(String labelText, String value1, String value2, String baseStyle, String highlightStyle) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
        label.setStyle(value1.equals(value2) ? baseStyle : highlightStyle);
        return label;
    }

    public Label createLabelWithHighlight(String text, double value1, double value2, String baseStyle, String highlightStyle) {
        Label label = new Label(text);
        label.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
        label.setStyle(value1 > value2 ? highlightStyle : baseStyle);
        return label;
    }

    public Label createLabelWithHighlight(String text, int value1, int value2, String baseStyle, String highlightStyle) {
        Label label = new Label(text);
        label.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
        label.setStyle(value1 > value2 ? highlightStyle : baseStyle);
        return label;
    }

    public Label createLabelWithHighlightForChip(String labelText, String value1, String value2, String baseStyle, String highlightStyle) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));
        if (isNumeric(value1) && isNumeric(value2)) {
            int numValue1 = Integer.parseInt(value1.replaceAll("[^0-9]", ""));
            int numValue2 = Integer.parseInt(value2.replaceAll("[^0-9]", ""));
            label.setStyle(numValue1 > numValue2 ? highlightStyle : baseStyle);
        } else {
            label.setStyle(value1.contains("ti") && !value2.contains("ti") || value1.contains("super") && !value2.contains("super") ? highlightStyle : baseStyle);
        }
        return label;
    }

    public Label createLabelWithHighlightForPrice(String labelText, double value1, double value2, String baseStyle, String highlightStyle, Label priceLabel1, Label priceLabel2) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Gotham", FontWeight.NORMAL, 16));

        BigDecimal price1 = BigDecimal.valueOf(value1);
        BigDecimal price2 = BigDecimal.valueOf(value2);
        int comparisonResult = price1.compareTo(price2);

        if (comparisonResult < 0) {
            label.setStyle(highlightStyle);
            if (priceLabel1 != null && priceLabel2 != null) {
                priceLabel1.setStyle(baseStyle);
                priceLabel2.setStyle(highlightStyle);
            }
        } else {
            label.setStyle(baseStyle);
            if (priceLabel1 != null && priceLabel2 != null) {
                priceLabel1.setStyle(highlightStyle);
                priceLabel2.setStyle(baseStyle);
            }
        }

        return label;
    }
}