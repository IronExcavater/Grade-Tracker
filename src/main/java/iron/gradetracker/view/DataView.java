package iron.gradetracker.view;

import iron.gradetracker.model.Data;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

public abstract class DataView<T extends Data<?, ?>> extends GridPane {

    protected final T data;
    private final int[] columnWidths;
    private final String[] columnNames;
    protected final TextField name = new TextField();

    protected DataView(T data, int[] columnWidths, String[] columnNames, String namePrompt) {
        this.data = data;
        this.columnWidths = columnWidths;
        this.columnNames = columnNames;
        setHgap(10);
        name.setPrefWidth(0);
        name.setPromptText(namePrompt);
        Bindings.bindBidirectional(name.textProperty(), data.nameProperty());
    }

    public T getData() { return data; }
    public int[] getColumnWidths() { return columnWidths; }
    public String[] getColumnNames() { return columnNames; }

    protected void setColumns(Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            add(nodes[i], i, 0);
            getColumnConstraints().add(columnPercentage(columnWidths[i]));
        }
    }

    protected ColumnConstraints columnPercentage(double percentWidth) {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentWidth);
        column.setHgrow(Priority.ALWAYS);
        return column;
    }
}
