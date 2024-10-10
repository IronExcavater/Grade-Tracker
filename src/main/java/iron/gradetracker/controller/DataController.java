package iron.gradetracker.controller;

import iron.gradetracker.model.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

public class DataController extends Controller<App> {

    @FXML private HBox hBxBreadcrumbs;
    @FXML private ListView<Data<?, ?>> lstData;

    private Data<?, ?> currentData;

    @FXML
    private void initialize() {

    }

    @FXML
    private void handleAdd(ActionEvent actionEvent) {
    }

    @FXML
    private void handleDelete(ActionEvent actionEvent) {
    }

    @FXML
    public void handleListClick(MouseEvent mouseEvent) {
    }
}
