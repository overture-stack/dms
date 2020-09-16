package bio.overture.dms.docker.properties;

import java.util.Collection;
import java.util.Map;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ContainerProperties {

  @NonNull private final String name;
  @NonNull private final DockerImage dockerImage;
  @NonNull private final Map<String, String> environment;
  @NonNull private final Collection<DockerPortMapping> portMappings;
  @NonNull private final Collection<DockerVolume> volumes;

  @Value
  @Builder
  public static class DockerVolume {
    @NonNull private final String containerMountPath;
    @NonNull private final String hostPath;
  }

  @Value
  @Builder
  public static class DockerPortMapping {
    private final int containerPort;
    private final int hostPort;
  }

  @Value
  @Builder
  public static class DockerImage {
    @NonNull private final String accountName;
    @NonNull private final String repositoryName;
    @NonNull private final String tag;

    public String getFullName() {
      return getAccountName() + '/' + getRepositoryName() + ":" + getTag();
    }
  }
}
