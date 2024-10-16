package iron.gradetracker.model;

public class App {
    public static volatile App instance;
    private final StudentData studentData;
    private final GradeScheme gradeScheme;

    private App(StudentData studentData, GradeScheme gradeScheme) {
        this.studentData = studentData;
        this.gradeScheme = gradeScheme;
    }

    public static void createInstance(StudentData studentData, GradeScheme gradeScheme) {
        if (instance == null) {
            synchronized (App.class) {
                if (instance == null)
                    instance = new App(studentData, gradeScheme);
            }
        } else
            throw new IllegalStateException("App instance is already initialized.");
    }

    public static App getInstance() {
        if (instance == null)
            throw new IllegalStateException("App instance is not initialized.");
        return instance;
    }

    public static StudentData getStudentData() {
        if (instance == null)
            throw new IllegalStateException("App instance is not initialized.");
        return instance.studentData;
    }

    public static GradeScheme getGradeMap() {
        if (instance == null)
            throw new IllegalStateException("App instance is not initialized.");
        return instance.gradeScheme;
    }
}
