package com.benton.passforge.controller;

import com.benton.passforge.model.Passwords;
import com.benton.passforge.util.DatabaseConnector;
import com.benton.passforge.util.EncryptionUtils;
import com.benton.passforge.util.PasswordUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;


public class AddPasswordController {

    @FXML private TextField txtName;
    @FXML private PasswordField pfPassword, pfConfirmPassword;

    private String masterPassword;

    @FXML
    protected void onAddPasswordButtonClick(ActionEvent event) throws Exception {
        if (pfPassword.getText().equals(pfConfirmPassword.getText())) {

            Connection conn = DatabaseConnector.connect();

            String userSalt = getUserSalt(conn);

            // System.out.println("user salt: " + userSalt);
            // String salt = PasswordUtils.generateSalt();
            // String hashedPassword = PasswordUtils.hashPassword(pfPassword.getText().toCharArray(), userSalt);

            SecretKey key = EncryptionUtils.getSecretKeyFromPassword(masterPassword.toCharArray(), userSalt);
            String encryptedPassword = EncryptionUtils.encrypt(pfPassword.getText(), key);

            String addPasswordSQL = String.format(
                    "INSERT INTO forge (site, password_encrypted) VALUES ('%s', '%s')",
                    txtName.getText(),
                    encryptedPassword
            );
            conn.createStatement().executeUpdate(addPasswordSQL);

            conn.close();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        }
    }

    public String getUserSalt(Connection conn) throws SQLException {
        String passwordList = "SELECT * FROM users";

        String salt = "", password = "";

        var rs = conn.createStatement().executeQuery(passwordList);
        while(rs.next()) {
            salt = rs.getString("salt");
            password = rs.getString("password_hash");
        }

        return salt;
    }

    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
        // System.out.println("Got Password: " + masterPassword);
    }
}
