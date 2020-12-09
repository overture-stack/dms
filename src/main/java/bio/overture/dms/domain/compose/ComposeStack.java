package bio.overture.dms.domain.compose;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

// Note1:  Spec -  https://github.com/compose-spec/compose-spec/blob/master/spec.md
// Note2:  Compose Spec -
// https://github.com/compose-spec/compose-spec/blob/master/schema/compose-spec.json

/**
 * This implements a subset of the above spec, which is enough to represent its deployment in the
 * DMS.
 */
@Data
@JsonInclude(NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
public class ComposeStack {

  private String version;

  @JsonDeserialize(using = ComposeServerDeserializer.class)
  private List<ComposeServer> services = new ArrayList<>();

  @JsonDeserialize(using = ComposeVolumeDeserializer.class)
  private List<ComposeVolume> volumes = new ArrayList<>();
}
