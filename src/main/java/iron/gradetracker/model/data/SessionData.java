package iron.gradetracker.model.data;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

public class SessionData extends Data<SubjectData> {

    private final IntegerProperty creditPoints = new SimpleIntegerProperty();
    private final DoubleProperty gradePoints = new SimpleDoubleProperty();

    public SessionData() {
        super(SubjectData::new);
    }

    public DoubleProperty gradePointsProperty() { return gradePoints; }
    public double getGradePoints() { return gradePoints.get(); }

    public IntegerProperty creditPointsProperty() { return creditPoints; }
    public int getCreditPoints() { return creditPoints.get(); }

    @Override
    protected void addChild(SubjectData child) {
        super.addChild(child);
        child.creditPointsProperty().addListener(changeListener);
        child.markProperty().addListener(changeListener);
        child.gradePointsProperty().addListener(changeListener);
    }
    @Override
    protected void removeChild(SubjectData child) {
        super.removeChild(child);
        child.creditPointsProperty().removeListener(changeListener);
        child.markProperty().removeListener(changeListener);
        child.gradePointsProperty().removeListener(changeListener);
    }

    @Override
    protected void update() {
        creditPointsProperty().bind(Bindings.createIntegerBinding(() ->
                children.stream()
                        .mapToInt(SubjectData::getCreditPoints)
                        .sum(), children
        ));

        markProperty().bind(Bindings.createDoubleBinding(() ->
                children.stream()
                        .mapToDouble(subject -> subject.getMark() * subject.getCreditPoints())
                        .sum() / getCreditPoints(), children
        ));

        gradePointsProperty().bind(Bindings.createDoubleBinding(() ->
                children.stream()
                        .mapToDouble(subject -> subject.getGradePoints() * subject.getCreditPoints())
                        .sum() / getCreditPoints(), children
        ));
    }

    @Override
    public String toString() {
        return "Session: %s worth %scp has a wam of %s and sgpa of %s".formatted(getName(), getCreditPoints(), getMark(), getGradePoints());
    }
}
