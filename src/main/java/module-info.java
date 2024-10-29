module niclasrogulski.gradetracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    exports iron.gradetracker;
    opens iron.gradetracker to javafx.fxml;
    exports iron.gradetracker.controller;
    opens iron.gradetracker.controller to javafx.fxml;
    exports iron.gradetracker.view;
    opens iron.gradetracker.view to javafx.fxml;

    exports iron.gradetracker.model;
    opens iron.gradetracker.model to com.google.gson;
    exports iron.gradetracker.model.action;
    opens iron.gradetracker.model.action to com.google.gson;
    exports iron.gradetracker.model.data;
    opens iron.gradetracker.model.data to com.google.gson;
}