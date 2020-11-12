package bio.overture.dms.infra.docker.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import bio.overture.dms.infra.properties.service.ServiceProperties;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class DockerContainer<T extends ServiceProperties> {

  @NonNull private final String name;
  @NonNull private final String network;
  @NonNull private final DockerImage dockerImage;
  @NonNull private final T serviceProperties;
  @Builder.Default private final Collection<DockerPortMapping> portMappings = new HashSet<>();
  @Builder.Default private final Collection<DockerVolume> volumes = new HashSet<>();
  @NonNull @Singular private final Set<Integer> exposedPorts;

}
