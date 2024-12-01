package iron.gradetracker.controller;

import iron.gradetracker.*;
import iron.gradetracker.Utils;
import iron.gradetracker.model.*;
import iron.gradetracker.model.action.RemoveAction;
import iron.gradetracker.model.data.StudentData;
import iron.gradetracker.view.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class SettingsController extends Controller {

    @FXML private StringTextField nameTf;
    @FXML private CheckBox roundingCbx;

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

        Settings settings = App.getSettings();
        hdMarkTf.setProperties(settings.getGrade("HD").mark, true, new SimpleDoubleProperty(100));
        hdPointTf.setProperties(settings.getGrade("HD").point, true, null);
        dMarkTf.setProperties(settings.getGrade("D").mark, true, hdMarkTf.boundProperty());
        dPointTf.setProperties(settings.getGrade("D").point, true, hdPointTf.boundProperty());
        cMarkTf.setProperties(settings.getGrade("C").mark, true, dMarkTf.boundProperty());
        cPointTf.setProperties(settings.getGrade("C").point, true, dPointTf.boundProperty());
        pMarkTf.setProperties(settings.getGrade("P").mark, true, cMarkTf.boundProperty());
        pPointTf.setProperties(settings.getGrade("P").point, true, cPointTf.boundProperty());

        roundingCbx.selectedProperty().bindBidirectional(settings.lessRoundingProperty());
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