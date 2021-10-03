package me.p3074098.gotcha.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import me.p3074098.gotcha.controller.ApplicationController;

import java.io.IOException;

public class ApplicationEntry extends Application {
    
    public static Stage mainStage;
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("application.fxml"));
        
        mainStage = primaryStage;
        
        primaryStage.setTitle("Gotcha!");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("app-icon.png")));
    }
    
    public static void main(String[] args) {
        launch();
    }
    
    @Override
    public void stop() throws Exception {
        ApplicationController.getInstance().serialize();
        
        super.stop();
    }
}