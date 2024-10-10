package iron.gradetracker.model;

import java.util.*;

public class SubjectData extends Data<SessionData, AssessmentData> {

    public SubjectData(SessionData parent) { super(parent, new LinkedList<>()); }
}
