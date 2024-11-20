package iron.gradetracker.model.action;

import iron.gradetracker.model.data.Data;
import java.util.List;

public class RemoveAction<E extends Data<?>> implements Action {

    private final Data<E> parent;
    private final List<E> elements;
    private final List<Integer> indexes;

    public RemoveAction(Data<E> parent, List<E> elements) {
        this.parent = parent;
        this.elements = elements;
        this.indexes = elements.stream().map(element -> parent.getChildren().indexOf(element)).toList();
    }

    @Override
    public void execute() { parent.getChildren().removeAll(elements); }

    @Override
    public void retract() {
        for (int i = 0; i < indexes.size(); i++) {
            parent.getChildren().add(indexes.get(i), elements.get(i));
        }
    }

    @Override
    public Data<?> getFocus() { return parent; }
}
