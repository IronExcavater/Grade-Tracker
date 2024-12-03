package ironbyte.gradetracker.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

public class StringTextField extends TextField {

    private StringProperty boundProperty;
    private boolean isBidirectional = false;
    private Runnable runnable;
    private int maxLength = 40;

    public StringTextField(StringProperty boundProperty, boolean isBidirectional) {
        this.boundProperty = boundProperty;
        this.isBidirectional = isBidirectional;
        initialize();
    }
    public StringTextField(StringProperty boundProperty) { this(boundProperty, false); }
    public StringTextField() {}

    public StringProperty boundProperty() { return boundProperty; }
    public void setBoundProperty(StringProperty boundProperty, boolean isBidirectional) {
        this.boundProperty = boundProperty;
        this.isBidirectional = isBidirectional;
        initialize();
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        initialize();
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
        initialize();
    }

    private void initialize() {
        //setPrefWidth(0);

        // Set listeners and events
        setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                getParent().requestFocus();
                if (runnable != null) runnable.run();
            }
        });

        // Set formatter
        setTextFormatter(stringTextFormatter());

        // Set binding
        if (boundProperty == null) return;
        if (isBidirectional)
            Bindings.bindBidirectional(textProperty(), boundProperty());
        else
            textProperty().bind(boundProperty());
    }

    private TextFormatter<String> stringTextFormatter() {
        return new TextFormatter<>(change -> {
            String text = change.getControlNewText();

            if (text.length() <= maxLength)
                return change;
            return null;
        });
    }
}
