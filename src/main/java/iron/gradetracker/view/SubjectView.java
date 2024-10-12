package iron.gradetracker.view;

import iron.gradetracker.model.SessionData;
import iron.gradetracker.model.SubjectData;
import javafx.beans.binding.Bindings;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.converter.NumberStringConverter;

public class SubjectView extends DataView {

    private final TextField creditPoints = new TextField();
    private final Text mark = new Text();
    private final Text grade = new Text();
    private final Text gradePoints = new Text();

    public SubjectView(SubjectData data) {
        super(data, "Unnamed Subject");
        creditPoints.setPrefWidth(0);

        NumberStringConverter converter = new NumberStringConverter();
        Bindings.bindBidirectional(creditPoints.textProperty(), data.creditPointsProperty(), converter);
        mark.textProperty().bind(data.markProperty().asString());
        grade.textProperty().bind(data.gradePointsProperty().asString());
        gradePoints.textProperty().bind(data.creditPointsProperty().asString());
        setColumns(new double[]{60, 10, 10, 10, 10}, name, creditPoints, mark, grade, gradePoints);
    }
}
