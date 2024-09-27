import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class CreateContract {

    private FirstConnectionToDataBase connectionToDataBase;
    private AddToExistingContract addToExistingContract;
    SessionManager sessionManager = SessionManager.getInstance();
    private final AlertServiceImpl alertService;

    public CreateContract() {
        alertService = new AlertServiceImpl();
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
            addToExistingContract = new AddToExistingContract();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    public void showContractFormOrAddToExistingContract(Announcement announcement, String username) {
        if (username == null || username.isEmpty()) {
            alertService.showErrorAlert("Please log in or register to buy.");
            return;
        }

        if (announcement.getAvailableQuantity() <= 0) {
            alertService.showInfoAlert(
                    "Out of Stock",
                    "The product is out of stock",
                    "We will try to replenish the stock as quickly as possible."
            );
            return;
        }

        if (addToExistingContract.hasContractsInProcessing(username)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Add Product to Contract");
            alert.setHeaderText("You already have contracts with status \"In processing\" or \"Under consideration\".");
            alert.setContentText("Do you want to add the product to an existing contract or create a new one?");

            ButtonType addToExistingContractButton = new ButtonType("Add to Existing Contract");
            ButtonType createNewContractButton = new ButtonType("Create New Contract");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(addToExistingContractButton, createNewContractButton, cancelButton);

            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == addToExistingContractButton) {
                    addToExistingContract.showChooseContractForm(announcement, username);
                } else if (buttonType == createNewContractButton) {
                    showContractForm(announcement, username);
                }
            });
        } else {
            showContractForm(announcement, username);
        }
    }

    private void showContractForm(Announcement announcement, String username) {

        String managerName = announcement.getManagerUsername();
        SessionManager sessionManager = SessionManager.getInstance();

        int managerId = sessionManager.getEmployeeIdByName(managerName);
        int clientId = sessionManager.getClientIdByName(sessionManager.getCurrentClientName());

        if (sessionManager.isClientEnter()) {
            Stage contractStage = new Stage();

            Label clientLabel = new Label("Client Name: " + username);
            clientLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

            Label managerLabel = new Label("Manager Name: " + managerName);
            managerLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

            Label productLabel = new Label("Product: " + announcement.getTitle());
            productLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");

            Label productQuantityLabel = new Label("Product Quantity:");
            productQuantityLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
            TextField quantityTextField = new TextField();
            quantityTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

            Label orderDeadlineLabel = new Label("Order Deadline:");
            orderDeadlineLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
            DatePicker deadlineDatePicker = new DatePicker();
            deadlineDatePicker.getEditor().setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white;");
            Tooltip tooltip = new Tooltip("Select desirable deadline");
            Tooltip.install(deadlineDatePicker, tooltip);

            deadlineDatePicker.setValue(LocalDate.now());
            deadlineDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && newValue.isBefore(LocalDate.now())) {
                    deadlineDatePicker.setValue(LocalDate.now());
                    alertService.showErrorAlert("The selected deadline cannot be in the past. Deadline set to today.");
                }
            });

            TextField additionalQuantityTextField = new TextField();
            additionalQuantityTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");

            Label deliveryMethodLabel = new Label("Delivery Method:");
            deliveryMethodLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
            ComboBox<String> deliveryMethodComboBox = new ComboBox<>();
            deliveryMethodComboBox.getItems().addAll("Courier delivery", "Mail delivery", "Self pick-up");
            deliveryMethodComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
            deliveryMethodComboBox.getEditor().setStyle("-fx-text-fill: white;");

            Label paymentMethodLabel = new Label("Payment Method:");
            paymentMethodLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
            ComboBox<String> paymentMethodComboBox = new ComboBox<>();
            paymentMethodComboBox.getItems().addAll("Cash", "Credit card", "Bank transfer");
            paymentMethodComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: white; -fx-border-radius: 15px;");
            paymentMethodComboBox.getEditor().setStyle("-fx-text-fill: white;");

            Button saveButton = ButtonStyle.expandPaneStyledButton(" Save ");
            Button cancelButton = ButtonStyle.expandPaneStyledButton("Cancel");

            Label totalAmountLabel = new Label("Total Amount: ");
            totalAmountLabel.setStyle("-fx-font-family: Gotham; -fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white;");
            int maxQuantityValue = Integer.MAX_VALUE;

            ChangeListener<String> listener = (observable, oldValue, newValue) -> {
                try {
                    int quantityValue = Integer.parseInt(newValue);
                    if (quantityValue <= maxQuantityValue) {
                        double totalAmount = calculateTotalAmount(
                                announcement.getPrice(),
                                quantityTextField.getText(),
                                additionalQuantityTextField.getText(),
                                announcement.getAvailableQuantity(),
                                announcement.getWholesalePrice(),
                                announcement.getWholesaleQuantity()
                        );
                        totalAmountLabel.setText("Total Amount: " + totalAmount);
                    } else {
                        alertService.showErrorAlert("Entered quantity exceeds the maximum value.");
                    }
                } catch (NumberFormatException e) {
                    alertService.showErrorAlert("Enter a valid number.");
                }
            };
            quantityTextField.textProperty().addListener(listener);
            additionalQuantityTextField.textProperty().addListener(listener);

            VBox layout = new VBox(10);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(10));

            AtomicReference<LocalDate> deadline = new AtomicReference<>();

            saveButton.setOnAction(event -> {
                try {
                    int quantity = quantityTextField.getText().isEmpty() ? 0 : Integer.parseInt(quantityTextField.getText());
                    if (quantity <= 0) {
                        alertService.showErrorAlert("Please enter a valid quantity.");
                        return;
                    }
                    if (quantityTextField.getText().isEmpty()) {
                        alertService.showErrorAlert("Please enter the quantity.");
                        return;
                    }

                    int additionalQuantity = additionalQuantityTextField.getText().isEmpty() ? 0 : Integer.parseInt(additionalQuantityTextField.getText());
                    if (additionalQuantity < 0) {
                        alertService.showErrorAlert("Additional quantity cannot be negative.");
                        return;
                    }

                    if (deliveryMethodComboBox.getValue() == null || deliveryMethodComboBox.getValue().isEmpty()) {
                        alertService.showErrorAlert("Please select the delivery method.");
                        return;
                    }
                    if (paymentMethodComboBox.getValue() == null || paymentMethodComboBox.getValue().isEmpty()) {
                        alertService.showErrorAlert("Please select the payment method.");
                        return;
                    }

                    LocalDate currentDate = LocalDate.now();
                    String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    String clientName = username;
                    String productName = announcement.getTitle();
                    int originalProductId = announcement.getProductId();
                    int availableQuantity = announcement.getAvailableQuantity();

                    if (quantity > availableQuantity) {
                        boolean orderAll = showConfirmationDialog(availableQuantity);
                        if (orderAll) {
                            quantity = availableQuantity;
                            quantityTextField.setText(String.valueOf(quantity));
                        } else {
                            return;
                        }
                    }

                    int totalQuantity = quantity + additionalQuantity;

                    double totalAmount = calculateTotalAmount(
                            announcement.getPrice(),
                            quantityTextField.getText(),
                            additionalQuantityTextField.getText(),
                            announcement.getAvailableQuantity(),
                            announcement.getWholesalePrice(),
                            announcement.getWholesaleQuantity()
                    );

                    LocalDate deadlineDate = deadlineDatePicker.getValue();
                    if (deadlineDate != null && deadlineDate.isBefore(LocalDate.now())) {
                        deadlineDatePicker.setValue(LocalDate.now());
                        alertService.showErrorAlert("The selected deadline cannot be in the past. Deadline set to today.");
                        return;
                    }

                    assert deadlineDate != null;
                    String formattedDeadline = deadlineDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String deliveryMethod = deliveryMethodComboBox.getValue();
                    String paymentMethod = paymentMethodComboBox.getValue();

                    try {
                        int contractId = saveContract(clientId, originalProductId, totalQuantity, deadlineDate, managerId, totalAmount, deliveryMethod, paymentMethod, formattedDate);

                        logContractCreation(contractId, clientName, managerName, announcement.getTitle(), quantity, totalAmount, formattedDeadline, additionalQuantity, deliveryMethod, paymentMethod);

                        String managerEmail = getManagerEmail(managerName);
                        if (managerEmail != null) {
                            String subject = "New Contract";
                            String messageText = "Dear " + managerName + ",\n\nA new contract has been created with the following details:\n\n"
                                    + "Client Name: " + clientName + "\n"
                                    + "Product: " + productName + "\n"
                                    + "Quantity: " + quantity + "\n"
                                    + "Total Amount: " + totalAmount + "\n"
                                    + "Deadline: " + formattedDeadline + "\n\n"
                                    + "Best regards,\nYour Team";

                            sendEmail(managerEmail, subject, messageText);

                            alertService.showSuccessAlert("Contract successfully created.");
                        } else {
                            alertService.showErrorAlert("Failed to retrieve the manager's email address.");
                        }
                    } catch (SQLException e) {
                        alertService.showErrorAlert("Error saving the contract. Please try again.");
                        e.printStackTrace();
                    }

                    contractStage.close();
                } catch (NumberFormatException e) {
                    alertService.showErrorAlert("Please enter a valid number for quantity.");
                }
            });

            cancelButton.setOnAction(event -> contractStage.close());

            layout.getChildren().addAll(
                    clientLabel, productLabel, managerLabel,
                    productQuantityLabel, quantityTextField,
                    orderDeadlineLabel, deadlineDatePicker,
                    deliveryMethodLabel, deliveryMethodComboBox,
                    paymentMethodLabel, paymentMethodComboBox,
                    totalAmountLabel,
                    saveButton, cancelButton
            );

            ScrollPane scrollPane = new ScrollPane(layout);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: black;");

            Scene contractScene = new Scene(scrollPane, 300, 600);
            contractScene.setFill(Color.BLACK);
            contractStage.setScene(contractScene);
            layout.setStyle("-fx-background-color: black;");

            contractStage.setTitle("Order processing");
            contractStage.show();
        } else {
            alertService.showErrorAlert("You cannot create contracts on behalf of clients.");
        }
    }

    private int saveContract(int clientId, int originalProductId, int quantity, LocalDate deadline, int managerId, double totalAmount, String deliveryMethod, String paymentMethod, String creationDate) throws SQLException {
        String sql = "INSERT INTO contracts (client_id, product_id, product_count, deadline, manager_id, total_amount, delivery_method, pay_method, creation_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int contractId = -1;

        try (Connection connection = FirstConnectionToDataBase.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            connection.setAutoCommit(false);

            statement.setInt(1, clientId);
            statement.setInt(2, originalProductId);
            statement.setInt(3, quantity);
            statement.setString(4, deadline.toString());
            statement.setInt(5, managerId);
            statement.setDouble(6, totalAmount);
            statement.setString(7, deliveryMethod);
            statement.setString(8, paymentMethod);
            statement.setString(9, creationDate);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating contract failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    contractId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating contract failed, no ID obtained.");
                }
            }

            updateManagersContracts(connection, managerId, contractId);
            updateUsersContacts(connection, clientId, contractId);

            connection.commit();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error saving the contract. Rolling back transaction.", e);
        }

        return contractId;
    }

    public void updateManagersContracts(Connection connection, int managerId, int contractId) throws SQLException {
        String insertManagerContractQuery = "INSERT INTO employee_contracts (manager_id, contract_id) VALUES (?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertManagerContractQuery)) {
            insertStatement.setInt(1, managerId);
            insertStatement.setInt(2, contractId);

            int rowsAffected = insertStatement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to insert into employee_contracts table.");
            }
        }
    }

    public void updateUsersContacts(Connection connection, int clientId, int contactId) throws SQLException {
        String insertUserContactQuery = "INSERT INTO user_contacts (user_id, contact_id) VALUES (?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertUserContactQuery)) {
            insertStatement.setInt(1, clientId);
            insertStatement.setInt(2, contactId);

            int rowsAffected = insertStatement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to insert into user_contacts table.");
            }
        }
    }

    private void logContractCreation(int contractId, String clientName, String managerName, String product, int quantity, double totalAmount, String deadline, int additionalQuantity, String deliveryMethod, String paymentMethod) {
        String actionType = "ContractCreation";
        String objectType = "Contract";
        String details = String.format("Client: %s, Manager: %s, Product: %s, Quantity: %d, Total Amount: %.2f, Deadline: %s, Additional Quantity: %d, Delivery Method: %s, Payment Method: %s",
                clientName, managerName, product, quantity, totalAmount, deadline, additionalQuantity, deliveryMethod, paymentMethod);

        int userId = sessionManager.getClientIdByName(clientName);
        int managerId = sessionManager.getEmployeeIdByName(managerName);
        String userIp = sessionManager.getIpAddress();
        String userDeviceType = sessionManager.getDeviceType();

        try (Connection connection = FirstConnectionToDataBase.getInstance().getConnection()) {
            String insertLogSQL = "INSERT INTO activity_log (user_id, manager_id, action_type, object_type, details, timestamp, user_ip, user_device_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertLogSQL)) {
                preparedStatement.setInt(1, userId);
                preparedStatement.setInt(2, managerId);
                preparedStatement.setString(3, actionType);
                preparedStatement.setString(4, objectType);
                preparedStatement.setString(5, details);
                preparedStatement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                preparedStatement.setString(7, userIp);
                preparedStatement.setString(8, userDeviceType);

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error logging contract creation: " + e.getMessage());
        }
    }

    private boolean showConfirmationDialog(int availableQuantity) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Order");
        alert.setHeaderText("Quantity exceeds available quantity");
        alert.setContentText("Do you want to order all remaining items (" + availableQuantity + ") or enter a new quantity?");

        ButtonType orderAllButton = new ButtonType("Order All");
        ButtonType enterNewButton = new ButtonType("Enter New Quantity");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(orderAllButton, enterNewButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == orderAllButton;
    }

    private double calculateTotalAmount(double productPrice, String quantity, String additionalQuantity, int availableQuantity, double wholesalePrice, int wholesaleQuantity) {
        int baseQuantity = quantity.isEmpty() ? 0 : Integer.parseInt(quantity);
        int additionalQuantityValue = additionalQuantity.isEmpty() ? 0 : Integer.parseInt(additionalQuantity);

        int totalQuantity = baseQuantity + additionalQuantityValue;

        double totalPrice;
        if (totalQuantity >= wholesaleQuantity) {
            totalPrice = wholesalePrice * totalQuantity;
        } else {
            totalPrice = productPrice * totalQuantity;
        }

        return totalPrice;
    }

    private String getManagerEmail(String managerName) {
        String email = null;
        try (Connection connection = establishDBConnection()) {
            String query = "SELECT manager_email FROM managers WHERE manager_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, managerName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        email = resultSet.getString("manager_email");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertService.showErrorAlert("Error getting manager email: " + e.getMessage());
        }
        return email;
    }

    public void sendEmail(String recipientEmail, String subject, String messageText) {
        try {
            if (recipientEmail == null) {
                System.err.println("Recipient email address is null");
                return;
            }

            Properties properties = new Properties();
            try (InputStream inputStream = getClass().getResourceAsStream("/mail.properties")) {
                if (inputStream == null) {
                    System.err.println("Error loading mail properties file: File not found");
                    return;
                }
                properties.load(inputStream);
            }

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(properties.getProperty("mail.smtp.user"), properties.getProperty("mail.password"));
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(properties.getProperty("mail.smtp.user")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(messageText);

            Transport.send(message);
            System.out.println("Email sent successfully!");

        } catch (FileNotFoundException e) {
            System.err.println("Error loading mail properties file: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading mail properties: " + e.getMessage());
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}
