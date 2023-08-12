package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class DeletePage extends Application {
    @Override
    public void start(Stage deletePage)  {

        try{
            Parent root =FXMLLoader.load(Objects.requireNonNull(getClass().getResource("deletePage.fxml")));
            Scene scene2 = new Scene(root, 850, 700);
            deletePage.setTitle("deletePage");
            deletePage.setScene(scene2);
            deletePage.setResizable(false);
            deletePage.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch();
    }
}