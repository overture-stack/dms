package bio.overture.dms.domain.compose;

public enum PullPolicies {
  ALWAYS,
  NEVER,
  IF_NOT_PRESENT;

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
