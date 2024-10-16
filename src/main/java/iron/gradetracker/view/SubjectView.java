package iron.gradetracker.view;

import iron.gradetracker.model.SubjectData;
import javafx.scene.text.Text;

public class SubjectView extends DataView<SubjectData> {

    private final IntegerTextField creditPoints;
    private final Text mark = new Text();
    private final Text grade = new Text();
    private final Text gradePoints = new Text();

    public SubjectView(SubjectData data) {
        super(data, new int[]{40, 15, 15, 15, 15}, new String[]{"Subject Name", "Credit Points", "Mark", "Grade", "Grade Points"}, "Unnamed Subject");
        creditPoints = new IntegerTextField(data.creditPointsProperty(), true);
        mark.textProperty().bind(data.markProperty().asString());
        grade.textProperty().bind(data.gradePointsProperty().asString());
        gradePoints.textProperty().bind(data.creditPointsProperty().asString());
        setColumns(name, creditPoints, mark, grade, gradePoints);
    }
}
