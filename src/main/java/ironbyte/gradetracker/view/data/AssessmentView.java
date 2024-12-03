package ironbyte.gradetracker.view.data;

import ironbyte.gradetracker.model.data.AssessmentData;
import ironbyte.gradetracker.view.*;
import javafx.scene.control.DatePicker;

public class AssessmentView extends DataView<AssessmentData> {

    private final DoubleTextField score;
    private final DoubleTextField maxScore;
    private final IntegerTextField weight;
    private final DatePicker date;

    public AssessmentView(AssessmentData data) {
        super(data, new int[]{35, 15, 15, 15, 20}, "Unnamed Assessment");
        score = new DoubleTextField(data.scoreProperty(), true, data.maxScoreProperty());
        maxScore = new DoubleTextField(data.maxScoreProperty(), true);
        weight = new IntegerTextField(data.weightProperty(), true, data.remainingWeightProperty());

        date = new DatePicker(data.getDate());
        date.valueProperty().bindBidirectional(data.dateProperty());
        date.setPrefWidth(130);

        setColumns(name, score, maxScore, weight, date);
    }

    public DoubleTextField getScoreTf() { return score; }
    public DoubleTextField getMaxScoreTf() { return maxScore; }
    public IntegerTextField getWeightTf() { return weight; }
    public DatePicker getDatePicker() { return date; }
}
