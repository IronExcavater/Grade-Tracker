package ironbyte.gradetracker.controller;

import ironbyte.gradetracker.model.*;
import ironbyte.gradetracker.model.data.*;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HomeController extends Controller {

    @FXML private Label gpaLbl;
    @FXML private Label wamLbl;

    @FXML private LineChart<String, Number> wamByMonthChart;
    @FXML private CategoryAxis monthAxis;
    @FXML private NumberAxis monthWamAxis;
    @FXML private BarChart<String, Number> subjectWamChart;
    @FXML private CategoryAxis subjectAxis;
    @FXML private NumberAxis subjectWamAxis;

    public HomeController(Stage stage) { super(stage); }

    @FXML
    private void initialize() {
        StudentData studentData = App.getStudentData();
        gpaLbl.textProperty().bind(Bindings.createStringBinding(() -> String.format(App.getSettings().getRounding(), studentData.cgpaProperty().get()),
                studentData.cgpaProperty(), App.getSettings().roundingProperty()));
        wamLbl.textProperty().bind(Bindings.createStringBinding(() -> String.format(App.getSettings().getRounding(), studentData.markProperty().get()),
                studentData.markProperty(), App.getSettings().roundingProperty()));

        XYChart.Series<String, Number> monthSeries = new XYChart.Series<>();
        Map<String, Double> wamByMonth = getWamByMonth();

        monthWamAxis.setAutoRanging(false);
        monthAxis.setCategories(FXCollections.observableArrayList(wamByMonth.keySet()));
        wamByMonth.forEach((month, wam) -> monthSeries.getData().add(new XYChart.Data<>(month, wam)));
        wamByMonthChart.getData().add(monthSeries);

        XYChart.Series<String, Number> subjectSeries = new XYChart.Series<>();
        Map<String, Double> wamBySubject = getWamBySubject();

        subjectWamAxis.setAutoRanging(false);
        subjectAxis.setCategories(FXCollections.observableArrayList(wamBySubject.keySet()));
        wamBySubject.forEach((name, wam) -> subjectSeries.getData().add(new XYChart.Data<>(name, wam)));
        subjectWamChart.getData().add(subjectSeries);
    }

    private Map<String, Double> getWamByMonth() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");

        StudentData student = App.getStudentData();
        LocalDate latestDate = LocalDate.EPOCH;
        List<AssessmentData> assessments = new ArrayList<>();

        for (SessionData session : student.getChildren()) {
            for (SubjectData subject : session.getChildren()) {
                for (AssessmentData assessment : subject.getChildren()) {
                    if (assessment.getDate() == null) continue;
                    if (assessment.getDate().isAfter(latestDate))
                        latestDate = assessment.getDate();
                    assessments.add(assessment);
                }
            }
        }

        LocalDate latestYear = latestDate.minusYears(1);
        return assessments.stream()
                .filter(assessment -> !assessment.getDate().isBefore(latestYear) && assessment.getWeight() != 0)
                .sorted(Comparator.comparing(AssessmentData::getDate))
                .collect(Collectors.groupingBy(assessment -> assessment.getDate().format(formatter),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.reducing(
                                        new double[]{0, 0}, // unweighted total mark, total weight
                                        assessment -> new double[]{assessment.getMark() / assessment.getWeight(), assessment.getWeight()},
                                        (a, b) -> new double[]{a[0] + b[0], a[1] + b[1]}
                                ),
                                totalMarkAndWeight -> totalMarkAndWeight[0] * Math.max(100, totalMarkAndWeight[1])
                        ))
                );
    }

    private Map<String, Double> getWamBySubject() {
        StudentData student = App.getStudentData();
        List<SubjectData> subjects = new ArrayList<>();

        for (SessionData session : student.getChildren()) {
            subjects.addAll(session.getChildren());
        }

        return subjects.stream()
                .filter(subject -> subject.getMark() != 0)
                .sorted((s1, s2) -> Double.compare(s2.getMark(), s1.getMark()))
                .limit(10)
                .collect(Collectors.toMap(subject ->
                        subject.getName().length() > 10 ? subject.getName().substring(0, 10) + "..." : subject.getName(),
                        SubjectData::getMark
                ));
    }
}