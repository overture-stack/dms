package bio.overture.dms.domain.compose;

import static bio.overture.dms.domain.compose.DependencyCondition.resolveDependencyCondition;
import static bio.overture.dms.util.Exceptions.checkArgument;
import static bio.overture.dms.util.ObjectSerializer.getFieldNames;
import static java.util.stream.Collectors.toUnmodifiableList;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.List;
import lombok.val;

public class DependsOnDeserializer extends JsonDeserializer<List<Dependency>> {

  private static final String CONDITION = "condition";

  @Override
  public List<Dependency> deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    val root = ctxt.readTree(p);
    checkArgument(root instanceof ArrayNode, "This node must be an arrayNode");
    return getFieldNames(root).stream()
        .map(
            f -> {
              var condition = DependencyCondition.SERVICE_STARTED; // Default
              if (root.hasNonNull(CONDITION)) {
                condition = resolveDependencyCondition(root.path(CONDITION).asText());
              }
              return Dependency.builder().serviceName(f).condition(condition).build();
            })
        .collect(toUnmodifiableList());
  }
}
