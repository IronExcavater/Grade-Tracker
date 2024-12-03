package ironbyte.gradetracker.view.data;

import ironbyte.gradetracker.*;
import ironbyte.gradetracker.model.data.Data;
import ironbyte.gradetracker.view.StringTextField;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;

public abstract class DataView<T extends Data<?>> extends GridPane {

    protected final T data;
    private final int[] columnWidths;
    protected final StringTextField name;

    protected DataView(T data, int[] columnWidths, String namePrompt) {
        this.data = data;
        setHgap(10);
        this.columnWidths = columnWidths;
        name = new StringTextField(data.nameProperty(), true);
        name.setPromptText(namePrompt);
        name.setPrefWidth(0);
    }

    public T getData() { return data; }

    protected void setColumns(Region... regions) {
        for (int i = 0; i < regions.length; i++) {
            add(regions[i], i, 0);
            getColumnConstraints().add(Utils.columnPercentage(columnWidths[i]));
        }
    }

    public String toClipboardData() { return DataManager.gson.toJson(data); }

    public StringTextField getNameTf() { return name; }
}
