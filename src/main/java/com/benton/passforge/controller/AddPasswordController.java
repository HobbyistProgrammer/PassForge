package com.benton.passforge.controller;

import com.benton.passforge.util.DatabaseConnector;
import com.benton.passforge.util.EncryptionUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


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

    /**
     * @description This method obtains user specific salt to be used for AES encryption/decryption on other passwords.
     * @param conn
     * @return user account salt
     * @throws SQLException
     */
    public String getUserSalt(Connection conn) throws SQLException {
        String query = "SELECT salt FROM users LIMIT 1";

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            if (rs.next()) {
                return rs.getString("salt");
            } else {
                return null;
            }
        }
    }

    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
        // System.out.println("Got Password: " + masterPassword);
    }
}
