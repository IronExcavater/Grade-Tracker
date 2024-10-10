module niclasrogulski.gradetracker {
    requires javafx.controls;
    requires javafx.fxml;


    opens iron.gradetracker to javafx.fxml;
    exports iron.gradetracker;
    exports iron.gradetracker.controller;
    opens iron.gradetracker.controller to javafx.fxml;
}