package com.benton.passforge.controller;

import com.benton.passforge.MainApplication;
import com.benton.passforge.model.LockoutManager;
import com.benton.passforge.model.PassforgeCell;
import com.benton.passforge.model.Passwords;
import com.benton.passforge.util.DatabaseConnector;
import com.benton.passforge.util.EncryptionUtils;
import com.benton.passforge.util.PasswordUtils;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;

public class MainController {

    @FXML private PasswordField unlockPassword;
    @FXML private ListView lvPasswords;
    @FXML private AnchorPane rootPane;
    @FXML private Button btnAddPassword, btnUnlockPassword;
    @FXML private Label lblError;

    private LockoutManager.LockoutData lockoutData;
    private Timeline lockoutTimer;

    /**
     * @description: purpose of this function is to check if .db user exists on startup.
     */
    @FXML
    protected void initialize() throws IOException {

        Path appDataDir = Paths.get(System.getProperty("user.home"), ".passforge");
        Path dbPath = appDataDir.resolve("passforge.db");

        File file = dbPath.toFile();

        // String userHome = System.getProperty("user.home");
        // File file = new File(userHome + File.separator + "Documents", "passforge.db");

        if (!file.exists() || file.length() == 0) {
            if(file.exists()) {
                file.delete(); // delete empty db.
            }

            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/com/benton/passforge/register-view.fxml"));
                    Stage stage = new Stage();

                    stage.setScene(new Scene(loader.load()));
                    stage.show();

                    // Open register page
                    Stage currStage = (Stage) rootPane.getScene().getWindow();
                    currStage.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            // System.out.println("Loading Lockout on Initialize");
            lockoutData = LockoutManager.load();

            // System.out.println(lockoutData.failedAttempts + ", " + lockoutData.lockoutEndTime);

            if(System.currentTimeMillis() < lockoutData.lockoutEndTime) {
                startLockoutTimer();
            }
        }
    }

    private void startLockoutTimer() {
        btnUnlockPassword.setDisable(true);

        lockoutTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {

            long remainingTime = lockoutData.lockoutEndTime - System.currentTimeMillis();
            if(remainingTime <= 0) {
                btnUnlockPassword.setDisable(false);
                lockoutTimer.stop();
                lockoutData.failedAttempts = 0;
                lockoutData.lockoutEndTime = 0;
                LockoutManager.save(lockoutData);
            } else {
                lblError.setText("Locked out: " + (remainingTime / 1000) + "s remaining");
                lblError.setVisible(true);
                btnUnlockPassword.setDisable(true);
            }
        }));
        lockoutTimer.setCycleCount(Timeline.INDEFINITE);
        lockoutTimer.play();
    }

    @FXML
    protected void onUnlockButtonClicked() throws Exception {

        if(System.currentTimeMillis() < lockoutData.lockoutEndTime) {
            lblError.setText("Locked out. Try again later");
        } else {

            Connection conn = DatabaseConnector.connect();
            String selectSQL = "SELECT * FROM users";

            var rs = conn.createStatement().executeQuery(selectSQL);
            boolean match = false;

            while(rs.next()) {
                match = PasswordUtils.verifyPassword(unlockPassword.getText().toCharArray(), rs.getString("password_hash"), rs.getString("salt"));
            }

            if(match) {
                refreshList();

                lockoutData.lockoutEndTime = 0;
                lockoutData.failedAttempts = 0;
                LockoutManager.save(lockoutData);

                unlockPassword.setDisable(true);
                btnAddPassword.setDisable(false);
                btnUnlockPassword.setDisable(true);
                lblError.setVisible(false);
            }
            else {
                lockoutData.failedAttempts++;
                lblError.setText("Incorrect Master Password.");
                lblError.setVisible(true);

                if (lockoutData.failedAttempts >= 5) {
                    lockoutData.lockoutEndTime = System.currentTimeMillis() + 900_000; // 15 Minutes Lockout
                    lblError.setText("Too many failed attempts.\nLocked out for 15 Minutes");
                    btnUnlockPassword.setDisable(true);
                }

                LockoutManager.save(lockoutData);
            }

            conn.close();
        }
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

    protected void refreshList() throws Exception{

        String passwordList = "SELECT * FROM forge";
        Connection conn = DatabaseConnector.connect();

        List<Passwords> listOfPasswords = new ArrayList<>();
        SecretKey key = EncryptionUtils.getSecretKeyFromPassword(unlockPassword.getText().toCharArray(), getUserSalt(conn));

        var rs = conn.createStatement().executeQuery(passwordList);
        while(rs.next()) {
            listOfPasswords.add(
                    new Passwords(
                            rs.getInt("id"),
                            rs.getString("site"),
                            EncryptionUtils.decrypt(rs.getString("password_encrypted"), key)
                    )
            );
        }

        ObservableList<Passwords> observableList = FXCollections.observableArrayList(listOfPasswords);
        lvPasswords.setItems(observableList);
        lvPasswords.setCellFactory(list -> new PassforgeCell(this));

        conn.close();
    }

    public ListView getListView() {
        return lvPasswords;
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

    /**
     * @description: This method mimics the Toast feature from Android.
     *               This will allow short pop-up messages to be displayed for the user.
     * @param message
     */
    public void showToast(String message) {
        Label toast = new Label(message);
        toast.setStyle("-fx-background-color: #323232; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 8px;");
        toast.setOpacity(0);
        toast.setTranslateY(20);
        // toast.setTranslateX((rootPane.getWidth() - 200) / 2);

        rootPane.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.millis(1500));
            pause.setOnFinished(event -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), toast);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> rootPane.getChildren().remove(toast));
                fadeOut.play();
            });
            pause.play();
        });
        fadeIn.play();
    }
}