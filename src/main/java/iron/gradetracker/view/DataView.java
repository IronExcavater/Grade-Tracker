package iron.gradetracker.view;

import iron.gradetracker.model.Data;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.*;

import java.util.regex.Pattern;

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

    protected static TextFormatter<Integer> integerFormatter(SimpleIntegerProperty maxValueProperty) {
        Pattern validIntegerPattern = Pattern.compile("([1-9]\\d*|0)?");
        return new TextFormatter<>(change -> {
            String text = change.getControlNewText();

            if (validIntegerPattern.matcher(text).matches()) {
                // If larger than maxValue, set to max value
                if (!text.isEmpty() && maxValueProperty != null) {
                    int maxValue = maxValueProperty.getValue();
                    int currentValue = Integer.parseInt(text);
                    if (currentValue > maxValue) {
                        change.setText(String.valueOf(maxValue));
                        change.setRange(0, change.getControlText().length());
                        change.selectRange(change.getControlNewText().length(), change.getControlNewText().length());
                    }
                }
                return change;
            }
            return null;
        });
    }

    protected static TextFormatter<Double> decimalFormatter(SimpleDoubleProperty maxValueProperty) {
        Pattern validDecimalPattern = Pattern.compile("([1-9]\\d*|0)?(\\.\\d{0,2})?");
        return new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            System.out.println(text);
            // Add leading zero before decimal point
            if (text.startsWith(".")) {
                if (change.isDeleted()) change.setRange(0, 2);
                change.setText("0.");
                change.selectRange(2, 2);
                return change;
            }
            if (validDecimalPattern.matcher(text).matches()) {
                // If larger than maxValue, set to max value
                if (!text.isEmpty() && maxValueProperty != null) {
                    double maxValue = maxValueProperty.getValue();
                    double currentValue = Double.parseDouble(text);
                    if (currentValue > maxValue) {
                        text = (maxValue == (int) maxValue ? String.valueOf((int) maxValue) : String.valueOf(maxValue));
                        change.setText(text);
                        change.setRange(0, change.getControlText().length());
                        change.selectRange(change.getControlNewText().length(), change.getControlNewText().length());
                    }
                }
                return change;
            }
            return null;
        });
    }
}
