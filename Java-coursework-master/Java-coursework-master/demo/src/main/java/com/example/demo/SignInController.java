package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
public class SignInController {

    @FXML
    private TextField signupUsername;

    @FXML
    private PasswordField signupPassword;

    @FXML
    private TextField email;

    @FXML
    private DatePicker dob;

    @FXML
    void switchToScene1(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("signin.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void handleSignup(ActionEvent event) {
        String username = signupUsername.getText();
        String password = signupPassword.getText();
        String emailInput = email.getText();
        LocalDate dateOfBirth = dob.getValue();

        if (username.isEmpty() || password.isEmpty() || dateOfBirth == null || emailInput.isEmpty()) {
            // Show an error message if any of the fields are empty
            System.out.println("Please fill in all fields!");
            return;
        }

        try {
            DBConnection.createUser(username, password, emailInput);
            System.out.println("User created successfully!");
            // Close the sign-up window
            Stage stage = (Stage) signupUsername.getScene().getWindow();
            stage.close();

// Open the login window
            Stage loginStage = new Stage();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("hello-view.fxml")));
            Scene scene = new Scene(root, 700, 400);
            loginStage.setTitle("Login");
            loginStage.setScene(scene);
            loginStage.show();

        } catch (SQLException | NoSuchAlgorithmException | IOException e) {
            System.out.println("Error creating user: " + e.getMessage());
        }
    }


    public void setPrevScene(Scene scene) {
    }



}
