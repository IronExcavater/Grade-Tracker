package iron.gradetracker.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.converter.NumberStringConverter;
import java.util.regex.Pattern;

public class IntegerTextField extends TextField {

    private final SimpleIntegerProperty boundProperty;
    private final SimpleIntegerProperty maxProperty;
    private final boolean isBidirectional;

    public IntegerTextField(SimpleIntegerProperty boundProperty, boolean isBidirectional, SimpleIntegerProperty maxProperty) {
        this.boundProperty = boundProperty;
        this.maxProperty = maxProperty;
        this.isBidirectional = isBidirectional;
        initialize();
    }

    public IntegerTextField(SimpleIntegerProperty boundProperty, boolean isBidirectional) { this(boundProperty, isBidirectional, null); }

    public SimpleIntegerProperty boundProperty() { return boundProperty; }
    public SimpleIntegerProperty maxProperty() { return maxProperty; }

    private void initialize() {
        setPrefWidth(0);

        // Set listeners and events
        focusedProperty().addListener((_, _, newValue) -> { if (!newValue && getText().isEmpty()) setText("0"); });
        setOnKeyPressed(keyEvent -> { if (keyEvent.getCode() == KeyCode.ENTER) getParent().requestFocus(); });

        // Set formatter
        setTextFormatter(integerTextFormatter());

        // Set binding
        NumberStringConverter converter = new NumberStringConverter();
        if (isBidirectional)
            Bindings.bindBidirectional(textProperty(), boundProperty(), converter);
        else
            textProperty().bind(boundProperty().asString());
    }

    // TODO: Add cap on length of whole number
    private TextFormatter<Integer> integerTextFormatter() {
        Pattern validPattern = Pattern.compile("([1-9]\\d*|0)?");
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
