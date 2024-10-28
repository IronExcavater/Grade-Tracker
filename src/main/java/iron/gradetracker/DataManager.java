package iron.gradetracker;

import com.google.gson.*;
import com.google.gson.stream.*;
import iron.gradetracker.model.*;
import iron.gradetracker.model.data.*;
import javafx.beans.property.*;
import java.io.*;
import java.lang.reflect.*;

public class DataManager {

    private static final String FILE_PATH = "data.json";
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
        } else {
            App.getStudentData().startListening();
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

class DataAdapter implements JsonSerializer<Data<?>>, JsonDeserializer<Data<?>> {

    @Override
    public JsonElement serialize(Data<?> data, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", data.getClass().getSimpleName());

        for (Field field : data.getClass().getDeclaredFields()) {
            field.setAccessible(true);
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