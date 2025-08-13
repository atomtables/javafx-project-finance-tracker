module dev.atomtables.financetracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.desktop;

    opens dev.atomtables.financetracker to javafx.fxml;
    exports dev.atomtables.financetracker;
    exports dev.atomtables.financetracker.styles;
    opens dev.atomtables.financetracker.styles to javafx.fxml;
    exports dev.atomtables.financetracker.views;
    opens dev.atomtables.financetracker.views to javafx.fxml;
}