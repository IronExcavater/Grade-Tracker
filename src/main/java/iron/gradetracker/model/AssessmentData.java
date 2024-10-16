package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import java.util.List;

public class AssessmentData extends Data<SubjectData, AssessmentData> {

    @Expose private final SimpleDoubleProperty score = new SimpleDoubleProperty();
    @Expose private final SimpleDoubleProperty maxScore = new SimpleDoubleProperty();
    @Expose private final SimpleIntegerProperty weight = new SimpleIntegerProperty();
    private final SimpleIntegerProperty remainingWeight = new SimpleIntegerProperty();
    private final SimpleDoubleProperty mark = new SimpleDoubleProperty();

    public AssessmentData(SubjectData parent, double score, double maxScore, int weight) {
        super(parent);
        getParent().update();

        remainingWeightProperty().bind(Bindings.createIntegerBinding(() -> (100 - getParent().getWeight()) + getWeight(), getParent().weightProperty()));
        markProperty().bind(scoreProperty().divide(maxScoreProperty()).multiply(weightProperty()));

        maxScoreProperty().set(maxScore);
        scoreProperty().set(Math.min(score, getMaxScore()));
        weightProperty().set(Math.min(weight, getRemainingWeight()));

        weightProperty().addListener(_ -> getParent().update());
        markProperty().addListener(_ -> getParent().update());
    }

    public AssessmentData(SubjectData parent) { this(parent, 0, 100, 20); }

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

    @Override
    public AssessmentData createChild() {
        throw new IllegalArgumentException("AssessmentData does not support children");
    }

    @Override
    public void removeChildren(List<AssessmentData> children) {
        throw new IllegalArgumentException("AssessmentData does not support children");
    }

    @Override
    protected void update() {}
}
