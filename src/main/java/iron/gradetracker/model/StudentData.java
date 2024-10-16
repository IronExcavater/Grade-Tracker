package iron.gradetracker.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

public class StudentData extends Data<StudentData, SessionData> {

    private final SimpleIntegerProperty creditPoints = new SimpleIntegerProperty();
    private final SimpleDoubleProperty cwam = new SimpleDoubleProperty();
    private final SimpleDoubleProperty cgpa = new SimpleDoubleProperty();

    public StudentData() {
        name.set("root");

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

        cwamProperty().bind(Bindings.createDoubleBinding(() ->
                children.stream()
                        .mapToDouble(session -> session.getGradePoints() * session.getCreditPoints())
                        .sum() / getCreditPoints(), children
        ));
    }

    public SimpleDoubleProperty cwamProperty() { return cwam; }
    public double getCwam() { return cwam.get(); }

    public SimpleDoubleProperty cgpaProperty() { return cgpa; }
    public double getCgpa() { return cgpa.get(); }

    public SimpleIntegerProperty creditPointsProperty() { return creditPoints; }
    public int getCreditPoints() { return creditPoints.get(); }
}
