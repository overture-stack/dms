package bio.overture.dms.infra.docker.model;

import static bio.overture.dms.core.util.Strings.isBlank;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value
@Builder
public class DockerImage {

  @NonNull @Builder.Default private final String containerRegistryName = "";

  @NonNull @Builder.Default private final String accountName = "";

  @NonNull private final String repositoryName;
  @NonNull private final String tag;

  public String getName() {
    val crsPrefix = isCRDefined() ? getContainerRegistryName() + "/" : "";
    val accountNamePrefix = isAccountNameDefined() ? getAccountName() + "/" : "";
    return crsPrefix + accountNamePrefix + getRepositoryName();
  }

  public String getFullName() {
    return getName() + ":" + getTag();
  }

  public boolean isCRDefined() {
    return !isBlank(getContainerRegistryName());
  }

  public boolean isAccountNameDefined() {
    return !isBlank(getAccountName());
  }
}
