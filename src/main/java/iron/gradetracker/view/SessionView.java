package iron.gradetracker.view;

import iron.gradetracker.model.SessionData;
import javafx.scene.text.Text;

public class SessionView extends DataView<SessionData> {

    private final Text mark = new Text();
    private final Text gradePoints = new Text();
    private final Text creditPoints = new Text();

    public SessionView(SessionData data) {
        super(data, new int[]{55, 15, 15, 15}, new String[]{"Session Name", "Mark", "Grade Points", "Credit Points"}, "Unnamed Session");
        mark.textProperty().bind(data.markProperty().asString());
        gradePoints.textProperty().bind(data.gradePointsProperty().asString());
        creditPoints.textProperty().bind(data.creditPointsProperty().asString());
        setColumns(name, mark, gradePoints, creditPoints);
    }
}
