package iron.gradetracker.controller;

import iron.gradetracker.model.*;
import iron.gradetracker.model.data.StudentData;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

public class HomeController extends Controller {

    @FXML private Text gpaTxt;
    @FXML private Text wamTxt;

    public HomeController(Stage stage) { super(stage); }

    @FXML
    private void initialize() {
        StudentData studentData = App.getStudentData();
        NumberStringConverter converter = new NumberStringConverter();
        gpaTxt.textProperty().bindBidirectional(studentData.cgpaProperty(), converter);
        wamTxt.textProperty().bindBidirectional(studentData.markProperty(), converter);
    }
}