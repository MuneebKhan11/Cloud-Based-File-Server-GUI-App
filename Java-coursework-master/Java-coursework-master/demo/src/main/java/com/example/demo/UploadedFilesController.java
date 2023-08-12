package com.example.demo;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class UploadedFilesController {
    private MinioClient minioClient;
public UploadedFilesController() {
    minioClient = initMinioClient();
}


    private MinioClient initMinioClient() {
        String endpoint = "http://172.21.102.129:9006";
        String accessKey = "minioadmin";
        String secretKey = "minioadmin";
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .httpClient(new OkHttpClient.Builder().build())
                .build();
    }

    private static final String CONTAINER_BASE_PATH = "/home/ntu-user/filestorage";
    private int currentUserId;
    @FXML
    private ListView<String> uploadedFilesList;
    @FXML
    private ImageView imageView;
    @FXML
    private TextArea textArea;
    private static Connection getConnection() throws SQLException {
        String relativeDbPath = "Database.db";
        File databaseFile = new File(relativeDbPath);
        String databaseUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
        return DriverManager.getConnection(databaseUrl);
    }

    private String getContainerFilePath(String fileName) {
        return "/home/ntu-user/filestorage/" + fileName;
    }

    @FXML
    private Button downloadButton;

    public void downloadSelectedFile(ActionEvent event) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, NoSuchAlgorithmException, XmlParserException, ServerException {
        String selectedFileName = uploadedFilesList.getSelectionModel().getSelectedItem();
        if (selectedFileName == null) {
            System.out.println("No file selected");
            return;
        }

        try {
            // Create a connection to the SQLite database
            Connection conn = getConnection();

            // Prepare the SQL statement to get the file path for the selected file
            PreparedStatement stmt = conn.prepareStatement("SELECT filePath FROM File WHERE fileName = ?");
            stmt.setString(1, selectedFileName);

            // Execute the SQL statement to get the file path
            ResultSet resultSet = stmt.executeQuery();

            // Get the file path from the result set
            if (resultSet.next()) {
                String filePath = resultSet.getString("filePath");

                // Create a File object from the file path
                String sourceFileName = new File(filePath).getName();
                File tempDownloadFile = new File(System.getProperty("java.io.tmpdir"), sourceFileName);

                // Download the file from MinIO server
                MinioClient minioClient = initMinioClient();
                minioClient.getObject(GetObjectArgs.builder()
                        .bucket("storage")
                        .object(filePath)
                        .object(tempDownloadFile.getAbsolutePath())
                        .build());

                File sourceFile = tempDownloadFile;
                // Show a directory chooser dialog to select the destination folder
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Select Destination Folder");
                Stage stage = (Stage) downloadButton.getScene().getWindow();
                File destinationFolder = directoryChooser.showDialog(stage);

                if (destinationFolder != null) {
                    // Copy the file to the selected folder
                    File destinationFile = new File(destinationFolder.getAbsolutePath() + File.separator + sourceFile.getName());
                    Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                        System.out.println("File downloaded to: " + destinationFile.getAbsolutePath());
                } else {
                    System.out.println("Download cancelled.");
                }
            }

            // Close the database connection
            conn.close();
        } catch (SQLException | IOException e) {
            System.out.println("Error downloading file: " + e.getMessage());
        } catch (ServerException ex) {
            Logger.getLogger(UploadedFilesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private Button backButton;

    public void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("createPage.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void populateUploadedFilesList(int userId) {
        currentUserId = userId;
        try {
            // Create a connection to the SQLite database
            Connection conn = getConnection();

            // Prepare the SQL statement to get the file names for the given user ID
            PreparedStatement stmt = conn.prepareStatement("SELECT fileName FROM File WHERE userId = ?");
            stmt.setInt(1, userId);

            // Execute the SQL statement to get the file names
            ResultSet resultSet = stmt.executeQuery();

            // Clear the ListView before adding new items
            uploadedFilesList.getItems().clear();

            // Add each file name to the ListView
            while (resultSet.next()) {
                String fileName = resultSet.getString("fileName");
                uploadedFilesList.getItems().add(fileName);
            }

            // Close the database connection
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error populating ListView with files: " + e.getMessage());
        }
    }
    private void updateFileNameInDatabase(String oldFileName, String newFileName) {
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE File SET fileName = ? WHERE fileName = ?");
            preparedStatement.setString(1, newFileName);
            preparedStatement.setString(2, oldFileName);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error updating file name in the database: " + e.getMessage());
        }
    }
private void deleteFileFromContainer(String fileName) throws Exception {
    minioClient.removeObject(
        RemoveObjectArgs.builder()
            .bucket("storage")
            .object(fileName)
            .build());
}


    private String getFilePath(String fileName) {
        String filePath = null;
        try {
            // Create a connection to the SQLite database
            Connection conn = getConnection();

            // Prepare the SQL statement to get the file path for the given user ID and file name
            PreparedStatement stmt = conn.prepareStatement("SELECT filePath FROM File WHERE userId = ? AND fileName = ?");
            stmt.setInt(1, currentUserId);
            stmt.setString(2, fileName);

            // Execute the SQL statement to get the file path
            ResultSet resultSet = stmt.executeQuery();

            // Get the file path
            if (resultSet.next()) {
                filePath = resultSet.getString("filePath");
            }

            // Close the database connection
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error getting the file path: " + e.getMessage());
        }

        return filePath;
    }

    public static void renameFileInContainer(String oldContainerFilePath, String newContainerFilePath) {
        try {
            File oldFile = new File(oldContainerFilePath);
            File newFile = new File(newContainerFilePath);
            if (oldFile.renameTo(newFile)) {
                System.out.println("File renamed in container.");
            } else {
                System.out.println("Failed to rename file in container.");
            }
        } catch (Exception e) {
            System.out.println("Error renaming file in container: " + e.getMessage());
        }
    }
    
    private void moveFileInContainer(String oldFileName, String newFileName) throws Exception {
    // Copy the object to a new location
    minioClient.copyObject(
        CopyObjectArgs.builder()
            .bucket("storage")
            .object(newFileName)
            .source(
                CopySource.builder()
                    .bucket("storage")
                    .object(oldFileName)
                    .build())
            .build());

    // Delete the old object
    minioClient.removeObject(
        RemoveObjectArgs.builder()
            .bucket("storage")
            .object(oldFileName)
            .build());
}


    @FXML
    private Button renameButton;

    public void renameSelectedFile(ActionEvent event) {
        String selectedFileName = uploadedFilesList.getSelectionModel().getSelectedItem();

        if (selectedFileName != null) {
            TextInputDialog dialog = new TextInputDialog(selectedFileName);
            dialog.setTitle("Rename File");
            dialog.setHeaderText("Enter a new name for the selected file:");
            dialog.setContentText("New File Name:");

            dialog.showAndWait().ifPresent(newFileName -> {
                if (!newFileName.isEmpty() && !newFileName.equals(selectedFileName)) {
                    String oldFilePath = getFilePath(selectedFileName);
                    String oldContainerFilePath = getContainerFilePath(selectedFileName);
                                        String newContainerFilePath = getContainerFilePath(newFileName);

                    // Rename the file in the container
                    renameFileInContainer(oldContainerFilePath, newContainerFilePath);

                    // Update the file path in the database
                    updateFilePathInDatabase(selectedFileName, newContainerFilePath);

                    // Update the file name in the database
                    updateFileNameInDatabase(selectedFileName, newFileName);

                    // Update the ListView
                    uploadedFilesList.getItems().remove(selectedFileName);
                    uploadedFilesList.getItems().add(newFileName);
                    uploadedFilesList.getSelectionModel().select(newFileName);
                }
            });
        } else {
            // Show an alert when no file is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please select a file to rename.");
            alert.showAndWait();
        }
    }

    @FXML
    private Button deleteButton;

    
    @FXML
private void deleteSelectedFile(ActionEvent event) {
    String selectedFileName = uploadedFilesList.getSelectionModel().getSelectedItem();
    if (selectedFileName == null) {
        System.out.println("No file selected");
        return;
    }

    try {
        // Create a connection to the SQLite database
        Connection conn = getConnection();

        // Prepare the SQL statement to retrieve the information about the selected file from the File table
        PreparedStatement stmtSelect = conn.prepareStatement("SELECT * FROM File WHERE FileName = ?");
        stmtSelect.setString(1, selectedFileName);
        ResultSet rs = stmtSelect.executeQuery();

        // Insert the retrieved information into the deletedFiles table
        if (rs.next()) {
            PreparedStatement stmtInsert = conn.prepareStatement("INSERT INTO deletedFiles (FileID, FileName, FilePath, FileSize, FileType, UploadDate, UserID) VALUES (?, ?, ?, ?, ?, ?, ?)");
            String fileID = rs.getString("FileID");
            String fileName = rs.getString("FileName");
            String filePath = rs.getString("FilePath");
            String fileSize = rs.getString("FileSize");
            String fileType = rs.getString("FileType");
            String uploadDate = rs.getString("UploadDate");
            String userID = rs.getString("UserID");
            stmtInsert.setString(1, fileID);
            stmtInsert.setString(2, fileName);
            stmtInsert.setString(3, filePath);
            stmtInsert.setString(4, fileSize);
            stmtInsert.setString(5, fileType);
            stmtInsert.setString(6, uploadDate);
            stmtInsert.setString(7, userID);
            stmtInsert.executeUpdate();
        }

        // Prepare the SQL statement to delete the selected file
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM File WHERE fileName = ?");
        stmt.setString(1, selectedFileName);

        // Execute the SQL statement to delete the file
        int rowsAffected = stmt.executeUpdate();
        SceneController sceneController = new SceneController();

        // If the file was successfully deleted from the database, remove it from the ListView as well
        if (rowsAffected > 0) {
            String containerFilePath = getContainerFilePath(selectedFileName);
            sceneController.deleteFileFromContainer(containerFilePath);

            uploadedFilesList.getItems().remove(selectedFileName);
            imageView.setVisible(false);
            textArea.setVisible(false);
        } else {
            System.out.println("File not found in the database");
        }

        // Close the database connection
        conn.close();
    } catch (SQLException e) {
        System.out.println("Error deleting file: " + e.getMessage());
    }
}

    @FXML
    private Button moveFileButton;

    public void moveFileClicked(ActionEvent event) {
        String selectedFileName = uploadedFilesList.getSelectionModel().getSelectedItem();
        if (selectedFileName == null) {
            System.out.println("No file selected");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Move File");
        dialog.setHeaderText("Enter the new directory path:");
        dialog.setContentText("New Directory:");

        dialog.showAndWait().ifPresent(newDirectory -> {
            if (!newDirectory.isEmpty()) {
                String sourceFilePath = getFilePath(selectedFileName);
                if (sourceFilePath != null) {
                    File sourceFile = new File(sourceFilePath);
                    File destinationFile = new File(newDirectory, sourceFile.getName());

                    String containerDestinationPath = CONTAINER_BASE_PATH + new File(newDirectory).getName() + "/" + sourceFile.getName();
                    try {
                        moveFileInContainer(sourceFilePath, containerDestinationPath);
                    } catch (Exception ex) {
                        Logger.getLogger(UploadedFilesController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("File moved to: " + destinationFile.getAbsolutePath());
                    updateFilePathInDatabase(selectedFileName, destinationFile.getAbsolutePath());
                }
            }
        });
}

private void updateFilePathInDatabase(String fileName, String newFilePath) {
    try {
        // Create a connection to the SQLite database
        Connection conn = getConnection();

        // Prepare the SQL statement to update the file path
        PreparedStatement stmt = conn.prepareStatement("UPDATE File SET filePath = ? WHERE fileName = ?");
        stmt.setString(1, newFilePath);
        stmt.setString(2, fileName);

        // Execute the SQL statement to update the file path
        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("File path updated in database.");
        } else {
            System.out.println("File not found in database.");
        }

        // Close the database connection
        conn.close();
    } catch (SQLException e) {
        System.out.println("Error updating file path in the database: " + e.getMessage());
    }
}


private void saveToFileTable(String fileName, String filePath, int userId) {
    try (Connection connection = DriverManager.getConnection(SceneController.DB_URL)) {
        String query = "INSERT INTO Files (fileName, filePath, userId) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, fileName);
            String containerFilePath = getContainerFilePath(fileName);
            preparedStatement.setString(2, containerFilePath);
            preparedStatement.setInt(3, userId);
            preparedStatement.executeUpdate();
        }
    } catch (SQLException e) {
        System.out.println("Error saving file to database: " + e.getMessage());
    }
}
}


