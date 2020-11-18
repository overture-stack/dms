package bio.overture.dms.infra.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.val;

import java.io.IOException;
import java.util.Set;

import static bio.overture.dms.infra.util.JsonProcessor.getFieldNames;
import static java.lang.String.format;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

@Data
@Accessors(chain = true)
public class DockerCompose {

  private String version;

  @JsonDeserialize(using = DCServiceDeserializer.class)
  private Set<DCService> services;

  /**
   * Deserializers
   */
  public static class DCServiceDeserializer extends JsonDeserializer<Set<DCService>>{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Set<DCService> deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      val root = ctxt.readTree(p);
      return getFieldNames(root).stream()
          .map(serviceName -> processDCService(root, serviceName))
          .collect(toUnmodifiableSet());
    }

    private static DCService processDCService(JsonNode root, String serviceName){
      val serviceNode =  root.path(serviceName);
      val dcService = OBJECT_MAPPER.convertValue(serviceNode, DCService.class);
      dcService.setServiceName(serviceName);
      return dcService;
    }
  }

}
