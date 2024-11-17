package iron.gradetracker.model.action;

import iron.gradetracker.model.data.Data;

public class AddAction<E extends Data<?>> implements Action {

    private final Data<E> parent;
    private final E element;
    private final int index;

    public AddAction(Data<E> parent, E element) {
        this(parent, element, parent.getChildren().size());
    }
    public AddAction(Data<E> parent, E element, int index) {
        this.parent = parent;
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
