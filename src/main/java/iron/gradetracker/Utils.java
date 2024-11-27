package iron.gradetracker;

import javafx.application.Platform;
import javafx.event.EventHandler;
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

public class Utils {
    private static final Map<KeyCombination, EventHandler<KeyEvent>> keyBinds = new HashMap<>();

    public static Alert createAlert(Alert.AlertType alertType, String title, String header, String content, ButtonType... buttonTypes) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header + "\n" + content);
        alert.getButtonTypes().setAll(buttonTypes);
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(Utils.class.getResource("dialog.css")).toExternalForm());
        alert.initStyle(StageStyle.TRANSPARENT);
        alert.getDialogPane().getScene().setFill(Color.TRANSPARENT);
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
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypes);
        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(Utils.class.getResource("dialog.css")).toExternalForm());
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.getDialogPane().getScene().setFill(Color.TRANSPARENT);
        return dialog;
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
}
