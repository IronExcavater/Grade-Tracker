module ironbyte.gradetracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.desktop;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    exports ironbyte.gradetracker;
    opens ironbyte.gradetracker to javafx.fxml;
    exports ironbyte.gradetracker.controller;
    opens ironbyte.gradetracker.controller to javafx.fxml;
    exports ironbyte.gradetracker.view;
    opens ironbyte.gradetracker.view to javafx.fxml;
    exports ironbyte.gradetracker.view.data;

    exports ironbyte.gradetracker.model;
    opens ironbyte.gradetracker.model to com.google.gson;
    exports ironbyte.gradetracker.model.action;
    opens ironbyte.gradetracker.model.action to com.google.gson;
    exports ironbyte.gradetracker.model.data;
    opens ironbyte.gradetracker.model.data to com.google.gson;
}