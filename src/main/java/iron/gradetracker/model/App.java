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

    public static App getInstance() {
        if (instance == null)
            throw new IllegalStateException("App instance is not initialized.");
        return instance;
    }

    public static void setInstance(App app) {
        instance.studentData = app.studentData;
        instance.gradeScheme = app.gradeScheme;
    }

    public static StudentData getStudentData() {
        if (instance == null)
            throw new IllegalStateException("App instance is not initialized.");
        return instance.studentData;
    }

    public static void setStudentData(StudentData studentData) {
        if (instance == null)
            throw new IllegalStateException("App instance is not initialized.");
        instance.studentData = studentData;
    }

    public static GradeScheme getGradeScheme() {
        if (instance == null)
            throw new IllegalStateException("App instance is not initialized.");
        return instance.gradeScheme;
    }

    public static void setGradeScheme(GradeScheme gradeScheme) {
        if (instance == null)
            throw new IllegalStateException("App instance is not initialized.");
        instance.gradeScheme = gradeScheme;
    }
}
