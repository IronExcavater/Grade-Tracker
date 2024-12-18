package ironbyte.gradetracker.model.data;

import com.google.gson.annotations.Expose;
import ironbyte.gradetracker.*;
import ironbyte.gradetracker.model.MoveObservableList;
import ironbyte.gradetracker.model.action.ChangeAction;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.*;
import java.util.function.Supplier;

public abstract class Data<C extends Data<?>> {

    private final Supplier<C> childSupplier;
    protected transient Data<?> parent;
    @Expose protected final MoveObservableList<C> children = new MoveObservableList<>();
    protected final ChangeListener<Number> changeListener = (_, _, _) -> update();

    @Expose protected final StringProperty name = new SimpleStringProperty();
    @Expose protected final DoubleProperty mark = new SimpleDoubleProperty();

    public Data(String name, Supplier<C> childSupplier) {
        this.childSupplier = childSupplier;
        nameProperty().set(name);
        children.addListener((ListChangeListener<? super C>) change -> {
            while (change.next()) {
                if (change.wasPermutated()) break;
                if (change.wasAdded()) change.getAddedSubList().forEach(this::addChild);
                if (change.wasRemoved()) change.getRemoved().forEach(this::removeChild);
            }
        });
    }

    public void startListening() {
        name.addListener((_, oldValue, newValue) -> {
            if (ActionManager.isActive()) return;
            ActionManager.executeAction(new ChangeAction<>(this, oldValue, newValue, "", nameProperty()::set));
        });
    }

    public void setParent(Data<?> parent) { this.parent = parent; }
    public Data<?> getParent() { return parent; }

    public boolean canParent() { return childSupplier != null; }
    public boolean hasParent() { return parent != null; }
    public boolean hasChildren() { return !children.isEmpty(); }

    public MoveObservableList<C> getChildren() {
        if (!canParent()) throw new IllegalArgumentException("This Data type doesn't support children");
        return children;
    }
    public C getChild(int index) {
        if (!canParent()) throw new IllegalArgumentException("This Data type doesn't support children");
        return children.get(index);
    }
    public C createChild() {
        if (!canParent()) throw new IllegalArgumentException("This Data type doesn't support children");
        C child = childSupplier.get();
        child.startListening();
        child.setParent(this);
        return child;
    }
    protected void addChild(C child) {
        if (!canParent()) throw new IllegalArgumentException("This Data type doesn't support children");
        child.setParent(this);
        update();
    }
    protected void removeChild(C child) {
        if (!canParent()) throw new IllegalArgumentException("This Data type doesn't support children");
        child.setParent(null);
        update();
    }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }

    public DoubleProperty markProperty() { return mark; }
    public double getMark() { return mark.get(); }

    protected abstract void update();
}
