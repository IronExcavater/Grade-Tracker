package iron.gradetracker.model;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;

import java.util.*;

public class MoveObservableList<E> extends ModifiableObservableListBase<E> {
    private final List<E> delegate = new ArrayList<>();

    public E get(int index) {
        return delegate.get(index);
    }

    public int size() {
        return delegate.size();
    }

    public void move(int from, int to) {
        if (from == to || from < 0 || to < 0 || from >= size() || to >= size()) return;

        E element = delegate.remove(from);
        delegate.add(to, element);
        Platform.runLater(() -> {
            try {
                fireChange(new MoveChange<>(this, from, to, element));
            } catch (Exception e) {
                e.printStackTrace(); // Log the error to identify any issues
            }
        });
    }

    protected void doAdd(int index, E element) {
        delegate.add(index, element);
    }

    protected E doSet(int index, E element) {
        return delegate.set(index, element);
    }

    protected E doRemove(int index) {
        return delegate.remove(index);
    }

    private static class MoveChange<E> extends ListChangeListener.Change<E> {
        private final E element;
        private final int from;
        private final int to;

        public MoveChange(ObservableList<E> list, int from, int to, E element) {
            super(list);
            this.from = from;
            this.to = to;
            this.element = element;
        }

        @Override
        public boolean next() { return true; }

        @Override
        public void reset() {}

        @Override
        public int getFrom() { return from; }

        @Override
        public int getTo() { return to; }

        @Override
        public List<E> getRemoved() { return Collections.emptyList(); }

        @Override
        protected int[] getPermutation() {
            int[] permutation = new int[Math.abs(to - from) + 1];
            int min = Math.min(from, to);
            for (int i = 0; i < permutation.length; i++) {
                int index = min + i;
                if (index == from) permutation[i] = to;
                else permutation[i] = index + (from < to ? -1 : 1);
            }
            return permutation;
        }
    }
}
