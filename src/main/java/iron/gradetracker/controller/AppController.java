package iron.gradetracker.controller;

import iron.gradetracker.*;
import iron.gradetracker.model.App;
import iron.gradetracker.view.ImageButton;
import javafx.animation.Transition;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class AppController extends Controller {

    @FXML private BorderPane root;

    @FXML private HBox titleHbx;
    @FXML private Label titleLbl;
    @FXML private HBox buttonHbx;
    @FXML private ImageButton minBtn;
    @FXML private ImageButton maxBtn;
    @FXML private ImageButton closeBtn;

    @FXML private MenuItem saveItm;
    @FXML private MenuItem undoItm;
    @FXML private MenuItem redoItm;
    @FXML private MenuItem addItm;
    @FXML private MenuItem deleteItm;
    @FXML private MenuItem sortMnu;
    @FXML private MenuItem findItm;

    private Utils.ResizeListener resizeListener;
    private final ObjectProperty<DataController> dataController = new SimpleObjectProperty<>();
    private final BooleanProperty isSelectionNull = new SimpleBooleanProperty(true);
    @FXML private TabPane tabMenu;

    public AppController(Stage stage) { super(stage); }

    @FXML
    protected void initialize() {
        resizeListener = new Utils.ResizeListener(stage, 10, 90);
        stage.addEventFilter(MouseEvent.MOUSE_MOVED, resizeListener);
        stage.addEventFilter(MouseEvent.MOUSE_PRESSED, resizeListener);
        stage.addEventFilter(MouseEvent.MOUSE_DRAGGED, resizeListener);
        stage.addEventFilter(MouseEvent.MOUSE_EXITED, resizeListener);
        stage.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, resizeListener);

        resizeListener.maximisedProperty().addListener((_, _, newValue) -> handleMaximise(resizeListener, newValue));
        stage.fullScreenProperty().addListener((_, _, newValue) -> handleMaximise(resizeListener, newValue));

        minBtn.setProperties(stage.focusedProperty(), buttonHbx.hoverProperty());
        maxBtn.setProperties(stage.focusedProperty(), buttonHbx.hoverProperty());
        closeBtn.setProperties(stage.focusedProperty(), buttonHbx.hoverProperty());

        Image unfocusedImg = Utils.getImage("images/window-buttons/unfocused.png");
        minBtn.setImages(
                Utils.getImage("images/window-buttons/min-focus.png"),
                unfocusedImg,
                Utils.getImage("images/window-buttons/min-hover.png"),
                Utils.getImage("images/window-buttons/min-press.png")
        );
        maxBtn.setImages(
                Utils.getImage("images/window-buttons/max-focus.png"),
                unfocusedImg,
                Utils.getImage("images/window-buttons/max-hover.png"),
                Utils.getImage("images/window-buttons/max-press.png")
        );
        closeBtn.setImages(
                Utils.getImage("images/window-buttons/close-focus.png"),
                unfocusedImg,
                Utils.getImage("images/window-buttons/close-hover.png"),
                Utils.getImage("images/window-buttons/close-press.png")
        );

        minBtn.setOnAction(_ -> stage.setIconified(true));
        maxBtn.setOnAction(_ -> stage.setFullScreen(!stage.isFullScreen()));
        closeBtn.setOnAction(_ -> {
            if (!DataManager.isDirty()) {
                stage.close();
                return;
            }

            var saveBtype = new ButtonType("Save", ButtonBar.ButtonData.YES);
            var dontSaveBtype = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);
            var savePopup = Utils.createAlert(Alert.AlertType.CONFIRMATION,
                    "Unsaved Changes", "You have unsaved changes.", "Do you want to save before exiting?",
                    saveBtype, dontSaveBtype, ButtonType.CANCEL);

            var saveResult = savePopup.showAndWait();
            saveResult.ifPresent(result -> {
                if (result.equals(saveBtype)) {
                    DataManager.saveData();
                    stage.close();
                } else if (result.equals(dontSaveBtype)) {
                    stage.close();
                }
            });
        });

        tabMenu.getSelectionModel().selectedItemProperty().addListener((_, _, newTab) -> loadContent(newTab));
        loadContent(tabMenu.getSelectionModel().getSelectedItem());

        saveItm.disableProperty().bind(DataManager.dirtyProperty().not());
        undoItm.disableProperty().bind(ActionManager.canUndoProperty().not());
        redoItm.disableProperty().bind(ActionManager.canRedoProperty().not());
        addItm.disableProperty().bind(dataController.isNull());
        deleteItm.disableProperty().bind(dataController.isNull().or(isSelectionNull));
        sortMnu.disableProperty().bind(dataController.isNull());
        findItm.disableProperty().bind(dataController.isNull());

        saveItm.setOnAction(_ -> DataManager.saveData());
        undoItm.setOnAction(_ -> ActionManager.undoAction());
        redoItm.setOnAction(_ -> ActionManager.redoAction());
        addItm.setOnAction(_ -> {
            if (dataController.get() != null) dataController.get().handleAdd();
        });
        deleteItm.setOnAction(_ -> {
            if (dataController.get() != null) dataController.get().handleDelete();
        });
        findItm.setOnAction(_ -> {
            if (dataController.get() != null) dataController.get().getFindTextField().requestFocus();
        });
    }

    private void loadContent(Tab tab) {
        if (stage.getScene() != null) {
            Utils.clearKeyBinds(stage.getScene());
            Utils.addKeyBind(stage.getScene(), Utils.createKeyBind(KeyCode.S), _ -> DataManager.saveData());
        }

        try {
            Class<?> tabClass = Class.forName("iron.gradetracker.controller.%sController".formatted(tab.getText()));
            Controller controller = (Controller) tabClass.getDeclaredConstructor(Stage.class).newInstance(stage);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/iron/gradetracker/view/%s-view.fxml"
                    .formatted(tab.getText().toLowerCase())), null, null, _ -> controller);
            root.setCenter(fxmlLoader.load());
            if (controller instanceof DataController newController) {
                dataController.set(newController);
                dataController.get().getListView().getSelectionModel().selectedItemProperty().addListener(
                        (_, _, newValue) -> isSelectionNull.set(newValue == null));
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Exception occurred while loading root for the %s tab".formatted(tab.getText()), e);
        }
    }

    private void handleMaximise(Utils.ResizeListener resizeListener, boolean maximise) {
        Rectangle2D stageBounds = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
        Rectangle2D screenBounds = Screen.getScreensForRectangle(stageBounds).getFirst().getBounds();
        if (!maximise && !screenBounds.equals(stageBounds)) return;

        if (maximise) {
            resizeListener.maxSize = new Utils.Point(stage.getWidth(), stage.getHeight());
            resizeListener.maxOrigin = new Utils.Point(stage.getX(), stage.getY());
        }

        Utils.Point startOrigin = new Utils.Point(stage.getX(), stage.getY());
        Utils.Point startSize = new Utils.Point(stage.getWidth(), stage.getHeight());
        Utils.Point endOrigin = new Utils.Point(
                maximise ? screenBounds.getMinX() : resizeListener.maxOrigin.x,
                maximise ? screenBounds.getMinY() : resizeListener.maxOrigin.y);
        Utils.Point endSize = new Utils.Point(
                maximise ? screenBounds.getWidth() : resizeListener.maxSize.x,
                maximise ? screenBounds.getHeight() : resizeListener.maxSize.y);

        AnimationManager.stageTransition(stage, startOrigin, startSize, endOrigin, endSize, dataController.get());
    }

    @FXML
    private void handleImport() {
        var jsonFilter = new FileChooser.ExtensionFilter("Json Files", "*.json");
        var xlsxFilter = new FileChooser.ExtensionFilter("Excel Files", "*.xlsx");
        var fileChooser = Utils.createFileChooser("Export File", jsonFilter, xlsxFilter);
        var file = fileChooser.showOpenDialog(stage);

        if (file == null) return;

        var overridePopup = Utils.createAlert(Alert.AlertType.WARNING,
                "Override Data", "Overriding existing data.", "Do you want to continue?",
                ButtonType.YES, ButtonType.CANCEL);

        var overrideResult = overridePopup.showAndWait();
        overrideResult.ifPresent(result -> {
            if (result.equals(ButtonType.YES))
                DataManager.importData(file);
        });
    }

    @FXML
    private void handleExport() {
        var csvFilter = new FileChooser.ExtensionFilter("CSV Files", "*.csv");
        var jsonFilter = new FileChooser.ExtensionFilter("Json Files", "*.json");
        var xlsxFilter = new FileChooser.ExtensionFilter("Excel Files", "*.xlsx");
        var fileChooser = Utils.createFileChooser("Export File", csvFilter, jsonFilter, xlsxFilter);
        fileChooser.setInitialFileName(App.getStudentData().getName());
        var file = fileChooser.showSaveDialog(stage);

        if (file == null) return;
        DataManager.exportData(file);
    }

    @FXML
    private void handleSort(ActionEvent actionEvent) {
        MenuItem source = (MenuItem) actionEvent.getSource();
        if (dataController.get() != null) dataController.get().setSortOption(source.getText());
    }
}