package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateNewFileController {
    @FXML
    private TextField fileNameInput;

    @FXML
    private TextArea fileContentInput;
    public void openCreateNewFileView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("createNewFile.fxml"));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFileToDatabase(String fileName, String fileContent) {
        try (Connection conn = DriverManager.getConnection(SceneController.DB_URL)) {
            String sql = "INSERT INTO File (name, content) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fileName);
            pstmt.setString(2, fileContent);
            pstmt.executeUpdate();
            System.out.println("File saved to database.");
        } catch (SQLException e) {
            System.out.println("Error saving file to database: " + e.getMessage());
        }
    }

    public void saveFile(ActionEvent event) {
        String fileName = fileNameInput.getText();
        String fileContent = fileContentInput.getText();

        if (fileName.isEmpty()) {
            System.out.println("File name cannot be empty.");
            return;
        }

        try {
            // Save the file to the RESTful API
            saveFileToAPI(fileName, fileContent);
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }
    private void saveFileToAPI(String fileName, String fileContent) throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody fileRequestBody = RequestBody.create(mediaType, fileContent.getBytes());
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileRequestBody)
                .build();

        Request request = new Request.Builder()
                .url("http://localhost:8080/upload") // Replace with your RESTful API URL
                .post(requestBody)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                System.out.println("File saved to API: " + fileName);
            } else {
                System.out.println("Error saving file to API: " + response.code());
            }
        } finally {

        }
    }




    public void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("createPage.fxml"));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
