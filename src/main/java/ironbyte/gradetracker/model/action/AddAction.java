package ironbyte.gradetracker.model.action;

import ironbyte.gradetracker.model.data.Data;

public class AddAction<E extends Data<?>> implements Action {

    private final Data<E> parent;
    private final E element;
    private final int index;

    @SuppressWarnings("unchecked")
    public AddAction(E element, int index) {
        this.parent = (Data<E>) element.getParent();
        this.element = element;
        this.index = index;
    }

    @Override
    public void execute() { parent.getChildren().add(index, element); }

    @Override
    public void retract() { parent.getChildren().remove(index); }

    @Override
    public Data<?> getFocus() { return parent; }
}
