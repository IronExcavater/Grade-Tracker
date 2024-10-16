package iron.gradetracker.controller;

import iron.gradetracker.model.*;
import iron.gradetracker.view.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DataController extends Controller {

    @FXML private HBox hBxBreadcrumbs;
    @FXML private GridPane gPaneHeadings;
    @FXML private ListView<DataView<?>> lstData;

    private Data<?, ?> currentData;

    public DataController(Stage stage) {
        super(stage);
        currentData = App.getStudentData();
    }

    @FXML
    private void initialize() {
        updateCurrentData(currentData);
    }

    @FXML
    private void handleAdd() {
        switch (currentData) {
            case StudentData studentData -> lstData.getItems().add(new SessionView(new SessionData(studentData)));
            case SessionData sessionData -> lstData.getItems().add(new SubjectView(new SubjectData(sessionData, 6)));
            case SubjectData subjectData -> lstData.getItems().add(new AssessmentView(new AssessmentData(subjectData, 0, 100, 20)));
            default -> {}
        }
        // Update logic
        updateColumnHeadings();
    }

    @FXML
    private void handleDelete() {
        var selectedItems = lstData.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) return;
        selectedItems.stream().map(view -> view.getData());

        lstData.getItems().removeAll(selectedItems);
        // Update logic
        updateColumnHeadings();
    }

    @FXML
    public void handleListClick(MouseEvent mouseEvent) {
        if (lstData.getSelectionModel().getSelectedItem() == null) return;
        Data clickedData = lstData.getSelectionModel().getSelectedItem().getData();

        if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
            if (clickedData.getName() == null || clickedData.getName().isBlank()) return;
            updateCurrentData(clickedData);
        }
    }

    public void updateCurrentData(Data currentData) {
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
        Data<?, ?> data = currentData;
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
        if (!lstData.getItems().isEmpty()) {
            DataView childView = lstData.getItems().getFirst();
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
}
