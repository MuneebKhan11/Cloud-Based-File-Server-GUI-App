package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class CreatePage extends Application {
    @Override
    public void start(Stage createPage)  {

        try{
            Parent root =FXMLLoader.load(Objects.requireNonNull(getClass().getResource("createPage.fxml")));
            Scene scene2 = new Scene(root, 850, 700);
            createPage.setTitle("createPage");
            createPage.setScene(scene2);
            createPage.setResizable(false);
            createPage.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch();
    }
}