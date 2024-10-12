package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import javafx.beans.property.*;

public class AssessmentData extends Data {

    @Expose private final SimpleDoubleProperty score = new SimpleDoubleProperty();
    @Expose private final SimpleDoubleProperty maxScore = new SimpleDoubleProperty();
    @Expose private final SimpleIntegerProperty weight = new SimpleIntegerProperty();
    private final SimpleIntegerProperty remainingWeight = new SimpleIntegerProperty();
    private final SimpleDoubleProperty mark = new SimpleDoubleProperty();

    public AssessmentData(SubjectData parent, double score, double maxScore, int weight) {
        super(parent);
        scoreProperty().set(score);
        maxScoreProperty().set(maxScore);
        weightProperty().set(weight);

        scoreProperty().addListener(_ -> notifyParent());
        maxScoreProperty().addListener(_ -> notifyParent());
        weightProperty().addListener(_ -> notifyParent());
        markProperty().addListener(_ -> notifyParent());

        remainingWeight.bind(((SubjectData) getParent()).remainingWeightProperty().add(weightProperty()));
        mark.bind(scoreProperty().divide(maxScoreProperty()).multiply(weightProperty()));
    }

    @Override
    protected void update() {}

    public SimpleDoubleProperty scoreProperty() { return score; }
    public double getScore() { return score.get(); }

    public SimpleDoubleProperty maxScoreProperty() { return maxScore; }
    public double getMaxScore() { return maxScore.get(); }

    public SimpleIntegerProperty weightProperty() { return weight; }
    public int getWeight() { return weight.get(); }

    public SimpleIntegerProperty remainingWeightProperty() { return remainingWeight; }
    public int getRemainingWeight() { return remainingWeight.get(); }

    public SimpleDoubleProperty markProperty() { return mark; }
    public double getMark() { return mark.get(); }
}
