package iron.gradetracker.controller;

import iron.gradetracker.model.*;
import iron.gradetracker.model.data.StudentData;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HomeController extends Controller {

    @FXML private Label gpaLbl;
    @FXML private Label wamLbl;

    public HomeController(Stage stage) { super(stage); }

    @FXML
    private void initialize() {
        StudentData studentData = App.getStudentData();
        gpaLbl.textProperty().bind(Bindings.format("%.2f", studentData.cgpaProperty()));
        wamLbl.textProperty().bind(Bindings.format("%.2f", studentData.markProperty()));
    }
}