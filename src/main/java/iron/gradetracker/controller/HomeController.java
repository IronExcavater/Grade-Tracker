package iron.gradetracker.controller;

import iron.gradetracker.model.*;
import iron.gradetracker.model.data.StudentData;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

public class HomeController extends Controller {

    @FXML private Label gpaLbl;
    @FXML private Label wamLbl;

    public HomeController(Stage stage) { super(stage); }

    @FXML
    private void initialize() {
        StudentData studentData = App.getStudentData();
        NumberStringConverter converter = new NumberStringConverter();
        gpaLbl.textProperty().bindBidirectional(studentData.cgpaProperty(), converter);
        wamLbl.textProperty().bindBidirectional(studentData.markProperty(), converter);
    }
}