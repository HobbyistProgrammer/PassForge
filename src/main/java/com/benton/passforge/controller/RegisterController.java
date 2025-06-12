package com.benton.passforge.controller;

import com.benton.passforge.MainApplication;
import com.benton.passforge.util.DatabaseConnector;
import com.benton.passforge.util.PasswordUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Statement;

public class RegisterController {

    @FXML private PasswordField masterPassword, confirmPassword;
    @FXML private Label lblIncorrect;

    /**
     * @author Benton Le
     * @param event
     * @throws Exception
     */
    @FXML
    protected void onRegisterButtonClick(ActionEvent event) throws Exception {

        if(masterPassword.getText().equals(confirmPassword.getText())) {
            System.out.println("Registering");
            String salt = PasswordUtils.generateSalt(); // Generate random salt using PBKDF2 with Salt
            String hash = PasswordUtils.hashPassword(masterPassword.getText().toCharArray(), salt); // Hashed password

            Connection conn = DatabaseConnector.connect();
            Statement stmt = conn.createStatement();

            String createTableSQL = "CREATE TABLE IF NOT EXISTS users (\n" +
                    "    id              INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    password_hash   TEXT    NOT NULL,\n" +
                    "    salt            TEXT    NOT NULL" +
                    ");";
            stmt.execute(createTableSQL);

            createTableSQL = "CREATE TABLE IF NOT EXISTS forge (\n" +
                    "    id                 INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    site               TEXT    NOT NULL,\n" +
                    "    password_encrypted TEXT    NOT NULL" +
                    ");";
            stmt.execute(createTableSQL);

            // Check to see if table exists to avoid multiple inserts or dummy data.
//            String checkSQL = "SELECT COUNT(*) FROM users WHERE name = 'admin'";
//            ResultSet set = conn.createStatement().executeQuery(checkSQL);
//            set.next();
//            if(set.getInt(1) == 0) {
//                String insertSQL = "INSERT INTO users (password_hash, salt) VALUES ('password', 'D5D5D3D2')";
//                conn.createStatement().executeUpdate(insertSQL);
//            }

            String insertSQL = String.format("INSERT INTO users (password_hash, salt) VALUES ('%s', '%s')", hash, salt);
            stmt.executeUpdate(insertSQL);

            System.out.println("Inserted");

            conn.close();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("passforge-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();

            stage.setTitle("PassForge - Enchanted with Protection IV");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }
        else {
            lblIncorrect.setText("Password does not match!!!");
            lblIncorrect.setVisible(true);
        }
    }
}
