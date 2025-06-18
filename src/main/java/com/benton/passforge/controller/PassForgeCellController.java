package com.benton.passforge.controller;

import com.benton.passforge.model.Passwords;
import com.benton.passforge.util.DatabaseConnector;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.sql.Connection;
import java.sql.SQLException;

public class PassForgeCellController {

    @FXML private Label lblName, lblPassword;
    @FXML private ImageView imgHideShow;

    private Passwords passwords;
    private MainController mainController;
    private boolean showHide = false;


    public void setData(Passwords password) {
        lblName.setText(password.getName());
        // lblPassword.setText(password.getPassword());
        lblPassword.setText("•".repeat(password.getPassword().length()));
        passwords = password;
    }

    /**
     * @description This method allows users to click copy icon beside a password to copy password to 'Clipboard'.
     *              This method works by getting a reference to the main controller to display a Toast.
     */
    public void onCopyBtnClicked() {
        if(passwords != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();

            content.putString(passwords.getPassword());
            clipboard.setContent(content);

            if(mainController != null){
                mainController.showToast("Password copied to clipboard");
            }
        }
    }

    public void setMainController(MainController controller) { mainController = controller; }

    public void onDeleteBtnClicked() throws SQLException {
        ListView listView = mainController.getListView();
        int index = listView.getSelectionModel().getSelectedIndex();

        if(index >= 0) {
            Passwords selected = (Passwords) listView.getItems().get(index);
            int idToDelete = selected.getId();

            Connection conn = DatabaseConnector.connect();
            conn.createStatement().execute("DELETE FROM forge WHERE id = " + idToDelete + ";");

            mainController.showToast("Password has been put into the forge");

            listView.getItems().remove(index);
        }
    }

    public void onShowHideBtnClicked() {

        Image image;
        if (showHide) {
            image = new Image(getClass().getResource("/icons/hide.png").toExternalForm());
            lblPassword.setText("•".repeat(passwords.getPassword().length())); // Need to change length, not being used anymore;
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
