package ironbyte.gradetracker.view.data;

import ironbyte.gradetracker.model.App;
import ironbyte.gradetracker.model.data.SubjectData;
import ironbyte.gradetracker.view.IntegerTextField;
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
        mark.textProperty().bind(Bindings.createStringBinding(() -> String.format(App.getSettings().getRounding(), data.getMark()),
                data.markProperty(), App.getSettings().roundingProperty()));
        grade.textProperty().bind(data.gradeProperty());
        gradePoints.textProperty().bind(Bindings.createStringBinding(() -> {
            double value = data.gradePointsProperty().get();
            if (value == (long) value) return String.format("%d", (long) value);
            else return String.format("%.2f", value);
        }, data.gradePointsProperty()));
        setColumns(name, creditPoints, mark, grade, gradePoints);
    }

    public IntegerTextField getCreditPointsTf() { return creditPoints; }
}
