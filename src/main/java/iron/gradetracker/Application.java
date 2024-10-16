package iron.gradetracker;

import iron.gradetracker.model.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import iron.gradetracker.controller.AppController;
import java.io.IOException;
import java.util.*;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        loadApp();

        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("view/app-view.fxml"), null, null,
                _ -> new AppController(stage));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Grade Tracker");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void loadApp() {
        App.createInstance(new StudentData(), new GradeScheme(new double[]{85, 75, 65, 50, 0},
                new String[]{"HD", "D", "C", "P", "F"}, new double[]{7, 6, 5, 4, 0}));
    }
}