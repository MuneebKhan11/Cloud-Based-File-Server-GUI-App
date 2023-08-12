package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Objects;

public class recover {
    private int currentUserId;
    @FXML
    private ListView<String> deletedFilesList;
    @FXML
    private ImageView recoverImageView;
    @FXML
    private TextArea recoverTextArea;

    private void previewDeletedFiles(String fileName){
        try {
// Create a connection to the SQLite database
            Connection conn = DriverManager.getConnection("jdbc:sqlite:Database.db");

// Prepare the SQL statement to get the file path for the selected file
            PreparedStatement stmt = conn.prepareStatement("SELECT FilePath, FileType FROM deletedFiles WHERE FileName = ?");
            stmt.setString(1, fileName);

// Execute the SQL statement to get the file path
            ResultSet resultSet = stmt.executeQuery();

// Get the file path from the result set
            if (resultSet.next()) {
                String filePath = resultSet.getString("FilePath");
                String fileType = resultSet.getString("FileType");

// Display the file based on its type
                if (fileType != null && fileType.startsWith("image")) {
                    recoverImageView.setImage(new Image("file:" + filePath));
                    recoverImageView.setVisible(true);
                    recoverTextArea.setVisible(false);
                } else if (fileType != null && fileType.startsWith("text")) {
                    String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
                    recoverTextArea.setText(fileContent);
                    recoverImageView.setVisible(false);
                    recoverTextArea.setVisible(true);
                } else {
                    recoverImageView.setVisible(false);
                    recoverTextArea.setVisible(false);
                }
            }

// Close the database connection
            conn.close();
        } catch (SQLException | IOException e) {
            System.out.println("Error fetching file path: " + e.getMessage());
        }
    }
    @FXML
    public void initialize() {
        recoverImageView.setVisible(false);
        recoverTextArea.setVisible(false);

        try {
// Create a connection to the SQLite database
            Connection conn = DriverManager.getConnection("jdbc:sqlite:Database.db");

// Prepare the SQL statement to retrieve the file names from the deletedFiles table
            PreparedStatement stmt = conn.prepareStatement("SELECT FileName FROM deletedFiles");

// Execute the SQL statement
            ResultSet rs = stmt.executeQuery();

// Add the file names to the deletedFilesList
            while (rs.next()) {
                deletedFilesList.getItems().add(rs.getString("FileName"));
            }

// Close the database connection
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error fetching file names: " + e.getMessage());
        }
        deletedFilesList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        previewDeletedFiles(newValue);
                    }
                });
    }
    @FXML
    public Button recoverButton;
    @FXML
    private void RecoverButton() {


        System.out.println("Recover button clicked");

        String selectedFile = deletedFilesList.getSelectionModel().getSelectedItem();
        System.out.println("Selected file: " + selectedFile);

        try {
// Create a connection to the SQLite database
            Connection conn = DriverManager.getConnection("jdbc:sqlite:Database.db");

// Retrieve the information about the selected file from the deletedFiles table
            PreparedStatement stmtSelect = conn.prepareStatement("SELECT * FROM deletedFiles WHERE FileName = ?");
            stmtSelect.setString(1, selectedFile);
            ResultSet rs = stmtSelect.executeQuery();

// Insert the retrieved information into the Files table
            if (rs.next()) {
                PreparedStatement stmtInsert = conn.prepareStatement("INSERT INTO File (FileID,FileName, FilePath,FileSize,FileType,UploadDate,UserID) VALUES (?,?,?,?,?,?,?)");
                String fileID = rs.getString("FileID");
                String fileName = rs.getString("FileName");
                String filePath = rs.getString("FilePath");
                String fileSize = rs.getString("FileSize");
                String fileType = rs.getString("FileType");
                String uploadDate =rs.getString("UploadDate");
                String userID =rs.getString("UserID");
                stmtInsert.setString(1, fileID);
                stmtInsert.setString(2, fileName);
                stmtInsert.setString(3, filePath);
                stmtInsert.setString(4, fileSize);
                stmtInsert.setString(5, fileType);
                stmtInsert.setString(6, uploadDate);
                stmtInsert.setString(7, userID);
                stmtInsert.executeUpdate();
            }

// Prepare the SQL statement to delete the file from the deletedFiles table
            PreparedStatement stmtDelete = conn.prepareStatement("DELETE FROM deletedFiles WHERE FileName = ?");
            stmtDelete.setString(1, selectedFile);

// Execute the SQL statement to delete the file
            int rowsAffected = stmtDelete.executeUpdate();
            if (rowsAffected > 0) {
                deletedFilesList.getItems().remove(selectedFile);
                recoverImageView.setVisible(false);
                recoverTextArea.setVisible(false);


            }

// Close the database connection
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error recovering file: " + e.getMessage());
        }
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