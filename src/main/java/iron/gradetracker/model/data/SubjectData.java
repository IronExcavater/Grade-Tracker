package iron.gradetracker.model.data;

import com.google.gson.annotations.Expose;
import iron.gradetracker.*;
import iron.gradetracker.model.App;
import iron.gradetracker.model.action.ChangeAction;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

public class SubjectData extends Data<AssessmentData> {

    @Expose private final IntegerProperty creditPoints = new SimpleIntegerProperty();
    private final IntegerProperty weight = new SimpleIntegerProperty();
    private final StringProperty grade = new SimpleStringProperty();
    private final DoubleProperty gradePoints = new SimpleDoubleProperty();

    public SubjectData(int creditPoints) {
        super(AssessmentData::new);
        gradeProperty().bind(Bindings.createStringBinding(() -> App.getGradeMap().getGrade(getMark()).name, markProperty()));
        gradePointsProperty().bind(Bindings.createDoubleBinding(() -> App.getGradeMap().getGrade(getMark()).point, markProperty()));

        creditPointsProperty().set(creditPoints);
    }

    public SubjectData() { this(6); }

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
            ActionManager.executeAction(new ChangeAction<>((Integer) oldValue, (Integer) newValue, creditPointsProperty()::set));
            DataManager.markDirty();
        });
    }

    @Override
    protected void addChild(AssessmentData child) {
        super.addChild(child);
        child.remainingWeightProperty().bind(Bindings.createIntegerBinding(() -> (100 - getWeight()) + getWeight(), weightProperty()));
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

        markProperty().bind(Bindings.createDoubleBinding(() ->
                children.stream()
                        .mapToDouble(AssessmentData::getMark)
                        .sum() / getWeight(), children
        ));
    }

    @Override
    public String toString() {
        return "Subject: %s worth %dcp has a mark of %f, receiving a %s".formatted(getName(), getCreditPoints(), getMark(), getGrade());
    }
}
