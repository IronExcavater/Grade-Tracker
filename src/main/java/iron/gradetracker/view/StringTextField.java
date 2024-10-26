package iron.gradetracker.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

public class StringTextField extends TextField {

    private final StringProperty boundProperty;
    private final boolean isBidirectional;
    private final int maxLength = 40;

    public StringTextField(StringProperty boundProperty, boolean isBidirectional) {
        this.boundProperty = boundProperty;
        this.isBidirectional = isBidirectional;
        initialize();
    }

    public StringTextField(StringProperty boundProperty, boolean isBidirectional, String promptText) {
        this(boundProperty, isBidirectional);
        setPromptText(promptText);
    }

    public StringProperty boundProperty() { return boundProperty; }

    private void initialize() {
        setPrefWidth(0);

        // Set listeners and events
        setOnKeyPressed(keyEvent -> { if (keyEvent.getCode() == KeyCode.ENTER) getParent().requestFocus(); });

        // Set formatter
        setTextFormatter(stringTextFormatter());

        // Set binding
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
