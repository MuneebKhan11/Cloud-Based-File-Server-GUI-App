package com.example.demo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UpdateDetailsController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField emailField;

    private String encryptedPassword;

    private String getSHA(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    @FXML
    void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    @FXML
    private TextArea currentUser;

    @FXML
    private TextArea currentEmail;

    public void initialize() throws SQLException {
// Fetch the current user's username and email from the database
        String username = SceneController.getCurrentUser();
        String email = SceneController.getCurrentUserEmail();

// Set the text of the corresponding TextAreas
        currentUser.setText(username);
        currentEmail.setText(email);
    }

    @FXML
    void updateDetails(ActionEvent event) {
// Get the current user's username
        String currentUser = null;
        currentUser = SceneController.getCurrentUser();

// Get the new user input
        String newUsername = usernameField.getText();
        String newPassword = passwordField.getText();
        String newEmail = emailField.getText();

        try (Connection conn = DBConnection.getConnection()) {
// Create a PreparedStatement for each update query
            PreparedStatement pstmt = null;
            System.out.println("Current user: " + currentUser);
            System.out.println("New username: " + newUsername);
            System.out.println("New password: " + newPassword);
            System.out.println("New email: " + newEmail);

            if (!newUsername.isEmpty()) {
                pstmt = conn.prepareStatement("UPDATE User SET Username = ? WHERE Username = ?");
                pstmt.setString(1, newUsername);
                pstmt.setString(2, currentUser);
                pstmt.executeUpdate();
// If the username was updated, set it as the new current user
                SceneController.setCurrentUser(newUsername);
            }
            if (!newPassword.isEmpty()) {
                String encryptedPassword = getSHA(newPassword);
                pstmt = conn.prepareStatement("UPDATE User SET Password = ? WHERE Username = ?");
                pstmt.setString(1, encryptedPassword);
                pstmt.setString(2, currentUser);
                pstmt.executeUpdate();
            }
            if (!newEmail.isEmpty()) {
                pstmt = conn.prepareStatement("UPDATE User SET Email = ? WHERE Username = ?");
                pstmt.setString(1, newEmail);
                pstmt.setString(2, currentUser);
                pstmt.executeUpdate();
            }
// Close the PreparedStatement and the Connection
            pstmt.close();
            conn.close();
// Close the window
            closeWindow(event);

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}