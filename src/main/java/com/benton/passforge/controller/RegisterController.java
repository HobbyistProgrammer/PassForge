package com.benton.passforge.controller;

import com.benton.passforge.MainApplication;
import com.benton.passforge.model.PolicyResults;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private PasswordField masterPassword, confirmPassword;
    @FXML private Label lblIncorrect, lblPasswordStrength, lblPasswordPolicy;
    @FXML private ProgressBar prgBarPassword;

    /**
     * @author Benton Le
     * @param event
     * @throws Exception
     */
    @FXML
    protected void onRegisterButtonClick(ActionEvent event) throws Exception {

        if(masterPassword.getText().equals(confirmPassword.getText())) {
            // System.out.println("Registering");
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

            String insertSQL = String.format("INSERT INTO users (password_hash, salt) VALUES ('%s', '%s')", hash, salt);
            stmt.executeUpdate(insertSQL);

            // System.out.println("Inserted");

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

    @FXML
    protected void onKeyboardPress() {

        Platform.runLater(() -> {
            PolicyResults pr = matchesPolicy(masterPassword.getText());
            String passwordError = String.join("\n", pr.errors);
            lblPasswordPolicy.setText("Master password should include:\n" + passwordError);
            if(pr.isValid)
                lblPasswordPolicy.setText("Password is valid.");

            lblPasswordStrength.setText(pr.strength);
            switch(pr.strength) {
                case "Weak": {
                    prgBarPassword.setProgress(.33);
                    prgBarPassword.setStyle("-fx-accent: #FF0000;");
                    break;
                }
                case "Medium": {
                    prgBarPassword.setStyle("-fx-accent: #FFBF00;");
                    prgBarPassword.setProgress(.66);
                    break;
                }
                case "Strong": {
                    prgBarPassword.setStyle("-fx-accent: #4caf50;");
                    prgBarPassword.setProgress(1);
                    break;
                }
            }
        });
    }

    /**
     * @description: This function checks the user password to see if it conforms to the password security policy.
     * @param password
     * @return PolicyResults Class
     */
    public PolicyResults matchesPolicy(String password) {
        List<String> errors = new ArrayList<>();

        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasSpecial = password.matches(".*[%@$^].*");
        boolean hasWhitespace = password.matches(".*\\s.*");
        boolean passwordLength = password.length() >= 8;

        if (!passwordLength) errors.add("Must have be at least 8 characters long.");
        if (!hasDigit) errors.add("Must have at least one digit.");
        if (!hasLower) errors.add("Must have at least one lowercase letter.");
        if (!hasUpper) errors.add("Must have at least one uppercase letter.");
        if (!hasSpecial) errors.add("Must have at least one special character (%@$^).");
        if (hasWhitespace) errors.add("Must not contain whitespace.");

        boolean isValid = errors.isEmpty();
        int score = 0;

        if (hasDigit) score++;
        if (hasLower) score++;
        if (hasUpper) score++;
        if (hasSpecial) score++;
        if (password.length() >= 12) score++; // give more score for length.

        String strength = "";
        if(score <= 2) strength = "Weak";
        else if(score == 2 || score == 3) strength = "Medium";
        else strength = "Strong";

        return new PolicyResults(isValid, strength, errors);
    }
}
