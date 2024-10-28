package iron.gradetracker.model.data;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

public class StudentData extends Data<SessionData> {

    private final IntegerProperty creditPoints = new SimpleIntegerProperty();
    private final DoubleProperty cgpa = new SimpleDoubleProperty();

    public StudentData() {
        super(SessionData::new);
        name.set("root");
    }

    public DoubleProperty cgpaProperty() { return cgpa; }
    public double getCgpa() { return cgpa.get(); }

    public IntegerProperty creditPointsProperty() { return creditPoints; }
    public int getCreditPoints() { return creditPoints.get(); }

    @Override
    protected void addChild(SessionData child) {
        super.addChild(child);
        child.creditPointsProperty().addListener(changeListener);
        child.markProperty().addListener(changeListener);
        child.gradePointsProperty().addListener(changeListener);
    }
    @Override
    protected void removeChild(SessionData child) {
        super.removeChild(child);
        child.creditPointsProperty().removeListener(changeListener);
        child.markProperty().removeListener(changeListener);
        child.gradePointsProperty().removeListener(changeListener);
    }

    @Override
    protected void update() {
        creditPointsProperty().bind(Bindings.createIntegerBinding(() ->
                children.stream()
                        .mapToInt(SessionData::getCreditPoints)
                        .sum(), children
        ));

        markProperty().bind(Bindings.createDoubleBinding(() ->
                children.stream()
                        .mapToDouble(session -> session.getMark() * session.getCreditPoints())
                        .sum() / getCreditPoints(), children
        ));

        cgpaProperty().bind(Bindings.createDoubleBinding(() ->
                children.stream()
                        .mapToDouble(session -> session.getGradePoints() * session.getCreditPoints())
                        .sum() / getCreditPoints(), children
        ));
    }

    @Override
    public String toString() {
        return "Student: Has a cwam of %f and cgpa of %f".formatted(getMark(), getCgpa());
    }
}
