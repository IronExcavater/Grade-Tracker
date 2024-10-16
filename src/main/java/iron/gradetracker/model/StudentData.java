package iron.gradetracker.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import java.util.List;

public class StudentData extends Data<StudentData, SessionData> {

    private final SimpleIntegerProperty creditPoints = new SimpleIntegerProperty();
    private final SimpleDoubleProperty cwam = new SimpleDoubleProperty();
    private final SimpleDoubleProperty cgpa = new SimpleDoubleProperty();

    public StudentData() {
        name.set("root");
    }

    public SimpleDoubleProperty cwamProperty() { return cwam; }
    public double getCwam() { return cwam.get(); }

    public SimpleDoubleProperty cgpaProperty() { return cgpa; }
    public double getCgpa() { return cgpa.get(); }

    public SimpleIntegerProperty creditPointsProperty() { return creditPoints; }
    public int getCreditPoints() { return creditPoints.get(); }

    @Override
    public SessionData createChild() {
        SessionData data = new SessionData(this);
        children.add(data);
        return data;
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

        cwamProperty().bind(Bindings.createDoubleBinding(() ->
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
}
