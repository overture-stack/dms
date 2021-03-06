package bio.overture.dms.core.util;

import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class Strings {

  public static boolean isNotDefined(@Nullable String s) {
    return isNull(s) || s.isBlank();
  }

  public static boolean isDefined(@Nullable String s) {
    return !isNotDefined(s);
  }
}
