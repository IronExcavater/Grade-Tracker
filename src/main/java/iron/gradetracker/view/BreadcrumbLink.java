package iron.gradetracker.view;

import iron.gradetracker.controller.DataController;
import iron.gradetracker.model.data.Data;
import javafx.scene.control.Hyperlink;

public class BreadcrumbLink extends Hyperlink {
    private final Data<?> data;

    public BreadcrumbLink(DataController controller, Data<?> data, boolean disabled) {
        this.data = data;

        textProperty().bind(data.nameProperty());
        if (disabled) disableProperty().set(true);
        setOnAction(_ -> controller.setCurrentData(data));
    }

    public Data<?> getData() { return data; }
}
