package bio.overture.dms.domain.compose;

public enum DeployModes {
  GLOBAL,
  REPLICATED;

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
