package com.example.trial;

import java.io.File;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.stage.Window;

public class HelloController {

    @FXML
    private ListView<String> chatHistory;

    @FXML
    private ListView<String> chatMessages;

    @FXML
    private TextField chatInput;

    private File selectedFile;

    @FXML
    public void initialize() {
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
                    messageLabel.setMaxWidth(400); // Prevent overly long messages
                    messageLabel.setMinHeight(Region.USE_PREF_SIZE); // Adjust height dynamically
                    messageLabel.setStyle(
                        "-fx-padding: 10px 15px; " +
                        "-fx-background-radius: 15px; " +
                        "-fx-border-radius: 15px; " +
                        "-fx-background-color: #0f62fe; " + // Blue background for all messages
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

        chatInput.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    onSendMessage(); // Call send function when Enter is pressed
                    event.consume(); // Prevents new line in the text field
                    break;
                default:
                    break;
            }
        });
    }


    @FXML
    public void onUploadResume(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        // Fix: Get the current window from the button that was clicked
        Window window = ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            selectedFile = file;
            chatMessages.getItems().add("You uploaded: " + file.getName());
            chatInput.setPromptText("File: " + file.getName());  // Display filename in input field
        }
    }

    @FXML
    public void onSendMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            chatMessages.getItems().add("You: " + message);
            chatInput.clear();

            // Simulating AI response with a delay but ensuring UI updates happen on the JavaFX thread
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Simulating AI processing
                    Platform.runLater(() -> chatMessages.getItems().add("AI: Processing your request..."));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
