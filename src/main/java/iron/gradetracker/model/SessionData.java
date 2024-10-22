package iron.gradetracker.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import java.util.List;

public class SessionData extends Data<SubjectData> {

    private final IntegerProperty creditPoints = new SimpleIntegerProperty();
    private final DoubleProperty mark = new SimpleDoubleProperty();
    private final DoubleProperty gradePoints = new SimpleDoubleProperty();

    public SessionData() {}

    public DoubleProperty markProperty() { return mark; }
    public double getMark() { return mark.get(); }

    public DoubleProperty gradePointsProperty() { return gradePoints; }
    public double getGradePoints() { return gradePoints.get(); }

    public IntegerProperty creditPointsProperty() { return creditPoints; }
    public int getCreditPoints() { return creditPoints.get(); }

    @Override
    public SubjectData createChild() {
        SubjectData child = new SubjectData();
        addChild(child);
        return child;
    }

    @Override
    public void addChild(SubjectData child) {
        children.add(child);
        child.setParent(this);
        child.creditPointsProperty().addListener(_ -> update());
        child.markProperty().addListener(_ -> update());
        child.gradePointsProperty().addListener(_ -> update());
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
