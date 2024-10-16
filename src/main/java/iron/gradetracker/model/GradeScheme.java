package iron.gradetracker.model;

import java.util.NavigableMap;
import java.util.TreeMap;

public class GradeScheme {
    private NavigableMap<Double, Grade> gradeMap = new TreeMap<>();

    public static class Grade {
        String name;
        double point;

        public Grade(String name, double point) {
            this.name = name;
            this.point = point;
        }
    }

    public GradeScheme(double[] minMarks, String[] gradeNames, double[] gradePoints) {
        for (int i = 0; i < minMarks.length; i++)
            gradeMap.put(minMarks[i], new Grade(gradeNames[i], gradePoints[i]));
    }

    public Grade getGrade(double mark) {
        return gradeMap.floorEntry(mark).getValue();
    }
}