package bio.overture.dms.docker.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DockerPortMapping {

  private final int containerPort;
  private final int hostPort;
}
