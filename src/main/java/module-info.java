module com.benton.passforge {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires jdk.jdi;
    requires java.sql;
    requires java.desktop;

    opens com.benton.passforge to javafx.fxml;
    exports com.benton.passforge;
    exports com.benton.passforge.controller;
    opens com.benton.passforge.controller to javafx.fxml;
    exports com.benton.passforge.model;
    opens com.benton.passforge.model to javafx.fxml;
}