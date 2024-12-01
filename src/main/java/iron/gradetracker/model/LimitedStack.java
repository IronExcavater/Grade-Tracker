package iron.gradetracker.model;

import java.util.Stack;

public class LimitedStack<E> extends Stack<E> {
    private final int maxSize;

    public LimitedStack(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public E push(E e) {
        if (size() == maxSize) { remove(0); }
        return super.push(e);
    }
}
