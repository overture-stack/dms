package bio.overture.dms.domain;

public interface EGOSpec {
  int getApiTokenDurationDays();

  long getJwtDurationMS();

  long getRefreshTokenDurationMS();
}
