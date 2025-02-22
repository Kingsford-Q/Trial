package com.example.trial;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.stage.Window;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class HelloController {

    @FXML
    private ListView<String> chatHistory;

    @FXML
    private ListView<String> chatMessages;

    @FXML
    private TextField chatInput;

    @FXML
    private Button newChatButton;

    @FXML
    private File selectedFile;

    private final ObservableList<String> historyList = FXCollections.observableArrayList();
    private final Map<String, List<String>> chatSessions = new HashMap<>();
    private String currentSession = null;
    
    private static final String SERVER_URL = "http://127.0.0.1:5000/upload"; // Flask server URL

    @FXML
    public void initialize() {
        chatHistory.setItems(historyList);

        chatHistory.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentSession = newVal;
                loadChatMessages();
            }
        });

        chatMessages.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label messageLabel = new Label(message.replaceFirst("^(You|AI): ", ""));
                    messageLabel.setWrapText(true);
                    messageLabel.setMaxWidth(400);
                    messageLabel.setMinHeight(Region.USE_PREF_SIZE);
                    messageLabel.setStyle(
                        "-fx-padding: 10px 15px; " +
                        "-fx-background-radius: 15px; " +
                        "-fx-border-radius: 15px; " +
                        "-fx-background-color: #0f62fe; " +
                        "-fx-text-fill: white;"
                    );

                    HBox container = new HBox(messageLabel);
                    container.setSpacing(5);

                    if (message.startsWith("You:")) {
                        messageLabel.setStyle(messageLabel.getStyle() + " -fx-background-color: #0f62fe; -fx-text-fill: white;");
                        container.setStyle("-fx-alignment: center-right; -fx-padding: 5px;");
                    } else {
                        messageLabel.setStyle(messageLabel.getStyle() + " -fx-background-color: #444444; -fx-text-fill: white;");
                        container.setStyle("-fx-alignment: center-left; -fx-padding: 5px;");
                    }

                    setGraphic(container);
                }
            }
        });

        Platform.runLater(() -> {
            chatInput.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    onSendMessage();
                    event.consume(); // Prevents default behavior like new line
                } else {
                    if (!chatInput.isFocused()) {
                        chatInput.requestFocus(); // Focus the input field if unfocused
                    }
                }
            });
        });

        newChatButton.setOnAction(event -> startNewSession());

        // Start with an initial chat session
        startNewSession();
    }

    @FXML
    public void onUploadResume(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Window window = ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            selectedFile = file;
            addMessageToSession("You uploaded: " + file.getName());
            chatInput.setPromptText("File: " + file.getName());

            // Upload file to Flask server
            new Thread(() -> uploadFileToServer(file)).start();
        }
    }

    private void uploadFileToServer(File file) {
        try {
            URL url = new URL(SERVER_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=*****");

            OutputStream outputStream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

            // Send file
            writer.append("--*****\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
            writer.append("Content-Type: application/pdf\r\n\r\n");
            writer.flush();

            Files.copy(file.toPath(), outputStream);
            outputStream.flush();

            writer.append("\r\n--*****--\r\n");
            writer.flush();
            writer.close();

            // Read response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                reader.close();

                // Display extracted resume text and entities
                Platform.runLater(() -> addMessageToSession("AI: " + response.toString()));

            } else {
                Platform.runLater(() -> addMessageToSession("AI: Error uploading file."));
            }

            connection.disconnect();
        } catch (Exception e) {
            Platform.runLater(() -> addMessageToSession("AI: Error: " + e.getMessage()));
        }
    }

    @FXML
    public void onSendMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            addMessageToSession("You: " + message);
            chatInput.clear();

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> addMessageToSession("AI: Processing your request..."));
                } catch (InterruptedException e) {
                }
            }).start();
        }
    }

    private void startNewSession() {
        String sessionName = "Session " + (historyList.size() + 1);
        historyList.add(sessionName);
        chatSessions.put(sessionName, new ArrayList<>());
        currentSession = sessionName;
        chatHistory.getSelectionModel().select(sessionName);
        chatMessages.getItems().clear();
    }

    private void addMessageToSession(String message) {
        if (currentSession == null) {
            startNewSession();
        }

        chatSessions.get(currentSession).add(message);
        Platform.runLater(this::loadChatMessages);
    }

    private void loadChatMessages() {
        chatMessages.getItems().setAll(chatSessions.getOrDefault(currentSession, new ArrayList<>()));
    }
}
