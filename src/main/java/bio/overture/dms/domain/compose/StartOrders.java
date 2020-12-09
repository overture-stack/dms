package bio.overture.dms.domain.compose;

public enum StartOrders implements ComposeEnum {
  START_FIRST,
  STOP_FIRST;

  @Override
  public String toString() {
    return lowerCaseKebabName();
  }
}
