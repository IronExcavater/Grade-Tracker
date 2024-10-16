package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;

import java.util.*;

public abstract class Data<P extends Data<?, ?>, C extends Data<?, ?>> {
    protected P parent;
    protected final ObservableList<C> children = FXCollections.observableArrayList();

    @Expose protected final SimpleStringProperty name = new SimpleStringProperty();

    public Data(P parent, List<C> children) {
        this.parent = parent;
        this.children.addAll(children);
    }

    public Data(P parent) { this(parent, new LinkedList<>()); }
    public Data() {}

    public List<C> getChildren() { return children; }
    public void addChild(C child) { children.add(child); }
    public void addChildren(List<C> children) { this.children.addAll(children); }
    public void removeChild(C child) { children.remove(child); }
    public void removeChildren(List<C> children) { this.children.removeAll(children); }

    public P getParent() { return parent; }
    public void setParent(P parent) { this.parent = parent; }

    public SimpleStringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
}
