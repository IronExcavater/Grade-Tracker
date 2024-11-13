package iron.gradetracker.model.action;

import iron.gradetracker.Utils;
import java.util.function.Consumer;

public class ChangeAction<T> implements Action {

    private final T oldValue;
    private final T newValue;
    private final Consumer<T> setter;

    public ChangeAction(T oldValue, T newValue, T defaultValue, Consumer<T> setter) {
        this.oldValue = Utils.defaultIfNull(oldValue, defaultValue);
        this.newValue = Utils.defaultIfNull(newValue, defaultValue);
        this.setter = setter;
    }

    @Override
    public void execute() { setter.accept(newValue); }

    @Override
    public void retract() { setter.accept(oldValue); }
}
