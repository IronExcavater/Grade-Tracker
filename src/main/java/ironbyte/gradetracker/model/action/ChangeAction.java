package ironbyte.gradetracker.model.action;

import ironbyte.gradetracker.Utils;
import ironbyte.gradetracker.model.data.Data;
import java.util.function.Consumer;

public class ChangeAction<E> implements Action {

    private final Data<?> element;
    private final E oldValue;
    private final E newValue;
    private final Consumer<E> setter;

    public ChangeAction(Data<?> element, E oldValue, E newValue, E defaultValue, Consumer<E> setter) {
        this.element = element;
        this.oldValue = Utils.defaultIfNull(oldValue, defaultValue);
        this.newValue = Utils.defaultIfNull(newValue, defaultValue);
        this.setter = setter;
    }

    @Override
    public void execute() { setter.accept(newValue); }

    @Override
    public void retract() { setter.accept(oldValue); }

    @Override
    public Data<?> getFocus() {
        if (element.hasParent()) return element.getParent();
        return element;
    }
}
