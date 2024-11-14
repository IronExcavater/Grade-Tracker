package iron.gradetracker;

import javafx.animation.*;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.File;
import java.util.*;


public class Utils {

    private static final Map<KeyCombination, EventHandler<KeyEvent>> keyBinds = new HashMap<>();

    public static void handleExit(Stage stage) {
        stage.setOnCloseRequest(event -> {
            if (!DataManager.isDirty()) {
                stage.close();
                return;
            }

            ButtonType saveButton = new ButtonType("Save");
            ButtonType dontSaveButton = new ButtonType("Don't Save");
            ButtonType cancelButton = ButtonType.CANCEL;
            var saveResult = createPopup(Alert.AlertType.CONFIRMATION,
                    "Unsaved Changes", "You have unsaved changes.", "Do you want to save before exiting?",
                    saveButton, dontSaveButton, cancelButton);

            saveResult.ifPresentOrElse(result -> {
                if (result.equals(saveButton)) {
                    DataManager.saveData();
                    stage.close();
                } else if (result.equals(dontSaveButton)) {
                    stage.close();
                } else {
                    event.consume();
                }
            }, event::consume);
        });
    }

    public static Optional<ButtonType> createPopup(Alert.AlertType alertType, String title, String header, String content, ButtonType... buttonTypes) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getButtonTypes().setAll(buttonTypes);
        return alert.showAndWait();
    }

    public static FileChooser createFileChooser(String title, FileChooser.ExtensionFilter... extensionFilters) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(extensionFilters);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        return fileChooser;
    }

    public static Optional<?> createDialog(String title, Node content, ButtonType... buttonTypes) {
        Dialog<?> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypes);
        return dialog.showAndWait();
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

    public static ColumnConstraints columnPercentage(double percentWidth) {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentWidth);
        column.setHgrow(Priority.ALWAYS);
        return column;
    }

    public static <T> T defaultIfNull(T value, T defaultValue) { return value == null ? defaultValue : value; }

    public static class Animation {
        private static final Set<Node> animatingNodes = new HashSet<>();

        public static boolean isAnimating(Node node) { return animatingNodes.contains(node); }
        public static void lockNode(Node node) { animatingNodes.add(node); }
        public static void unlockNode(Node node) { animatingNodes.remove(node); }

        public static Node getNode(Transition transition) {
            return switch (transition) {
                case TranslateTransition translate -> translate.getNode();
                case FadeTransition fade -> fade.getNode();
                default -> throw new IllegalStateException("Unexpected value: " + transition);
            };
        }

        public static TranslateTransition byYTranslation(Node node, double byY, double duration) { return byYTranslation(node, byY, duration, null); }
        public static TranslateTransition byYTranslation(Node node, double byY, double duration, Runnable onFinished) {
            TranslateTransition transition = new TranslateTransition(Duration.millis(duration), node);
            lockNode(node);
            transition.setByY(byY);
            if (onFinished != null) transition.setOnFinished(_ -> onFinished.run());
            return transition;
        }

        public static FadeTransition toOpacityFade(Node node, double fromFade, double toFade, double duration) { return toOpacityFade(node, fromFade, toFade, duration, null); }
        public static FadeTransition toOpacityFade(Node node, double fromFade, double toFade, double duration, Runnable onFinished) {
            FadeTransition transition = new FadeTransition(Duration.millis(duration), node);
            lockNode(node);
            transition.setFromValue(fromFade);
            transition.setToValue(toFade);
            if (onFinished != null) transition.setOnFinished(_ -> onFinished.run());
            return transition;
        }

        public static SequentialTransition sequentialTransition(Transition... transitions) { return sequentialTransition(null, transitions); }
        public static SequentialTransition sequentialTransition(Runnable onFinished, Transition... transitions) {
            SequentialTransition transition = new SequentialTransition();
            transition.getChildren().addAll(transitions);
            if (onFinished != null) transition.setOnFinished(_ -> onFinished.run());
            return transition;
        }
    }
}
