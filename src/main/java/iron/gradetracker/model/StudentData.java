package iron.gradetracker.model;

import javafx.beans.property.*;

public class StudentData extends Data {

    private final SimpleFloatProperty cwam = new SimpleFloatProperty();
    private final SimpleFloatProperty cgpa = new SimpleFloatProperty();

    public StudentData() {
        name.set("root");
    }

    @Override
    protected void update() {

    }

    public SimpleFloatProperty cwamProperty() { return cwam; }
    public float getCwam() { return cwam.get(); }

    public SimpleFloatProperty cgpaProperty() { return cgpa; }
    public float getCgpa() { return cgpa.get(); }
}
