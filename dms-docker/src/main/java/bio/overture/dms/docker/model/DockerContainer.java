package bio.overture.dms.docker.model;

import java.util.Collection;
import java.util.Map;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DockerContainer {

  @NonNull private final String name;
  @NonNull private final DockerImage dockerImage;
  @NonNull private final Map<String, String> environment;
  @NonNull private final Collection<DockerPortMapping> portMappings;
  @NonNull private final Collection<DockerVolume> volumes;

}
