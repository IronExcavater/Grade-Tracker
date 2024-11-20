package iron.gradetracker.model.action;

import iron.gradetracker.model.App;
import iron.gradetracker.model.data.StudentData;

public class ImportAction implements Action {

    private final StudentData oldElement;
    private final StudentData newElement;
    private boolean isNew;

    public ImportAction(StudentData oldElement, StudentData newElement) {
        this.oldElement = oldElement;
        this.newElement = newElement;
    }

    @Override
    public void execute() {
        App.setStudentData(newElement);
        isNew = true;
    }

    @Override
    public void retract() {
        App.setStudentData(oldElement);
        isNew = false;
    }

    @Override
    public StudentData getFocus() {
        return isNew ? oldElement : newElement;
    }
}
