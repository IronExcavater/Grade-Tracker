package iron.gradetracker.controller;

import iron.gradetracker.*;
import iron.gradetracker.model.*;
import iron.gradetracker.model.action.*;
import iron.gradetracker.model.data.*;
import iron.gradetracker.view.*;
import iron.gradetracker.view.data.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
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

    private final MoveObservableList<DataView<?>> currentViewList = new MoveObservableList<>();
    private final SortedList<DataView<?>> sortedViewList = new SortedList<>(currentViewList);

    private final ListChangeListener<Data<?>> changeListener = change -> {
        while (change.next()) {
            if (change.wasPermutated()) {
                int from = change.getFrom();
                int to = change.getTo();
                var view = currentViewList.get(from);
                double cellHeight = view.getHeight() + 6;
                int indexDiff = Math.abs(to - from);
                // Animate in-between cell fade out
                var fadeOut = Utils.Animation.sequentialTransition();
                var fadeOutTransitions = fadeOut.getChildren();
                for (int i = Math.min(from + 1, to); i < Math.max(from, to + 1); i++) {
                    Node cellView = dataLst.getItems().get(i);
                    fadeOutTransitions.add(i > to ? 0 : fadeOutTransitions.size(),
                            Utils.Animation.toOpacityFade(cellView, 1, 0, 300f / indexDiff));
                }
                fadeOut.play();

                Utils.Animation.byYTranslation(view, cellHeight * indexDiff * (from > to ? -1 : 1), 600, () -> {
                    view.setTranslateY(0);
                    currentViewList.remove(view);
                    currentViewList.add(to, view);
                    // Animate in-between cell fade in
                    var fadeIn = Utils.Animation.sequentialTransition(() -> currentViewList.forEach(Utils.Animation::unlockNode));
                    var fadeInTransitions = fadeIn.getChildren();
                    for (int i = Math.min(from, to + 1); i < Math.max(from + 1, to); i++) {
                        Node cellView = dataLst.getItems().get(i);
                        fadeInTransitions.add(i > to ? 0 : fadeInTransitions.size(),
                                Utils.Animation.toOpacityFade(cellView, 0, 1, 300f / indexDiff));
                    }
                    fadeIn.play();
                }).play();
                break;
            }
            for (int i = change.getRemovedSize() - 1; i >= 0; i--) {
                var view = currentViewList.get(change.getFrom() + i);
                if (Utils.Animation.isAnimating(view))
                    currentViewList.remove(view);
                else Utils.Animation.toOpacityFade(view, 1, 0, 300, () -> {
                    currentViewList.remove(view);
                    Utils.Animation.unlockNode(view);
                }).play();
            }
            for (int i = 0; i < change.getAddedSize(); i++) {
                var view = createView(change.getAddedSubList().get(i));
                currentViewList.add(change.getFrom() + i, view);
                if (!Utils.Animation.isAnimating(view))
                    Utils.Animation.toOpacityFade(view, 0, 1, 300,() -> Utils.Animation.unlockNode(view)).play();
            }
        }
    };

    private DataView<?> createView(Data<?> data) {
        return switch (data) {
            case SessionData sessionData -> new SessionView(sessionData);
            case SubjectData subjectData -> new SubjectView(subjectData);
            case AssessmentData assessmentData -> new AssessmentView(assessmentData);
            default -> throw new IllegalStateException("Unexpected value: " + data);
        };
    }

    public DataController(Stage stage) { super(stage); }

    @FXML
    private void initialize() {
        ActionManager.controller = this;
        DataManager.controller = this;

        currentViewList.addListener((ListChangeListener<? super DataView<?>>) _ -> {
            updateBreadcrumbs();
            updateColumnHeadings();
        });
        sortedViewList.comparatorProperty().bind(sortCmb.valueProperty().map(option -> switch(option) {
            case "A to Z" -> Comparator.comparing((DataView<?> view) -> view.getData().getName());
            case "Z to A" -> Comparator.comparing((DataView<?> view) -> view.getData().getName()).reversed();
            case "High to Low" -> Comparator.comparing((DataView<?> view) -> view.getData().getMark());
            case "Low to High" -> Comparator.comparing((DataView<?> view) -> view.getData().getMark()).reversed();
            default -> null;
        }));

        dataLst.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dataLst.setCellFactory(_ -> new DataCell());
        dataLst.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            FilteredList<DataView<?>> filteredList = new FilteredList<>(sortedViewList);
            findTf.textProperty().addListener((_, _, newValue) -> {
                String query = newValue.trim().toLowerCase();
                if (query.isEmpty()) filteredList.setPredicate(_ -> true);
                else filteredList.setPredicate(view -> view.getData().getName().toLowerCase().contains(query));
            });
            return filteredList;
        }));

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

        setCurrentData(App.getStudentData());
    }

    @FXML
    public void handleAdd() {
        int addIndex = dataLst.getSelectionModel().getSelectedIndex() + 1;
        ActionManager.executeAction(switch (getCurrentData()) {
            case StudentData studentData -> new AddAction<>(studentData, studentData.createChild(), addIndex);
            case SessionData sessionData -> new AddAction<>(sessionData, sessionData.createChild(), addIndex);
            case SubjectData subjectData -> new AddAction<>(subjectData, subjectData.createChild(), addIndex);
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
            int pasteIndex = dataLst.getSelectionModel().getSelectedIndex() + 1;
            ActionManager.executeAction(switch (getCurrentData()) {
                case StudentData studentData -> new AddAction<>(studentData,
                        DataManager.gson.fromJson(json, SessionData.class), pasteIndex);
                case SessionData sessionData -> new AddAction<>(sessionData,
                        DataManager.gson.fromJson(json, SubjectData.class), pasteIndex);
                case SubjectData subjectData -> new AddAction<>(subjectData,
                        DataManager.gson.fromJson(json, AssessmentData.class), pasteIndex);
                default -> throw new IllegalStateException("Unexpected value: " + currentData);
            });
        }
    }

    public void handleRename(Data<?> data) {
        var renameField = new StringTextField();
        renameField.setPromptText("Set new name");
        var validationMessage = new Label();
        validationMessage.setStyle("-fx-text-fill: red;");
        var dialogContent = new VBox(2, renameField, validationMessage);
        dialogContent.setPrefWidth(300);
        var renameBtype = new ButtonType("Rename");
        var renameDialog = Utils.createDialog("Rename", dialogContent, renameBtype, ButtonType.CANCEL);
        var renameButton = (Button) renameDialog.getDialogPane().lookupButton(renameBtype);
        renameButton.addEventFilter(ActionEvent.ACTION, event -> {
            String newName = renameField.getText().trim();
            if (newName.isEmpty()) {
                validationMessage.setText("Name cannot be blank!");
                event.consume();
            } else {
                data.nameProperty().set(newName);
            }
        });

        renameField.setRunnable(renameButton::fire);
        renameDialog.showAndWait();
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
        if (getCurrentData() != null) getCurrentData().getChildren().removeListener(changeListener);
        currentData.set(data);
        getCurrentData().getChildren().addListener(changeListener);
        currentViewList.setAll(getCurrentData().getChildren().stream().map(this::createView).toList());
    }

    private void updateBreadcrumbs() {
        // Populate hBxBreadcrumbs with Hyperlinks of currentData ancestors
        hBxBreadcrumbs.getChildren().clear();
        Data<?> data = getCurrentData();
        hBxBreadcrumbs.getChildren().add(new BreadcrumbLink(this, data));
        while (!data.equals(App.getStudentData())) {
            data = data.getParent();
            hBxBreadcrumbs.getChildren().addFirst(new Text(">"));
            hBxBreadcrumbs.getChildren().addFirst(new BreadcrumbLink(this, data));
        }
    }

    private void updateColumnHeadings() {
        // Update gPaneHeadings with column headings of currentData
        gPaneHeadings.getChildren().clear();
        gPaneHeadings.getColumnConstraints().clear();
        if (!currentViewList.isEmpty()) {
            DataView<?> childView = currentViewList.getFirst();
            int[] columnWidths = childView.getColumnWidths();
            String[] columnNames = childView.getColumnNames();
            for (int i = 0; i < columnNames.length; i++) {
                Text heading = new Text(columnNames[i]);
                gPaneHeadings.add(heading, i, 0);
                gPaneHeadings.getColumnConstraints().add(Utils.columnPercentage(columnWidths[i]));
            }
        }
    }

    public void setSortOption(String sortOption) { sortCmb.setValue(sortOption); }

    public String getSortOption() { return sortCmb.getValue() == null ? "Custom" : sortCmb.getValue(); }

    private class DataCell extends ListCell<DataView<?>> {

        private final ContextMenu contextMenu;

        public DataCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            var delete = new MenuItem("Delete");
            delete.setOnAction(_ -> handleDelete());
            var copy = new MenuItem("Copy");
            copy.setOnAction(_ -> handleCopy());
            var paste = new MenuItem("Paste");
            paste.setOnAction(_ -> handlePaste());
            var rename = new MenuItem("Rename");
            rename.setOnAction(_ -> handleRename(getItem().getData()));
            contextMenu = new ContextMenu(delete, copy, paste, rename);

            setOnDragDetected(event -> {
                if (getItem() == null) return;
                boolean suitable = true;

                if (sortCmb.getValue() == null || !sortCmb.getValue().equals("Custom")) {
                    if (sortCmb.getValue() != null) suitable = false;
                    sortCmb.setValue("Custom");
                }
                if (!findTf.getText().isBlank()) {
                    findTf.setText("");
                    suitable = false;
                }

                if (suitable) {
                    Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();

                    int dragIndex = dataLst.getItems().indexOf(getItem());
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
                if (event.getGestureSource() != this && event.getDragboard().hasContent(DATAVIEW_DATAFORMAT))
                    setOpacity(0.3);
                event.consume();
            });

            setOnDragExited(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasContent(DATAVIEW_DATAFORMAT))
                    setOpacity(1);
                event.consume();
            });

            setOnDragDropped(event -> {
                if (getItem() == null) return;
                Dragboard dragboard = event.getDragboard();
                boolean success = false;

                if (dragboard.hasContent(DATAVIEW_DATAFORMAT)) {
                    int dragIndex = (int) dragboard.getContent(DATAVIEW_DATAFORMAT);
                    int dropIndex = dataLst.getItems().indexOf(getItem());
                    ActionManager.executeAction(new MoveAction<>(getCurrentData(), dragIndex, dropIndex));
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });

            setOnDragDone(DragEvent::consume);
        }

        @Override
        public void updateItem(DataView<?> view, boolean empty) {
            super.updateItem(view, empty);
            if (empty || view == null) {
                setGraphic(null);
                setContextMenu(null);
            } else {
                if (view.getMoveToFront()) {
                    System.out.println("hiiii");
                    toFront();
                }
                setGraphic(view);
                setContextMenu(contextMenu);
            }
        }
    }
}
