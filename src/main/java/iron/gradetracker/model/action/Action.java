package iron.gradetracker.model.action;

import iron.gradetracker.model.data.Data;

public interface Action {
    void execute();
    void retract();
    Data<?> getFocus();
}
