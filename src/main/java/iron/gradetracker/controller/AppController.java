package iron.gradetracker.controller;

import iron.gradetracker.*;
import iron.gradetracker.model.App;
import iron.gradetracker.view.ImageButton;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class AppController extends Controller {

    @FXML private BorderPane root;

    @FXML private HBox titleHbx;
    @FXML private HBox buttonHbx;
    @FXML private ImageButton minBtn;
    @FXML private ImageButton maxBtn;
    @FXML private ImageButton closeBtn;

    private static boolean isMaximised;
    private static Point prevOrigin;
    private static Point prevSize;

    @FXML private MenuItem saveMnu;
    @FXML private MenuItem undoMnu;
    @FXML private MenuItem redoMnu;

    private final ObjectProperty<Controller> tabController = new SimpleObjectProperty<>();
    private final BooleanBinding isDataController = Bindings.createBooleanBinding(() ->
            !tabController.getValue().getClass().equals(DataController.class), tabController);

    @FXML private TabPane tabMenu;

    public AppController(Stage stage) { super(stage); }

    @FXML
    protected void initialize() {
        ResizeListener resizeListener = new ResizeListener(stage);
        stage.addEventFilter(MouseEvent.MOUSE_MOVED, resizeListener);
        stage.addEventFilter(MouseEvent.MOUSE_PRESSED, resizeListener);
        stage.addEventFilter(MouseEvent.MOUSE_DRAGGED, resizeListener);
        stage.addEventFilter(MouseEvent.MOUSE_EXITED, resizeListener);
        stage.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, resizeListener);

        titleHbx.setOnMousePressed(resizeListener);
        titleHbx.setOnMouseDragged(resizeListener);
        titleHbx.setOnMouseClicked(resizeListener);

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
        maxBtn.setOnAction(_ -> {
            if (stage.isFullScreen()) {
                stage.setWidth(prevSize.x);
                stage.setHeight(prevSize.y);
                stage.setX(prevOrigin.x);
                stage.setY(prevOrigin.y);
            } else {
                prevOrigin = new Point(stage.getX(), stage.getY());
                prevSize = new Point(stage.getWidth(), stage.getHeight());

                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                stage.setX(screenBounds.getMinX());
                stage.setY(screenBounds.getMinY());
                stage.setWidth(screenBounds.getWidth());
                stage.setHeight(screenBounds.getHeight());
            }
            stage.setFullScreen(!stage.isFullScreen());
        });
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

        saveMnu.disableProperty().bind(DataManager.dirtyProperty().not());
        undoMnu.disableProperty().bind(ActionManager.canUndoProperty().not());
        redoMnu.disableProperty().bind(ActionManager.canRedoProperty().not());
        undoMnu.setOnAction(_ -> ActionManager.undoAction());
        redoMnu.setOnAction(_ -> ActionManager.redoAction());
    }

    private void loadContent(Tab tab) {
        if (stage.getScene() != null) {
            Utils.clearKeyBinds(stage.getScene());
            Utils.addKeyBind(stage.getScene(), Utils.createKeyBind(KeyCode.S), _ -> DataManager.saveData());
        }

        try {
            Class<?> tabClass = Class.forName("iron.gradetracker.controller.%sController".formatted(tab.getText()));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/iron/gradetracker/view/%s-view.fxml".formatted(tab.getText().toLowerCase())),
                    null, null, _ -> {
                try {
                    Controller controller = (Controller) tabClass.getDeclaredConstructor(Stage.class).newInstance(stage);
                    tabController.set(controller);
                    return controller;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
            root.setCenter(fxmlLoader.load());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Exception occurred while loading root for the %s tab".formatted(tab.getText()), e);
        }
    }

    @FXML
    private void handleSave() {
        DataManager.saveData();
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

    private static class ResizeListener implements EventHandler<MouseEvent> {

        private static final double RESIZE_MARGIN = 10;
        private static final double TITLE_MARGIN = 90;
        private final Stage stage;
        private Point mousePos;
        private Point prevOrigin;
        private Point prevSize;

        public ResizeListener(Stage stage) {
            this.stage = stage;
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();

            Scene scene = stage.getScene();
            Cursor cursor = scene.getCursor();
            double mouseX = mouseEvent.getScreenX();
            double mouseY = mouseEvent.getScreenY();

            boolean isNorthBorder = Math.abs(mouseY - stage.getY()) <= RESIZE_MARGIN;
            boolean isEastBorder = Math.abs(mouseX - (stage.getX() + stage.getWidth())) <= RESIZE_MARGIN;
            boolean isSouthBorder = Math.abs(mouseY - (stage.getY() + stage.getHeight())) <= RESIZE_MARGIN;
            boolean isWestBorder = Math.abs(mouseX - stage.getX()) <= RESIZE_MARGIN;
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
            }
            else if (mouseEventType == MouseEvent.MOUSE_PRESSED && !(mouseEvent.getTarget() instanceof Control)) {
                mousePos = new Point(mouseX, mouseY);
                prevOrigin = new Point(stage.getX(), stage.getY());
                prevSize = new Point(stage.getWidth(), stage.getHeight());

                if (mouseEvent.getClickCount() == 2) {
                    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                    if (isNorthBorder) {
                        stage.setHeight(stage.getHeight() + (stage.getY() - screenBounds.getMinY()));
                        stage.setY(screenBounds.getMinY());
                    }
                    if (isEastBorder) stage.setWidth(stage.getWidth() + (screenBounds.getMaxX() - (stage.getX() + stage.getWidth())));
                    if (isSouthBorder) stage.setHeight(stage.getHeight() + (screenBounds.getMaxY() - (stage.getY() + stage.getHeight())));
                    if (isWestBorder) {
                        stage.setWidth(stage.getWidth() + (stage.getX() - screenBounds.getMinX()));
                        stage.setX(screenBounds.getMinX());
                    }
                    if (isBorder) {
                        if (stage.getWidth() == screenBounds.getWidth() && stage.getHeight() == screenBounds.getHeight()
                                && stage.getX() == screenBounds.getMinX() && stage.getY() == screenBounds.getMinY()) {
                            if (prevSize.x != screenBounds.getWidth() && prevSize.y != screenBounds.getHeight()
                                    && prevOrigin.x != screenBounds.getMinX() && prevOrigin.y != screenBounds.getMinY()) {
                                AppController.prevOrigin = prevOrigin;
                                AppController.prevSize = prevSize;
                            }
                            isMaximised = true;
                        }
                    }
                    else if (mousePos.y - prevOrigin.y <= TITLE_MARGIN) {
                        if (isMaximised) {
                            System.out.println("Shrink!");
                            System.out.println(AppController.prevSize.x + ", " + AppController.prevSize.y);
                            System.out.println(AppController.prevOrigin.x + ", " + AppController.prevOrigin.y);
                            stage.setWidth(AppController.prevSize.x);
                            stage.setHeight(AppController.prevSize.y);
                            stage.setX(AppController.prevOrigin.x);
                            stage.setY(AppController.prevOrigin.y);
                            isMaximised = false;
                        } else {
                            AppController.prevOrigin = prevOrigin;
                            AppController.prevSize = prevSize;
                            System.out.println("Maximise!");
                            System.out.println(AppController.prevSize.x + ", " + AppController.prevSize.y);
                            System.out.println(AppController.prevOrigin.x + ", " + AppController.prevOrigin.y);
                            stage.setX(screenBounds.getMinX());
                            stage.setY(screenBounds.getMinY());
                            stage.setWidth(screenBounds.getWidth());
                            stage.setHeight(screenBounds.getHeight());
                            isMaximised = true;
                        }
                    }
                    mouseEvent.consume();
                }
            }
            else if (mouseEventType == MouseEvent.MOUSE_DRAGGED && !(mouseEvent.getTarget() instanceof Control)) {
                if (cursor == Cursor.N_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.NW_RESIZE) {
                    setHeight(prevSize.y + mousePos.y - mouseEvent.getScreenY());
                    if (stage.getHeight() > stage.getMinHeight()) stage.setY(mouseEvent.getScreenY());
                    else stage.setY(mousePos.y + prevSize.y - stage.getMinHeight());
                }
                if (cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE) {
                    setHeight(prevSize.y + mouseEvent.getScreenY() - mousePos.y);
                }
                if (cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE) {
                    setWidth(prevSize.x + mousePos.x - mouseEvent.getScreenX());
                    if (stage.getWidth() > stage.getMinWidth()) stage.setX(mouseEvent.getScreenX());
                    else stage.setX(mousePos.x + prevSize.x - stage.getMinWidth());
                }
                if (cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE) {
                    setWidth(prevSize.x + mouseEvent.getScreenX() - mousePos.x);
                }
                if (cursor == Cursor.DEFAULT && mousePos.y - prevOrigin.y <= TITLE_MARGIN) {
                    stage.setX(prevOrigin.x + mouseEvent.getScreenX() - mousePos.x);
                    stage.setY(prevOrigin.y + mouseEvent.getScreenY() - mousePos.y);
                }
                isMaximised = false;
            }
            else if (mouseEventType == MouseEvent.MOUSE_EXITED || mouseEventType == MouseEvent.MOUSE_EXITED_TARGET) {
                if (!mouseEvent.isPrimaryButtonDown()) scene.setCursor(Cursor.DEFAULT);
            }
        }
        private void setHeight(double height) { stage.setHeight(Math.clamp(height, stage.getMinHeight(), stage.getMaxHeight())); }
        private void setWidth(double width) { stage.setWidth(Math.clamp(width, stage.getMinWidth(), stage.getMaxWidth())); }
    }

    private record Point(double x, double y) {}
}