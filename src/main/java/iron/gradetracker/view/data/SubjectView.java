package iron.gradetracker.view.data;

import iron.gradetracker.model.data.SubjectData;
import iron.gradetracker.view.IntegerTextField;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;

public class SubjectView extends DataView<SubjectData> {

    private final IntegerTextField creditPoints;
    private final Label mark = new Label();
    private final Label grade = new Label();
    private final Label gradePoints = new Label();

    public SubjectView(SubjectData data) {
        super(data, new int[]{40, 15, 15, 15, 15}, "Unnamed Subject");
        creditPoints = new IntegerTextField(data.creditPointsProperty(), true);
        mark.textProperty().bind(Bindings.format("%.2f", data.markProperty()));
        grade.textProperty().bind(data.gradeProperty());
        gradePoints.textProperty().bind(Bindings.format("%.0f", data.gradePointsProperty()));
        setColumns(name, creditPoints, mark, grade, gradePoints);
    }

    public IntegerTextField getCreditPointsTf() { return creditPoints; }
}
