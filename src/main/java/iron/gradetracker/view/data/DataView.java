package iron.gradetracker.view.data;

import iron.gradetracker.Utils;
import iron.gradetracker.model.data.Data;
import iron.gradetracker.view.StringTextField;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

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
        name = new StringTextField(data.nameProperty(), true, namePrompt);
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
}
