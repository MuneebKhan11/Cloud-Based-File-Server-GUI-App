package com.example.demo;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Terminal extends Application {

    public static final String[] VALID_COMMANDS = {"mv", "cp", "ls", "mkdir", "ps", "whoami", "tree", "nano"};

    @FXML
    private TextArea terminalOutput;
    @FXML
    private TextField terminalTextField;

    public void start(Stage terminal) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("terminal.fxml"));
            Parent root = loader.load();
            Scene scene2 = new Scene(root, 850, 700);
            terminal.setTitle("terminal");
            terminal.setScene(scene2);
            terminal.setResizable(false);
            terminal.show();

// Initialize terminalOutput and terminalTextField
            terminalOutput = (TextArea) loader.getNamespace().get("terminalOutput");
            terminalTextField = (TextField) loader.getNamespace().get("terminalTextField");
            terminalTextField.setOnAction(this::handleTerminalInput);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void handleTerminalInput(ActionEvent event) {
        String command = terminalTextField.getText();

// Check if command is valid
        boolean isValidCommand = false;
        for (String validCommand : VALID_COMMANDS) {
            if (command.startsWith(validCommand)) {
                isValidCommand = true;
                break;
            }
        }
        if (!isValidCommand) {
            terminalOutput.appendText("Invalid command: " + command + "\n");
            return;
        }

// Execute command
        String[] commandArray = command.split(" ");
        String[] commandWithFullPath = new String[commandArray.length];
        for (int i = 0; i < commandArray.length; i++) {
            if (Arrays.stream(VALID_COMMANDS).noneMatch(commandArray[i]::startsWith)) {
                commandWithFullPath[i] = "/bin/" + commandArray[i];
            } else {
                commandWithFullPath[i] = commandArray[i];
            }
        }

        ProcessBuilder pb = new ProcessBuilder(commandWithFullPath);
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            List<String> output = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
            p.waitFor();
            reader.close();
// Print output to terminalOutput TextArea
            for (String out : output) {
                terminalOutput.appendText(out + "\n");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }



    public void createScene(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("createPage.fxml")));
        Scene scene3 = new Scene(root, 850, 700);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Create Page");
        stage.setScene(scene3);
        stage.setResizable(false);
        stage.show();
    }
}