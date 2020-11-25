package bio.overture.dms.util;

import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class Strings {

  public static boolean isBlank(@Nullable String s) {
    return isNull(s) || s.trim().equals("");
  }
}
