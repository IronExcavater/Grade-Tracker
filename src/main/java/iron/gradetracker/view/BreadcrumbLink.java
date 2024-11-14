package iron.gradetracker.view;

import iron.gradetracker.Utils;
import iron.gradetracker.controller.DataController;
import iron.gradetracker.model.data.Data;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;

public class BreadcrumbLink extends Hyperlink {
    private final Data<?> data;

    public BreadcrumbLink(DataController controller, Data<?> data) {
        this.data = data;

        textProperty().bind(data.nameProperty());
        if (controller.getCurrentData().equals(data))
            opacityProperty().bind(Bindings.createDoubleBinding(() -> controller.getCurrentData().equals(data) ? 0.3 : 1));
        setOnAction(_ -> controller.setCurrentData(data));

        var rename = new MenuItem("Rename");
        rename.setOnAction(_ -> handleRename());
        setContextMenu(new ContextMenu(rename));
    }

    public Data<?> getData() { return data; }

    public void handleRename() {
        StringTextField renameField = new StringTextField();
        renameField.setPromptText("Rename");
        renameField.setRunnable(() -> data.nameProperty().set(renameField.getText()));
        var renameResult = Utils.createDialog("Rename", renameField, ButtonType.OK, ButtonType.CANCEL);
        renameResult.ifPresent(result -> {
            if (result.equals(ButtonType.OK))
                data.nameProperty().set(renameField.getText());
        });
    }
}
