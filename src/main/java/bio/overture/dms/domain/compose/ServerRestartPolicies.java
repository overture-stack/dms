package bio.overture.dms.domain.compose;

public enum ServerRestartPolicies implements ComposeEnum {
  NO,
  ALWAYS,
  ON_FAILURE,
  UNLESS_STOPPED;

  @Override
  public String toString() {
    return lowerCaseKebabName();
  }
}
