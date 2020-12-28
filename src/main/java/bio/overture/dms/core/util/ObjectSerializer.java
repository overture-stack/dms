package bio.overture.dms.core.util;

import static bio.overture.dms.core.util.FileUtils.checkFileExists;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

// TODO: handle JsonParseException
@RequiredArgsConstructor
public class ObjectSerializer {

  @NonNull private final ObjectMapper objectMapper;

  // TODO: handle JsonParseException
  @SneakyThrows
  public JsonNode deserializeFile(@NonNull InputStream is) {
    return objectMapper.readTree(is);
  }

  @SneakyThrows
  public JsonNode deserializeFile(@NonNull File f) {
    checkFileExists(f.toPath());
    return objectMapper.readTree(f);
  }

  @SneakyThrows
  public <T> T deserializeFile(@NonNull InputStream is, Class<T> tClass) {
    return objectMapper.readValue(is, tClass);
  }

  @SneakyThrows
  public <T> T deserializeFile(@NonNull File f, Class<T> tClass) {
    checkFileExists(f.toPath());
    return objectMapper.readValue(f, tClass);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> convertToMap(@NonNull Object o) {
    return objectMapper.convertValue(o, Map.class);
  }

  // TODO: handle JsonParseException
  public <T> T convertValue(@NonNull Object root, @NonNull Class<T> tClass) {
    return objectMapper.convertValue(root, tClass);
  }

  @SneakyThrows
  public <T> T convertValue(@NonNull String payload, @NonNull Class<T> tClass) {
    return convertValue(objectMapper.readTree(payload), tClass);
  }

  @SneakyThrows
  public String serializeValue(@NonNull Object o) {
    return objectMapper.writeValueAsString(o);
  }

  @SneakyThrows
  public void serializeToFile(@NonNull Object o, @NonNull File file) {
    objectMapper.writeValue(file, o);
  }

  public static List<String> getFieldNames(@NonNull JsonNode root) {
    val out = new ArrayList<String>();
    root.fieldNames().forEachRemaining(out::add);
    return List.copyOf(out);
  }
}
