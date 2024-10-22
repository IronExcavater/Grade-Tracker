package iron.gradetracker;

import javafx.scene.control.*;
import javafx.stage.Stage;
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

}
