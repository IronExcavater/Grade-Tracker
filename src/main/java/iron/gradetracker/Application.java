package iron.gradetracker;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.*;
import iron.gradetracker.controller.AppController;
import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        DataManager.loadData();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("view/app-view.fxml"), null, null,
                _ -> new AppController(stage));
        Scene scene = new Scene(fxmlLoader.load(), 700, 400);

        Image icon = Utils.getImage("images/icon.jpeg");
        stage.getIcons().add(icon);
        stage.setTitle("Grade Tracker");
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(400);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getScene().setFill(Color.TRANSPARENT);
        System.setProperty("prism.allowhidpi", "false");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}