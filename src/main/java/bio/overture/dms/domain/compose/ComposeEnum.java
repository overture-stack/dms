package bio.overture.dms.domain.compose;

public interface ComposeEnum {

  String UNDERSCORE = "_";
  String DASH = "_";

  String name();

  default String lowerCaseKebabName() {
    return name().toLowerCase().replaceAll(UNDERSCORE, DASH);
  }
}
