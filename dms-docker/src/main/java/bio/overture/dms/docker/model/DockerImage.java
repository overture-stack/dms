package bio.overture.dms.docker.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DockerImage {

  @NonNull private final String accountName;
  @NonNull private final String repositoryName;
  @NonNull private final String tag;

  public String getFullName() {
    return getAccountName() + '/' + getRepositoryName() + ":" + getTag();
  }
}
