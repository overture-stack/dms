package bio.overture.dms.infra.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static bio.overture.dms.infra.util.FileUtils.checkFileExists;

@RequiredArgsConstructor
public class JsonProcessor {

  @NonNull private final ObjectMapper objectMapper;

  //TODO: handle JsonParseException
  @SneakyThrows
  public JsonNode readJsonFile(@NonNull File f){
    checkFileExists(f.toPath());
    return objectMapper.readTree(f);
  }

  //TODO: handle JsonParseException
  @SneakyThrows
  public String nodeToString(@NonNull JsonNode root){
    return objectMapper.writeValueAsString(root);
  }

  //TODO: handle JsonParseException
  @SneakyThrows
  public <T> T readValue(@NonNull File f, @NonNull Class<T> tClass){
    return objectMapper.readValue(f, tClass);
  }
  //TODO: handle JsonParseException
  public <T> T convertValue(@NonNull JsonNode root, @NonNull Class<T> tClass){
    return objectMapper.convertValue(root, tClass);
  }

  public static List<String> getFieldNames(@NonNull JsonNode root){
    val out = new ArrayList<String>();
    root.fieldNames().forEachRemaining(out::add);
    return List.copyOf(out);
  }
}
