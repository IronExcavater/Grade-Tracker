package iron.gradetracker.view.data;

import iron.gradetracker.model.data.SubjectData;
import iron.gradetracker.view.IntegerTextField;
import javafx.scene.control.Label;

public class SubjectView extends DataView<SubjectData> {

    private final IntegerTextField creditPoints;
    private final Label mark = new Label();
    private final Label grade = new Label();
    private final Label gradePoints = new Label();

    public SubjectView(SubjectData data) {
        super(data, new int[]{40, 15, 15, 15, 15}, new String[]{"Subject Name", "Credit Points", "Mark", "Grade", "Grade Points"}, "Unnamed Subject");
        creditPoints = new IntegerTextField(data.creditPointsProperty(), true);
        mark.textProperty().bind(data.markProperty().asString());
        grade.textProperty().bind(data.gradeProperty());
        gradePoints.textProperty().bind(data.gradePointsProperty().asString());
        setColumns(name, creditPoints, mark, grade, gradePoints);
    }

    public IntegerTextField getCreditPointsTf() { return creditPoints; }
}
