package iron.gradetracker.model.action;

import java.util.List;

public class RemoveAction<T> implements Action {

    private final List<T> list;
    private final T item;
    private final int index;

    public RemoveAction(List<T> list, T item) {
        this.list = list;
        this.item = item;
        this.index = list.indexOf(item);
    }

    @Override
    public void execute() {
        list.remove(index);
    }

    @Override
    public void retract() {
        list.add(index, item);
    }
}
