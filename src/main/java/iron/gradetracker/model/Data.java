package iron.gradetracker.model;

import java.util.*;

public abstract class Data<P, C> {
    protected P parent;
    protected List<C> children;

    Data(P parent, List<C> children) {
        this.parent = parent;
        this.children = children;
    }

    Data(P parent) { this(parent, null); }

    Data(List<C> children) { this(null, children); }

    public P getParent() { return parent; }

    public List<C> getChildren() { return children; }

    public boolean hasChildren() { return !children.isEmpty(); }

    public void addChild(C child) { children.add(child); }

    public void removeChild(C child) { children.remove(child); }
}
