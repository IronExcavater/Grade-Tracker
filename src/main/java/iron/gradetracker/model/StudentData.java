package iron.gradetracker.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import java.util.List;

public class StudentData extends Data<SessionData> {

    private final IntegerProperty creditPoints = new SimpleIntegerProperty();
    private final DoubleProperty cgpa = new SimpleDoubleProperty();

    public StudentData() {
        name.set("root");
    }

    public DoubleProperty cgpaProperty() { return cgpa; }
    public double getCgpa() { return cgpa.get(); }

    public IntegerProperty creditPointsProperty() { return creditPoints; }
    public int getCreditPoints() { return creditPoints.get(); }

    @Override
    public SessionData createChild() {
        SessionData child = new SessionData();
        addChild(child);
        return child;
    }

    @Override
    public void addChild(SessionData child) {
        children.add(child);
        child.setParent(this);
        child.creditPointsProperty().addListener(_ -> update());
        child.markProperty().addListener(_ -> update());
        child.gradePointsProperty().addListener(_ -> update());
    }

    @Override
    public void removeChildren(List<SessionData> children) {
        this.children.removeAll(children);
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
