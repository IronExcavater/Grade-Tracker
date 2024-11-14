package iron.gradetracker.model.action;

import iron.gradetracker.model.data.Data;

public class MoveAction<T extends Data<?>> implements Action {

    private final Data<T> parent;
    private final T item;
    private final int initialIndex;
    private final int finalIndex;

    public MoveAction(Data<T> parent, int initialIndex, int finalIndex) {
        this.parent = parent;
        this.item = parent.getChildren().get(initialIndex);
        this.initialIndex = initialIndex;
        this.finalIndex = finalIndex;
    }

    @Override
    public void execute() {
        parent.getChildren().remove(initialIndex);
        parent.getChildren().add(finalIndex, item);
    }

    @Override
    public void retract() {
        parent.getChildren().remove(finalIndex);
        parent.getChildren().add(initialIndex, item);
    }

    @Override
    public Data<?> getItem() { return item; }
}
