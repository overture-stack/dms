package bio.overture.dms.infra.docker;

import bio.overture.dms.infra.job.DeployJob;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DCServiceJobContext {

  @NonNull private final DeployJob imagePull;
  @NonNull private final DeployJob containerDeploy;
}
