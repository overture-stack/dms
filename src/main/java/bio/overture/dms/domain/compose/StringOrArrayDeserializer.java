package bio.overture.dms.domain.compose;

import static bio.overture.dms.util.ObjectSerializer.getFieldNames;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableList;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.List;
import lombok.val;

public class StringOrArrayDeserializer extends JsonDeserializer<List<String>> {

  private static final String CONDITION = "condition";

  @Override
  public List<String> deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    val root = ctxt.readTree(p);
    if (root instanceof ArrayNode) {
      return getFieldNames(root).stream().collect(toUnmodifiableList());
    } else {
      return stream(root.textValue().split("\\s+")).collect(toUnmodifiableList());
    }
  }
}
