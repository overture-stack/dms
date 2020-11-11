package bio.overture.dms.infra.docker.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DockerVolume {

  @NonNull private final String containerMountPath;
  @NonNull private final String hostPath;
}
