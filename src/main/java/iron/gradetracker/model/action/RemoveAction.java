package iron.gradetracker.model.action;

import iron.gradetracker.model.data.Data;

public class RemoveAction<E extends Data<?>> implements Action {

    private final Data<E> parent;
    private final E element;
    private final int index;

    public RemoveAction(Data<E> parent, E element) {
        this.parent = parent;
        this.element = element;
        this.index = parent.getChildren().indexOf(element);
    }

    @Override
    public void execute() { parent.getChildren().remove(index); }

    @Override
    public void retract() { parent.getChildren().add(index, element); }

    @Override
    public Data<?> getFocus() { return parent; }
}
