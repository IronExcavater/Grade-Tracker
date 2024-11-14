package iron.gradetracker.controller;

import iron.gradetracker.*;
import iron.gradetracker.model.*;
import iron.gradetracker.model.action.*;
import iron.gradetracker.model.data.*;
import iron.gradetracker.view.*;
import iron.gradetracker.view.data.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.*;

public class DataController extends Controller {

    @FXML private HBox hBxBreadcrumbs;
    @FXML private GridPane gPaneHeadings;
    @FXML private ListView<DataView<?>> dataLst;

    @FXML private ComboBox<String> sortCmb;
    @FXML private StringTextField findTf;
    @FXML private Button undoBtn;
    @FXML private Button redoBtn;

    private static final DataFormat DATAVIEW_DATAFORMAT = new DataFormat("iron/data");
    private final ObjectProperty<Data<?>> currentData = new SimpleObjectProperty<>();
    private final ListChangeListener<Data<?>> childChangeListener = _ -> updateDataViewList();

    public DataController(Stage stage) { super(stage); }

    @FXML
    private void initialize() {
        ActionManager.controller = this;

        currentData.addListener((_, _, _) -> updateDataViewList());
        setCurrentData(App.getStudentData());

        dataLst.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dataLst.setCellFactory(_ -> new DataCell());

        undoBtn.disableProperty().bind(ActionManager.canUndoProperty().not());
        redoBtn.disableProperty().bind(ActionManager.canRedoProperty().not());

        Utils.addKeyBind(stage.getScene(), Utils.createKeyBind(KeyCode.S), _ -> DataManager.saveData());
        Utils.addKeyBind(stage.getScene(), Utils.createKeyBind(KeyCode.Z), _ -> handleUndo());
        Utils.addKeyBind(stage.getScene(), Utils.createKeyBind(KeyCode.Z, true), _ -> handleRedo());
        Utils.addKeyBind(stage.getScene(), Utils.createKeyBind(KeyCode.N), _ -> handleAdd());
        Utils.addKeyBind(stage.getScene(), Utils.createKeyBind(KeyCode.D), _ -> handleDelete());
        Utils.addKeyBind(stage.getScene(), Utils.createKeyBind(KeyCode.F), _ -> findTf.requestFocus());
        Utils.addKeyBind(stage.getScene(), Utils.createKeyBind(KeyCode.C), _ -> handleCopy());
        Utils.addKeyBind(stage.getScene(), Utils.createKeyBind(KeyCode.V), _ -> handlePaste());
    }

    @FXML
    public void handleAdd() {
        ActionManager.executeAction(switch (getCurrentData()) {
            case StudentData studentData -> new AddAction<>(studentData, studentData.createChild());
            case SessionData sessionData -> new AddAction<>(sessionData, sessionData.createChild());
            case SubjectData subjectData -> new AddAction<>(subjectData, subjectData.createChild());
            default -> throw new IllegalStateException("Unexpected value: " + currentData);
        });
    }

    @FXML
    public void handleDelete() {
        var selectedViews = dataLst.getSelectionModel().getSelectedItems();
        if (selectedViews.isEmpty()) return;

        var deleteAction = new CompositeAction();
        for (var data : selectedViews.stream().map(DataView::getData).toList().reversed())
            switch (getCurrentData()) {
            case StudentData studentData -> deleteAction.addAction(new RemoveAction<>(studentData, (SessionData) data));
            case SessionData sessionData -> deleteAction.addAction(new RemoveAction<>(sessionData, (SubjectData) data));
            case SubjectData subjectData -> deleteAction.addAction(new RemoveAction<>(subjectData, (AssessmentData) data));
            default -> {}
        }
        ActionManager.executeAction(deleteAction);
    }

    @FXML
    public void handleSort() {
        String sortOption = sortCmb.getValue();
        if (sortOption == null) return;

        switch (sortOption) {
            case "A to Z" -> dataLst.getItems().sort(Comparator.comparing(view -> view.getData().getName()));
            case "Z to A" -> dataLst.getItems().sort(Comparator.comparing(view -> ((DataView<?>)view).getData().getName()).reversed());
            case "High to Low" -> dataLst.getItems().sort(Comparator.comparing(view -> view.getData().getMark()));
            case "Low to High" -> dataLst.getItems().sort(Comparator.comparing(view -> ((DataView<?>)view).getData().getMark()).reversed());
            case "Custom" -> updateDataViewList();
        }
    }

    @FXML
    public void handleUndo() { ActionManager.undoAction(); }

    @FXML
    public void handleRedo() { ActionManager.redoAction(); }

    public void handleCopy() {
        var selectedView = dataLst.getSelectionModel().getSelectedItem();
        if (selectedView == null) return;
        ClipboardContent content = new ClipboardContent();
        content.put(DATAVIEW_DATAFORMAT, selectedView.toClipboardData());
        Clipboard.getSystemClipboard().setContent(content);
    }

    public void handlePaste() {
        var clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasContent(DATAVIEW_DATAFORMAT)) {
            String json = (String) clipboard.getContent(DATAVIEW_DATAFORMAT);
            int dropIndex = dataLst.getSelectionModel().getSelectedIndex() + 1;
            ActionManager.executeAction(switch (getCurrentData()) {
                case StudentData studentData -> new AddAction<>(studentData,
                        DataManager.gson.fromJson(json, SessionData.class), dropIndex);
                case SessionData sessionData -> new AddAction<>(sessionData,
                        DataManager.gson.fromJson(json, SubjectData.class), dropIndex);
                case SubjectData subjectData -> new AddAction<>(subjectData,
                        DataManager.gson.fromJson(json, AssessmentData.class), dropIndex);
                default -> throw new IllegalStateException("Unexpected value: " + currentData);
            });
        }
    }

    @FXML
    private void handleListClick(MouseEvent mouseEvent) {
        if (dataLst.getSelectionModel().getSelectedItem() == null) return;
        Data<?> clickedData = dataLst.getSelectionModel().getSelectedItem().getData();

        if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
            if (clickedData.getName() == null || clickedData.getName().isBlank()) return;
            if (clickedData instanceof AssessmentData) return;
            setCurrentData(clickedData);
        }
    }

    public Data<?> getCurrentData() { return currentData.get(); }

    public void setCurrentData(Data<?> data) {
        if (getCurrentData() != null) getCurrentData().getChildren().removeListener(childChangeListener);
        currentData.set(data);
        getCurrentData().getChildren().addListener(childChangeListener);
    }

    private void updateDataViewList() {
        // Populate lstData with new DataViews observing currentData children
        ObservableList<DataView<?>> dataViews = FXCollections.observableArrayList(
                getCurrentData().getChildren().stream()
                        .map(data -> switch (data) {
                            case SessionData sessionData -> new SessionView(sessionData);
                            case SubjectData subjectData -> new SubjectView(subjectData);
                            case AssessmentData assessmentData -> new AssessmentView(assessmentData);
                            default -> throw new IllegalStateException("Unexpected value: " + data);
                        })
                        .toList());

        FilteredList<DataView<?>> filteredList = new FilteredList<>(dataViews);
        findTf.textProperty().addListener((_, _, newValue) -> {
            String query = newValue.trim().toLowerCase();
            if (query.isEmpty()) filteredList.setPredicate(_ -> true);
            else filteredList.setPredicate(view -> view.getData().getName().toLowerCase().contains(query));
        });

        dataLst.setItems(filteredList);
        updateBreadcrumbs();
        updateColumnHeadings();
    }

    private void updateBreadcrumbs() {
        // Populate hBxBreadcrumbs with Hyperlinks of currentData ancestors
        hBxBreadcrumbs.getChildren().clear();
        Data<?> data = getCurrentData();
        hBxBreadcrumbs.getChildren().add(new BreadcrumbLink(this, data, true));
        while (!data.equals(App.getStudentData())) {
            data = data.getParent();
            hBxBreadcrumbs.getChildren().addFirst(new Text(">"));
            hBxBreadcrumbs.getChildren().addFirst(new BreadcrumbLink(this, data, false));
        }
    }

    private void updateColumnHeadings() {
        // Update gPaneHeadings with column headings of currentData
        gPaneHeadings.getChildren().clear();
        gPaneHeadings.getColumnConstraints().clear();
        if (!dataLst.getItems().isEmpty()) {
            DataView<?> childView = dataLst.getItems().getFirst();
            int[] columnWidths = childView.getColumnWidths();
            String[] columnNames = childView.getColumnNames();
            for (int i = 0; i < columnNames.length; i++) {
                Text heading = new Text(columnNames[i]);
                gPaneHeadings.add(heading, i, 0);
                gPaneHeadings.getColumnConstraints().add(Utils.columnPercentage(columnWidths[i]));
            }
        }
    }

    private class DataCell extends ListCell<DataView<?>> {
        private static DataCell dragCell;

        public DataCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            var copy = new MenuItem("Copy");
            copy.setOnAction(_ -> handleCopy());
            var paste = new MenuItem("Paste");
            paste.setOnAction(_ -> handlePaste());
            setContextMenu(new ContextMenu(copy, paste));

            setOnDragDetected(event -> {
                if (getItem() == null) return;
                boolean suitable = true;

                if (sortCmb.getValue() == null || !sortCmb.getValue().equals("Custom")) {
                    sortCmb.setValue("Custom");
                    suitable = false;
                }
                if (!findTf.getText().isBlank()) {
                    findTf.setText("");
                    suitable = false;
                }

                if (suitable) {
                    Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();

                    int dragIndex = dataLst.getItems().indexOf(getItem());
                    dragCell = this;
                    content.put(DATAVIEW_DATAFORMAT, dragIndex);
                    dragboard.setContent(content);
                    dragboard.setDragView(getGraphic().snapshot(null, null));
                }
                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasContent(DATAVIEW_DATAFORMAT))
                    event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            });

            setOnDragEntered(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasContent(DATAVIEW_DATAFORMAT)) {
                    setOpacity(0.3);
                }
                event.consume();
            });

            setOnDragExited(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasContent(DATAVIEW_DATAFORMAT)) {
                    setOpacity(1);
                }
                event.consume();
            });

            setOnDragDropped(event -> {
                if (getItem() == null) return;
                Dragboard dragboard = event.getDragboard();
                boolean success = false;

                if (dragboard.hasContent(DATAVIEW_DATAFORMAT)) {
                    int dragIndex = (int) dragboard.getContent(DATAVIEW_DATAFORMAT);
                    int dropIndex = dataLst.getItems().indexOf(getItem());
                    DataView<?> dragView = dragCell.getItem();
                    int indexDiff = Math.abs(dropIndex - dragIndex);

                    // Animate in-between cell fade out
                    var fadeOut = Utils.Animation.sequentialTransition();
                    var fadeOutTransitions = fadeOut.getChildren();
                    for (int i = Math.min(dragIndex + 1, dropIndex); i < Math.max(dragIndex, dropIndex + 1); i++) {
                        Node cellView = dataLst.getItems().get(i);
                        fadeOutTransitions.add(i > dropIndex ? 0 : fadeOutTransitions.size(),
                                Utils.Animation.toOpacityFade(cellView, 1, 0, 300f / indexDiff));
                    }
                    fadeOut.play();

                    // Animate dragCell to move from dragIndex to dropIndex
                    double cellHeight = dragView.getHeight() + 6;
                    dragCell.toFront();
                    Utils.Animation.byYTranslation(dragView, cellHeight * indexDiff * (dragIndex > dropIndex ? -1 : 1), 600, () -> {
                        dragView.setTranslateY(0);
                        ActionManager.executeAction(new MoveAction<>(getCurrentData().getParent(), dragIndex, dropIndex));
                        // Animate in-between cell fade in
                        var fadeIn = Utils.Animation.sequentialTransition();
                        var fadeInTransitions = fadeIn.getChildren();
                        for (int i = Math.min(dragIndex, dropIndex + 1); i < Math.max(dragIndex + 1, dropIndex); i++) {
                            Node cellView = dataLst.getItems().get(i);
                            fadeInTransitions.add(i > dropIndex ? 0 : fadeInTransitions.size(),
                                    Utils.Animation.toOpacityFade(cellView, 0, 1, 300f / indexDiff));
                        }
                        fadeIn.play();
                    }).play();
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });

            setOnDragDone(DragEvent::consume);
        }

        @Override
        public void updateItem(DataView<?> data, boolean empty) {
            super.updateItem(data, empty);
            if (empty || data == null)
                setGraphic(null);
            else
                setGraphic(getItem());
        }
    }
}
