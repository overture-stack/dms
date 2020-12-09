package bio.overture.dms.domain.compose;

import static bio.overture.dms.util.ObjectSerializer.getFieldNames;
import static java.util.stream.Collectors.toUnmodifiableList;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.val;

public class ComposeServerDeserializer extends JsonDeserializer<List<ComposeServer>> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public List<ComposeServer> deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    val root = ctxt.readTree(p);
    return getFieldNames(root).stream()
        .map(serviceName -> processServer(root, serviceName))
        .collect(toUnmodifiableList());
  }

  private static ComposeServer processServer(JsonNode root, String serviceName) {
    val serviceNode = root.path(serviceName);
    val server = OBJECT_MAPPER.convertValue(serviceNode, ComposeServer.class);
    server.setName(serviceName);
    return server;
  }
}
