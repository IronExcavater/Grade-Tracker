package iron.gradetracker.model.data;

import com.google.gson.annotations.Expose;
import iron.gradetracker.*;
import iron.gradetracker.model.action.ChangeAction;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.*;
import java.util.*;
import java.util.function.Supplier;

public abstract class Data<C extends Data<?>> {

    private final Supplier<C> childSupplier;
    protected transient Data<?> parent;
    @Expose protected final ObservableList<C> children = FXCollections.observableArrayList();
    protected final ChangeListener<Number> changeListener = (_, _, _) -> update();

    @Expose protected final StringProperty name = new SimpleStringProperty();
    @Expose protected final DoubleProperty mark = new SimpleDoubleProperty();

    public Data(List<C> children, Supplier<C> childSupplier) {
        this(childSupplier);
        this.children.addAll(children);
    }

    public Data(Supplier<C> childSupplier) {
        this.childSupplier = childSupplier;
        children.addListener((ListChangeListener<? super C>) change -> {
            while (change.next()) {
                if (change.wasAdded()) change.getAddedSubList().forEach(this::addChild);
                if (change.wasRemoved()) change.getRemoved().forEach(this::removeChild);
            }
        });
    }

    public void startListening() {
        children.addListener((ListChangeListener<? super C>) _ -> {
            if (ActionManager.isActive()) return;
            DataManager.markDirty();
        });
        name.addListener((_, oldValue, newValue) -> {
            if (ActionManager.isActive()) return;
            ActionManager.executeAction(
                    new ChangeAction<>(Utils.defaultIfNull(oldValue, ""), Utils.defaultIfNull(newValue, ""),
                    nameProperty()::set));
            DataManager.markDirty();
        });
    }

    public void setParent(Data<?> parent) { this.parent = parent; }
    public Data<?> getParent() { return parent; }

    public boolean canParent() { return childSupplier != null; }

    public List<C> getChildren() {
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
        return child;
    }
    protected void addChild(C child) {
        if (!canParent()) throw new IllegalArgumentException("This Data type doesn't support children");
        child.setParent(this);
    }
    protected void removeChild(C child) {
        if (!canParent()) throw new IllegalArgumentException("This Data type doesn't support children");
        child.setParent(null);
    }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }

    public DoubleProperty markProperty() { return mark; }
    public double getMark() { return mark.get(); }

    protected abstract void update();
}
