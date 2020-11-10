package bio.overture.dms.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Strings {

  public static boolean isBlank(@Nullable String s){
    return isNull(s) || s.trim().equals("");
  }
}
