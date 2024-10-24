package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import javafx.beans.property.*;
import java.util.List;

public class AssessmentData extends Data<AssessmentData> {

    @Expose private final DoubleProperty score = new SimpleDoubleProperty();
    @Expose private final DoubleProperty maxScore = new SimpleDoubleProperty();
    @Expose private final IntegerProperty weight = new SimpleIntegerProperty();
    private final IntegerProperty remainingWeight = new SimpleIntegerProperty();

    public AssessmentData(double score, double maxScore, int weight) {
        markProperty().bind(scoreProperty().divide(maxScoreProperty()).multiply(weightProperty()).multiply(100));

        maxScoreProperty().set(maxScore);
        scoreProperty().set(Math.min(score, getMaxScore()));
        weightProperty().set(Math.min(weight, getRemainingWeight()));
    }

    public AssessmentData() { this(0, 100, 20); }

    public DoubleProperty scoreProperty() { return score; }
    public double getScore() { return score.get(); }

    public DoubleProperty maxScoreProperty() { return maxScore; }
    public double getMaxScore() { return maxScore.get(); }

    public IntegerProperty weightProperty() { return weight; }
    public int getWeight() { return weight.get(); }

    public IntegerProperty remainingWeightProperty() { return remainingWeight; }
    public int getRemainingWeight() { return remainingWeight.get(); }

    @Override
    public AssessmentData createChild() {
        throw new IllegalArgumentException("AssessmentData does not support children");
    }

    @Override
    public void addChild(AssessmentData child) {
        throw new IllegalArgumentException("AssessmentData does not support children");
    }

    @Override
    public void removeChildren(List<AssessmentData> children) {
        throw new IllegalArgumentException("AssessmentData does not support children");
    }

    @Override
    protected void update() {}

    @Override
    public String toString() {
        return "Assessment: %s weighted %d%% has a score of %f/%f".formatted(getName(), getWeight(), getScore(), getMaxScore());
    }
}
