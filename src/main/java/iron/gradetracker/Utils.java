package iron.gradetracker;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.WritableValue;
import javafx.event.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class Utils {
    private static final Map<KeyCombination, EventHandler<KeyEvent>> keyBinds = new HashMap<>();

    public static Alert createAlert(Alert.AlertType alertType, String title, String header, String content, ButtonType... buttonTypes) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header + "\n" + content);
        alert.getButtonTypes().setAll(buttonTypes);
        alert.initStyle(StageStyle.TRANSPARENT);
        DialogPane dialogPane = alert.getDialogPane();
        initializeDialog(dialogPane);
        return alert;
    }

    public static FileChooser createFileChooser(String title, FileChooser.ExtensionFilter... extensionFilters) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(extensionFilters);

        File initial = new File(System.getProperty("user.home"), "Downloads");
        if (!initial.exists() || !initial.isDirectory()) initial = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(initial);

        return fileChooser;
    }

    public static Dialog<?> createDialog(String title, Node content, ButtonType... buttonTypes) {
        Dialog<?> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initStyle(StageStyle.TRANSPARENT);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().addAll(buttonTypes);
        initializeDialog(dialogPane);
        return dialog;
    }

    private static void initializeDialog(DialogPane dialogPane) {
        dialogPane.getStylesheets().add(Objects.requireNonNull(Utils.class.getResource("dialog.css")).toExternalForm());
        dialogPane.getScene().setFill(Color.TRANSPARENT);
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.getScene().setCursor(Cursor.DEFAULT);
        ResizeListener resizeListener = new ResizeListener(stage, 0, Integer.MAX_VALUE);
        dialogPane.addEventFilter(MouseEvent.MOUSE_PRESSED, resizeListener);
        dialogPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, resizeListener);
        dialogPane.addEventFilter(MouseEvent.MOUSE_EXITED, resizeListener);
        dialogPane.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, resizeListener);
    }

    public static KeyCombination createKeyBind(KeyCode key) { return createKeyBind(key, false); }
    public static KeyCombination createKeyBind(KeyCode key, boolean andShift) {
        if (andShift) return new KeyCodeCombination(key, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        return new KeyCodeCombination(key, KeyCombination.CONTROL_DOWN);
    }

    public static void addKeyBind(Scene scene, KeyCombination keyCombination, EventHandler<KeyEvent> action) {
        EventHandler<KeyEvent> keyHandler = event -> {
            if (keyCombination.match(event)) {
                action.handle(event);
                event.consume();
            }
        };
        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        keyBinds.put(keyCombination, keyHandler);
    }

    public static void removeKeyBind(Scene scene, KeyCombination keyCombination) {
        var keyHandler = keyBinds.remove(keyCombination);
        if (keyHandler != null) scene.removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
    }

    public static void clearKeyBinds(Scene scene) {
        for (var handler : keyBinds.values())
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, handler);
        keyBinds.clear();
    }

    public static Image getImage(String string) {
        return new Image(Objects.requireNonNull(Utils.class.getResourceAsStream(string)));
    }

    public static ColumnConstraints columnPercentage(double percentWidth) {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentWidth);
        column.setHgrow(Priority.ALWAYS);
        return column;
    }

    public static <T> T defaultIfNull(T value, T defaultValue) { return value == null ? defaultValue : value; }

    public static <T> WritableValue<T> createWritableValue(Consumer<T> setter, Supplier<T> getter) {
        return new WritableValue<>() {
            @Override
            public T getValue() {
                return getter.get();
            }
            @Override
            public void setValue(T t) {
                setter.accept(t);
            }
        };
    }

    public static class Coroutine {
        public static void runAsync(long delay, TimeUnit timeUnit, Runnable runnable) {
            CompletableFuture.delayedExecutor(delay, timeUnit).execute(() -> Platform.runLater(runnable));
        }

        public static void runAsync(long delay, TimeUnit timeUnit, Runnable... runnables) {
            CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
            for (var runnable : runnables)
                future = future.thenRunAsync(() -> Platform.runLater(runnable), CompletableFuture.delayedExecutor(delay, timeUnit));
        }
    }

    public static class ResizeListener implements EventHandler<MouseEvent> {

        private final double resizeMargin;
        private final double titleMargin;
        private final Stage stage;

        private boolean isSuitableTarget = false;
        private final BooleanProperty isMaximised = new SimpleBooleanProperty(false);
        public Point maxOrigin;
        public Point maxSize;

        public Point pressPos;
        public Point pressOrigin;
        public Point pressSize;

        public ResizeListener(Stage stage, double resizeMargin, double titleMargin) {
            this.stage = stage;
            this.resizeMargin = resizeMargin;
            this.titleMargin = titleMargin;
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();

            Scene scene = stage.getScene();
            Cursor cursor = scene.getCursor();
            double mouseX = mouseEvent.getScreenX();
            double mouseY = mouseEvent.getScreenY();

            boolean isNorthBorder = Math.abs(mouseY - stage.getY()) <= resizeMargin;
            boolean isEastBorder = Math.abs(mouseX - (stage.getX() + stage.getWidth())) <= resizeMargin;
            boolean isSouthBorder = Math.abs(mouseY - (stage.getY() + stage.getHeight())) <= resizeMargin;
            boolean isWestBorder = Math.abs(mouseX - stage.getX()) <= resizeMargin;
            boolean isBorder = isNorthBorder || isEastBorder || isSouthBorder || isWestBorder;

            if (mouseEventType == MouseEvent.MOUSE_MOVED) {
                Cursor resizeCursor = Cursor.DEFAULT;
                if (isNorthBorder && isEastBorder) resizeCursor = Cursor.NE_RESIZE;
                else if (isNorthBorder && isWestBorder) resizeCursor = Cursor.NW_RESIZE;
                else if (isSouthBorder && isEastBorder) resizeCursor = Cursor.SE_RESIZE;
                else if (isSouthBorder && isWestBorder) resizeCursor = Cursor.SW_RESIZE;
                else if (isNorthBorder) resizeCursor = Cursor.N_RESIZE;
                else if (isEastBorder) resizeCursor = Cursor.E_RESIZE;
                else if (isSouthBorder) resizeCursor = Cursor.S_RESIZE;
                else if (isWestBorder) resizeCursor = Cursor.W_RESIZE;
                scene.setCursor(resizeCursor);
                if (isBorder) mouseEvent.consume();
            }
            else if (mouseEventType == MouseEvent.MOUSE_PRESSED) {
                pressPos = new Point(mouseX, mouseY);
                pressOrigin = new Point(stage.getX(), stage.getY());
                pressSize = new Point(stage.getWidth(), stage.getHeight());
                isSuitableTarget = suitableTarget(mouseEvent);

                if (isSuitableTarget && mouseEvent.getClickCount() == 2) {
                    Rectangle2D screenBounds = Screen.getScreensForRectangle(stage.getX(), stage.getY(),
                            stage.getWidth(), stage.getHeight()).getFirst().getBounds();

                    Point startOrigin = new Point(stage.getX(), stage.getY());
                    Point startSize = new Point(stage.getWidth(), stage.getHeight());
                    Point endOrigin = new Point(stage.getX(), stage.getY());
                    Point endSize = new Point(stage.getWidth(), stage.getHeight());

                    if (isNorthBorder) {
                        endSize.y = stage.getHeight() + (stage.getY() - screenBounds.getMinY());
                        endOrigin.y = screenBounds.getMinY();
                    }
                    if (isEastBorder) endSize.x = stage.getWidth() + (screenBounds.getMaxX() - (stage.getX() + stage.getWidth()));
                    if (isSouthBorder) endSize.y = stage.getHeight() + (screenBounds.getMaxY() - (stage.getY() + stage.getHeight()));
                    if (isWestBorder) {
                        endSize.x = stage.getWidth() + (stage.getX() - screenBounds.getMinX());
                        endOrigin.x = screenBounds.getMinX();
                    }

                    if (isBorder) {
                        scene.setCursor(Cursor.DEFAULT);
                        if (stage.getWidth() == screenBounds.getWidth() && stage.getHeight() == screenBounds.getHeight()
                                && stage.getX() == screenBounds.getMinX() && stage.getY() == screenBounds.getMinY()) {
                            if (pressSize.x != screenBounds.getWidth() && pressSize.y != screenBounds.getHeight()
                                    && pressOrigin.x != screenBounds.getMinX() && pressOrigin.y != screenBounds.getMinY()) {
                                maxOrigin = pressOrigin;
                                maxSize = pressSize;
                            }
                            setMaximised(true);
                        }
                        AnimationManager.stageTransition(stage, startOrigin, startSize, endOrigin, endSize);
                    }
                    else if (pressPos.y - pressOrigin.y <= titleMargin) setMaximised(!isMaximised());
                    mouseEvent.consume();
                }
            }
            else if (mouseEventType == MouseEvent.MOUSE_DRAGGED) {
                if (isSuitableTarget && mouseEvent.getClickCount() == 1) {
                    boolean changed = false;

                    if (cursor == Cursor.N_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.NW_RESIZE) {
                        setHeight(pressSize.y + pressPos.y - mouseEvent.getScreenY());
                        if (stage.getHeight() > stage.getMinHeight()) stage.setY(mouseEvent.getScreenY());
                        else stage.setY(pressPos.y + pressSize.y - stage.getMinHeight());
                        changed = true;
                    }
                    if (cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE) {
                        setHeight(pressSize.y + mouseEvent.getScreenY() - pressPos.y);
                        changed = true;
                    }
                    if (cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE) {
                        setWidth(pressSize.x + pressPos.x - mouseEvent.getScreenX());
                        if (stage.getWidth() > stage.getMinWidth()) stage.setX(mouseEvent.getScreenX());
                        else stage.setX(pressPos.x + pressSize.x - stage.getMinWidth());
                        changed = true;
                    }
                    if (cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE) {
                        setWidth(pressSize.x + mouseEvent.getScreenX() - pressPos.x);
                        changed = true;
                    }
                    if (cursor == Cursor.DEFAULT && pressPos.y - pressOrigin.y <= titleMargin) {
                        stage.setX(pressOrigin.x + mouseEvent.getScreenX() - pressPos.x);
                        stage.setY(pressOrigin.y + mouseEvent.getScreenY() - pressPos.y);
                        changed = true;
                    }
                    if (changed) {
                        mouseEvent.consume();
                        setMaximised(false);
                    }
                }
            }
            else if (mouseEventType == MouseEvent.MOUSE_EXITED || mouseEventType == MouseEvent.MOUSE_EXITED_TARGET) {
                if (!mouseEvent.isPrimaryButtonDown()) scene.setCursor(Cursor.DEFAULT);
            }
        }

        private boolean suitableTarget(MouseEvent mouseEvent) {
            Node node = mouseEvent.getPickResult().getIntersectedNode();

            while (node != null) {
                if (node.getClass().toString().contains("DialogPane")) return true;
                if (node.getClass().toString().contains("ButtonBarSkin")) return true;
                if (node.getId() != null && node.getId().equals("titleLbl")) return true;
                if (Control.class.isAssignableFrom(node.getClass())) return false;
                if (node.getClass().toString().contains("TabHeaderSkin")) return false;
                if (Pane.class.isAssignableFrom(node.getClass())) return true;
                node = node.getParent();
            }
            return true;
        }

        private void setHeight(double height) { stage.setHeight(Math.clamp(height, stage.getMinHeight(), stage.getMaxHeight())); }
        private void setWidth(double width) { stage.setWidth(Math.clamp(width, stage.getMinWidth(), stage.getMaxWidth())); }

        public BooleanProperty maximisedProperty() { return isMaximised; }
        private boolean isMaximised() { return isMaximised.get(); }
        private void setMaximised(boolean isMaximised) { this.isMaximised.set(isMaximised); }
    }

    public static class Point {
        public double x;
        public double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
