package iron.gradetracker.controller;

import javafx.stage.Stage;

public class Controller<T> {
    protected Stage stage;
    protected T model;

    public Controller(Stage stage, T model) {
        this.stage = stage;
        this.model = model;
    }
}
