package iron.gradetracker.view;

import iron.gradetracker.model.SessionData;
import javafx.scene.text.Text;

public class SessionView extends DataView {

    private final Text mark = new Text();
    private final Text gradePoints = new Text();
    private final Text creditPoints = new Text();

    public SessionView(SessionData data) {
        super(data, "Unnamed Session");
        mark.textProperty().bind(data.markProperty().asString());
        gradePoints.textProperty().bind(data.gradePointsProperty().asString());
        creditPoints.textProperty().bind(data.creditPointsProperty().asString());
        setColumns(new double[]{70, 10, 10, 10}, name, mark, gradePoints, creditPoints);
    }
}
