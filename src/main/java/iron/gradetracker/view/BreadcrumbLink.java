package iron.gradetracker.view;

import iron.gradetracker.controller.DataController;
import iron.gradetracker.model.data.Data;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;

public class BreadcrumbLink extends Hyperlink {
    private final Data<?> data;

    public BreadcrumbLink(DataController controller, Data<?> data) {
        this.data = data;

        textProperty().bind(data.nameProperty());
        if (controller.getFocusedData().equals(data))
            opacityProperty().bind(Bindings.createDoubleBinding(() -> controller.getFocusedData().equals(data) ? 0.3 : 1));
        setOnAction(_ -> controller.setFocusedData(data));

        var rename = new MenuItem("Rename");
        rename.setOnAction(_ -> controller.handleRename(data));
        setContextMenu(new ContextMenu(rename));
    }

    public Data<?> getData() { return data; }
}
