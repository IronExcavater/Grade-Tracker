package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import javafx.beans.property.*;

public class SubjectData extends Data {

    @Expose private final SimpleStringProperty name = new SimpleStringProperty();
    @Expose private final SimpleIntegerProperty creditPoints = new SimpleIntegerProperty();
    private final SimpleDoubleProperty mark = new SimpleDoubleProperty();
    private final SimpleStringProperty grade = new SimpleStringProperty();
    private final SimpleDoubleProperty gradePoints = new SimpleDoubleProperty();
    private final SimpleIntegerProperty remainingWeight = new SimpleIntegerProperty();

    public SubjectData(SessionData parent, int creditPoints) {
        super(parent);
        creditPointsProperty().set(creditPoints);

        creditPointsProperty().addListener(_ -> notifyParent());
        markProperty().addListener(_ -> notifyParent());
        gradeProperty().addListener(_ -> notifyParent());
        gradePointsProperty().addListener(_ -> notifyParent());
    }

    @Override
    protected void update() {
        remainingWeightProperty().set(100 - children.stream()
                .map(AssessmentData.class::cast)
                .mapToInt(AssessmentData::getWeight)
                .sum());
    }

    public SimpleStringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }

    public SimpleIntegerProperty creditPointsProperty() { return creditPoints; }
    public float getCreditPoints() { return creditPoints.get(); }

    public SimpleDoubleProperty markProperty() { return mark; }
    public double getMark() { return mark.get(); }

    public SimpleStringProperty gradeProperty() { return grade; }
    public String getGrade() { return grade.get(); }

    public SimpleDoubleProperty gradePointsProperty() { return gradePoints; }
    public double getGradePoints() { return gradePoints.get(); }

    public SimpleIntegerProperty remainingWeightProperty() { return remainingWeight; }
    public int getRemainingWeight() { return remainingWeight.get(); }
}
