package iron.gradetracker.controller;

import iron.gradetracker.model.*;
import iron.gradetracker.view.*;
import javafx.fxml.FXML;
import javafx.stage.Stage;

public class SettingsController extends Controller {

    @FXML private DoubleTextField hdMarkTf;
    @FXML private DoubleTextField hdPointTf;
    @FXML private DoubleTextField dMarkTf;
    @FXML private DoubleTextField dPointTf;
    @FXML private DoubleTextField cMarkTf;
    @FXML private DoubleTextField cPointTf;
    @FXML private DoubleTextField pMarkTf;
    @FXML private DoubleTextField pPointTf;

    public SettingsController(Stage stage) { super(stage); }

    @FXML
    private void initialize() {
        GradeScheme gradeScheme = App.getGradeScheme();
        hdMarkTf.setBoundProperty(gradeScheme.getGrade("HD").mark, true);
        hdPointTf.setBoundProperty(gradeScheme.getGrade("HD").point, true);
        dMarkTf.setBoundProperty(gradeScheme.getGrade("D").mark, true);
        dPointTf.setBoundProperty(gradeScheme.getGrade("D").point, true);
        cMarkTf.setBoundProperty(gradeScheme.getGrade("C").mark, true);
        cPointTf.setBoundProperty(gradeScheme.getGrade("C").point, true);
        pMarkTf.setBoundProperty(gradeScheme.getGrade("P").mark, true);
        pPointTf.setBoundProperty(gradeScheme.getGrade("P").point, true);
    }
}