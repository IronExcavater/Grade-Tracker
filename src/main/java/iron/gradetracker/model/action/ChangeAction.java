package iron.gradetracker.model.action;

import java.util.function.Consumer;

public class ChangeAction<T> implements Action {

    private final T oldValue;
    private final T newValue;
    private final Consumer<T> setter;

    public ChangeAction(T oldValue, T newValue, Consumer<T> setter) {
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.setter = setter;
    }

    @Override
    public void execute() { setter.accept(newValue); }

    @Override
    public void retract() { setter.accept(oldValue); }
}
