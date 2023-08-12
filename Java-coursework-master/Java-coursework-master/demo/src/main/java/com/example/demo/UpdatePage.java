package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class UpdatePage extends Application {
    @Override
    public void start(Stage updatePage)  {

        try{
            Parent root =FXMLLoader.load(Objects.requireNonNull(getClass().getResource("updatePage.fxml")));
            Scene scene2 = new Scene(root, 850, 700);
            updatePage.setTitle("updatePage");
            updatePage.setScene(scene2);
            updatePage.setResizable(false);
            updatePage.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch();
    }
}