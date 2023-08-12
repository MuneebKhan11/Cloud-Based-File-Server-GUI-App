package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SignIn extends Application {
    @Override
    public void start(Stage signup)  {

        try{
            Parent root = FXMLLoader.load(getClass().getResource("signin.fxml"));
            Scene scene2 = new Scene(root, 308, 503);
            signup.setTitle("SignUp");
            signup.setScene(scene2);
            signup.setResizable(false);
            signup.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch();
    }
}
