package bio.overture.dms.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Exceptions {

  public static void checkArgument(boolean expression, String formattedMessage, Object ... args){
    if (!expression){
      throw new IllegalArgumentException(format(formattedMessage, args));
    }
  }

}
