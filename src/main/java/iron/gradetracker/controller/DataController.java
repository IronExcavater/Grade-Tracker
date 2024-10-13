package iron.gradetracker.controller;

import iron.gradetracker.model.*;
import iron.gradetracker.view.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DataController extends Controller<App> {

    @FXML private HBox hBxBreadcrumbs;
    @FXML private ListView<DataView> lstData;

    private Data currentData;

    public DataController(Stage stage, App app) {
        super(stage, app);
        currentData = model.studentData;
    }

    @FXML
    private void initialize() {
        setCurrentData(currentData);
    }

    @FXML
    private void handleAdd() {
        switch (currentData) {
            case StudentData studentData -> lstData.getItems().add(new SessionView(new SessionData(studentData)));
            case SessionData sessionData -> lstData.getItems().add(new SubjectView(new SubjectData(sessionData, 6)));
            case SubjectData subjectData -> lstData.getItems().add(new AssessmentView(new AssessmentData(subjectData, 0, 100, 20)));
            default -> {}
        }
    }

    @FXML
    private void handleDelete() {
        currentData.removeChildren(lstData.getSelectionModel().getSelectedItems().stream()
                .map(DataView::getData)
                .toList());
        lstData.getItems().removeAll(lstData.getSelectionModel().getSelectedItems());
    }

    @FXML
    public void handleListClick(MouseEvent mouseEvent) {
        if (lstData.getSelectionModel().getSelectedItem() == null) return;
        Data clickedData = lstData.getSelectionModel().getSelectedItem().getData();

        if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
            if (clickedData.getName() == null || clickedData.getName().isBlank()) return;
            setCurrentData(clickedData);
        }
    }

    public void setCurrentData(Data currentData) {
        this.currentData = currentData;
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
        // Populate hBxBreadcrumbs with Hyperlinks of currentData ancestors
        hBxBreadcrumbs.getChildren().clear();
        Data data = currentData;
        hBxBreadcrumbs.getChildren().add(new BreadcrumbView(this, data));
        while (!data.equals(model.studentData)) {
            data = data.getParent();
            hBxBreadcrumbs.getChildren().addFirst(new Text(">"));
            hBxBreadcrumbs.getChildren().addFirst(new BreadcrumbView(this, data));
        }
    }
}
