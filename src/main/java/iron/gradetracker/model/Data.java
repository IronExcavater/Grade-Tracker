package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import javafx.beans.property.SimpleStringProperty;

import java.util.*;

public abstract class Data {
    protected Data parent = null;
    protected final List<Data> children = new LinkedList<>();

    @Expose protected final SimpleStringProperty name = new SimpleStringProperty();

    public Data(Data parent, List<Data> children) {
        this.parent = parent;
        this.children.addAll(children);
        parent.addChild(this);
    }

    public Data(Data parent) { this(parent, new LinkedList<>()); }
    public Data() {}

    public List<Data> getChildren() { return children; }
    public void addChild(Data child) { children.add(child); }
    public void addChildren(List<Data> children) { this.children.addAll(children); }
    public void removeChild(Data child) { children.remove(child); }
    public void removeChildren(List<Data> children) { this.children.removeAll(children); }
    protected void notifyChildren() { for (Data child : children) child.update(); }

    public Data getParent() { return parent; }
    public void setParent(Data parent) { this.parent = parent; }
    protected void notifyParent() { parent.update();}

    public SimpleStringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }

    protected abstract void update();
}
