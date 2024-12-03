package ironbyte.gradetracker.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.converter.NumberStringConverter;
import java.util.regex.Pattern;

public class DoubleTextField extends TextField {

    private DoubleProperty boundProperty;
    private DoubleProperty maxProperty;
    private boolean isBidirectional;
    private Runnable runnable;
    private int maxLength = 4;

    public DoubleTextField(DoubleProperty boundProperty, boolean isBidirectional, DoubleProperty maxProperty) {
        this.boundProperty = boundProperty;
        this.maxProperty = maxProperty;
        this.isBidirectional = isBidirectional;
        initialize();
    }
    public DoubleTextField(DoubleProperty boundProperty, boolean isBidirectional) { this(boundProperty, isBidirectional, null); }
    public DoubleTextField(DoubleProperty boundProperty) { this(boundProperty, false, null); }
    public DoubleTextField() {}

    public DoubleProperty boundProperty() { return boundProperty; }
    public DoubleProperty maxProperty() { return maxProperty; }

    public void setBoundProperty(DoubleProperty boundProperty, boolean isBidirectional) {
        this.boundProperty = boundProperty;
        this.isBidirectional = isBidirectional;
        initialize();
    }
    public void setBoundProperty(DoubleProperty boundProperty) { setBoundProperty(boundProperty, false); }

    public void setProperties(DoubleProperty boundProperty, boolean isBidirectional, DoubleProperty maxProperty) {
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
        setTextFormatter(doubleTextFormatter());

        // Set binding
        if (boundProperty == null) return;
        NumberStringConverter converter = new NumberStringConverter();
        if (isBidirectional)
            Bindings.bindBidirectional(textProperty(), boundProperty(), converter);
        else
            textProperty().bind(boundProperty().asString());
    }

    private TextFormatter<Double> doubleTextFormatter() {
        Pattern validPattern = Pattern.compile("([1-9]\\d{0,%d}|0)?(\\.\\d{0,2})?".formatted(maxLength - 1));
        return new TextFormatter<>(change -> {
            String text = change.getControlNewText();

            // Add leading zero before decimal point
            if (text.startsWith(".")) {
                if (change.isDeleted()) change.setRange(0, 2);
                change.setText("0.");
                change.selectRange(2, 2);
                return change;
            }
            if (validPattern.matcher(text).matches()) {
                // If larger than maxValue, set to max value
                if (!text.isEmpty() && maxProperty != null) {
                    double maxValue = maxProperty.getValue();
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
