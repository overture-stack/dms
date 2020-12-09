package bio.overture.dms.domain.compose;

public enum RetryCondition implements ComposeEnum {
  NONE,
  ON_FAILURE,
  ANY;

  @Override
  public String toString() {
    return lowerCaseKebabName();
  }
}
