package iron.gradetracker.view;

import iron.gradetracker.controller.DataController;
import iron.gradetracker.model.Data;
import javafx.scene.control.Hyperlink;

public class BreadcrumbView extends Hyperlink {
    private final Data data;

    public BreadcrumbView(DataController controller, Data data) {
        this.data = data;

        textProperty().bind(data.nameProperty());
        setOnAction(_ -> controller.setCurrentData(data));
    }

    public Data getData() { return data; }
}
