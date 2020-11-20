package bio.overture.dms.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Exceptions {

  public static void checkArgument(boolean expression, String formattedMessage, Object ... args){
    if (!expression){
      throw new IllegalArgumentException(format(formattedMessage, args));
    }
  }

  public static void checkState(boolean expr, @NonNull String formattedString, Object ...args) {
    if(!expr){
      throw new IllegalStateException(format(formattedString, args));
    }
  }

  public static String joinStackTrace(Throwable t){
    return stream(t.getStackTrace())
        .map(StackTraceElement::toString)
        .collect(joining("\n"));
  }
}
