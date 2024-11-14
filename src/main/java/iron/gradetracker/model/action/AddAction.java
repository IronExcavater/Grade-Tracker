package iron.gradetracker.model.action;

import iron.gradetracker.model.data.Data;

public class AddAction<T extends Data<?>> implements Action {

    private final Data<T> parent;
    private final T item;
    private final int index;

    public AddAction(Data<T> parent, T item) {
        this(parent, item, parent.getChildren().size());
    }
    public AddAction(Data<T> parent, T item, int index) {
        this.parent = parent;
        this.item = item;
        this.index = index;
    }

    @Override
    public void execute() {
        parent.getChildren().add(index, item);
        item.setParent(parent);
    }

    @Override
    public void retract() {
        parent.getChildren().remove(index);
        item.setParent(null);
    }

    @Override
    public Data<?> getItem() { return item; }
}
