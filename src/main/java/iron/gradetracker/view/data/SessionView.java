package iron.gradetracker.view.data;

import iron.gradetracker.model.data.SessionData;
import javafx.scene.control.Label;

public class SessionView extends DataView<SessionData> {

    private final Label mark = new Label();
    private final Label gradePoints = new Label();
    private final Label creditPoints = new Label();

    public SessionView(SessionData data) {
        super(data, new int[]{55, 15, 15, 15}, new String[]{"Session Name", "Mark", "Grade Points", "Credit Points"}, "Unnamed Session");
        mark.textProperty().bind(data.markProperty().asString());
        gradePoints.textProperty().bind(data.gradePointsProperty().asString());
        creditPoints.textProperty().bind(data.creditPointsProperty().asString());
        setColumns(name, mark, gradePoints, creditPoints);
    }
}
