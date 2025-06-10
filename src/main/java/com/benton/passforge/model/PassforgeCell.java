package com.benton.passforge.model;

import com.benton.passforge.controller.MainController;
import com.benton.passforge.controller.PassForgeCellController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;

public class PassforgeCell extends ListCell<Passwords> {
    private FXMLLoader loader;

    @Override
    protected void updateItem(Passwords password, boolean empty) {
        super.updateItem(password, empty);

        if(empty || password == null) {
            setGraphic(null);
            setStyle("-fx-background-color: #090c9b;");
        } else {
            if(loader == null) {
                loader = new FXMLLoader(getClass().getResource("/com/benton/passforge/passforge_cell-view.fxml"));
                try {

                    loader.load();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            PassForgeCellController controller = loader.getController();
            if(controller != null) {
                controller.setData(password);
                setStyle("-fx-padding: 0px;");
                setGraphic(loader.getRoot());
            }
        }
    }
}
