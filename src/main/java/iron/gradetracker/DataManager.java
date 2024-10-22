package iron.gradetracker;

import com.google.gson.*;
import com.google.gson.stream.*;
import iron.gradetracker.model.*;
import javafx.beans.property.*;
import javafx.collections.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class DataManager {

    private static final String FILE_PATH = "data.json";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(IntegerProperty.class, new IntegerPropertyAdapter())
            .registerTypeAdapter(DoubleProperty.class, new DoublePropertyAdapter())
            .registerTypeAdapter(StringProperty.class, new StringPropertyAdapter())
            .registerTypeAdapter(ObservableList.class, new ObservableListAdapter())
            .registerTypeAdapter(StudentData.class, new DataAdapter())
            .registerTypeAdapter(SessionData.class, new DataAdapter())
            .registerTypeAdapter(SubjectData.class, new DataAdapter())
            .registerTypeAdapter(AssessmentData.class, new DataAdapter())
            .excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    private static boolean isDirty = false;

    public static void saveData() {
        if (!isDirty) return;

        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(App.getInstance(), writer);
            isDirty = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadData() {
        File file = new File(FILE_PATH);
        App.createInstance();

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                App.loadInstance(gson.fromJson(reader, App.class));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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

class ObservableListAdapter implements JsonSerializer<ObservableList<?>>, JsonDeserializer<ObservableList<?>> {

    @Override
    public JsonElement serialize(ObservableList src, Type type, JsonSerializationContext context) {
        return context.serialize(new ArrayList(src));
    }

    @Override
    public ObservableList<?> deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        List<?> list = context.deserialize(element, ArrayList.class);
        return FXCollections.observableArrayList(list);
    }
}

class DataAdapter implements JsonSerializer<Data<?>>, JsonDeserializer<Data<?>> {

    @Override
    public JsonElement serialize(Data<?> data, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", data.getClass().getSimpleName());
        object.addProperty("name", data.getName());
        switch (data) {
            case SubjectData subjectData ->
                object.addProperty("creditPoints", subjectData.getCreditPoints());
            case AssessmentData assessmentData -> {
                object.addProperty("score", assessmentData.getScore());
                object.addProperty("maxScore", assessmentData.getMaxScore());
                object.addProperty("weight", assessmentData.getWeight());
            }
            default -> {}
        }
        object.add("children", context.serialize(data.getChildren()));
        return object;
    }

    @Override
    public Data<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        String className = object.get("type").getAsString();

        try {
            Class<?> dataClass = Class.forName("iron.gradetracker.model." + className);

            if (dataClass == StudentData.class) {
                return deserializeStudentData(object, context);
            } else if (dataClass == SessionData.class) {
                return deserializeSessionData(object, context);
            } else if (dataClass == SubjectData.class) {
                return deserializeSubjectData(object, context);
            } else if (dataClass == AssessmentData.class) {
                return deserilizeAssessmentData(object, context);
            }

            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Data<?> deserializeStudentData(JsonObject object, JsonDeserializationContext context) throws JsonParseException {
        StudentData data = new StudentData();
        data.nameProperty().set(object.get("name").getAsString());
        object.get("children").getAsJsonArray().forEach(child -> data.addChild(context.deserialize(child, SessionData.class)));
        return data;
    }

    private Data<?> deserializeSessionData(JsonObject object, JsonDeserializationContext context) throws JsonParseException {
        SessionData data = new SessionData();
        data.nameProperty().set(object.get("name").getAsString());
        object.get("children").getAsJsonArray().forEach(child -> data.addChild(context.deserialize(child, SubjectData.class)));
        return data;
    }

    private Data<?> deserializeSubjectData(JsonObject object, JsonDeserializationContext context) throws JsonParseException {
        SubjectData data = new SubjectData(object.get("creditPoints").getAsInt());
        data.nameProperty().set(object.get("name").getAsString());
        object.get("children").getAsJsonArray().forEach(child -> data.addChild(context.deserialize(child, AssessmentData.class)));
        return data;
    }

    private Data<?> deserilizeAssessmentData(JsonObject object, JsonDeserializationContext context) throws JsonParseException {
        AssessmentData data = new AssessmentData(object.get("score").getAsDouble(), object.get("maxScore").getAsDouble(), object.get("weight").getAsInt());
        data.nameProperty().set(object.get("name").getAsString());
        return data;
    }
}