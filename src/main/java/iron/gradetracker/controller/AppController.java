package iron.gradetracker.controller;

import iron.gradetracker.model.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AppController extends Controller<App> {

    @FXML private BorderPane bPaneRoot;
    @FXML private TabPane tPaneMenu;

    public AppController(Stage stage) {
        this.stage = stage;
    }

    @FXML
    protected void initialize() {
        tPaneMenu.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tPaneMenu.getSelectionModel().selectedItemProperty().addListener((_, _, newTab) -> loadContent(newTab));
    }

    private void loadContent(Tab tab) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/iron/gradetracker/view/%s-view.fxml".formatted(tab.getText().toLowerCase())));
            bPaneRoot.setCenter(fxmlLoader.load());
            Controller<App> controller = fxmlLoader.getController();
            controller.model = model;
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while loading content for the %s tab".formatted(tab.getText()));
        }
    }
}