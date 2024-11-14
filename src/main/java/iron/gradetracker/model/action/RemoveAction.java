package iron.gradetracker.model.action;

import iron.gradetracker.model.data.Data;

public class RemoveAction<T extends Data<?>> implements Action {

    private final Data<T> parent;
    private final T item;
    private final int index;

    public RemoveAction(Data<T> parent, T item) {
        this.parent = parent;
        this.item = item;
        this.index = parent.getChildren().indexOf(item);
    }

    @Override
    public void execute() { parent.getChildren().remove(index); }

    @Override
    public void retract() { parent.getChildren().add(index, item); }

    @Override
    public Data<?> getFocus() { return parent; }
}
