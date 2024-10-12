package iron.gradetracker.view;

import iron.gradetracker.model.*;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.util.converter.NumberStringConverter;

public class AssessmentView extends DataView {

    private final TextField score = new TextField();
    private final TextField maxScore = new TextField();
    private final TextField weight = new TextField();

    public AssessmentView(AssessmentData data) {
        super(data, "Unnamed Assessment");
        score.setPrefWidth(0);
        maxScore.setPrefWidth(0);
        weight.setPrefWidth(0);

        score.setTextFormatter(decimalFormatter(data.maxScoreProperty()));
        weight.setTextFormatter(integerFormatter(data.remainingWeightProperty()));

        NumberStringConverter converter = new NumberStringConverter();
        Bindings.bindBidirectional(score.textProperty(), data.scoreProperty(), converter);
        Bindings.bindBidirectional(maxScore.textProperty(), data.maxScoreProperty(), converter);
        Bindings.bindBidirectional(weight.textProperty(), data.weightProperty(), converter);
        setColumns(new double[]{70, 10, 10, 10}, name, score, maxScore, weight);
    }
}
