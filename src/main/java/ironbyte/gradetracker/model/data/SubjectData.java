package ironbyte.gradetracker.model.data;

import com.google.gson.annotations.Expose;
import ironbyte.gradetracker.*;
import ironbyte.gradetracker.model.App;
import ironbyte.gradetracker.model.action.ChangeAction;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

public class SubjectData extends Data<AssessmentData> {

    @Expose private final IntegerProperty creditPoints = new SimpleIntegerProperty();
    private final IntegerProperty weight = new SimpleIntegerProperty();
    private final StringProperty grade = new SimpleStringProperty();
    private final DoubleProperty gradePoints = new SimpleDoubleProperty();

    public SubjectData() { this("", 6); }
    public SubjectData(String name, int creditPoints) {
        super(name, AssessmentData::new);
        creditPointsProperty().set(creditPoints);
        update();
    }

    public IntegerProperty creditPointsProperty() { return creditPoints; }
    public int getCreditPoints() { return creditPoints.get(); }

    public StringProperty gradeProperty() { return grade; }
    public String getGrade() { return grade.get(); }

    public DoubleProperty gradePointsProperty() { return gradePoints; }
    public double getGradePoints() { return gradePoints.get(); }

    public IntegerProperty weightProperty() { return weight; }
    public int getWeight() { return weight.get(); }

    @Override
    public void startListening() {
        super.startListening();
        creditPointsProperty().addListener((_, oldValue, newValue) -> {
            if (ActionManager.isActive()) return;
            ActionManager.executeAction(
                    new ChangeAction<>(this, (Integer) oldValue, (Integer) newValue, 0, creditPointsProperty()::set));
        });
        App.getSettings().getGrades().forEach(grade -> {
            grade.mark.addListener(_ -> update());
            grade.point.addListener(_ -> update());
        });
    }

    @Override
    protected void addChild(AssessmentData child) {
        super.addChild(child);
        child.remainingWeightProperty().bind(Bindings.createIntegerBinding(() -> (100 - getWeight()) + child.getWeight(),
                weightProperty(), child.weightProperty()));
        child.weightProperty().set(Math.min(child.getWeight(), child.getRemainingWeight()));
        child.weightProperty().addListener(changeListener);
        child.markProperty().addListener(changeListener);
    }
    @Override
    protected void removeChild(AssessmentData child) {
        super.removeChild(child);
        child.remainingWeightProperty().unbind();
        child.weightProperty().removeListener(changeListener);
        child.markProperty().removeListener(changeListener);
    }

    @Override
    protected void update() {
        weightProperty().bind(Bindings.createIntegerBinding(() ->
                children.stream()
                        .mapToInt(AssessmentData::getWeight)
                        .sum(), children
        ));
        markProperty().bind(Bindings.createDoubleBinding(() -> {
                double mark = children.stream()
                        .mapToDouble(assessment -> assessment.getMark() * 100)
                        .sum() / getWeight();
                return mark > 0 ? mark : 0;
                }, children
        ));
        gradeProperty().bind(Bindings.createStringBinding(() ->
                App.getSettings().getGrade(getMark()).name.get(), markProperty()
        ));
        gradePointsProperty().bind(Bindings.createDoubleBinding(() ->
                App.getSettings().getGrade(getMark()).point.get(), markProperty()
        ));
    }

    @Override
    public String toString() {
        return "Subject: %s worth %dcp has a mark of %f, receiving a %s".formatted(getName(), getCreditPoints(), getMark(), getGrade());
    }
}
