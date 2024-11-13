package iron.gradetracker;

import iron.gradetracker.model.action.Action;
import javafx.beans.property.*;
import java.util.Stack;

public class ActionManager {
    private static final Stack<Action> undoStack = new Stack<>();
    private static final Stack<Action> redoStack = new Stack<>();

    public static Action savedAction = null;

    private static boolean isActive = false;
    private static final BooleanProperty canUndo = new SimpleBooleanProperty(false);
    private static final BooleanProperty canRedo = new SimpleBooleanProperty(false);

    public static boolean isActive() { return isActive; }
    public static BooleanProperty canUndoProperty() { return canUndo; }
    public static BooleanProperty canRedoProperty() { return canRedo; }

    public static void executeAction(Action action) {
        isActive = true;
        action.execute();
        undoStack.push(action);
        redoStack.clear();

        DataManager.markDirty();
        canUndo.set(true);
        canRedo.set(false);
        isActive = false;
    }

    public static void undoAction() {
        if (undoStack.isEmpty()) return;
        isActive = true;
        Action action = undoStack.pop();
        action.retract();
        redoStack.push(action);

        markAction();
        if (undoStack.isEmpty()) canUndo.set(false);
        canRedo.set(true);
        isActive = false;
    }

    public static void redoAction() {
        if (redoStack.isEmpty()) return;
        isActive = true;
        Action action = redoStack.pop();
        action.execute();
        undoStack.push(action);

        markAction();
        if (redoStack.isEmpty()) canRedo.set(false);
        canUndo.set(true);
        isActive = false;
    }

    public static void saveAction() { savedAction = undoStack.empty() ? null : undoStack.peek(); }

    private static void markAction() {
        if ((savedAction != null && !undoStack.empty() && savedAction.equals(undoStack.peek()))
                || (savedAction == null && undoStack.empty())) DataManager.markClean();
        else DataManager.markDirty();
    }
}
