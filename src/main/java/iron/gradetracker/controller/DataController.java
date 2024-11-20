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
import javafx.collections.transformation.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.*;

public class DataController extends Controller {

    @FXML private HBox hBxBreadcrumbs;
    @FXML private GridPane gPaneHeadings;
    @FXML private ListView<DataView<?>> dataViewLst;

    @FXML private ComboBox<String> sortCmb;
    @FXML private StringTextField findTf;
    @FXML private Button undoBtn;
    @FXML private Button redoBtn;

    private static final DataFormat DATAVIEW_DATAFORMAT = new DataFormat("iron/data");
    private final ObjectProperty<Data<?>> focusedData = new SimpleObjectProperty<>();

    private final MoveObservableList<DataView<?>> originalViewList = new MoveObservableList<>();
    private final SortedList<DataView<?>> sortedViewList = new SortedList<>(originalViewList);

    private final ListChangeListener<Data<?>> changeListener = change -> {
        while (change.next()) {
            if (change.wasPermutated()) {
                int from = change.getFrom();
                int to = change.getTo();
                var viewFrom = originalViewList.get(from);
                double cellHeight = viewFrom.getHeight() + 6;
                int indexDiff = Math.abs(to - from);

                AnimationManager.startAnimation(() -> {
                    // Animate fade-out for in-between cells
                    var fadeOut = AnimationManager.sequenceTransition(null);
                    for (int i = Math.min(from + 1, to); i < Math.max(from, to + 1); i++) {
                        fadeOut.add(
                                i > to ? 0 : fadeOut.getSequence().size(),
                                AnimationManager.toOpacityFade(dataViewLst.getItems().get(i), 1, 0, 300f / indexDiff)
                        );
                    }
                    // Animate slide over for permutation cell
                    var slideOver = AnimationManager.byYTranslation(viewFrom, cellHeight * indexDiff * (from > to ? -1 : 1), 600, () -> {
                        viewFrom.setTranslateY(0);
                        originalViewList.remove(viewFrom);
                        originalViewList.add(to, viewFrom);
                    });
                    // Animate fade-in in-between cells
                    var fadeIn = AnimationManager.sequenceTransition(null);
                    for (int i = Math.min(from + 1, to); i < Math.max(from, to + 1); i++) {
                        var view = dataViewLst.getItems().get(i);
                        fadeIn.add(
                                i > to ? 0 : fadeIn.getSequence().size(),
                                AnimationManager.toOpacityFade(view, 0, 1, 300f / indexDiff, () -> view.setOpacity(1))
                        );
                    }
                    AnimationManager.parallelTransition(null, fadeOut, AnimationManager.sequenceTransition(null, slideOver, fadeIn)).play();
                });
                break;
            }

            if (change.wasRemoved()) {
                AnimationManager.startAnimation(() -> {
                    var fadeOut = AnimationManager.parallelTransition(null);
                    for (int i = 0; i < change.getRemovedSize(); i++) {
                        var view = originalViewList.get(change.getFrom() + i);
                        fadeOut.add(AnimationManager.toOpacityFade(view, 1, 0, 300, () -> originalViewList.remove(view)));
                    }
                    fadeOut.play();
                });
            }

            if (change.wasAdded()) {
                AnimationManager.startAnimation(() -> {
                    var fadeIn = AnimationManager.parallelTransition(null);
                    for (int i = 0; i < change.getAddedSize(); i++) {
                        var view = createView(change.getAddedSubList().get(i));
                        originalViewList.add(change.getFrom() + i, view);
                        if (view.getData().getName().isEmpty()) view.getNameTf().requestFocus();
                        fadeIn.add(AnimationManager.toOpacityFade(view, 0, 1, 300, () -> view.setOpacity(1)));
                    }
                    fadeIn.play();
                });
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

        originalViewList.addListener((ListChangeListener<? super DataView<?>>) _ -> {
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

        dataViewLst.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dataViewLst.setCellFactory(_ -> new DataCell());
        dataViewLst.itemsProperty().bind(Bindings.createObjectBinding(() -> {
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

        setFocusedData(App.getStudentData());
    }

    @FXML
    public void handleAdd() {
        int addIndex = dataViewLst.getSelectionModel().getSelectedIndex() + 1;
        ActionManager.executeAction(new AddAction<>(getFocusedData().createChild(), addIndex));
    }

    @FXML
    public void handleDelete() {
        var selectedViews = dataViewLst.getSelectionModel().getSelectedItems();
        if (selectedViews.isEmpty()) return;

        var selectedData = selectedViews.stream().map(DataView::getData).toList();
        ActionManager.executeAction(new RemoveAction<>(selectedData));
    }

    @FXML
    public void handleUndo() { ActionManager.undoAction(); }

    @FXML
    public void handleRedo() { ActionManager.redoAction(); }

    public void handleCopy() {
        var selectedView = dataViewLst.getSelectionModel().getSelectedItem();
        if (selectedView == null) return;
        ClipboardContent content = new ClipboardContent();
        content.put(DATAVIEW_DATAFORMAT, selectedView.toClipboardData());
        Clipboard.getSystemClipboard().setContent(content);
    }

    public void handlePaste() {
        var clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasContent(DATAVIEW_DATAFORMAT)) {
            String json = (String) clipboard.getContent(DATAVIEW_DATAFORMAT);
            int pasteIndex = dataViewLst.getSelectionModel().getSelectedIndex() + 1;
            ActionManager.executeAction(new AddAction<>(DataManager.gson.fromJson(json, Data.class), pasteIndex));
        }
    }

    public void handleRename(Data<?> data) {
        var renameField = new StringTextField();
        renameField.setPromptText("Set new name");
        var validationMessage = new Label();
        validationMessage.getStyleClass().add("warning");
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
        if (dataViewLst.getSelectionModel().getSelectedItem() == null) return;
        Data<?> clickedData = dataViewLst.getSelectionModel().getSelectedItem().getData();

        if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
            if (clickedData.getName() == null || clickedData.getName().isBlank()) return;
            if (clickedData instanceof AssessmentData) return;
            setFocusedData(clickedData);
        }
    }

    public Data<?> getFocusedData() { return focusedData.get(); }

    public void setFocusedData(Data<?> data) {
        if (getFocusedData() != null) getFocusedData().getChildren().removeListener(changeListener);
        focusedData.set(data);
        getFocusedData().getChildren().addListener(changeListener);
        originalViewList.setAll(getFocusedData().getChildren().stream().map(this::createView).toList());
    }

    private void updateBreadcrumbs() {
        // Populate hBxBreadcrumbs with Hyperlinks of focusedData ancestors
        hBxBreadcrumbs.getChildren().clear();
        Data<?> data = getFocusedData();
        hBxBreadcrumbs.getChildren().add(new BreadcrumbLink(this, data));
        while (data.hasParent()) {
            data = data.getParent();
            hBxBreadcrumbs.getChildren().addFirst(new Text(">"));
            hBxBreadcrumbs.getChildren().addFirst(new BreadcrumbLink(this, data));
        }
    }

    private void updateColumnHeadings() {
        // Update gPaneHeadings with column headings of focusedData
        gPaneHeadings.getChildren().clear();
        gPaneHeadings.getColumnConstraints().clear();
        if (!originalViewList.isEmpty()) {
            DataView<?> childView = originalViewList.getFirst();
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
            rename.setOnAction(_ -> {
                getItem().getNameTf().requestFocus();
                getItem().getNameTf().selectAll();
            });
            contextMenu = new ContextMenu(delete, copy, paste, rename);

            setOnDragDetected(event -> {
                if (getItem() == null || AnimationManager.isAnimating()) return;
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

                    int dragIndex = dataViewLst.getItems().indexOf(getItem());
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
                    int dropIndex = dataViewLst.getItems().indexOf(getItem());
                    ActionManager.executeAction(new MoveAction<>(getFocusedData(), dragIndex, dropIndex));
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
                setGraphic(view);
                setContextMenu(contextMenu);
            }
        }
    }
}
