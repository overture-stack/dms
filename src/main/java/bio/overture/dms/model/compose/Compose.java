package bio.overture.dms.model.compose;

import static bio.overture.dms.util.ObjectSerializer.getFieldNames;
import static java.util.stream.Collectors.toUnmodifiableSet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.val;

@Data
@Accessors(chain = true)
public class Compose {

  private String version;

  @JsonDeserialize(using = DCServiceDeserializer.class)
  private Set<ComposeService> services;

  /** Deserializers */
  public static class DCServiceDeserializer extends JsonDeserializer<Set<ComposeService>> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Set<ComposeService> deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      val root = ctxt.readTree(p);
      return getFieldNames(root).stream()
          .map(serviceName -> processDCService(root, serviceName))
          .collect(toUnmodifiableSet());
    }

    private static ComposeService processDCService(JsonNode root, String serviceName) {
      val serviceNode = root.path(serviceName);
      val dcService = OBJECT_MAPPER.convertValue(serviceNode, ComposeService.class);
      dcService.setServiceName(serviceName);
      return dcService;
    }
  }
}
