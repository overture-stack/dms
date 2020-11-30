package bio.overture.dms.domain.impl.docker;

import bio.overture.dms.domain.DMSSpec;

public class DockerDMSSpec implements DMSSpec<DockerEgoSpec> {

  @Override
  public String getVersion() {
    return null;
  }

  @Override
  public DockerEgoSpec getEgo() {
    return null;
  }
}
