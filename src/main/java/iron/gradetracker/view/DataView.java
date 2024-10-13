package iron.gradetracker.view;

import iron.gradetracker.model.Data;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

public abstract class DataView extends GridPane {

    private final Data data;
    protected final TextField name = new TextField();

    protected DataView(Data data, String namePrompt) {
        this.data = data;
        setHgap(10);
        name.setPrefWidth(0);
        name.setPromptText(namePrompt);
        Bindings.bindBidirectional(name.textProperty(), data.nameProperty());
    }

    public Data getData() { return data; }

    protected void setColumns(double[] percentageWidths, Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            add(nodes[i], i, 0);
            getColumnConstraints().add(columnPercentage(percentageWidths[i]));
        }
    }

    protected ColumnConstraints columnPercentage(double percentWidth) {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentWidth);
        column.setHgrow(Priority.ALWAYS);
        return column;
    }
}
