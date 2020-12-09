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

public class ComposeVolumeDeserializer extends JsonDeserializer<List<ComposeVolume>> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public List<ComposeVolume> deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    val root = ctxt.readTree(p);
    return getFieldNames(root).stream()
        .map(volumeReferenceName -> processVolume(root, volumeReferenceName))
        .collect(toUnmodifiableList());
  }

  private static ComposeVolume processVolume(JsonNode root, String volumeReferenceName) {
    val serviceNode = root.path(volumeReferenceName);
    val composeVolume = OBJECT_MAPPER.convertValue(serviceNode, ComposeVolume.class);
    composeVolume.setReferenceName(volumeReferenceName);
    return composeVolume;
  }
}
