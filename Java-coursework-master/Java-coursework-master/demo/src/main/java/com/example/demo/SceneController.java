package com.example.demo;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Objects;
import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.DownloadObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.shape.Path;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
public class SceneController {
    @FXML
    private TextField loginUsername;

    @FXML
    private PasswordField loginPassword;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button backButton;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    @FXML
    private TextField fileNameTextField;
    @FXML
    private Button uploadButton;

    private static final String RELATIVE_DB_PATH = "Database.db";
    static final String DB_URL = "jdbc:sqlite:" + new File(RELATIVE_DB_PATH).getAbsolutePath();
    public static String getCurrentUserEmail() throws SQLException {
        String email = null;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Database.db");
             PreparedStatement stmt = conn.prepareStatement("SELECT Email FROM User WHERE Username = ?")) {

            stmt.setString(1, currentUser);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    email = rs.getString("Email");
                }
            }
        }

        return email;
    }


    private String getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageDigest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : messageDigest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    public void recover(ActionEvent event) throws IOException{
        Parent root=FXMLLoader.load(getClass().getResource("recover.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Object scene=new Scene(root);
        stage.setScene((Scene) scene);
        stage.show();
    }

    public void copyFileToContainer(InputStream inputStream, String objectName) {
    // MinioClient configuration
    String endpoint = "http://172.21.102.129:9006";
    String accessKey = "minioadmin";
    String secretKey = "minioadmin";
    boolean secure = false;
    MinioClient minioClient = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .httpClient(new OkHttpClient.Builder().build())
            .build();

    try {
        // Create the "storage" bucket if it doesn't exist
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket("storage").build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket("storage").build());
        }

        // Upload the file to the "storage" bucket
        minioClient.putObject(PutObjectArgs.builder()
                .bucket("storage")
                .object(objectName)
                .stream(inputStream, inputStream.available(), -1)
                .contentType("application/octet-stream")
                .build());

        System.out.println("File copied to container successfully.");
    } catch (Exception e) {
        System.out.println("Error copying file to container: " + e.getMessage());
        e.printStackTrace();
    }
}
public void copyFileToNewContainer(InputStream inputStream, String objectName) {
    String containerId = "cf87bf48c37b";
    String containerIP = "172.17.0.4";
    int containerPort = 2222;
    String username = "ntu-user"; // Change this to your preferred username
    String password = "ntu-user"; // Change this to your actual password

    JSch jsch = new JSch();
    Session session = null;
    ChannelSftp channelSftp = null;

    try {
        // Connect to the container via SFTP
        session = jsch.getSession(username, containerIP, containerPort);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);
        session.connect();

        // Open an SFTP channel
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        // Upload the file to the container
        channelSftp.put(inputStream, objectName);

        System.out.println("File uploaded successfully to the encrypted container.");

    } catch (Exception e) {
        System.out.println("Error uploading file to the encrypted container: " + e.getMessage());
        e.printStackTrace();
    } finally {
        // Close the SFTP channel and session
        if (channelSftp != null) {
            channelSftp.exit();
        }
        if (session != null) {
            session.disconnect();
        }
    }
}

    @FXML
    protected void switchToScene2(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("signin.fxml"));
            Parent root = loader.load();
            SignInController signInController = loader.getController();
            signInController.setPrevScene(((Node) event.getSource()).getScene());
            Scene scene2 = new Scene(root, 308, 503);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Sign-Up");
            stage.setScene(scene2);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showUploadedFiles(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("UploadedFiles.fxml"));
        Parent root = loader.load();
        UploadedFilesController uploadedFilesController = loader.getController();
        uploadedFilesController.populateUploadedFilesList(getCurrentUserId());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    @FXML
    protected void create(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String hashedPass = "";
        try {
            hashedPass = getSHA(pass);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(DB_URL);
            preparedStatement = connection.prepareStatement("SELECT * FROM User WHERE Username = ? and Password = ?");
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, hashedPass);
            resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                System.out.println("Incorrect username or password");
                System.out.println("Entered username: " + user);
                System.out.println("Entered password: " + pass);
                System.out.println("Hashed password: " + hashedPass);
            } else {
                Parent root = FXMLLoader.load(getClass().getResource("createPage.fxml"));
                Scene scene3 = new Scene(root, 850, 700);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setTitle("Create Page");
                stage.setScene(scene3);
                stage.setResizable(false);
                stage.show();
            }
        } catch (SQLException | IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
                if (preparedStatement != null)
                    preparedStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    private static int currentUserId;

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    private static String currentUser;

    public static String getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(String user) {
        currentUser = user;
    }
    @FXML
    protected void delete(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String hashedPass = "";
        try {
            hashedPass = getSHA(pass);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(DB_URL);
            preparedStatement = connection.prepareStatement("SELECT * FROM User WHERE Username = ?");
            preparedStatement.setString(1, user);
            resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                System.out.println("Incorrect username or password");
            } else {
                String dbHashedPass = resultSet.getString("Password");
                if (!dbHashedPass.equals(hashedPass)) {
                    System.out.println("Incorrect username or password");
                } else {
                    preparedStatement = connection.prepareStatement("DELETE FROM User WHERE Username = ?");
                    preparedStatement.setString(1, user);
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected == 0) {
                        System.out.println("No rows were deleted.");
                    } else {
                        System.out.println(rowsAffected + " rows were deleted.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
                if (preparedStatement != null)
                    preparedStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    @FXML
    private Button logoutButton;
    public void logout(ActionEvent event) throws IOException {
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "DELETE FROM loggedInUsers";
            stmt.executeUpdate(sql);
            System.out.println("Logged out successfully. Deleted all rows from loggedInUsers table.");
        } catch (SQLException e) {
            System.out.println("Error logging out: " + e.getMessage());
        }

        Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
        Scene scene = new Scene(root);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Button button = (Button) event.getSource();
        window.setScene(scene);
        window.show();
    }
    
    @FXML
    public void updateDetailsScene(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("UpdateDetails.fxml"));
        Parent root = loader.load();
        UpdateDetailsController updateDetailsController = loader.getController();
        DBConnection.setCurrentUser(getCurrentUser());

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    }

   public void updateScene(ActionEvent event) throws IOException{
        Parent root=FXMLLoader.load(getClass().getResource("updatePage.fxml"));
       Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
       Object scene=new Scene(root);
       stage.setScene((Scene) scene);
       stage.show();
   }

    public void terminalScence (ActionEvent event) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("terminal.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Object scene = new Scene(root);
        stage.setScene((Scene) scene);
        stage.show();
    }
    public void createScene(ActionEvent event) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("createPage.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Object scene = new Scene(root);
        stage.setScene((Scene) scene);
        stage.show();
    }

    java.util.Date utilDate = new java.util.Date();
    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

    private void saveToFileTable(String fileName, String filePath, int userId) {
        try {
            // Create a connection to the SQLite database
            Connection conn = DriverManager.getConnection("jdbc:sqlite:Database.db");

            // Get the file object and fetch the additional details
            File file = new File(filePath);
            long fileSize = file.length();
            String fileType = Files.probeContentType(file.toPath());
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date uploadDate = new java.sql.Date(utilDate.getTime());


            // Prepare the SQL statement to insert the file details into the File table
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO File (fileName, filePath, fileSize, fileType, uploadDate, userId) VALUES (?, ?, ?, ?, ?, ?)");
            stmt.setString(1, fileName);
            stmt.setString(2, filePath);
            stmt.setLong(3, fileSize);
            stmt.setString(4, fileType);
            stmt.setString(5, uploadDate.toString());
            stmt.setInt(6, userId);

            // Execute the SQL statement to insert the data into the File table
            int numRowsInserted = stmt.executeUpdate();

            // Close the database connection
            conn.close();

            // Print a message indicating whether the data was successfully inserted into the File table
            if (numRowsInserted == 1) {
                System.out.println("Operation successful ! ");
            } else {
                System.out.println("Error saving file : no rows were inserted.");
            }
        } catch (SQLException | IOException e) {
            System.out.println("Error uploading files. : " + e.getMessage());
        }
    }

    public void deleteScene(ActionEvent event) throws IOException{
        Parent root=FXMLLoader.load(getClass().getResource("deletePage.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Object scene=new Scene(root);
        stage.setScene((Scene) scene);
        stage.show();
    }
    public void uploadFile(ActionEvent event) {
        String endpoint = "http://172.21.102.129:9006";
        String accessKey = "minioadmin";
        String secretKey = "minioadmin";
        boolean secure = false;
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .httpClient(new OkHttpClient.Builder().build())
                .build();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a File");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            String fileName = fileNameTextField.getText();
            if (fileName.isEmpty()) {
                fileName = file.getName();
            }

            int userId = getCurrentUserId();

            try {
                // Create the "storage" bucket if it doesn't exist
                boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket("storage").build());
                if (!bucketExists) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket("storage").build());
                }

                // Upload the file to the "storage" bucket
                InputStream inputStream = new FileInputStream(file);
                String contentType = Files.probeContentType(file.toPath());
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket("storage")
                        .object(fileName)
                        .stream(inputStream, file.length(), -1)
                        .contentType(contentType)
                        .build());

                // Save the file details to the database
                saveToFileTable(fileName, file.getAbsolutePath(), userId);

                System.out.println("File uploaded to the container successfully.");
                String objectName = fileName + ".enc";

                // Assuming inputStream and objectName are already defined
                copyFileToNewContainer(inputStream, objectName);

            } catch (Exception e) {
                System.out.println("Error uploading file to the container: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("File selection cancelled.");
        }
    }
    public void downloadFileFromContainer(String objectName, String localFilePath) {
    // MinioClient configuration
    String endpoint = "http://172.21.102.129:9006";
    String accessKey = "minioadmin";
    String secretKey = "minioadmin";
    boolean secure = false;
    MinioClient minioClient = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .httpClient(new OkHttpClient.Builder().build())
            .build();

    try {
        minioClient.downloadObject(
                DownloadObjectArgs.builder()
                        .bucket("storage")
                        .object(objectName)
                        .filename(localFilePath)
                        .build()
        );
        System.out.println("File downloaded from the container successfully.");
    } catch (Exception e) {
        System.out.println("Error downloading file from container: " + e.getMessage());
    }
}
    public void encryptAndUploadFile(String filePath, String userPassword, String objectName) {
    try {
        // Read the file content
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

        // Generate an encryption key from the user's password
        byte[] keyBytes = userPassword.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        // Generate a random initialization vector (IV)
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Encrypt the file content using AES
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        byte[] encryptedContent = cipher.doFinal(fileContent);

        // Combine the IV and the encrypted content
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(iv);
        outputStream.write(encryptedContent);
        byte[] combinedContent = outputStream.toByteArray();

        // Upload the encrypted file to the new container
        InputStream encryptedInputStream = new ByteArrayInputStream(combinedContent);
        copyFileToNewContainer(encryptedInputStream, objectName);

    } catch (Exception e) {
        System.out.println("Error encrypting and uploading file: " + e.getMessage());
        e.printStackTrace();
    }
}

    public void deleteFileFromContainer(String objectName) {
    // MinioClient configuration
    String endpoint = "http://172.21.102.129:9006";
    String accessKey = "minioadmin";
    String secretKey = "minioadmin";
    boolean secure = false;
    MinioClient minioClient = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .httpClient(new OkHttpClient.Builder().build())
            .build();

    try {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket("storage")
                        .object(objectName)
                        .build()
        );
        System.out.println("File deleted from container successfully.");
    } catch (Exception e) {
        System.out.println("Error deleting file from container: " + e.getMessage());
    }
}

    public void moveFileInContainer(String sourceObjectName, String destinationObjectName) {
    // MinioClient configuration
    String endpoint = "http://172.21.102.129:9006";
    String accessKey = "minioadmin";
String secretKey = "minioadmin";
boolean secure = false;
MinioClient minioClient = MinioClient.builder()
.endpoint(endpoint)
.credentials(accessKey, secretKey)
.httpClient(new OkHttpClient.Builder().build())
.build();
try {
    // Copy the source object to the destination object
    minioClient.copyObject(CopyObjectArgs.builder()
            .bucket("storage")
            .object(destinationObjectName)
            .source(CopySource.builder().bucket("storage").object(sourceObjectName).build())
            .build());

    // Remove the source object
    minioClient.removeObject(RemoveObjectArgs.builder()
            .bucket("storage")
            .object(sourceObjectName)
            .build());

    System.out.println("File moved within container successfully.");
} catch (Exception e) {
    System.out.println("Error moving file within container: " + e.getMessage());
}
    }

    public void moveFile(String sourcePath, String destinationPath, String fileName) {
        try {
            Path source = (Path) Paths.get(sourcePath, fileName);
            Path destination = (Path) Paths.get(destinationPath, fileName);
            Files.move((java.nio.file.Path) source, (java.nio.file.Path) destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File moved from " + source.toString() + " to " + destination.toString());
        } catch (IOException e) {
            System.out.println("Error moving file: " + e.getMessage());
        }
    }
    @FXML
    void handleLogin(ActionEvent event) throws IOException, SQLException, NoSuchAlgorithmException {
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Please fill in both fields!");
            return;
        }

        try (Connection conn1 = DBConnection.getConnection();
             PreparedStatement statement = conn1.prepareStatement("SELECT COUNT(*) FROM LoggedInUsers WHERE Username = ?")) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            // Check if the current user is logged in
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Can't log in, this user is already logged in!");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error checking logged in users: " + e.getMessage());
        }

        if (DBConnection.verifyUser(username, password)) {
            System.out.println("Login successful!");

            // Fetch the user ID from the database
            int userId = DBConnection.getUserId(username);

            // Set the user ID in the SceneController class
            SceneController.setCurrentUserId(userId);
            SceneController.setCurrentUser(username);

            // Update the user's login time
            DBConnection.updateLoginTime(username);

            try (Connection conn2 = DBConnection.getConnection();
                 PreparedStatement stmt = conn2.prepareStatement("INSERT INTO loggedInUsers (Username) VALUES (?)")) {
                stmt.setString(1, SceneController.getCurrentUser());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error inserting logged in user: " + e.getMessage());
            }

            // Continue with the rest of the login process
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("createPage.fxml")));
            Scene scene3 = new Scene(root, 850, 700);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Create Page");
            stage.setScene(scene3);
            stage.setResizable(false);
            stage.show();

        } else {
            System.out.println("Incorrect username or password");
        }
    }

    public static void uploadFileToEncryptedContainer(String localFilePath, String containerFilePath, String encryptedPassword) {
        String encryptedContainerId = "cf87b548c37b";
        String encryptedContainerUser = "ntu-user";
        String encryptedContainerPassword = "ntu-user";
        String ipAddress = "http://172.17.0.2:2222";

        try {
            // Encrypt the file using the user's encrypted password and save it as a temporary file
            String tempFilePath = localFilePath + ".encrypted";
            DBConnection.encryptFile(localFilePath, tempFilePath, encryptedPassword);

            // Upload the encrypted file to the encrypted container
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",
                    String.format("sshpass -p %s scp %s %s@%s:%s", encryptedContainerPassword, tempFilePath, encryptedContainerUser, ipAddress, containerFilePath));
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("File uploaded to encrypted container successfully.");
            } else {
                System.out.println("Error uploading file to encrypted container.");
            }

            // Delete the temporary encrypted file
            Files.deleteIfExists(Paths.get(tempFilePath));
        } catch (IOException | InterruptedException | NoSuchAlgorithmException | SQLException e) {
            System.out.println("Error uploading file to encrypted container: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    void switchToScene1(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("signin.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private TextArea terminalOutput;
    @FXML
    private TextField terminalTextField;
    private final Terminal terminal = new Terminal();

    @FXML
    public Button selectFilesButton;
    public void handleSelectFiles(ActionEvent event) {
        // Get the stage from the button's scene
        Stage stage = (Stage) selectFilesButton.getScene().getWindow();

        // Create a file chooser and set the initial directory and file extensions
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open a file");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPEG image", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG image", "*.png"),
                new FileChooser.ExtensionFilter("Text file", "*.txt"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );

        // Show the file chooser and get the selected file
        File selectedFile = fileChooser.showOpenDialog(stage);

        // Handle the selected file or cancelled file chooser dialog
        if (selectedFile != null) {
            // The user selected a file. Do something with it.
            String fileName = selectedFile.getName();
            String filePath = selectedFile.getPath();
            int userID = 1; // Replace this with the actual user ID value
            saveToFileTable(fileName, filePath, userID);
        } else {
            // The user cancelled the file chooser dialog.
            System.out.println("No file has been selected");
        }
    }
    @FXML
    private void terminalScene(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/TerminalScene.fxml"));
            Parent terminalScene = loader.load();

            // Get the current scene
            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(terminalScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

