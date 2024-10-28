module niclasrogulski.gradetracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens iron.gradetracker to javafx.fxml;
    exports iron.gradetracker;
    exports iron.gradetracker.controller;
    exports iron.gradetracker.model;
    opens iron.gradetracker.controller to javafx.fxml;

    opens iron.gradetracker.model to com.google.gson;
    exports iron.gradetracker.model.action;
    opens iron.gradetracker.model.action to com.google.gson;
    exports iron.gradetracker.model.data;
    opens iron.gradetracker.model.data to com.google.gson;
}