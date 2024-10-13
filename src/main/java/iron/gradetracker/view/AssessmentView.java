package iron.gradetracker.view;

import iron.gradetracker.model.*;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.converter.NumberStringConverter;

public class AssessmentView extends DataView {

    private final TextField score = new TextField();
    private final TextField maxScore = new TextField();
    private final TextField weight = new TextField();

    public AssessmentView(AssessmentData data) {
        super(data, "Unnamed Assessment");
        score.setPrefWidth(0);
        maxScore.setPrefWidth(0);
        weight.setPrefWidth(0);

        score.focusedProperty().addListener((_, _, newValue) -> { if (!newValue && score.getText().isEmpty()) score.setText("0"); });
        score.setOnKeyPressed(keyEvent -> { if (keyEvent.getCode() == KeyCode.ENTER) score.getParent().requestFocus(); });
        score.setTextFormatter(decimalFormatter(data.maxScoreProperty()));
        maxScore.focusedProperty().addListener((_, _, newValue) -> { if (!newValue && maxScore.getText().isEmpty()) maxScore.setText("0"); });
        maxScore.setOnKeyPressed(keyEvent -> { if (keyEvent.getCode() == KeyCode.ENTER) maxScore.getParent().requestFocus(); });
        maxScore.setTextFormatter(decimalFormatter(null));
        weight.focusedProperty().addListener((_, _, newValue) -> { if (!newValue && weight.getText().isEmpty()) weight.setText("0"); });
        weight.setOnKeyPressed(keyEvent -> { if (keyEvent.getCode() == KeyCode.ENTER) weight.getParent().requestFocus(); });
        weight.setTextFormatter(integerFormatter(data.remainingWeightProperty()));

        NumberStringConverter converter = new NumberStringConverter();
        Bindings.bindBidirectional(score.textProperty(), data.scoreProperty(), converter);
        Bindings.bindBidirectional(maxScore.textProperty(), data.maxScoreProperty(), converter);
        Bindings.bindBidirectional(weight.textProperty(), data.weightProperty(), converter);
        setColumns(new double[]{70, 10, 10, 10}, name, score, maxScore, weight);
    }
}
