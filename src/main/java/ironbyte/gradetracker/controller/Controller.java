package ironbyte.gradetracker.controller;

import javafx.stage.Stage;

public class Controller {
    protected Stage stage;

    public Controller(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() { return stage; }
}
