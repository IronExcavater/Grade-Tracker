package iron.gradetracker.view.data;

import com.google.gson.JsonObject;
import iron.gradetracker.DataManager;
import iron.gradetracker.Utils;
import iron.gradetracker.model.data.Data;
import iron.gradetracker.view.StringTextField;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;

public abstract class DataView<T extends Data<?>> extends GridPane {

    protected final T data;
    private final int[] columnWidths;
    private final String[] columnNames;
    protected final TextField name;

    protected DataView(T data, int[] columnWidths, String[] columnNames, String namePrompt) {
        this.data = data;
        this.columnWidths = columnWidths;
        this.columnNames = columnNames;
        setHgap(10);
        name = new StringTextField(data.nameProperty(), true);
        name.setPromptText(namePrompt);
    }

    public T getData() { return data; }
    public int[] getColumnWidths() { return columnWidths; }
    public String[] getColumnNames() { return columnNames; }

    protected void setColumns(Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            add(nodes[i], i, 0);
            getColumnConstraints().add(Utils.columnPercentage(columnWidths[i]));
        }
    }

    public String toClipboardData() { return DataManager.gson.toJson(data); }

    public Data<?> fromClipboardData(String json) { return DataManager.gson.fromJson(json, Data.class); }
}
