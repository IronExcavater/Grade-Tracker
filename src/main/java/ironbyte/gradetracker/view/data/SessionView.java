package ironbyte.gradetracker.view.data;

import ironbyte.gradetracker.model.App;
import ironbyte.gradetracker.model.data.SessionData;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;

public class SessionView extends DataView<SessionData> {

    private final Label mark = new Label();
    private final Label gradePoints = new Label();
    private final Label creditPoints = new Label();

    public SessionView(SessionData data) {
        super(data, new int[]{55, 15, 15, 15}, "Unnamed Session");
        mark.textProperty().bind(Bindings.createStringBinding(() -> String.format(App.getSettings().getRounding(), data.getMark()),
                data.markProperty(), App.getSettings().roundingProperty()));
        gradePoints.textProperty().bind(Bindings.createStringBinding(() -> String.format(App.getSettings().getRounding(), data.getGradePoints()),
                data.gradePointsProperty(), App.getSettings().roundingProperty()));
        creditPoints.textProperty().bind(Bindings.format("%d", data.creditPointsProperty()));
        setColumns(name, mark, gradePoints, creditPoints);
    }
}
