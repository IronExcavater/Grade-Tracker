package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import iron.gradetracker.model.data.StudentData;

public class App {
    private static volatile App instance;
    @Expose private Settings settings;
    @Expose private StudentData studentData;

    private App(StudentData studentData, Settings settings) {
        this.studentData = studentData;
        this.settings = settings;
    }

    public static void createInstance() { createInstance(new StudentData(), new Settings()); }
    public static void createInstance(StudentData studentData, Settings settings) { createInstance(new App(studentData, settings)); }
    public static void createInstance(App app) {
        if (instance == null) {
            synchronized (App.class) {
                if (instance == null)
                    instance = app;
            }
        } else
            throw new IllegalStateException("App instance is already initialized.");
    }

    public static App getInstance() {
        if (instance == null)
            throw new IllegalStateException("App instance is not initialized.");
        return instance;
    }

    public static StudentData getStudentData() { return getInstance().studentData; }
    public void setStudentData(StudentData studentData) { this.studentData = studentData; }

    public static Settings getSettings() { return instance.settings; }
    public void setSettings(Settings settings) { this.settings = settings; }
}
