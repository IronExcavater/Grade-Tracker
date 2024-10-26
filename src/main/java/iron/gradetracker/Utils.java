package iron.gradetracker;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Optional;

public class Utils {
    public static void handleExit(Stage stage) {
        stage.setOnCloseRequest(event -> {
            if (!DataManager.isDirty()) {
                stage.close();
                return;
            }

            ButtonType saveButton = new ButtonType("Save");
            ButtonType dontSaveButton = new ButtonType("Don't Save");
            ButtonType cancelButton = ButtonType.CANCEL;
            Optional<ButtonType> saveResult = createPopup(Alert.AlertType.CONFIRMATION,
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

    private static Optional<ButtonType> createPopup(Alert.AlertType alertType, String title, String header, String content, ButtonType... buttonTypes) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getButtonTypes().setAll(buttonTypes);
        return alert.showAndWait();
    }


    public static class Animation {
        public static TranslateTransition byYTranslation(Node node, double byY, double duration) { return byYTranslation(node, byY, duration, null); }
        public static TranslateTransition byYTranslation(Node node, double byY, double duration, Runnable onFinished) {
            TranslateTransition transition = new TranslateTransition(Duration.millis(duration), node);
            transition.setByY(byY);
            if (onFinished != null) transition.setOnFinished(_ -> onFinished.run());
            return transition;
        }

        public static FadeTransition toOpacityFade(Node node, double fromFade, double toFade, double duration) { return toOpacityFade(node, fromFade, toFade, duration, null); }
        public static FadeTransition toOpacityFade(Node node, double fromFade, double toFade, double duration, Runnable onFinished) {
            FadeTransition transition = new FadeTransition(Duration.millis(duration), node);
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
