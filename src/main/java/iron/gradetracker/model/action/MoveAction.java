package iron.gradetracker.model.action;

import iron.gradetracker.model.data.Data;

public class MoveAction<E extends Data<?>> implements Action {

    private final Data<E> parent;
    private final E element;
    private final int from;
    private final int to;

    public MoveAction(Data<E> parent, int from, int to) {
        this.parent = parent;
        this.element = parent.getChildren().get(from);
        this.from = from;
        this.to = to;
    }

    @Override
    public void execute() {
        parent.getChildren().move(from, to);
    }

    @Override
    public void retract() {
        parent.getChildren().move(to, from);
    }

    @Override
    public Data<?> getFocus() { return parent; }
}
