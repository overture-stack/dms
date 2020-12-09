package bio.overture.dms.domain.compose;

public enum VolumeTypes implements ComposeEnum {
  VOLUME,
  BIND,
  TMPFS,
  NPIPE;

  @Override
  public String toString() {
    return lowerCaseKebabName();
  }
}
