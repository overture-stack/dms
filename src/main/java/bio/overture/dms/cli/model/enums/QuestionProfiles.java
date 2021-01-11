package bio.overture.dms.cli.model.enums;

public enum QuestionProfiles {
  QUESTION,
  LINK,
  LABEL,
  STATUS,
  WARNING,
  ERROR;

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
