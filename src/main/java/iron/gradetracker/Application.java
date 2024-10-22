package iron.gradetracker;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import iron.gradetracker.controller.AppController;
import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        DataManager.loadData();

        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("view/app-view.fxml"), null, null,
                _ -> new AppController(stage));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        Utils.handleExit(stage);

        stage.setTitle("Grade Tracker");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}