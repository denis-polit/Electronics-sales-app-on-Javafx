import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class SupportWindow extends Application {

    private final AlertServiceImpl alertService = new AlertServiceImpl();

    @Override
    public void start(Stage supportStage) {
        MenuPage menuPage = new MenuPage();

        supportStage.setTitle("Support");

        HBox mainPane = new HBox(50);
        mainPane.setAlignment(Pos.CENTER);
        mainPane.setStyle("-fx-background-color: black;");

        VBox welcomePane = createWelcomePane(supportStage);
        VBox contactForm = createContactForm();

        welcomePane.setPrefWidth(900 * 0.5);
        contactForm.setPrefWidth(900 * 0.5);

        mainPane.getChildren().addAll(welcomePane, contactForm);

        Scene supportScene = new Scene(mainPane, 900, 600);
        supportStage.setScene(supportScene);
        supportStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, supportStage, supportScene);
        hotKeysHandler.addHotkeys();
    }

    private VBox createWelcomePane(Stage supportStage) {
        VBox welcomePane = new VBox(20);
        welcomePane.setAlignment(Pos.CENTER);
        welcomePane.setStyle("-fx-background-color: #7331FF;");
        welcomePane.setPadding(new Insets(30));

        ImageView profileIcon = new ImageView("file:icons/support.png");
        profileIcon.setFitHeight(120);
        profileIcon.setFitWidth(120);

        Label welcomeLabel = new Label("Support");
        welcomeLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 30));
        welcomeLabel.setTextFill(Color.WHITE);

        Button backButton = ButtonStyle.expandPaneStyledButton("<");
        backButton.setOnAction(event -> supportStage.close());
        backButton.setStyle("-fx-background-color: #7331FF; -fx-border-color: white; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 15px;");
        backButton.setPrefWidth(100);
        backButton.setPrefHeight(30);

        Label registerLabel = new Label("" +
                " Contacts: Phone: +380639999559\n" +
                " Email: 2024cybershop2024@gmail.com\n" +
                " Address: 13 Konovaltsia Street, Kyiv, Ukraine\n");
        registerLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 20));
        registerLabel.setTextFill(Color.DARKGRAY);

        welcomePane.getChildren().addAll(profileIcon, welcomeLabel, registerLabel, backButton);

        VBox.setMargin(backButton, new Insets(50, 0, 0, 0));
        VBox.setMargin(profileIcon, new Insets(0, 0, 50, 0));
        VBox.setMargin(welcomeLabel, new Insets(0, 0, 10, 0));

        return welcomePane;
    }

    private VBox createContactForm() {
        VBox contactForm = new VBox(20);
        contactForm.setAlignment(Pos.CENTER);
        contactForm.setStyle("-fx-background-color: #07080A;");
        contactForm.setPadding(new Insets(30));

        Label contactLabel = new Label(" Contact Form ");
        contactLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 30));
        contactLabel.setTextFill(Color.WHITE);
        VBox.setMargin(contactLabel, new Insets(0, 0, 30, 0));

        TextField problemField = new TextField();
        problemField.setPromptText("Enter your problem");
        problemField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        Tooltip problemTooltip = new Tooltip("Enter description of your problem");
        problemTooltip.setFont(Font.font("Gotham", FontWeight.NORMAL, 12));
        problemField.setTooltip(problemTooltip);

        TextField emailField = new TextField();
        emailField.setPromptText("Your Email");
        emailField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        Tooltip emailTooltip = new Tooltip("Enter your email");
        emailTooltip.setFont(Font.font("Gotham", FontWeight.NORMAL, 12));
        emailField.setTooltip(emailTooltip);

        ComboBox<String> topicComboBox = new ComboBox<>();
        topicComboBox.getItems().addAll("Interface Failure", "Operation Failure", "Other");
        topicComboBox.setPromptText("Select topic");
        topicComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        topicComboBox.getEditor().setStyle("-fx-text-fill: white;");

        topicComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-radius: 15px;");
                }
            }
        });

        topicComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
                }
            }
        });

        Button sendButton = ButtonStyle.expandPaneStyledButton("Send");
        sendButton.setOnAction(e -> {
            String problem = problemField.getText();
            String userEmail = emailField.getText();
            String topic = topicComboBox.getValue();

            if (problem.isEmpty()) {
                alertService.showErrorAlert("Please enter your problem");
                return;
            }

            if (userEmail.isEmpty()) {
                alertService.showErrorAlert("Please enter your email");
                return;
            }

            if (!isValidEmail(userEmail)) {
                alertService.showErrorAlert("Please enter a valid email address");
                return;
            }

            if (countWords(problem) < 5) {
                alertService.showErrorAlert("Please provide a problem description with at least 5 words");
                return;
            }

            if (topic == null) {
                alertService.showErrorAlert("Please select a topic");
                return;
            }

            saveProblemToFile(problem, topic);

            boolean messageSent = sendFeedback(problem, userEmail, topic);
            if (!messageSent) {
                alertService.showErrorAlert("Failed to send error report");
                return;
            }

            alertService.showSuccessAlert("Error report sent");
            problemField.clear();
            emailField.clear();
            topicComboBox.getSelectionModel().clearSelection();
        });

        contactForm.getChildren().addAll(contactLabel, problemField, emailField, topicComboBox, sendButton);

        return contactForm;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    private int countWords(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        String[] words = str.split("\\s+");
        return words.length;
    }

    private void saveProblemToFile(String problem, String topic) {
        String filePath = "problems.txt";

        try {
            java.io.File file = new java.io.File(filePath);

            boolean fileCreated = file.createNewFile();
            if (fileCreated) {
                System.out.println("File created: " + filePath);
            }

            java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file, true));
            writer.println("Topic: " + topic + " | Problem: " + problem + " | Time of Submission: " + getCurrentTime());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return currentTime.format(formatter);
    }

    private boolean sendFeedback(String problem, String userEmail, String topic) {
        final Properties properties = new Properties();

        String username = "2024cybershop2024@gmail.com";
        String password = "CyberShop2023-2024";

        if (username == null || password == null) {
            System.err.println("Username or password is null.");
            return false;
        }

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2"); // Use TLSv1.2 protocol
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com"); // Trust SMTP server

        Session mailSession = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(mailSession);

            message.setFrom(new InternetAddress(username));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(username));
            message.setSubject("Error Report: " + topic);
            message.setText("Error report from " + userEmail + ":\n\n" + problem);

            for (int i = 0; i < 3; i++) {
                try {
                    Transport.send(message);
                    return true;
                } catch (MessagingException e) {
                    e.printStackTrace();
                    Thread.sleep(3000);
                }
            }
            return false;
        } catch (InterruptedException | MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}