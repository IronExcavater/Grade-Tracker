package iron.gradetracker;

import iron.gradetracker.controller.DataController;
import iron.gradetracker.model.action.*;
import javafx.beans.property.*;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class ActionManager {
    private static final Stack<Action> undoStack = new Stack<>();
    private static final Stack<Action> redoStack = new Stack<>();

    public static DataController controller;
    private static Action savedAction;

    private static boolean isActive = false;
    private static final BooleanProperty canUndo = new SimpleBooleanProperty(false);
    private static final BooleanProperty canRedo = new SimpleBooleanProperty(false);

    public static boolean isActive() { return isActive; }
    public static BooleanProperty canUndoProperty() { return canUndo; }
    public static BooleanProperty canRedoProperty() { return canRedo; }

    public static void executeAction(Action action) {
        isActive = true;
        focusAction(action);
        action.execute();
        undoStack.push(action);
        redoStack.clear();

        DataManager.markDirty();
        canUndo.set(true);
        canRedo.set(false);
        isActive = false;
    }

    public static void undoAction() {
        if (undoStack.isEmpty() || isActive) return;
        processAction(undoStack.pop(), true);
    }

    public static void redoAction() {
        if (redoStack.isEmpty() || isActive) return;
        processAction(redoStack.pop(), false);
    }

    public static void processAction(Action action, boolean isUndo) {
        Runnable runnable = () -> {
            isActive = true;
            if (isUndo) {
                action.retract();
                redoStack.push(action);
                if (undoStack.isEmpty()) canUndo.set(false);
                canRedo.set(true);
            }
            else {
                action.execute();
                undoStack.push(action);
                if (redoStack.isEmpty()) canRedo.set(false);
                canUndo.set(true);
            }
            markAction();
            isActive = false;
        };

        if (focusAction(action)) Utils.Coroutine.runAsync(100, TimeUnit.MILLISECONDS, runnable);
        else runnable.run();
    }

    public static void saveAction() { savedAction = undoStack.empty() ? null : undoStack.peek(); }

    private static void markAction() {
        if ((savedAction != null && !undoStack.empty() && savedAction.equals(undoStack.peek()))
                || (savedAction == null && undoStack.empty())) DataManager.markClean();
        else DataManager.markDirty();
    }

    private static boolean focusAction(Action action) {
        boolean focused = false;
        if (!action.getFocus().equals(controller.getFocusedData())) {
            controller.setFocusedData(action.getFocus());
            focused = true;
        }
        if (action instanceof MoveAction<?> && !controller.getSortOption().equals("Custom")) {
            controller.setSortOption("Custom");
            focused = true;
        }
        return focused;
    }
}
