package iron.gradetracker.model.data;

import com.google.gson.annotations.Expose;
import iron.gradetracker.ActionManager;
import iron.gradetracker.model.action.ChangeAction;
import javafx.beans.property.*;

public class AssessmentData extends Data<AssessmentData> {

    @Expose private final DoubleProperty score = new SimpleDoubleProperty();
    @Expose private final DoubleProperty maxScore = new SimpleDoubleProperty();
    @Expose private final IntegerProperty weight = new SimpleIntegerProperty();
    private final IntegerProperty remainingWeight = new SimpleIntegerProperty();

    public AssessmentData(double score, double maxScore, int weight) {
        super(null);
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
    public void startListening() {
        super.startListening();
        scoreProperty().addListener((_, oldValue, newValue) -> {
            if (ActionManager.isActive()) return;
            ActionManager.executeAction(
                    new ChangeAction<>(this, (Double) oldValue, (Double) newValue, 0d, scoreProperty()::set));
        });
        maxScoreProperty().addListener((_, oldValue, newValue) -> {
            if (ActionManager.isActive()) return;
            ActionManager.executeAction(
                    new ChangeAction<>(this, (Double) oldValue, (Double) newValue, 0d, maxScoreProperty()::set));
        });
        weightProperty().addListener((_, oldValue, newValue) -> {
            if (ActionManager.isActive()) return;
            ActionManager.executeAction(
                    new ChangeAction<>(this, (Integer) oldValue, (Integer) newValue, 0, weightProperty()::set));
        });
    }

    @Override
    protected void update() {}

    @Override
    public String toString() {
        return "Assessment: %s weighted %d%% has a score of %f/%f".formatted(getName(), getWeight(), getScore(), getMaxScore());
    }
}
