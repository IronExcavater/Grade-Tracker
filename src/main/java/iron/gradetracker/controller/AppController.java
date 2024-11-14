package iron.gradetracker.controller;

import iron.gradetracker.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class AppController extends Controller {

    @FXML private BorderPane bPaneRoot;
    @FXML private TabPane tabMenu;

    public AppController(Stage stage) { super(stage); }

    @FXML
    protected void initialize() {
        tabMenu.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabMenu.getSelectionModel().selectedItemProperty().addListener((_, _, newTab) -> loadContent(newTab));
        loadContent(tabMenu.getSelectionModel().getSelectedItem());
    }

    private void loadContent(Tab tab) {
        try {
            Utils.clearKeyBinds(stage.getScene());
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

    @FXML
    private void handleImport() {
        var csvFilter = new FileChooser.ExtensionFilter("CSV Files", "*.csv");
        var jsonFilter = new FileChooser.ExtensionFilter("Json Files", "*.json");
        var xlsxFilter = new FileChooser.ExtensionFilter("Excel Files", "*.xlsx");
        var fileChooser = Utils.createFileChooser("Export File", csvFilter, jsonFilter, xlsxFilter);
        var file = fileChooser.showOpenDialog(stage);

        if (file == null) return;
        DataManager.importData(file);
    }

    @FXML
    private void handleExport() {
        var csvFilter = new FileChooser.ExtensionFilter("CSV Files", "*.csv");
        var jsonFilter = new FileChooser.ExtensionFilter("Json Files", "*.json");
        var xlsxFilter = new FileChooser.ExtensionFilter("Excel Files", "*.xlsx");
        var fileChooser = Utils.createFileChooser("Export File", csvFilter, jsonFilter, xlsxFilter);
        var file = fileChooser.showSaveDialog(stage);

        if (file == null) return;
        DataManager.exportData(file);
    }
}