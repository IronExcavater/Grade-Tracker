package iron.gradetracker.controller;

import iron.gradetracker.*;
import iron.gradetracker.Utils;
import iron.gradetracker.model.*;
import iron.gradetracker.model.action.RemoveAction;
import iron.gradetracker.model.data.StudentData;
import iron.gradetracker.view.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class SettingsController extends Controller {

    @FXML private StringTextField nameTf;

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
        StudentData studentData = App.getStudentData();
        nameTf.setBoundProperty(studentData.nameProperty(), true);

        GradeScheme gradeScheme = App.getGradeScheme();
        hdMarkTf.setBoundProperty(gradeScheme.getGrade("HD").mark, true);
        hdPointTf.setBoundProperty(gradeScheme.getGrade("HD").point, true);
        dMarkTf.setBoundProperty(gradeScheme.getGrade("D").mark, true);
        dPointTf.setBoundProperty(gradeScheme.getGrade("D").point, true);
        cMarkTf.setBoundProperty(gradeScheme.getGrade("C").mark, true);
        cPointTf.setBoundProperty(gradeScheme.getGrade("C").point, true);
        pMarkTf.setBoundProperty(gradeScheme.getGrade("P").mark, true);
        pPointTf.setBoundProperty(gradeScheme.getGrade("P").point, true);

        Utils.addKeyBind(stage.getScene(), Utils.createKeyBind(KeyCode.S), _ -> DataManager.saveData());
    }

    @FXML
    private void handleErase() {
        var overridePopup = Utils.createAlert(Alert.AlertType.WARNING,
                "Erase data", "Erase existing data.", "Do you want to continue?",
                ButtonType.YES, ButtonType.CANCEL);

        var overrideResult = overridePopup.showAndWait();
        overrideResult.ifPresent(result -> {
            if (result.equals(ButtonType.YES))
                ActionManager.executeAction(new RemoveAction<>(App.getStudentData().getChildren()));
        });
    }
}