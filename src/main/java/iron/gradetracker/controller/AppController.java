package iron.gradetracker.controller;

import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class AppController extends Controller {

    @FXML private BorderPane bPaneRoot;
    @FXML private TabPane tPaneMenu;

    public AppController(Stage stage) { super(stage); }

    @FXML
    protected void initialize() {
        tPaneMenu.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tPaneMenu.getSelectionModel().selectedItemProperty().addListener((_, _, newTab) -> loadContent(newTab));
    }

    private void loadContent(Tab tab) {
        try {
            Class<?> tabClass = Class.forName("iron.gradetracker.controller.%sController".formatted(tab.getText()));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/iron/gradetracker/view/%s-view.fxml".formatted(tab.getText().toLowerCase())),
                    null, null, _ -> {
                try {
                    return tabClass.getDeclaredConstructor(Stage.class).newInstance(stage);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
            bPaneRoot.setCenter(fxmlLoader.load());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Exception occurred while loading content for the %s tab".formatted(tab.getText()), e);
        }
    }
}