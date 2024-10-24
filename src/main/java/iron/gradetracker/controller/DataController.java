package iron.gradetracker.controller;

import iron.gradetracker.DataManager;
import iron.gradetracker.model.*;
import iron.gradetracker.view.*;
import javafx.collections.ListChangeListener;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.List;

public class DataController extends Controller {

    @FXML private HBox hBxBreadcrumbs;
    @FXML private GridPane gPaneHeadings;
    @FXML private ListView<DataView<?>> lstData;

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
        lstData.getItems().addListener((ListChangeListener<? super DataView<?>>) _ -> DataManager.markDirty());
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

        public DataCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            setOnDragDetected(event -> {
                if (getItem() == null) return;

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(getItem().getData().toString());
                int dragIndex = lstData.getItems().indexOf(getItem());
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
            });

            setOnDragExited(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasContent(DATAVIEW_DATAFORMAT)) {
                    setOpacity(1);
                }
            });

            setOnDragDropped(event -> {
                if (getItem() == null) return;

                Dragboard dragboard = event.getDragboard();
                boolean success = false;

                if (dragboard.hasContent(DATAVIEW_DATAFORMAT)) {
                    int dragIndex = (int) dragboard.getContent(DATAVIEW_DATAFORMAT);
                    DataView<?> dragView = lstData.getItems().get(dragIndex);
                    int dropIndex = lstData.getItems().indexOf(getItem());

                    lstData.getItems().set(dragIndex, getItem());
                    lstData.getItems().set(dropIndex, dragView);
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
