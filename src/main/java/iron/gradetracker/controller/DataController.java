package iron.gradetracker.controller;

import iron.gradetracker.Utils;
import iron.gradetracker.model.*;
import iron.gradetracker.view.*;
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
    @FXML private ListView<DataView<?>> lstData;

    @FXML private ComboBox<String> sortCmb;

    private Data<?> currentData;

    public DataController(Stage stage) {
        super(stage);
        currentData = App.getStudentData();
    }

    @FXML
    private void initialize() {
        lstData.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstData.setCellFactory(_ -> new DataCell());
        updateCurrentData(currentData);
    }

    @FXML
    private void handleAdd() {
        switch (currentData) {
            case StudentData studentData -> lstData.getItems().add(new SessionView(studentData.createChild()));
            case SessionData sessionData -> lstData.getItems().add(new SubjectView(sessionData.createChild()));
            case SubjectData subjectData -> lstData.getItems().add(new AssessmentView(subjectData.createChild()));
            default -> {}
        }
        // Update logic
        updateColumnHeadings();
    }

    @FXML
    private void handleDelete() {
        List<DataView<?>> selectedViews = lstData.getSelectionModel().getSelectedItems();
        if (selectedViews.isEmpty()) return;

        List<?> selectedData = selectedViews.stream().map(DataView::getData).toList();
        switch (currentData) {
            case StudentData studentData -> studentData.removeChildren(selectedData.stream().map(SessionData.class::cast).toList());
            case SessionData sessionData -> sessionData.removeChildren(selectedData.stream().map(SubjectData.class::cast).toList());
            case SubjectData subjectData -> subjectData.removeChildren(selectedData.stream().map(AssessmentData.class::cast).toList());
            default -> {}
        }
        lstData.getItems().removeAll(selectedViews);
        // Update logic
        updateColumnHeadings();
    }

    @FXML
    private void handleSort() {
        String sortOption = sortCmb.getValue();
        if (sortOption == null) return;

        switch (sortOption) {
            case "A to Z" -> lstData.getItems().sort(Comparator.comparing(view -> view.getData().getName()));
            case "Z to A" -> lstData.getItems().sort(Comparator.comparing(view -> ((DataView<?>)view).getData().getName()).reversed());
            case "High to Low" -> lstData.getItems().sort(Comparator.comparing(view -> view.getData().getMark()));
            case "Low to High" -> lstData.getItems().sort(Comparator.comparing(view -> ((DataView<?>)view).getData().getMark()).reversed());
            case "Custom" -> updateDataViewList();
        }
    }

    @FXML
    private void handleListClick(MouseEvent mouseEvent) {
        if (lstData.getSelectionModel().getSelectedItem() == null) return;
        Data<?> clickedData = lstData.getSelectionModel().getSelectedItem().getData();

        if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
            if (clickedData.getName() == null || clickedData.getName().isBlank()) return;
            if (clickedData instanceof AssessmentData) return;
            updateCurrentData(clickedData);
        }
    }

    public void updateCurrentData(Data<?> currentData) {
        this.currentData = currentData;
        updateDataViewList();
        updateBreadcrumbs();
        updateColumnHeadings();
    }

    private void updateDataViewList() {
        // Populate lstData with new DataViews observing currentData children
        lstData.getItems().clear();
        lstData.getItems().addAll(currentData.getChildren().stream()
                .map(data -> switch (data) {
                    case SessionData sessionData -> new SessionView(sessionData);
                    case SubjectData subjectData -> new SubjectView(subjectData);
                    case AssessmentData assessmentData -> new AssessmentView(assessmentData);
                    default -> throw new IllegalStateException("Unexpected value: " + data);
                })
                .toList());
    }

    private void updateBreadcrumbs() {
        // Populate hBxBreadcrumbs with Hyperlinks of currentData ancestors
        hBxBreadcrumbs.getChildren().clear();
        Data<?> data = currentData;
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
        if (!lstData.getItems().isEmpty()) {
            DataView<?> childView = lstData.getItems().getFirst();
            int[] columnWidths = childView.getColumnWidths();
            String[] columnNames = childView.getColumnNames();
            for (int i = 0; i < columnNames.length; i++) {
                Text heading = new Text(columnNames[i]);
                gPaneHeadings.add(heading, i, 0);
                gPaneHeadings.getColumnConstraints().add(columnPercentage(columnWidths[i]));
            }
        }
    }

    protected ColumnConstraints columnPercentage(double percentWidth) {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentWidth);
        column.setHgrow(Priority.ALWAYS);
        return column;
    }

    private class DataCell extends ListCell<DataView<?>> {
        private static final DataFormat DATAVIEW_DATAFORMAT = new DataFormat("iron/data");
        private static DataCell dragCell;

        public DataCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            setOnDragDetected(event -> {
                if (getItem() == null) return;

                if (sortCmb.getValue() == null || !sortCmb.getValue().equals("Custom")) {
                    sortCmb.setValue("Custom");
                    event.consume();
                    return;
                }

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();

                int dragIndex = lstData.getItems().indexOf(getItem());
                dragCell = this;
                content.put(DATAVIEW_DATAFORMAT, dragIndex);
                dragboard.setContent(content);
                dragboard.setDragView(getGraphic().snapshot(null, null));
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
                    int dropIndex = lstData.getItems().indexOf(getItem());
                    DataView<?> dragView = dragCell.getItem();
                    int indexDiff = Math.abs(dropIndex - dragIndex);

                    // Animate in-between cell fade out
                    var fadeOut = Utils.Animation.sequentialTransition(null);
                    var fadeOutTransitions = fadeOut.getChildren();
                    for (int i = Math.min(dragIndex + 1, dropIndex); i < Math.max(dragIndex, dropIndex + 1); i++) {
                        Node cellView = lstData.getItems().get(i);
                        fadeOutTransitions.add(i > dropIndex ? 0 : fadeOutTransitions.size(),
                                Utils.Animation.toOpacityFade(cellView, 1, 0, 150f / indexDiff, null));
                    }
                    fadeOut.play();

                    // Animate dragCell to move from dragIndex to dropIndex
                    double cellHeight = dragView.getHeight() + 6;
                    dragCell.toFront();
                    Utils.Animation.byYTranslation(dragView, cellHeight * indexDiff * (dragIndex > dropIndex ? -1 : 1), 300, () -> {
                        dragView.setTranslateY(0);
                        lstData.getItems().remove(dragView);
                        lstData.getItems().add(dropIndex, dragView);
                        currentData.shiftChild(dragIndex, dropIndex);

                        // Animate in-between cell fade in
                        var fadeIn = Utils.Animation.sequentialTransition(null);
                        var fadeInTransitions = fadeIn.getChildren();
                        for (int i = Math.min(dragIndex, dropIndex + 1); i < Math.max(dragIndex + 1, dropIndex); i++) {
                            Node cellView = lstData.getItems().get(i);
                            fadeInTransitions.add(i > dropIndex ? 0 : fadeInTransitions.size(),
                                    Utils.Animation.toOpacityFade(cellView, 0, 1, 150f / indexDiff, null));
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
