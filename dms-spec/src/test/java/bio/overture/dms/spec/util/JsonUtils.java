package bio.overture.dms.spec.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static java.nio.file.Files.writeString;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class JsonUtils {

  private static final ObjectMapper MAPPER = new ObjectMapper().enable(INDENT_OUTPUT);

  @SneakyThrows
  public static String writeToString(@NonNull Object o){
    return MAPPER.writeValueAsString(o);
  }

  @SneakyThrows
  public static <T> T readFromString(@NonNull String jsonString, @NonNull Class<T> tClass){
    return MAPPER.readValue(jsonString, tClass);
  }

}
