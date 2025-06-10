package com.benton.passforge.controller;

import com.benton.passforge.model.Passwords;
import com.benton.passforge.util.PasswordUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class PassForgeCellController {

    @FXML private Label lblName, lblPassword;
    @FXML private ImageView imgHideShow;

    private Passwords passwords;
    private boolean showHide = false;


    public void setData(Passwords password) {
        lblName.setText(password.getName());
        // lblPassword.setText(password.getPassword());
        lblPassword.setText("•".repeat(password.getPassword_length()));
        passwords = password;
    }

    public void onCopyBtnClicked(ActionEvent event) {

    }

    public void onDeleteBtnClicked() {

    }

    public void onShowHideBtnClicked() {

        Image image;
        if (showHide) {
            image = new Image(getClass().getResource("/icons/hide.png").toExternalForm());
            lblPassword.setText("•".repeat(passwords.getPassword_length())); // Need to change length, not being used anymore;
            showHide = false;
        }
        else {
            image = new Image(getClass().getResource("/icons/show.png").toExternalForm());
            lblPassword.setText(passwords.getPassword());
            showHide = true;
        }
        imgHideShow.setImage(image);
    }
}
