package ironbyte.gradetracker.model.action;

import ironbyte.gradetracker.model.data.Data;

public interface Action {
    void execute();
    void retract();
    Data<?> getFocus();
}
