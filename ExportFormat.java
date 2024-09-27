import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;

public class ExportFormat {

    private BorderPane root;
    FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    BookkeepingPage bookkeepingPage = new BookkeepingPage();

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            System.out.println("Attempting to establish DB connection...");
            Connection conn = connectionToDataBase.getConnection();
            if (conn != null) {
                System.out.println("Database connection established successfully.");
            } else {
                System.out.println("Failed to establish database connection.");
            }
            return conn;
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    public void exportToPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                InputStream fontStream = getClass().getResourceAsStream("/resources/arial.ttf");
                if (fontStream == null) {
                    alertService.showErrorAlert("Font file not found");
                    return;
                }

                PDFont font = PDType0Font.load(document, fontStream);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(font, 12);

                String sql = "SELECT * FROM bookkeeping";
                try (Connection connection = establishDBConnection()) {
                    PreparedStatement statement = connection.prepareStatement(sql);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        int yPosition = 750;
                        while (resultSet.next()) {
                            String employeeName = resultSet.getString("employee_name");
                            double employeeSalary = resultSet.getDouble("employee_salary");
                            Date changeDate = resultSet.getDate("change_date");
                            String changeType = resultSet.getString("change_type");

                            String record = employeeName + " - Salary: " + employeeSalary + " - Date: " + changeDate + " - Type: " + changeType;
                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, yPosition);
                            contentStream.showText(record);
                            contentStream.endText();

                            yPosition -= 20;
                        }
                    }
                }

                LocalDate startDate = bookkeepingPage.getStartDateForPeriod("Month").toLocalDate();
                LocalDate endDate = LocalDate.now();
                String dateRange = "Data for period: " + startDate + " to " + endDate;
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 25);
                contentStream.showText(dateRange);
                contentStream.endText();

                contentStream.close();
                document.save(file);

                alertService.showSuccessAlert("Bookkeeping information exported to PDF successfully.");
            } catch (IOException | SQLException e) {
                alertService.showErrorAlert("Error exporting to PDF: " + e.getMessage());
            }
        }
    }

    public void exportToTable() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());

        if (file != null) {
            int rowNum = 1;

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Bookkeeping");

                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Employee Name");
                headerRow.createCell(1).setCellValue("Employee Salary");
                headerRow.createCell(2).setCellValue("Change Date");
                headerRow.createCell(3).setCellValue("Successful Contracts");
                headerRow.createCell(4).setCellValue("Successful Contracts Price");
                headerRow.createCell(5).setCellValue("Bonus");

                String sql = "SELECT * FROM bookkeeping";
                try (Connection connection = establishDBConnection()) {
                    PreparedStatement statement = connection.prepareStatement(sql);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            Row row = sheet.createRow(rowNum++);
                            row.createCell(0).setCellValue(resultSet.getString("employee_name"));
                            row.createCell(1).setCellValue(resultSet.getString("employee_salary"));
                            row.createCell(2).setCellValue(resultSet.getString("change_date"));
                            row.createCell(3).setCellValue(resultSet.getString("successful_contracts"));
                            row.createCell(4).setCellValue(resultSet.getString("successful_contracts_price"));
                            row.createCell(5).setCellValue(resultSet.getString("bonus"));
                        }
                    }
                }

                LocalDate startDate = bookkeepingPage.getStartDateForPeriod("Month").toLocalDate();
                LocalDate endDate = LocalDate.now();
                String dateRange = "Data for period: " + startDate + " to " + endDate;
                Row dateRangeRow = sheet.createRow(rowNum++);
                dateRangeRow.createCell(0).setCellValue(dateRange);

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                alertService.showSuccessAlert("Bookkeeping information exported to Excel successfully.");
            } catch (IOException | SQLException e) {
                alertService.showErrorAlert("Error exporting to Excel: " + e.getMessage());
            }
        }
    }
}
