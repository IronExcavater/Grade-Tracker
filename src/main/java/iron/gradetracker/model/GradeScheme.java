package iron.gradetracker.model;

import com.google.gson.annotations.Expose;

import java.util.NavigableMap;
import java.util.TreeMap;

public class GradeScheme {
    @Expose private NavigableMap<Double, Grade> gradeMap = new TreeMap<>();

    public static class Grade {
        @Expose String name;
        @Expose double point;

        public Grade(String name, double point) {
            this.name = name;
            this.point = point;
        }
    }

    public GradeScheme(double[] minMarks, String[] gradeNames, double[] gradePoints) {
        for (int i = 0; i < minMarks.length; i++)
            gradeMap.put(minMarks[i], new Grade(gradeNames[i], gradePoints[i]));
    }

    public GradeScheme() { this(
            new double[]{85, 75, 65, 50, 0},
            new String[]{"HD", "D", "C", "P", "F"},
            new double[]{7, 6, 5, 4, 0});
    }

    public Grade getGrade(double mark) {
        return gradeMap.floorEntry(mark).getValue();
    }
}