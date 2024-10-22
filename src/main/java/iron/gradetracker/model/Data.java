package iron.gradetracker.model;

import com.google.gson.annotations.Expose;
import javafx.beans.property.*;
import javafx.collections.*;

import java.util.*;

public abstract class Data<C extends Data<?>> {

    protected transient Data<?> parent;
    @Expose protected final ObservableList<C> children = FXCollections.observableArrayList();

    @Expose protected final StringProperty name = new SimpleStringProperty();

    public Data(List<C> children) {
        this.children.addAll(children);
    }

    public Data() {}

    public void setParent(Data<?> parent) { this.parent = parent; }
    public Data<?> getParent() { return parent; }

    public List<C> getChildren() { return children; }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }

    public abstract C createChild();
    public abstract void addChild(C child);
    public abstract void removeChildren(List<C> children);

    protected abstract void update();
}
