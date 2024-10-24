package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import iron.gradetracker.DataManager;
import javafx.beans.property.*;
import javafx.collections.*;

import java.util.*;

public abstract class Data<C extends Data<?>> {

    protected transient Data<?> parent;
    @Expose protected final ObservableList<C> children = FXCollections.observableArrayList();

    @Expose protected final StringProperty name = new SimpleStringProperty();
    @Expose protected final DoubleProperty mark = new SimpleDoubleProperty();

    public Data(List<C> children) { this.children.addAll(children); }

    public Data() {}

    public void startListening() { children.addListener((ListChangeListener<? super C>) _ -> DataManager.markDirty()); }

    public void setParent(Data<?> parent) { this.parent = parent; }
    public Data<?> getParent() { return parent; }

    public List<C> getChildren() { return children; }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }

    public DoubleProperty markProperty() { return mark; }
    public double getMark() { return mark.get(); }

    public abstract C createChild();
    public abstract void addChild(C child);
    public abstract void removeChildren(List<C> children);

    public void swapChildren(int i, int j) {
        C temp = children.get(i);
        children.set(i, children.get(j));
        children.set(j, temp);
    }

    protected abstract void update();
}
