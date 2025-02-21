package com.example.trial;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
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
            // Add user's message to the right panel
            chatMessages.getItems().add("You: " + message);
            chatInput.clear();

            // Simulating AI response after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Simulating AI processing
                    chatMessages.getItems().add("AI: Processing your request...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
