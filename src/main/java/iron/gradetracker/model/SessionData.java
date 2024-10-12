package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import javafx.beans.property.*;

public class SessionData extends Data {

    @Expose private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleDoubleProperty mark = new SimpleDoubleProperty();
    private final SimpleDoubleProperty gradePoints = new SimpleDoubleProperty();
    private final SimpleIntegerProperty creditPoints = new SimpleIntegerProperty();

    public SessionData(StudentData parent) {
        super(parent);

        nameProperty().addListener(_ -> notifyParent());
        markProperty().addListener(_ -> notifyParent());
        gradePointsProperty().addListener(_ -> notifyParent());
        creditPointsProperty().addListener(_ -> notifyParent());
    }

    @Override
    protected void update() {

    }

    public SimpleStringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }

    public SimpleDoubleProperty markProperty() { return mark; }
    public double getMark() { return mark.get(); }

    public SimpleDoubleProperty gradePointsProperty() { return gradePoints; }
    public double getGradePoints() { return gradePoints.get(); }

    public SimpleIntegerProperty creditPointsProperty() { return creditPoints; }
    public int getCreditPoints() { return creditPoints.get(); }
}
