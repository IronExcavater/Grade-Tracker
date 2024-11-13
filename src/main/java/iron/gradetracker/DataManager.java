package iron.gradetracker;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.*;
import iron.gradetracker.model.*;
import iron.gradetracker.model.data.*;
import javafx.beans.property.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.zip.*;

public class DataManager {

    private static final String SAVE_PATH = "data.json";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(IntegerProperty.class, new IntegerPropertyAdapter())
            .registerTypeAdapter(DoubleProperty.class, new DoublePropertyAdapter())
            .registerTypeAdapter(StringProperty.class, new StringPropertyAdapter())
            .registerTypeAdapter(StudentData.class, new DataAdapter())
            .registerTypeAdapter(SessionData.class, new DataAdapter())
            .registerTypeAdapter(SubjectData.class, new DataAdapter())
            .registerTypeAdapter(AssessmentData.class, new DataAdapter())
            .excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    private static boolean isDirty = false;

    public static void saveData() {
        if (!isDirty) return;

        try (FileWriter writer = new FileWriter(SAVE_PATH)) {
            gson.toJson(App.getInstance(), writer);
            isDirty = false;
        } catch (IOException _) {}
    }

    public static void loadData() {
        File file = new File(SAVE_PATH);
        App.createInstance();

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                App.loadInstance(gson.fromJson(reader, App.class));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            App.getStudentData().startListening();
        }
    }

    public static void exportData(File file) {
        String directoryPath = file.getParent();
        String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
        String fileExtension = file.getName().substring(file.getName().lastIndexOf("."));
        switch (fileExtension) {
            case ".csv" -> exportToCsv(new File(directoryPath, fileName + ".zip"));
            case ".json" -> exportToJson(file);
            case ".xlsx" -> exportToXlsx(file);
        }
    }

    private static void exportToCsv(File file) {
        try (FileOutputStream fos = new FileOutputStream(file); ZipOutputStream zos = new ZipOutputStream(fos)) {
            StringBuilder sessionCsv = new StringBuilder("Session,Credit Points,WAM,GPA\n");
            StringBuilder subjectCsv = new StringBuilder("Session,Subject,Credit Points,Mark,Grade,Grade Points\n");
            StringBuilder assessmentCsv = new StringBuilder("Session,Subject,Assessment,Score,Max Score,Weight,Mark\n");

            for (var session : App.getStudentData().getChildren()) {
                sessionCsv.append("%s,%s,%s,%s\n".formatted(session.getName(), session.getCreditPoints(),
                        session.getMark(), session.getGradePoints()));

                for (var subject : session.getChildren()) {
                    subjectCsv.append("%s,%s,%s,%s,%s,%s\n".formatted(session.getName(), subject.getName(),
                            subject.getCreditPoints(), subject.getMark(), subject.getGrade(),
                            subject.getGradePoints()));

                    for (var assessment : subject.getChildren()) {
                        assessmentCsv.append("%s,%s,%s,%s,%s,%s,%s\n".formatted(session.getName(), subject.getName(),
                                assessment.getName(), assessment.getScore(), assessment.getMaxScore(),
                                assessment.getWeight(), assessment.getMark()));
                    }
                }
            }

            writeToZip(zos, "sessions.csv", sessionCsv.toString());
            writeToZip(zos, "subjects.csv", subjectCsv.toString());
            writeToZip(zos, "assessments.csv", assessmentCsv.toString());
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
        } catch (IOException _) {}
    }

    private static void exportToJson(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(App.getStudentData(), writer);
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
        } catch (IOException _) {}
    }

    private static void exportToXlsx(File file) {
        try (FileOutputStream fos = new FileOutputStream(file); Workbook workbook = new XSSFWorkbook()) {
            Sheet sessionSheet = workbook.createSheet("Sessions");
            Sheet subjectSheet = workbook.createSheet("Subjects");
            Sheet assessmentSheet = workbook.createSheet("Assessments");
            populateRow(sessionSheet, "Session", "Credit Points", "WAM", "GPA");
            populateRow(subjectSheet, "Session", "Subject", "Credit Points", "Mark", "Grade", "Grade Points");
            populateRow(assessmentSheet, "Session", "Subject", "Assessment", "Score", "Max Score", "Weight", "Mark");

            for (var session : App.getStudentData().getChildren()) {
                populateRow(sessionSheet, session.getName(), session.getCreditPoints(), session.getMark(),
                        session.getGradePoints());

                for (var subject : session.getChildren()) {
                    populateRow(subjectSheet, session.getName(), subject.getName(), subject.getCreditPoints(),
                            subject.getMark(), subject.getGrade(), subject.getGradePoints());

                    for (var assessment : subject.getChildren()) {
                        populateRow(assessmentSheet, session.getName(), subject.getName(), assessment.getName(),
                                assessment.getScore(), assessment.getMaxScore(), assessment.getWeight(),
                                assessment.getMark());
                    }
                }
            }

            workbook.write(fos);
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
        } catch (IOException _) {}
    }

    private static void populateRow(Sheet sheet, Object... values) {
        Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
        for (var i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            switch (values[i]) {
                case Integer integer -> cell.setCellValue(integer);
                case Float f -> cell.setCellValue(f);
                case Double d -> cell.setCellValue(d);
                case Short s -> cell.setCellValue(s);
                case Long l -> cell.setCellValue(l);
                case Boolean b -> cell.setCellValue(b);
                default -> cell.setCellValue(values[i].toString());
            }
            sheet.autoSizeColumn(i);
        }
    }

    private static void writeToZip(ZipOutputStream zos, String fileName, String fileContent) throws IOException {
        ZipEntry zipEntry = new ZipEntry(fileName);
        zos.putNextEntry(zipEntry);
        byte[] bytes = fileContent.getBytes();
        zos.write(bytes, 0, bytes.length);
        zos.closeEntry();
    }

    public static boolean isDirty() { return isDirty; }
    public static void markDirty() { isDirty = true; }
}

class IntegerPropertyAdapter extends TypeAdapter<IntegerProperty> {

    @Override
    public void write(JsonWriter jsonWriter, IntegerProperty integerProperty) throws IOException {
        jsonWriter.value(integerProperty.get());
    }

    @Override
    public IntegerProperty read(JsonReader jsonReader) throws IOException {
        return new SimpleIntegerProperty(jsonReader.nextInt());
    }
}

class DoublePropertyAdapter extends TypeAdapter<DoubleProperty> {

    @Override
    public void write(JsonWriter jsonWriter, DoubleProperty doubleProperty) throws IOException {
        jsonWriter.value(doubleProperty.get());
    }

    @Override
    public DoubleProperty read(JsonReader jsonReader) throws IOException {
        return new SimpleDoubleProperty(jsonReader.nextDouble());
    }
}

class StringPropertyAdapter extends TypeAdapter<StringProperty> {

    @Override
    public void write(JsonWriter jsonWriter, StringProperty stringProperty) throws IOException {
        jsonWriter.value(stringProperty.get());
    }

    @Override
    public StringProperty read(JsonReader jsonReader) throws IOException {
        return new SimpleStringProperty(jsonReader.nextString());
    }
}

class DataAdapter implements JsonSerializer<Data<?>>, JsonDeserializer<Data<?>> {

    @Override
    public JsonElement serialize(Data<?> data, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", data.getClass().getSimpleName());
        object.addProperty("name", data.getName());

        for (Field field : data.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(Expose.class)) continue;
            try {
                object.add(field.getName(), context.serialize(field.get(data)));
            } catch (IllegalAccessException e) {
                throw new JsonParseException("Error serializing field: " + field.getName(), e);
            }
        }

        if (data.canParent()) object.add("children", context.serialize(data.getChildren()));
        return object;
    }

    @Override
    public Data<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        String className = object.get("type").getAsString();

        try {
            Class<?> dataClass = Class.forName("iron.gradetracker.model.data." + className);
            Data<?> data = (Data<?>) dataClass.getDeclaredConstructor().newInstance();

            if (object.has("name")) data.nameProperty().set(object.get("name").getAsString());
            for (Field field : data.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                if (object.has(fieldName)) field.set(data, context.deserialize(object.get(fieldName), field.getType()));
            }
            if (data.canParent() && object.has("children")) {
                ParameterizedType superClass = (ParameterizedType) dataClass.getGenericSuperclass();
                Type childType = superClass.getActualTypeArguments()[0];
                object.get("children").getAsJsonArray().forEach(child ->
                        data.getChildren().add(context.deserialize(child, childType)));
            }

            data.startListening();
            return data;
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}