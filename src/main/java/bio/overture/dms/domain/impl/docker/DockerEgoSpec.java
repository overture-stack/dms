package bio.overture.dms.domain.impl.docker;

import bio.overture.dms.domain.EGOSpec;

public class DockerEgoSpec implements EGOSpec {

  @Override
  public int getApiTokenDurationDays() {
    return 0;
  }

  @Override
  public long getJwtDurationMS() {
    return 0;
  }

  @Override
  public long getRefreshTokenDurationMS() {
    return 0;
  }
}
