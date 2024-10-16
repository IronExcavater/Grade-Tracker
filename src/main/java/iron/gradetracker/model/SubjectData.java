package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import java.util.List;

public class SubjectData extends Data<SessionData, AssessmentData> {

    @Expose private final SimpleStringProperty name = new SimpleStringProperty();
    @Expose private final SimpleIntegerProperty creditPoints = new SimpleIntegerProperty();
    private final SimpleIntegerProperty weight = new SimpleIntegerProperty();
    private final SimpleDoubleProperty mark = new SimpleDoubleProperty();
    private final SimpleStringProperty grade = new SimpleStringProperty();
    private final SimpleDoubleProperty gradePoints = new SimpleDoubleProperty();

    public SubjectData(SessionData parent, int creditPoints) {
        super(parent);
        getParent().update();

        gradeProperty().bind(Bindings.createStringBinding(() -> App.getGradeMap().getGrade(getMark()).name, markProperty()));
        gradePointsProperty().bind(Bindings.createDoubleBinding(() -> App.getGradeMap().getGrade(getMark()).point, markProperty()));

        creditPointsProperty().set(creditPoints);

        creditPointsProperty().addListener(_ -> getParent().update());
        markProperty().addListener(_ -> getParent().update());
        gradePointsProperty().addListener(_ -> getParent().update());
    }

    public SubjectData(SessionData parent) { this(parent, 6); }

    public SimpleStringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }

    public SimpleIntegerProperty creditPointsProperty() { return creditPoints; }
    public int getCreditPoints() { return creditPoints.get(); }

    public SimpleDoubleProperty markProperty() { return mark; }
    public double getMark() { return mark.get(); }

    public SimpleStringProperty gradeProperty() { return grade; }
    public String getGrade() { return grade.get(); }

    public SimpleDoubleProperty gradePointsProperty() { return gradePoints; }
    public double getGradePoints() { return gradePoints.get(); }

    public SimpleIntegerProperty weightProperty() { return weight; }
    public int getWeight() { return weight.get(); }

    @Override
    public AssessmentData createChild() {
        AssessmentData data = new AssessmentData(this);
        children.add(data);
        return data;
    }

    @Override
    public void removeChildren(List<AssessmentData> children) {
        this.children.removeAll(children);
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
}
