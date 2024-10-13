package iron.gradetracker.view;

import iron.gradetracker.model.*;

public class AssessmentView extends DataView {

    private final DoubleTextField score;
    private final DoubleTextField maxScore;
    private final IntegerTextField weight;

    public AssessmentView(AssessmentData data) {
        super(data, "Unnamed Assessment");
        score = new DoubleTextField(data.scoreProperty(), true, data.maxScoreProperty());
        maxScore = new DoubleTextField(data.maxScoreProperty(), true);
        weight = new IntegerTextField(data.weightProperty(), true, data.remainingWeightProperty());
        setColumns(new double[]{70, 10, 10, 10}, name, score, maxScore, weight);
    }
}
