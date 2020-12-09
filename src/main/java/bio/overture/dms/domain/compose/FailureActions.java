package bio.overture.dms.domain.compose;

public enum FailureActions implements ComposeEnum {
  CONTINUE,
  PAUSE;

  @Override
  public String toString() {
    return lowerCaseKebabName();
  }
}
