package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import iron.gradetracker.model.data.StudentData;

public class App {
    private static volatile App instance;
    @Expose private GradeScheme gradeScheme;
    @Expose private StudentData studentData;

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

    public static void createInstance() { createInstance(new StudentData(), new GradeScheme()); }

    public static void loadInstance(App app) {
        instance.studentData = app.studentData;
        instance.gradeScheme = app.gradeScheme;
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
