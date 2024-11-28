package iron.gradetracker.view.data;

import iron.gradetracker.*;
import iron.gradetracker.model.data.Data;
import iron.gradetracker.view.StringTextField;
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
    }

    public T getData() { return data; }

    protected void setColumns(Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            add(nodes[i], i, 0);
            getColumnConstraints().add(Utils.columnPercentage(columnWidths[i]));
        }
    }

    public String toClipboardData() { return DataManager.gson.toJson(data); }

    public StringTextField getNameTf() { return name; }
}
