package iron.gradetracker.model.action;

import iron.gradetracker.controller.DataController;

import java.util.List;

public class AddAction<T> implements Action {

    private final List<T> list;
    private final T item;
    private final int index;

    public AddAction(List<T> list, T item, int index) {
        this.list = list;
        this.item = item;
        this.index = index;
    }
    public AddAction(List<T> list, T item) {
        this(list, item, list.size());
    }

    @Override
    public void execute() {
        list.add(index, item);
    }

    @Override
    public void retract() {
        list.remove(index);
    }
}
