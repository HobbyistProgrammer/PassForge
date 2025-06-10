package com.benton.passforge;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Font f = Font.loadFont(getClass().getResourceAsStream("/fonts/MonoCraft.ttc"), 14);
        if (f != null) {
            System.out.println("Loaded font: " + f.getName());
        } else {
            System.out.println("Failed to load MonoCraft");
        }

        stage.getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResourceAsStream("/icons/icon.png"))));

        String username = System.getProperty("user.name"); // This code gets the name of user on the Desktop
        File file = new File("C:\\Users\\" + username + "\\Documents\\", "passforge.db");

        if (file.exists()) {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("passforge-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("PassForge - Enchanted with Protection IV");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }
        else {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("register-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("PassForge - Enchanted with Protection IV");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}