package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import java.util.List;

public class SessionData extends Data<StudentData, SubjectData> {

    @Expose private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleIntegerProperty creditPoints = new SimpleIntegerProperty();
    private final SimpleDoubleProperty mark = new SimpleDoubleProperty();
    private final SimpleDoubleProperty gradePoints = new SimpleDoubleProperty();

    public SessionData(StudentData parent) {
        super(parent);
        getParent().update();

        creditPointsProperty().addListener(_ -> getParent().update());
        markProperty().addListener(_ -> getParent().update());
        gradePointsProperty().addListener(_ -> getParent().update());
    }

    public SimpleStringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }

    public SimpleDoubleProperty markProperty() { return mark; }
    public double getMark() { return mark.get(); }

    public SimpleDoubleProperty gradePointsProperty() { return gradePoints; }
    public double getGradePoints() { return gradePoints.get(); }

    public SimpleIntegerProperty creditPointsProperty() { return creditPoints; }
    public int getCreditPoints() { return creditPoints.get(); }

    @Override
    public SubjectData createChild() {
        SubjectData data = new SubjectData(this);
        children.add(data);
        return data;
    }

    @Override
    public void removeChildren(List<SubjectData> children) {
        this.children.removeAll(children);
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
}
