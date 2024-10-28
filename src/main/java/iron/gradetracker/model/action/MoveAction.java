package iron.gradetracker.model.action;

import java.util.List;

public class MoveAction<T> implements Action {

    private final List<T> list;
    private final T item;
    private final int initialIndex;
    private final int finalIndex;

    public MoveAction(List<T> list, int initialIndex, int finalIndex) {
        this.list = list;
        this.item = list.get(initialIndex);
        this.initialIndex = initialIndex;
        this.finalIndex = finalIndex;
    }

    @Override
    public void execute() {
        list.remove(initialIndex);
        list.add(finalIndex, item);
    }

    @Override
    public void retract() {
        list.remove(finalIndex);
        list.add(initialIndex, item);
    }
}
