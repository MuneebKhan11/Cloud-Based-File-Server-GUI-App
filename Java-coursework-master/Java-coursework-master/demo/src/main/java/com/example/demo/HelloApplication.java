package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage Primarystage) {

        try{
            Parent root =FXMLLoader.load(Objects.requireNonNull(getClass().getResource("hello-view.fxml")));
            Scene scene1 = new Scene(root, 700, 400);
            Primarystage.setTitle("LogIn / SignUp");
            Primarystage.setScene(scene1);
            Primarystage.setResizable(false);
            Primarystage.show();
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch();
    }

    
}