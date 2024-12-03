package ironbyte.gradetracker.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.converter.NumberStringConverter;
import java.util.regex.Pattern;

public class IntegerTextField extends TextField {

    private IntegerProperty boundProperty;
    private IntegerProperty maxProperty;
    private boolean isBidirectional = false;
    private Runnable runnable;
    private int maxLength = 4;

    public IntegerTextField(IntegerProperty boundProperty, boolean isBidirectional, IntegerProperty maxProperty) {
        this.boundProperty = boundProperty;
        this.maxProperty = maxProperty;
        this.isBidirectional = isBidirectional;
        initialize();
    }
    public IntegerTextField(IntegerProperty boundProperty, boolean isBidirectional) { this(boundProperty, isBidirectional, null); }
    public IntegerTextField(IntegerProperty boundProperty) { this(boundProperty, false, null); }
    public IntegerTextField() {}

    public IntegerProperty boundProperty() { return boundProperty; }
    public IntegerProperty maxProperty() { return maxProperty; }

    public void setBoundProperty(IntegerProperty boundProperty, boolean isBidirectional) {
        this.boundProperty = boundProperty;
        this.isBidirectional = isBidirectional;
        initialize();
    }
    public void setBoundProperty(IntegerProperty boundProperty) { setBoundProperty(boundProperty, false); }

    public void setProperties(IntegerProperty boundProperty, boolean isBidirectional, IntegerProperty maxProperty) {
        this.boundProperty = boundProperty;
        this.isBidirectional = isBidirectional;
        this.maxProperty = maxProperty;
        initialize();
    }

    private void initialize() {
        setPrefWidth(0);

        // Set listeners and events
        focusedProperty().addListener((_, _, newValue) -> { if (!newValue && getText().isEmpty()) setText("0"); });
        setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                getParent().requestFocus();
                if (runnable != null) runnable.run();
            }
        });

        // Set formatter
        setTextFormatter(integerTextFormatter());

        // Set binding
        if (boundProperty == null) return;
        NumberStringConverter converter = new NumberStringConverter();
        if (isBidirectional)
            Bindings.bindBidirectional(textProperty(), boundProperty(), converter);
        else
            textProperty().bind(boundProperty().asString());
    }

    private TextFormatter<Integer> integerTextFormatter() {
        Pattern validPattern = Pattern.compile("([1-9]\\d{0,%d}|0)?".formatted(maxLength - 1));
        return new TextFormatter<>(change -> {
            String text = change.getControlNewText();

            if (validPattern.matcher(text).matches()) {
                // If larger than maxValue, set to max value
                if (!text.isEmpty() && maxProperty != null) {
                    int maxValue = maxProperty.getValue();
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
}
