package ironbyte.gradetracker.model;

import com.google.gson.annotations.Expose;
import ironbyte.gradetracker.model.data.StudentData;

public class App {
    private static volatile App instance;
    @Expose private Settings settings;
    @Expose private StudentData studentData;

    private App(StudentData studentData, Settings settings) {
        this.studentData = studentData;
        this.settings = settings;
    }

    public static void createInstance() {
        if (instance == null) {
            synchronized (App.class) {
                if (instance == null)
                    instance = new App(new StudentData(), new Settings());
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
    public static void setStudentData(StudentData studentData) { instance.studentData = studentData; }

    public static Settings getSettings() { return instance.settings; }
    public static void setSettings(Settings settings) { instance.settings = settings; }
}
