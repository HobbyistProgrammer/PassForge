package com.benton.passforge.controller;

import com.benton.passforge.MainApplication;
import com.benton.passforge.model.PassforgeCell;
import com.benton.passforge.model.Passwords;
import com.benton.passforge.util.DatabaseConnector;
import com.benton.passforge.util.EncryptionUtils;
import com.benton.passforge.util.PasswordUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController {

    @FXML private PasswordField unlockPassword;
    @FXML private ListView lvPasswords;
    @FXML private Button btnAddPassword, btnUnlockPassword;
    @FXML private Label lblError;

    @FXML
    protected void onUnlockButtonClicked() throws Exception {
        Connection conn = DatabaseConnector.connect();

        String selectSQL = "SELECT * FROM users";

        var rs = conn.createStatement().executeQuery(selectSQL);
        boolean match = false;

        while(rs.next()) {
            match = PasswordUtils.verifyPassword(unlockPassword.getText().toCharArray(), rs.getString("password_hash"), rs.getString("salt"));
        }

        if(match) {
            refreshList();
            unlockPassword.setDisable(true);
            btnAddPassword.setDisable(false);
            btnUnlockPassword.setDisable(true);
            lblError.setVisible(false);
        }
        else {
            lblError.setVisible(true);
        }

        conn.close();
    }

    @FXML
    protected void onAddNewPasswordButtonClicked() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newpassword-view.fxml"));
        Parent root  = fxmlLoader.load();

        Stage stage = new Stage();
        stage.setTitle("PassForge - Enchanted with Protection IV");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        // stage.setUserData(unlockPassword.getText());
        AddPasswordController addPasswordController = fxmlLoader.getController();
        addPasswordController.setMasterPassword(unlockPassword.getText());

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        refreshList();
    }

    public String getMasterPassword() {
        return unlockPassword.getText();
    }

    protected void refreshList() throws Exception{

        String passwordList = "SELECT * FROM forge";
        Connection conn = DatabaseConnector.connect();

        List<Passwords> listOfPasswords = new ArrayList<>();
        SecretKey key = EncryptionUtils.getSecretKeyFromPassword(unlockPassword.getText().toCharArray(), getUserSalt(conn));

        var rs = conn.createStatement().executeQuery(passwordList);
        while(rs.next()) {
            listOfPasswords.add(
                    new Passwords(
                            rs.getString("site"),
                            EncryptionUtils.decrypt(rs.getString("password_encrypted"), key),
                            rs.getInt("password_length")
                    )
            );
        }

        ObservableList<Passwords> observableList = FXCollections.observableArrayList(listOfPasswords);
        lvPasswords.setItems(observableList);
        lvPasswords.setCellFactory(list -> new PassforgeCell());

        conn.close();
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
}