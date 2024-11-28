package iron.gradetracker.model.action;

import iron.gradetracker.model.data.Data;
import java.util.*;

public class RemoveAction<E extends Data<?>> implements Action {

    private final Data<E> parent;
    private final Map<Integer, E> elementsMap = new TreeMap<>();

    public RemoveAction(List<E> elements) {
        this.parent = (Data<E>) elements.getFirst().getParent();

        List<Integer> indexes = elements.stream().map(element -> parent.getChildren().indexOf(element)).toList();
        for (int i = 0; i < elements.size(); i++) {
            elementsMap.put(indexes.get(i), elements.get(i));
        }
    }

    @Override
    public void execute() { parent.getChildren().removeAll(elementsMap.values()); }

    @Override
    public void retract() {
        for (var entry : elementsMap.entrySet()) {
            parent.getChildren().add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Data<?> getFocus() { return parent; }
}
